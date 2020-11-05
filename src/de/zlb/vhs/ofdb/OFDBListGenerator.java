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

	public static final String OFDB_LINK_PREFIX = "https://ssl.ofdb.de/";
	public static final String[] MEDIUMS = {"V", "D", "B"};
	public static final String[] INDEXED = {"N", "J"};

	public static final Logger log = LogManager.getLogger(OFDBListGenerator.class);
	public static final int OFDB_PAGE_SIZE = 50;

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

		CSVListUtil.writeOFBDListToCSV(films, "films3.csv");
		log.info("Done!");
	}

	private void collectOFDBData() throws IOException, InterruptedException {
		log.info("Generating OFDB list from web...");

		for (String medium : MEDIUMS) {
			for (String indexed : INDEXED) {
				boolean finished = false;
				for (int position = 0; !finished; position += OFDB_PAGE_SIZE) {
					String url = WebUtil.generateOfdbUrl(medium, indexed, position);
					Set <FilmEntry> films = WebUtil.generateOFDBList(url);
					int count = films.size();
					finished = count == 0;
					log.info("Collected " + count + " films from ofdb.");
					films.forEach(this::addFilm);
					Thread.sleep(100);
				}
			}
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
