package de.zlb.vhs.ofdb;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

public class OFDBListGenerator {
	
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
	}
	
	public void writeOFBDListToCSV () throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
		
		FileWriter writer = new FileWriter("films.csv");
		
		ColumnPositionMappingStrategy<FilmVersionEntryBean> mappingStrategy = 
				new ColumnPositionMappingStrategy<>();
		mappingStrategy.setType(FilmVersionEntryBean.class);
		
		String[] columns = new String[] { "title", "year", "filmLink", "medium", "publisher", "country", "rating", "versionLink" };
		mappingStrategy.setColumnMapping(columns);
		
		StatefulBeanToCsvBuilder<FilmVersionEntryBean> builder = new StatefulBeanToCsvBuilder<>(writer);
		StatefulBeanToCsv<FilmVersionEntryBean> beanWriter = builder
			.withSeparator(SEPARATOR)
			.withMappingStrategy(mappingStrategy)
			.build();
		
		List<FilmVersionEntryBean> beans = films
				.stream()
				.flatMap(f -> f.getVersions())
				.map(v -> v.toBean())
				.collect(Collectors.toList());
		
		beanWriter.write(beans);
		writer.close();
	}

	
	public static void main(String [] args) {
		try {
			OFDBListGenerator generator = new OFDBListGenerator();
			log.info("Generating OFDB list...");
			generator.generateOFDBList();
			log.info("Writing OFDB list to CSV file...");
			generator.writeOFBDListToCSV();
			log.info("Done!");
		} catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
			log.error(e.getMessage());
		}
	}
}
