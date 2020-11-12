package de.zlb.vhs.ofdb;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import de.zlb.vhs.ofdb.csv.CSVListHandler;
import de.zlb.vhs.ofdb.csv.FilmVersionEntryBean;
import de.zlb.vhs.ofdb.csv.LibraryCatalogEntryBean;
import de.zlb.vhs.ofdb.stats.StatsCollector;
import de.zlb.vhs.ofdb.web.WebUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.common.collect.Lists;

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

	private final Set<FilmEntry> films = new HashSet<>();
	private final Set<LibraryCatalogEntryBean> libraryCatalog = new HashSet<>();

	private final CSVListHandler<FilmVersionEntryBean> ofdbCsvListHandler = new CSVListHandler<>(',');
	private final CSVListHandler<LibraryCatalogEntryBean> libraryCsvListHandler = new CSVListHandler<>('#');

	private void addFilm(FilmEntry film) {
		if (films.contains(film)) {
			films.stream().filter(f -> f.equals(film)).forEach(f -> f.mergeVersions(film));
		} else {
			films.add(film);
		}
	}

	private void convertBeansToFilmList(List<FilmVersionEntryBean> beans) {
		beans
				.stream()
				.map(FilmEntry::new)
				.forEach(this::addFilm);
	}

	private void addBeansToLibraryCatalog(List<LibraryCatalogEntryBean> beans) {
		libraryCatalog.addAll(beans);
	}

	private Set<FilmEntry> getVHSOnlyFilms() {
		return films
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
			ofdbCsvListHandler.writeListToCSVFile(beans, fileName);
		} catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
			log.error("Failed to write to file " + fileName, e);
		}
	}

	public void generateListAndWriteToCSV() {

		readDataFromFiles();

		try {
			if (films.isEmpty()) {
				collectOFDBData();
			}
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		} finally {
			processFilmData();
			log.info("Done!");
		}

	}

	private void readDataFromFiles() {
		libraryCsvListHandler.readListFromDirectory("input/zlb", this::addBeansToLibraryCatalog, LibraryCatalogEntryBean.class);
		log.info(libraryCatalog.size() + " library catalog entries loaded.");

		ofdbCsvListHandler.readListFromDirectory("input/ofdb", this::convertBeansToFilmList, FilmVersionEntryBean.class);
		log.info(films.size() + " films loaded.");

		log.info("Done reading files.");
	}

	private void fixBrokenData() {
		log.info("Looking for invalid entries...");
		films
				.stream()
				.flatMap(FilmEntry::getVersions)
				.forEach(FilmVersionEntry::fixBrokenEntry);
		log.info("Invalid entries fixed!");
	}

	private void processFilmData() {

		log.info("Evaluating all OFDB data:");
		new StatsCollector().collectStats(films);

		log.info("Evaluating VHS-only OFDB data:");
		Set<FilmEntry> vhsOnly = getVHSOnlyFilms();
		new StatsCollector().collectStats(vhsOnly);

		log.info("Writing data to CSV files...");
		List <FilmEntry> sortedFilms = films
				.stream()
				.sorted(Comparator.comparing(FilmEntry::getTitle))
				.collect(Collectors.toList());
		List<List<FilmEntry>> smallerLists = Lists.partition(sortedFilms, MAX_FILMS_PER_SMALL_FILE);
		AtomicInteger i = new AtomicInteger();
		smallerLists.forEach(l -> writeFilmListToFile(l, "output/ofdb_" + i.getAndIncrement() + ".csv"));
		writeFilmListToFile(films, "output/ofdb.csv");
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
