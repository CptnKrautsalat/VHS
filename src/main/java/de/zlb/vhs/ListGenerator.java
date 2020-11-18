package de.zlb.vhs;

import de.zlb.vhs.catalog.LibraryCatalog;
import de.zlb.vhs.catalog.LibraryCatalogEntry;
import de.zlb.vhs.ofdb.CombinedFilm;
import de.zlb.vhs.ofdb.OfdbManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ListGenerator {

	private static final Logger log = LogManager.getLogger(ListGenerator.class);

	private final Set<CombinedFilm> matchedFilms = new HashSet<>();
	private final Set<CombinedFilm> duplicateFilms = new HashSet<>();
	private final OfdbManager ofdbManager = new OfdbManager();
	private final LibraryCatalog libraryCatalog = new LibraryCatalog();

	private void matchLibraryCatalogWithOfdb() {
		log.info("Matching library catalog with ofdb...");
		ofdbManager
				.getFilms()
				.forEach(f -> {
					List<LibraryCatalogEntry> libraryCatalogEntries = libraryCatalog
							.getEntriesWithYear(f.year)
							.filter(f::matchesTitles)
							.collect(Collectors.toList());
					if (!libraryCatalogEntries.isEmpty()) {
						CombinedFilm combinedFilm = new CombinedFilm(f);
						libraryCatalogEntries.forEach(combinedFilm::addLibraryCatalogEntry);
						matchedFilms.add(combinedFilm);
					}
				});
		log.info("... found {} matching films!", matchedFilms.size());
	}

	private void findDuplicateFilms () {
		log.info("Looking for duplicate films in library catalog...");
		libraryCatalog
				.getAllEntries()
				.filter(LibraryCatalogEntry::hasYear)
				.forEach(lce -> {
					List<LibraryCatalogEntry> matches = libraryCatalog
							.getEntriesWithYear(lce.year)
							.filter(lce::matchesTitlesAndDirectors)
							.collect(Collectors.toList());
					if (matches.size() > 1) {
						CombinedFilm combinedFilm = new CombinedFilm();
						combinedFilm.addLibraryCatalogEntry(lce);
						matches.forEach(combinedFilm::addLibraryCatalogEntry);
						duplicateFilms.add(combinedFilm);
					}
				});
		log.info("... found {} duplicate films!", duplicateFilms.size());
	}

	public void generateListAndWriteToCSV() {

		readDataFromFiles();

		if (ofdbManager.isEmpty()) {
			ofdbManager.collectOFDBData();
		}

		ofdbManager.processFilmData();
		matchLibraryCatalogWithOfdb();
		findDuplicateFilms();

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
