package de.zlb.vhs.ofdb;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import de.zlb.vhs.ofdb.csv.CSVListUtil;
import de.zlb.vhs.ofdb.csv.FilmVersionEntryBean;
import de.zlb.vhs.ofdb.web.WebUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class OFDBListGenerator {

	public static final Logger log = LogManager.getLogger(OFDBListGenerator.class);

	public static final String OFDB_LINK_PREFIX = "https://ssl.ofdb.de/";
	public static final String[] MEDIUMS = {"D", "B"};
	public static final String[] INDEXED = {"N", "J"};

	public static final int OFDB_PAGE_SIZE = 50;
	public static final int OFDB_RETRY_INTERVAL_MILLIS = 60000;
	public static final int OFDB_ATTEMPTS = 5;
	public static final int OFDB_INTERVAL_MILLIS = 100;

	private final Set<FilmEntry> films = new HashSet<>();

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


	private void readOFDBListFromDirectory(String directory) {
		try (Stream<Path> paths = Files.walk(Paths.get(directory))) {
			paths
					.filter(Files::isRegularFile)
					.map(Path::toString)
					.forEach(this::readOFDBListFromFile);
		} catch (IOException e) {
			log.error("Failed to read files in directory " + directory, e);
		}
	}

	public void generateListAndWriteToCSV() throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, IOException {

		readOFDBListFromDirectory("input/ofdb");
		log.info("Done reading files, " + films.size() + " films loaded.");

		try {
			if (films.isEmpty()) {
				collectOFDBData();
			}
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		} finally {
			CSVListUtil.writeOFBDListToCSV(films, "output/ofdb.csv");
			log.info("Done!");
		}

	}

	private void readOFDBListFromFile(String fileName) {
		File csvFile = new File(fileName);
		try {
			convertBeansToFilmList(CSVListUtil.readOFDBListFromCSV(csvFile));
		} catch (IOException e) {
			log.error("Failed to read file " + fileName, e);
		}
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
		try {
			OFDBListGenerator generator = new OFDBListGenerator();
			generator.generateListAndWriteToCSV();
		} catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
			log.error(e.getMessage());
		}
	}
}
