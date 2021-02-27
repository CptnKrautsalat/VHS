package de.zlb.vhs;

import de.zlb.vhs.catalog.LibraryCatalog;
import de.zlb.vhs.catalog.LibraryCatalogEntry;
import de.zlb.vhs.cdh.CdhCatalog;
import de.zlb.vhs.cdh.CdhCombinedFilm;
import de.zlb.vhs.ofdb.FilmEntry;
import de.zlb.vhs.ofdb.OfdbManager;
import de.zlb.vhs.ofdb.web.OfdbAccessUtil;
import de.zlb.vhs.ofdb.web.SearchParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ListGenerator {

	private static final Logger log = LogManager.getLogger(ListGenerator.class);
	private static final AtomicInteger mysteryFilms = new AtomicInteger(0);

	private final Set<CdhCombinedFilm> combinedFilms = new HashSet<>();
	private final OfdbManager ofdbManager = new OfdbManager();
	private final LibraryCatalog libraryCatalog = new LibraryCatalog();
	private final CdhCatalog cdhCatalog = new CdhCatalog();

	private void combineFilms() {
		log.info("Combining films from cdh catalog and OFDB ...");
		cdhCatalog
				.getAllEntries()
				.forEach(lce -> {
					Optional<FilmEntry> film = findMatchingOfdbFilmEntry(lce);
					if (film.isPresent()) {
						CdhCombinedFilm combinedFilm = new CdhCombinedFilm(film.get(), lce);
						combinedFilms.add(combinedFilm);
					}
				});
		combinedFilms.removeIf(ICombinedFilm::isEmpty);
		log.info("... created {} combined films, linked to {} catalog entries and {} OFDB films!",
				combinedFilms.size(),
				combinedFilms.stream().flatMap(ICombinedFilm::getCatalogEntries).count(),
				combinedFilms.stream().filter(ICombinedFilm::hasOfdbEntry).count());
	}

	private Set<LibraryCatalogEntry> findMatchingLibraryCatalogEntries(LibraryCatalogEntry lce) {
		Set<LibraryCatalogEntry> matches = libraryCatalog
				.getEntriesWithSignaturePrefix(lce.signaturePrefix)
				.collect(Collectors.toSet());
		libraryCatalog
				.getEntriesWithYear(lce.year)
				.filter(lce::matches)
				.forEach(matches::add);
		return matches;
	}

	private Optional<FilmEntry> findMatchingOfdbFilmEntry(ComparableFilmEntry libraryCatalogEntry) {
		Set<FilmEntry> films = ofdbManager
				.getAllPossibleMatches(libraryCatalogEntry)
				.filter(FilmEntry::isFeatureFilm)
				.filter(libraryCatalogEntry::matches)
				.collect(Collectors.toSet());

		if (films.isEmpty()) {
			return Optional.empty();
		}

		if (films.size() > 1) {
			films = libraryCatalogEntry.findBestMatches(films);
		}

		if (films.size() > 1 && !libraryCatalogEntry.hasDirector() && !libraryCatalogEntry.hasYear()) {
			return Optional.empty();
		}

		if (films.size() > 1) {
			films.forEach(FilmEntry::getOrCreateAdditionalOfdbData);
			films = libraryCatalogEntry.findBestMatches(films);
		}

		if (films.size() > 1) {
			log.debug("Library catalog entry {} matches {} films: {}", libraryCatalogEntry, films.size(),
					films.stream().map(f -> f.link).collect(Collectors.joining(" ; ")));
		}

		return films.stream().findFirst();
	}

	private Set<FilmEntry> tryToFilterFilmEntries(Set<FilmEntry> entries, Predicate<FilmEntry> negatedFilter) {
		if (entries.size() == 1) {
			return entries;
		}
		Set<FilmEntry> copy = new HashSet<>(entries);
		copy.removeIf(negatedFilter);
		return copy.isEmpty() ? entries : copy;
	}

	public Optional<FilmEntry> identifyMysteryFilm(ComparableFilmEntry entry) {
		if (mysteryFilms.get() >= 10) {
			return Optional.empty();
		}
		String year = entry.getYear();
		Optional<String> director = entry.getDirectors().stream().findAny();
		if (year.isEmpty() || director.isEmpty()) {
			return Optional.empty();
		}
		SearchParameters searchParameters = OfdbAccessUtil.generateParametersForSpecificSearch(year, director.get());
		try {
			Set<FilmEntry> films = OfdbAccessUtil.generateOFDBList(searchParameters);
			Thread.sleep(250);
			for (FilmEntry newFilm : films) {
				FilmEntry oldFilm = ofdbManager.getFilm(newFilm.link);
				if (oldFilm == null) {
					log.warn("{} is not in the catalog!", newFilm);
					ofdbManager.addEntry(newFilm);
					ofdbManager.addFilm(newFilm);
					mysteryFilms.incrementAndGet();
				} else {
					oldFilm.getOrCreateAdditionalOfdbData();
					return Optional.of(oldFilm);
				}
			}
		} catch (IOException | InterruptedException e) {
			log.error("Failed to identify {}!", entry, e);
		}
		return Optional.empty();
	}

	public void generateListAndWriteToCSV() {

		long start = System.currentTimeMillis();

		readDataFromFiles();

		if (ofdbManager.isEmpty()) {
			ofdbManager.collectOFDBData();
		}

		ofdbManager.processFilmData();
		combineFilms();

		ofdbManager.getFilms()
				.filter(f -> f.isVHSOnly() && !f.isTVShow())
				.forEach(FilmEntry::getOrCreateAdditionalOfdbData);

		cdhCatalog.createUnidentifiedButCompleteEntryList().forEach(this::identifyMysteryFilm);

		writeDataToFiles();

		log.info("{} film entries haven been added!", mysteryFilms.get());
		log.info("{} film entries haven been updated!", FilmEntry.getTotalOfdbUpdates());
		log.info("Done in {} minutes!", (System.currentTimeMillis() - start) / 60000.0);

	}

	private void readDataFromFiles() {
		cdhCatalog.readDataFromFiles();
		libraryCatalog.readDataFromFiles();
		ofdbManager.readDataFromFiles();
		log.info("Done reading files.");
	}

	private void writeDataToFiles() {
		cdhCatalog.writeDataToFiles();
		ofdbManager.writeToFiles();
		libraryCatalog.writeToFiles();
	}

	public static void main(String [] args) {
		ListGenerator generator = new ListGenerator();
		generator.generateListAndWriteToCSV();
	}
}
