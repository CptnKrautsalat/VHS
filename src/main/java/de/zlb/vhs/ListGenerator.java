package de.zlb.vhs;

import de.zlb.vhs.catalog.LibraryCatalog;
import de.zlb.vhs.catalog.LibraryCatalogEntry;
import de.zlb.vhs.ofdb.CombinedFilm;
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
					List<FilmEntry> films = ofdbManager
							.getEntriesWithYear(lce.year)
							.filter(f -> f.matchesTitles(lce))
							.collect(Collectors.toList());
					List<LibraryCatalogEntry> matches = libraryCatalog
							.getEntriesWithYear(lce.year)
							.filter(lce::matchesTitlesAndDirectors)
							.collect(Collectors.toList());
					if (!(films.isEmpty() && matches.size() < 2)) {
						Optional<FilmEntry> film = films.isEmpty()
								? Optional.empty()
								: Optional.of(films.get(0));
						if (films.size() > 1) {
							log.warn("Library catalog entry {} matches {} films: {}", lce, films.size(),
									films.stream().map(f -> f.link).collect(Collectors.joining(";")));
						}
						CombinedFilm combinedFilm = film.isPresent() && film.get().isLinkedToFilm()
								? film.get().film
								: new CombinedFilm(film);
						combinedFilm.addLibraryCatalogEntry(lce);
						combinedFilm = lce.getFilm();
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
