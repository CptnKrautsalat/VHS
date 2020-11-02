package de.zlb.vhs.ofdb;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class OFDBListGenerator {
	
	public static final Logger log = LogManager.getLogger(OFDBListGenerator.class);
	
	private final Set<FilmEntry> films = new HashSet<>();
	
	private FilmEntry parseFilmEntry (Element element) {
		return new FilmEntry(element.text(), element.attr("href"));
	}
	
	private FilmVersionEntry parseFilmVersionEntry (Element element) {
		return new FilmVersionEntry(element.text(), element.attr("href"));
	}
	
	private FilmEntry parseListElement(Element element, FilmEntry lastFilm) {
		if (lastFilm == null || element.attr("href").startsWith("film")) {
			return parseFilmEntry(element);
		}
		
		if (element.attr("href").contains("fassung")) {
			lastFilm.addVersion(parseFilmVersionEntry(element));
		}
		
		return lastFilm;
	}
	
	public void generateOFDBList() throws IOException {
		String url ="https://ssl.ofdb.de/view.php?page=fsuche&Typ=N&AB=-&Titel=&Genre=-&Note=&HLand=-&Jahr=&Regie=&Darsteller=&Wo=V&Wer=&Land=%21D&Freigabe=-&Cut=A&Indiziert=A&Info=&Submit2=Suche+ausf%C3%BChren";
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
		log.debug("done");
	}

	
	public static void main(String [] args) {
		try {
			new OFDBListGenerator().generateOFDBList();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
}
