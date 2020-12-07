package de.zlb.vhs;

import de.zlb.vhs.catalog.LibraryCatalog;
import de.zlb.vhs.catalog.LibraryCatalogEntry;
import de.zlb.vhs.ofdb.FilmEntry;
import de.zlb.vhs.ofdb.OfdbManager;
import de.zlb.vhs.ofdb.web.WebUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ListGenerator {

	private static final Logger log = LogManager.getLogger(ListGenerator.class);

	private final Set<CombinedFilm> combinedFilms = new HashSet<>();
	private final OfdbManager ofdbManager = new OfdbManager();
	private final LibraryCatalog libraryCatalog = new LibraryCatalog();

	private void combineFilms() {
		log.info("Combining films from library catalog and OFDB ...");
		libraryCatalog
				.getAllEntries()
				.filter(lce -> lce.hasYear() && !lce.hasWrongYear() && !lce.isTvShow())
				.forEach(lce -> {
					Optional<FilmEntry> film = findMatchingOfdbFilmEntry(lce);
					Set<LibraryCatalogEntry> matches = findMatchingLibraryCatalogEntries(lce);
					if (!(film.isEmpty() && matches.size() < 2)) {
						CombinedFilm combinedFilm = film.isPresent() && film.get().isLinkedToFilm()
								? film.get().getFilm()
								: new CombinedFilm(film.orElse(null));
						combinedFilm = combinedFilm.addLibraryCatalogEntry(lce);
						matches.forEach(combinedFilm::addLibraryCatalogEntry);
						combinedFilms.add(combinedFilm);
					}
				});
		combinedFilms.removeIf(CombinedFilm::isEmpty);
		log.info("... created {} combined films, linked to {} catalog entries and {} OFDB films!",
				combinedFilms.size(),
				combinedFilms.stream().flatMap(CombinedFilm::getLibraryCatalogEntries).count(),
				combinedFilms.stream().filter(CombinedFilm::hasOfdbEntry).count());
	}

	private Set<LibraryCatalogEntry> findMatchingLibraryCatalogEntries(LibraryCatalogEntry lce) {
		Set<LibraryCatalogEntry> matches = libraryCatalog
				.getEntriesWithSignaturePrefix(lce.signaturePrefix)
				.collect(Collectors.toSet());
		libraryCatalog
				.getEntriesWithYear(lce.year)
				.filter(lce::matchesTitlesAndDirectors)
				.forEach(matches::add);
		return matches;
	}

	private Optional<FilmEntry> findMatchingOfdbFilmEntry(LibraryCatalogEntry libraryCatalogEntry) {
		Set<FilmEntry> films = ofdbManager
				.getEntriesWithYear(libraryCatalogEntry.year)
				.filter(FilmEntry::isFeatureFilm)
				.filter(f -> f.matchesTitles(libraryCatalogEntry, true, true))
				.collect(Collectors.toSet());

		if (films.isEmpty()) {
			return Optional.empty();
		}

		films = tryToFilterFilmEntries(films, f -> !f.matchesDirectors(libraryCatalogEntry, false));
		films = tryToFilterFilmEntries(films, f -> !f.matchesTitles(libraryCatalogEntry, true, false));
		films = tryToFilterFilmEntries(films, f -> !f.matchesTitles(libraryCatalogEntry, false, false));
		films = tryToFilterFilmEntries(films, f -> !libraryCatalogEntry.matchesYear(f.year, true));
		films = tryToFilterFilmEntries(films, f -> !f.matchesDirectors(libraryCatalogEntry, true));

		if (films.size() > 1 && !libraryCatalogEntry.hasDirector()) {
			return Optional.empty();
		}

		if (films.size() > 1) {
			films.forEach(FilmEntry::getOrCreateAdditionalOfdbData);
			films.removeIf(f -> !f.matchesDirectors(libraryCatalogEntry, true));
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
		Set<FilmEntry> backup = new HashSet<>(entries);
		entries.removeIf(negatedFilter);
		return entries.isEmpty() ? backup : entries;
	}

	public Optional<FilmEntry> identifyMysteryFilm(LibraryCatalogEntry entry) {
		String year = entry.year;
		Optional<String> director = entry.directors.stream().findAny();
		if (year.isEmpty() || director.isEmpty()) {
			return Optional.empty();
		}
		String url = WebUtil.generateOfdbUrlForSpecificSearch(year, director.get());
		try {
			Set<FilmEntry> films = WebUtil.generateOFDBList(url);
			Thread.sleep(250);
			for (FilmEntry newFilm : films) {
				FilmEntry oldFilm = ofdbManager.getFilm(newFilm.link);
				if (oldFilm == null) {
					log.warn("{} is not in the catalog!", newFilm);
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

		Set<LibraryCatalogEntry> mandatoryVhsTapes = libraryCatalog
				.getAllEntries()
				.filter(e -> e.isVhs() && e.isMandatory() && !e.isLinkedToOfdbFilm())
				.collect(Collectors.toSet());

		mandatoryVhsTapes.forEach(this::identifyMysteryFilm);

		writeDataToFiles();

		log.info("{} film entries haven been updated!", FilmEntry.getTotalOfdbUpdates());
		log.info("Done in {} minutes!", (System.currentTimeMillis() - start) / 60000.0);

	}

	private void readDataFromFiles() {
		libraryCatalog.readDataFromFiles();
		ofdbManager.readDataFromFiles();
		log.info("Done reading files.");
	}

	private void writeDataToFiles() {
		ofdbManager.writeToFiles();
		libraryCatalog.writeToFiles();
	}

	public static void main(String [] args) {
		ListGenerator generator = new ListGenerator();
		generator.generateListAndWriteToCSV();
	}
}
