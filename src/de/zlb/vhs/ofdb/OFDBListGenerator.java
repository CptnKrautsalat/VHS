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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OFDBListGenerator {

	public static final Logger log = LogManager.getLogger(OFDBListGenerator.class);

	public static final String OFDB_LINK_PREFIX = "https://ssl.ofdb.de/";
	public static final String[] MEDIUMS = {"D", "B"};
	public static final String[] INDEXED = {"N", "J"};

	public static final int OFDB_PAGE_SIZE = 50;
	public static final int OFDB_RETRY_INTERVAL_MILLIS = 60000;

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

	public void generateListAndWriteToCSV() throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, InterruptedException {
		File csvFile = new File("films.csv");

		if (csvFile.exists() && csvFile.isFile()) {
			convertBeansToFilmList(CSVListUtil.readOFDBListFromCSV(csvFile));
		} else {
			collectOFDBData();
		}

		CSVListUtil.writeOFBDListToCSV(films, "films9.csv");
		log.info("Done!");
	}

	private void collectOFDBData() throws IOException, InterruptedException {
		log.info("Generating OFDB list from web...");

		for (String medium : MEDIUMS) {
			for (String indexed : INDEXED) {
				collectOFDBDataForMedium(medium, indexed);
			}
		}

	}

	private void collectOFDBDataForMedium(String medium, String indexed) throws IOException, InterruptedException {
		boolean emptyPage = false;
		for (int position = 0; !emptyPage; position += OFDB_PAGE_SIZE) {
			for (int attempt = 0; (attempt == 0) || (emptyPage && attempt < 3); ++attempt) {
				String url = WebUtil.generateOfdbUrl(medium, indexed, position);
				Set <FilmEntry> films = WebUtil.generateOFDBList(url);
				int count = films.size();
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
			Thread.sleep(250);
		}
	}

	public static void main(String [] args) {
		try {
			OFDBListGenerator generator = new OFDBListGenerator();
			generator.generateListAndWriteToCSV();
		} catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException | InterruptedException e) {
			log.error(e.getMessage());
		}
	}
}
