package de.zlb.vhs.ofdb.web;

import de.zlb.vhs.ofdb.FilmEntry;
import de.zlb.vhs.ofdb.FilmVersionEntry;
import de.zlb.vhs.ofdb.OFDBListGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class WebUtil {
    public static final Logger log = LogManager.getLogger(WebUtil.class);

    private static FilmEntry parseFilmEntry (Element element) {
        return new FilmEntry(element.text(), element.attr("href"));
    }

    private static FilmVersionEntry parseFilmVersionEntry (FilmEntry film, Element element) {
        return new FilmVersionEntry(film, element.text(), element.attr("href"));
    }

    private static FilmEntry parseListElement(Element element, FilmEntry lastFilm) {
        if (lastFilm == null || element.attr("href").startsWith("film")) {
            return parseFilmEntry(element);
        }

        if (element.attr("href").contains("fassung")) {
            lastFilm.addVersion(parseFilmVersionEntry(lastFilm, element));
        }

        return lastFilm;
    }

    public static Set<FilmEntry> generateOFDBList(String url) throws IOException {
        log.info("Generating OFDB list from web...");

        Set<FilmEntry> films = new HashSet<>();
        Document doc = Jsoup.connect(url).get();
        log.debug(doc.title());
        Elements tds = doc.select("td:contains(Liste der gefundenen Fassungen)");
        FilmEntry lastFilm = null;
        for (Element td: tds) {
            Elements links = td.select("p a");
            for (Element link : links) {
                OFDBListGenerator.log.debug(link.text());
                lastFilm = parseListElement(link, lastFilm);
                films.add(lastFilm);
            }
        }
        for (FilmEntry film : films) {
            log.debug(film);
        }

        log.info("Created " + films.size() + " films!");

        return films;
    }
}
