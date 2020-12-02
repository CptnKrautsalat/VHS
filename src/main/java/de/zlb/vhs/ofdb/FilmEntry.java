package de.zlb.vhs.ofdb;

import de.zlb.vhs.CombinedFilm;
import de.zlb.vhs.ISortableEntry;
import de.zlb.vhs.catalog.LibraryCatalogEntry;
import de.zlb.vhs.csv.FilmVersionEntryBean;
import de.zlb.vhs.ofdb.stats.OFDBFilmStats;
import de.zlb.vhs.ofdb.web.AdditionalOfdbData;
import de.zlb.vhs.ofdb.web.WebUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FilmEntry implements ISortableEntry {

	private static final Logger log = LogManager.getLogger(FilmEntry.class);
	private static final AtomicInteger ofdbUpdateCount = new AtomicInteger();

	private static final AdditionalOfdbData EMPTY_ADDITIONAL_OFDB_DATA = new AdditionalOfdbData(
			Collections.emptySet(), Collections.emptySet(), "");
	public static final String STRING_SET_SEPARATOR = " \\| ";

	public final String title;
	public final String year;
	public final String link;

	private String mainTitle;
	final Set<String> titles = new HashSet<>();

	private CombinedFilm film;
	private AdditionalOfdbData additionalOfdbData;

	public CombinedFilm getFilm() {
		return film;
	}

	public void setFilm(CombinedFilm film) {
		this.film = film;
	}

	private final List<FilmVersionEntry> versions = new LinkedList<>();

	public FilmEntry(String title, String link) {
		super();
		this.title = extractTitle(title);
		this.titles.addAll(generateTitleVariations(title));
		this.year = extractYear(title);
		this.link = OfdbManager.OFDB_LINK_PREFIX + link;
	}

	public FilmEntry(FilmVersionEntryBean filmVersionEntryBean) {
		super();
		this.title = filmVersionEntryBean.title;
		this.titles.addAll(generateTitleVariations(title));
		this.year = filmVersionEntryBean.year;
		this.link = filmVersionEntryBean.filmLink;
		this.additionalOfdbData = extractAdditionalOfdbData(
				filmVersionEntryBean.imdbLink, filmVersionEntryBean.alternativeTitles, filmVersionEntryBean.directors)
				.orElse(null);
		if (this.additionalOfdbData != null) {
			this.titles.addAll(this.additionalOfdbData.alternativeTitles);
		}
		this.addVersion(new FilmVersionEntry(this, filmVersionEntryBean));
	}

	public boolean hasAdditionalOfdbData() {
		return additionalOfdbData != null;
	}

	public void mergeVersions (FilmEntry otherFilm) {
		otherFilm.versions.forEach(this::addVersion);
	}

	public boolean isValid () {
		return !year.isEmpty() && !versions.isEmpty();
	}

	public boolean isVHSOnly() {
		return getVersions().allMatch(FilmVersionEntry::isVHS);
	}

	public boolean isTVShow() {
		return title.endsWith("[TV-Serie]");
	}

	public boolean isShortFilm() {
		return title.endsWith("[Kurzfilm]");
	}

	public boolean isFeatureFilm() {
		return !title.endsWith("]");
	}

	public boolean hasDigitalRelease() {
		return getVersions().anyMatch(FilmVersionEntry::isDigital);
	}

	public boolean hasDigitalReleaseWithGermanDub() {
		return getVersions().anyMatch(v -> v.isDigital() && v.isGermanLanguage());
	}

	public boolean matchesTitles(LibraryCatalogEntry libraryCatalogEntry, boolean includeAltTiles, boolean includeGeneratedTitles) {

		if (libraryCatalogEntry.matchesTitle(mainTitle, includeAltTiles, includeGeneratedTitles)) {
			return true;
		}

		if (includeAltTiles && getAdditionalOfdbData().alternativeTitles.stream()
				.anyMatch(t -> libraryCatalogEntry.matchesTitle(t, includeAltTiles, includeGeneratedTitles))) {
			return true;
		}

		return includeGeneratedTitles && titles.stream()
				.anyMatch(t -> libraryCatalogEntry.matchesTitle(t, includeAltTiles, includeGeneratedTitles));
	}

	public boolean isLinkedToFilm() {
		return film != null;
	}

	public void addToStats(OFDBFilmStats stats) {
		stats.films++;
		stats.features += isFeatureFilm() ? 1 : 0;
		stats.shorts += isShortFilm() ? 1 : 0;
		stats.tvShows += isTVShow() ? 1 : 0;
		stats.releases += versions.size();
		stats.vhsReleases += getVersionCount(FilmVersionEntry::isVHS);
		stats.dvdReleases += getVersionCount(FilmVersionEntry::isDVD);
		stats.blurayReleases += getVersionCount(FilmVersionEntry::isBluRay);

		getVersions()
				.map(v -> v.country)
				.forEach(stats::incrementCountry);
	}

	public long getVersionCount (Predicate<FilmVersionEntry> predicate) {
		return getVersions()
				.filter(predicate)
				.count();
	}

	public String getImdbLink() {
		return getAdditionalOfdbData().imdbLink;
	}

	public String getAlternativeTitlesAsString() {
		return String.join(STRING_SET_SEPARATOR, getAdditionalOfdbData()
				.alternativeTitles);
	}

	public String getDirectorsAsString() {
		return String.join(STRING_SET_SEPARATOR, getAdditionalOfdbData()
				.directors);
	}

	public String getTitle() {
		return title;
	}

	public AdditionalOfdbData getAdditionalOfdbData() {
		return additionalOfdbData == null ? EMPTY_ADDITIONAL_OFDB_DATA : additionalOfdbData;
	}

	public Optional<AdditionalOfdbData> getOrCreateAdditionalOfdbData() {
		if (additionalOfdbData != null) {
			return Optional.of(additionalOfdbData);
		}

		Optional<AdditionalOfdbData> ofdbResult = WebUtil.getAdditionalOfdbData(link);
		if (ofdbResult.isPresent()) {
			log.info("Updating {} with {}.", title, ofdbResult.get());
			ofdbUpdateCount.incrementAndGet();
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				log.error("Something went terribly wrong!", e);
			}
			additionalOfdbData = ofdbResult.get();
			titles.addAll(additionalOfdbData.alternativeTitles);
		}

		return ofdbResult;
	}

	public boolean matchesDirectors(LibraryCatalogEntry libraryCatalogEntry, boolean strict) {
		Set<String> directors = getAdditionalOfdbData().directors;
		return (!strict && (directors.isEmpty() || !libraryCatalogEntry.hasDirector()))
				|| directors.stream().anyMatch(libraryCatalogEntry::matchesDirector);
	}

	private String extractTitle(String title) {
		int index = title.lastIndexOf('(');
		return index == -1 ? title : title.substring(0, index-1);
	}
	
	private String extractYear(String title) {
		int index1 = title.lastIndexOf('(');
		int index2 = title.lastIndexOf(')');
		return (index1 == -1 || index2 == -1) ? "" : title.substring(index1 + 1, index2);
	}

	public static int getTotalOfdbUpdates() {
		return ofdbUpdateCount.get();
	}

	static Optional<AdditionalOfdbData> extractAdditionalOfdbData(String imdbLink, String alternativeTitles, String directors) {
		if (Strings.isBlank(imdbLink) && Strings.isBlank(alternativeTitles) && Strings.isBlank(directors)) {
			return Optional.empty();
		}
		Set<String> altTitles = Arrays.stream(alternativeTitles.split(STRING_SET_SEPARATOR))
				.collect(Collectors.toSet());
		Set<String> dirs = Arrays.stream(directors.split(STRING_SET_SEPARATOR))
				.collect(Collectors.toSet());
		return Optional.of(new AdditionalOfdbData(dirs, altTitles, imdbLink));
	}

	Set<String> generateTitleVariations(String title) {
		Set<String> result = new HashSet<>();

		String titleWithoutMedium = title;
		if (title.endsWith("[TV-Serie]") || title.endsWith("[Kurzfilm]") || title.endsWith("[TV-Mini-Serie]")
		|| title.endsWith("[Webminiserie]")) {
			titleWithoutMedium = title.substring(0, title.lastIndexOf('[')).trim();
		}

		mainTitle = titleWithoutMedium;

		result.add(titleWithoutMedium);
		result.add(titleWithoutMedium.replaceAll(" ?[:\\-] ?", " "));
		result.add(titleWithoutMedium.replaceAll("ß", "ss"));

		String shortTitle = titleWithoutMedium.split("[:\\-] ")[0].strip();
		result.add(shortTitle);

		if (shortTitle.endsWith(".")) {
			result.add(shortTitle.substring(0, shortTitle.length() - 1));
		}

		return result;
	}
	
	public void addVersion(FilmVersionEntry version) {
		versions.add(version);
	}
	
	public Stream<FilmVersionEntry> getVersions() {
		return versions.stream();
	}

	@Override
	public String toString() {
		return "FilmEntry [title=" + title + ", year=" + year + ", link=" + link + ", versions=" + versions + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((link == null) ? 0 : link.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FilmEntry other = (FilmEntry) obj;
		if (link == null) {
			return other.link == null;
		} else return link.equals(other.link);
	}

	@Override
	public String getYear() {
		return year;
	}
}
