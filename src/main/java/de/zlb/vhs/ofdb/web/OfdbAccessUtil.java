package de.zlb.vhs.ofdb.web;

import de.zlb.vhs.ofdb.FilmEntry;
import de.zlb.vhs.ofdb.FilmVersionEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class OfdbAccessUtil {
    public static final Logger log = LogManager.getLogger(OfdbAccessUtil.class);

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

    public static SearchParameters generateParametersForGeneralSearch(Medium medium, IndexStatus indexed, int position) {
        return SearchParameters.builder()
                .medium(medium)
                .indexStatus(indexed)
                .position(position)
                .build();
    }

    public static SearchParameters generateParametersForSpecificSearch(String year, String director) {
        return SearchParameters.builder()
                .year(year)
                .director(director.replaceAll(" ", "+"))
                .build();
    }

    public static Set<FilmEntry> generateOFDBList(SearchParameters searchParameters) throws IOException {
        return generateOFDBList(searchParameters.toUrl());
    }

    public static Set<FilmEntry> generateOFDBList(String url) throws IOException {
        Set<FilmEntry> films = new HashSet<>();
        Document doc = Jsoup.connect(url).get();
        log.trace(doc.title());
        Elements tds = doc.select("td:contains(Liste der gefundenen Fassungen)");
        FilmEntry lastFilm = null;
        for (Element td: tds) {
            Elements links = td.select("p a");
            for (Element link : links) {
                log.trace(link.text());
                lastFilm = parseListElement(link, lastFilm);
                if (lastFilm.isValid()) {
                    films.add(lastFilm);
                }
            }
        }
        for (FilmEntry film : films) {
            log.trace(film);
        }

        return films;
    }

    public static Optional<AdditionalOfdbData> getAdditionalOfdbData(String url) {
        try {
            Set<String> titles = new HashSet<>();
            Document doc = Jsoup.connect(url).get();
            Element imdbLinkElement = doc.selectFirst("td a[href^=http://www.imdb.com/Title?]");
            Element altTitleTd = doc.selectFirst("font:contains(Alternativtitel:)");
            Element origTitleTr = doc.selectFirst("font:contains(Originaltitel:)");
            Element directorTr = doc.selectFirst("font:contains(Regie:)");
            titles.addAll(extractTitlesFromTableData(origTitleTr));
            titles.addAll(extractTitlesFromTableData(altTitleTd));
            String imdbLink = imdbLinkElement == null ? "" : imdbLinkElement.attr("href");
            return Optional.of(new AdditionalOfdbData(extractNamesFromTableData(directorTr), titles, imdbLink));
        } catch (IOException e) {
            log.error("Failed to get additional OFDB data from {}.", url, e);
            return Optional.empty();
        }
    }

    private static Set<String> extractNamesFromTableData(Element tableData) {
        Set<String> result = new HashSet<>();
        if (tableData == null) {
            return result;
        }
        for (Element span: tableData.parent().lastElementSibling().select("span")) {
            result.add(span.text());
        }
        return result;
    }

    private static Set<String> extractTitlesFromTableData(Element tableData) {
        Set<String> result = new HashSet<>();
        if (tableData == null) {
            return result;
        }
        for (Element b: tableData.parent().lastElementSibling().select("b")) {
            result.add(b.text());
        }
        return result;
    }
}
