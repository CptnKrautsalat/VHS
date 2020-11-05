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

	public static final Logger log = LogManager.getLogger(OFDBListGenerator.class);
	
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

	public void generateListAndWriteToCSV() throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
		File csvFile = new File("films.csv");

		if (csvFile.exists() && csvFile.isFile()) {
			convertBeansToFilmList(CSVListUtil.readOFDBListFromCSV(csvFile));
		} else {
			Set <FilmEntry> films = WebUtil.generateOFDBList("https://ssl.ofdb.de/view.php?page=fsuche&Typ=N&AB=-&Titel=&Genre=-&Note=&HLand=-&Jahr=&Regie=&Darsteller=&Wo=V&Wer=&Land=-&Freigabe=-&Cut=A&Indiziert=N&Info=&Submit2=Suche+ausf%C3%BChren");
			films.forEach(this::addFilm);
		}

		CSVListUtil.writeOFBDListToCSV(films, "films3.csv");
		log.info("Done!");
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
