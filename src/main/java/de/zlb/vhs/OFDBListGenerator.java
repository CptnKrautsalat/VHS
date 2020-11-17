package de.zlb.vhs;

import com.google.common.collect.Lists;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import de.zlb.vhs.catalog.LibraryCatalog;
import de.zlb.vhs.catalog.LibraryCatalogEntry;
import de.zlb.vhs.ofdb.CombinedFilm;
import de.zlb.vhs.ofdb.FilmEntry;
import de.zlb.vhs.ofdb.FilmVersionEntry;
import de.zlb.vhs.ofdb.csv.CSVListHandler;
import de.zlb.vhs.ofdb.csv.FilmVersionEntryBean;
import de.zlb.vhs.ofdb.stats.StatsCollector;
import de.zlb.vhs.ofdb.web.WebUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class OFDBListGenerator {

	public static final Logger log = LogManager.getLogger(OFDBListGenerator.class);

	public static final String OFDB_LINK_PREFIX = "https://ssl.ofdb.de/";
	public static final String[] MEDIUMS = {"V", "D", "B"};
	public static final String[] INDEXED = {"N", "J"};

	public static final int OFDB_PAGE_SIZE = 50;
	public static final int OFDB_RETRY_INTERVAL_MILLIS = 60000;
	public static final int OFDB_ATTEMPTS = 5;
	public static final int OFDB_INTERVAL_MILLIS = 100;
	public static final int MAX_FILMS_PER_SMALL_FILE = 10000;

	private final Map<String, FilmEntry> ofdbFilms = new HashMap<>();
	private final List<CombinedFilm> combinedFilms = new LinkedList<>();

	private final CSVListHandler<FilmVersionEntryBean> ofdbCsvListHandler = new CSVListHandler<>(',');

	private final LibraryCatalog libraryCatalog = new LibraryCatalog();

	private void addFilm(FilmEntry film) {
		FilmEntry existingFilm = ofdbFilms.get(film.link);
		if (existingFilm == null) {
			ofdbFilms.put(film.link, film);
		} else {
			existingFilm.mergeVersions(film);
		}
	}

	private void convertBeansToFilmList(List<FilmVersionEntryBean> beans) {
		beans
				.stream()
				.map(FilmEntry::new)
				.forEach(this::addFilm);
	}

	private Set<FilmEntry> getVHSOnlyFilms() {
		return ofdbFilms
				.values()
				.stream()
				.filter(FilmEntry::isVHSOnly)
				.collect(Collectors.toSet());
	}

	private List<FilmVersionEntryBean> convertFilmListToBeans(Collection<FilmEntry> films) {
		return films
				.stream()
				.flatMap(FilmEntry::getVersions)
				.map(FilmVersionEntry::toBean)
				.collect(Collectors.toList());
	}

	private void writeFilmListToFile(Collection<FilmEntry> films, String fileName) {
		List<FilmVersionEntryBean> beans = convertFilmListToBeans(films);
		try {
			log.info("Writing {} beans from {} films to file {}...",
					beans.size(), films.size(), fileName);
			ofdbCsvListHandler.writeListToCSVFile(beans, fileName);
			log.info("... done writing to file {}!", fileName);
		} catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
			log.error("Failed to write to file {}!", fileName, e);
		}
	}

	private void combineFilms() {
		log.info("Combining films...");
		ofdbFilms
				.values()
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

		try {
			if (ofdbFilms.isEmpty()) {
				collectOFDBData();
			}
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		} finally {
			combineFilms();
			processFilmData();
			log.info("Done!");
		}

	}

	private void readDataFromFiles() {
		libraryCatalog.readDataFromFiles();

		ofdbCsvListHandler.readListFromDirectory("input/ofdb", this::convertBeansToFilmList, FilmVersionEntryBean.class);
		log.info(ofdbFilms.size() + " films loaded.");

		log.info("Done reading files.");
	}

	private void fixBrokenData() {
		log.info("Looking for invalid entries...");
		ofdbFilms
				.values()
				.stream()
				.flatMap(FilmEntry::getVersions)
				.forEach(FilmVersionEntry::fixBrokenEntry);
		log.info("Invalid entries fixed!");
	}

	private void processFilmData() {

		log.info("Evaluating all OFDB data:");
		new StatsCollector().collectStats(ofdbFilms.values());

		log.info("Evaluating VHS-only OFDB data:");
		Set<FilmEntry> vhsOnly = getVHSOnlyFilms();
		new StatsCollector().collectStats(vhsOnly);

		log.info("Writing data to CSV files...");
		List <FilmEntry> sortedFilms = ofdbFilms
				.values()
				.stream()
				.sorted(Comparator.comparing(FilmEntry::getTitle))
				.collect(Collectors.toList());
		List<List<FilmEntry>> smallerLists = Lists.partition(sortedFilms, MAX_FILMS_PER_SMALL_FILE);
		AtomicInteger i = new AtomicInteger();
		smallerLists.forEach(l -> writeFilmListToFile(l, "output/ofdb_" + i.getAndIncrement() + ".csv"));
		writeFilmListToFile(ofdbFilms.values(), "output/ofdb.csv");
		writeFilmListToFile(vhsOnly, "output/vhs_only.csv");
		log.info("...done writing!");
	}

	private void collectOFDBData() throws InterruptedException {
		log.info("Generating OFDB list from web...");

		for (String medium : MEDIUMS) {
			for (String indexed : INDEXED) {
				collectOFDBDataForMedium(medium, indexed);
			}
		}

	}

	private void collectOFDBDataForMedium(String medium, String indexed) throws InterruptedException {
		boolean emptyPage = false;
		for (int position = 0; !emptyPage; position += OFDB_PAGE_SIZE) {
			for (int attempt = 0; (attempt == 0) || (emptyPage && attempt < OFDB_ATTEMPTS); ++attempt) {
				String url = WebUtil.generateOfdbUrl(medium, indexed, position);
				Set <FilmEntry> films = null;
				try {
					films = WebUtil.generateOFDBList(url);
				} catch (IOException e) {
					log.error("Failed to collect data from ofdb.", e);
				}
				int count = films == null ? 0 : films.size();
				emptyPage = count == 0;

				//OFDB sometimes returns empty pages even if there is still data left
				if (emptyPage) {
					log.info("Empty page at M=" + medium + ", indiziert=" + indexed + ", pos=" + position + ", attempt=" + attempt);
					Thread.sleep(OFDB_RETRY_INTERVAL_MILLIS);
				} else {
					log.info("Collected " + count + " films from ofdb.");
					films.forEach(this::addFilm);
				}
			}
			//let's take it easy on the old OFDB
			Thread.sleep(OFDB_INTERVAL_MILLIS);
		}
	}

	public static void main(String [] args) {
		OFDBListGenerator generator = new OFDBListGenerator();
		generator.generateListAndWriteToCSV();
	}
}
