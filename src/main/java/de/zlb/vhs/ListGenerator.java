package de.zlb.vhs;

import de.zlb.vhs.catalog.LibraryCatalog;
import de.zlb.vhs.catalog.LibraryCatalogEntry;
import de.zlb.vhs.ofdb.FilmEntry;
import de.zlb.vhs.ofdb.OfdbManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
				.filter(LibraryCatalogEntry::hasYear)
				.forEach(lce -> {
					Optional<FilmEntry> film = findMatchingOfdbFilmEntry(lce);
					List<LibraryCatalogEntry> matches = libraryCatalog
							.getEntriesWithYear(lce.year)
							.filter(other -> lce.matchesTitlesAndDirectors(other, false))
							.collect(Collectors.toList());
					if (!(film.isEmpty() && matches.size() < 2)) {
						CombinedFilm combinedFilm = film.isPresent() && film.get().isLinkedToFilm()
								? film.get().getFilm()
								: new CombinedFilm(film);
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

	Optional<FilmEntry> findMatchingOfdbFilmEntry(LibraryCatalogEntry libraryCatalogEntry) {
		Set<FilmEntry> films = ofdbManager
				.getEntriesWithYear(libraryCatalogEntry.year)
				.filter(f -> f.matchesTitles(libraryCatalogEntry, false))
				.collect(Collectors.toSet());
		if (films.size() > 1) {
			films.removeIf(f -> !f.matchesTitles(libraryCatalogEntry, true));
		}
		if (films.size() > 1) {
			films.removeIf(f -> !libraryCatalogEntry.matchesYear(f.year, true));
		}
		if (films.size() > 1) {
			log.debug("Library catalog entry {} matches {} films: {}", libraryCatalogEntry, films.size(),
					films.stream().map(f -> f.link).collect(Collectors.joining(" ; ")));
		}
		return films.stream().findFirst();
	}

	public void generateListAndWriteToCSV() {

		readDataFromFiles();

		if (ofdbManager.isEmpty()) {
			ofdbManager.collectOFDBData();
		}

		ofdbManager.processFilmData();
		combineFilms();
		writeDataToFiles();

		log.info("Done!");

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
