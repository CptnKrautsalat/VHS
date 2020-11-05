package de.zlb.vhs.ofdb;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.opencsv.bean.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

public class OFDBListGenerator {

	public static final String OFDB_LINK_PREFIX = "https://ssl.ofdb.de/";
	private static final char SEPARATOR = ',';

	public static final Logger log = LogManager.getLogger(OFDBListGenerator.class);
	
	private final Set<FilmEntry> films = new HashSet<>();
	
	private FilmEntry parseFilmEntry (Element element) {
		return new FilmEntry(element.text(), element.attr("href"));
	}
	
	private FilmVersionEntry parseFilmVersionEntry (FilmEntry film, Element element) {
		return new FilmVersionEntry(film, element.text(), element.attr("href"));
	}
	
	private FilmEntry parseListElement(Element element, FilmEntry lastFilm) {
		if (lastFilm == null || element.attr("href").startsWith("film")) {
			return parseFilmEntry(element);
		}
		
		if (element.attr("href").contains("fassung")) {
			lastFilm.addVersion(parseFilmVersionEntry(lastFilm, element));
		}
		
		return lastFilm;
	}
	
	public void generateOFDBList() throws IOException {
		log.info("Generating OFDB list from web...");

		String url ="https://ssl.ofdb.de/view.php?page=fsuche&Typ=N&AB=-&Titel=&Genre=-&Note=&HLand=-&Jahr=&Regie=&Darsteller=&Wo=V&Wer=&Land=-&Freigabe=-&Cut=A&Indiziert=N&Info=&Submit2=Suche+ausf%C3%BChren";
		Document doc = Jsoup.connect(url).get();
		log.debug(doc.title());
		Elements tds = doc.select("td:contains(Liste der gefundenen Fassungen)");
		FilmEntry lastFilm = null;
		for (Element td: tds) {
			Elements links = td.select("p a");
			for (Element link : links) {
				log.debug(link.text());
				lastFilm = parseListElement(link, lastFilm);
				films.add(lastFilm);
			}
		}
		for (FilmEntry film : films) {
			log.debug(film);
		}

		log.info("Created " + films.size() + " films!");
	}

	public List<FilmVersionEntryBean> readOFDBListFromCSV (File csvFile) throws IOException {

		log.info("Reading OFDB list from file...");

		FileReader reader = new FileReader(csvFile);

		CsvToBeanBuilder<FilmVersionEntryBean> builder = new CsvToBeanBuilder<FilmVersionEntryBean>(reader);
		CsvToBean<FilmVersionEntryBean> beanReader = builder
				.withSeparator(SEPARATOR)
				.withType(FilmVersionEntryBean.class)
				.build();

		List<FilmVersionEntryBean> beans = beanReader
				.stream()
				.collect(Collectors.toList());

		log.info("Read " + beans.size() + " beans from file!");

		reader.close();

		return beans;
	}

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
	
	public void writeOFBDListToCSV () throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
		log.info("Writing OFDB list to CSV file...");

		FileWriter writer = new FileWriter("films2.csv");
		
		StatefulBeanToCsvBuilder<FilmVersionEntryBean> builder = new StatefulBeanToCsvBuilder<>(writer);
		StatefulBeanToCsv<FilmVersionEntryBean> beanWriter = builder
			.withSeparator(SEPARATOR)
			.build();

		List<FilmVersionEntryBean> beans = films
				.stream()
				.flatMap(FilmEntry::getVersions)
				.map(FilmVersionEntry::toBean)
				.collect(Collectors.toList());

		beanWriter.write(beans);
		writer.close();
	}

	public void generateListAndWriteToCSV() throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
		File csvFile = new File("films.csv");

		if (csvFile.exists() && csvFile.isFile()) {
			convertBeansToFilmList(readOFDBListFromCSV(csvFile));
		} else {
			generateOFDBList();
		}

		writeOFBDListToCSV();
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
