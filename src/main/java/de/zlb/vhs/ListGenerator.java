package de.zlb.vhs;

import de.zlb.vhs.catalog.LibraryCatalog;
import de.zlb.vhs.catalog.LibraryCatalogEntry;
import de.zlb.vhs.ofdb.CombinedFilm;
import de.zlb.vhs.ofdb.OfdbManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ListGenerator {

	private static final Logger log = LogManager.getLogger(ListGenerator.class);

	private final List<CombinedFilm> combinedFilms = new LinkedList<>();
	private final OfdbManager ofdbManager = new OfdbManager();
	private final LibraryCatalog libraryCatalog = new LibraryCatalog();

	private void combineFilms() {
		log.info("Combining films...");
		ofdbManager
				.getFilms()
				.forEach(f -> {
					List<LibraryCatalogEntry> libraryCatalogEntries = libraryCatalog
							.getCatalogValues()
							.filter(f::matchesLibraryCatalogEntry)
							.collect(Collectors.toList());
					if (!libraryCatalogEntries.isEmpty()) {
						CombinedFilm combinedFilm = new CombinedFilm(f);
						libraryCatalogEntries.forEach(combinedFilm::addLibraryCatalogEntry);
						combinedFilms.add(combinedFilm);
					}
				});
		log.info("... finished combining {} films!", combinedFilms.size());
	}

	public void generateListAndWriteToCSV() {

		readDataFromFiles();

		if (ofdbManager.isEmpty()) {
			ofdbManager.collectOFDBData();
		}

		ofdbManager.processFilmData();
		combineFilms();

		log.info("Done!");

	}

	private void readDataFromFiles() {
		libraryCatalog.readDataFromFiles();
		ofdbManager.readDataFromFiles();
		log.info("Done reading files.");
	}

	public static void main(String [] args) {
		ListGenerator generator = new ListGenerator();
		generator.generateListAndWriteToCSV();
	}
}
