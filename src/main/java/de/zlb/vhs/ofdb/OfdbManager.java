package de.zlb.vhs.ofdb;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import de.zlb.vhs.SortedManager;
import de.zlb.vhs.csv.CSVListHandler;
import de.zlb.vhs.csv.FilmVersionEntryBean;
import de.zlb.vhs.ofdb.stats.StatsCollector;
import de.zlb.vhs.ofdb.web.WebUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OfdbManager extends SortedManager<FilmEntry> {

    private static final Logger log = LogManager.getLogger(OfdbManager.class);

    public static final String OFDB_LINK_PREFIX = "https://ssl.ofdb.de/";
    public static final String[] MEDIUMS = {"V", "D", "B"};
    public static final String[] INDEXED = {"N", "J"};

    public static final int OFDB_PAGE_SIZE = 50;
    public static final int OFDB_RETRY_INTERVAL_MILLIS = 60000;
    public static final int OFDB_ATTEMPTS = 5;
    public static final int OFDB_INTERVAL_MILLIS = 100;

    public static final int MAX_FILMS_PER_SMALL_FILE = 10000;

    private final Map<String, FilmEntry> ofdbFilms = new HashMap<>();
    private final CSVListHandler<FilmVersionEntryBean> ofdbCsvListHandler = new CSVListHandler<>(',');
    private Set<FilmEntry> vhsOnly;

    private void addFilm(FilmEntry film) {
        FilmEntry existingFilm = ofdbFilms.get(film.link);
        if (existingFilm == null) {
            ofdbFilms.put(film.link, film);
        } else {
            existingFilm.mergeVersions(film);
        }
    }

    private void convertBeansToFilmList(List<FilmVersionEntryBean> beans) {
        beans
                .stream()
                .map(FilmEntry::new)
                .forEach(this::addFilm);
    }

    private void sortFilms() {
        getFilms().forEach(this::addEntry);
    }

    private Set<FilmEntry> getVHSOnlyFilms() {
        return ofdbFilms
                .values()
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
            log.info("Writing {} beans from {} films to file {}...",
                    beans.size(), films.size(), fileName);
            ofdbCsvListHandler.writeListToCSVFile(beans, fileName);
            log.info("... done writing to file {}!", fileName);
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            log.error("Failed to write to file {}!", fileName, e);
        }
    }

    public Stream<FilmEntry> getFilms() {
        return ofdbFilms.values().stream();
    }

    public boolean isEmpty() {
        return ofdbFilms.isEmpty();
    }

    public void readDataFromFiles() {
        ofdbCsvListHandler.readListFromDirectory("input/ofdb", this::convertBeansToFilmList, FilmVersionEntryBean.class);
        log.info("{} films loaded, {} with additional OFDB data.", ofdbFilms.size(), getFilms().filter(FilmEntry::hasAdditionalOfdbData).count());

        log.info("Done reading files.");
    }

    public void processFilmData() {

        sortFilms();

        log.info("Evaluating all OFDB data:");
        new StatsCollector().collectStats(ofdbFilms.values());

        log.info("Evaluating VHS-only OFDB data:");
        vhsOnly = getVHSOnlyFilms();
        new StatsCollector().collectStats(vhsOnly);
    }

    public void writeToFiles() {
        log.info("Writing OFDB data to CSV files...");
        writeFilmListToFile(ofdbFilms.values(), "output/ofdb/ofdb.csv");
        writeFilmListToFile(vhsOnly, "output/ofdb/vhs_only.csv");
        log.info("...done writing!");
    }

    public void collectOFDBData() {
        log.info("Generating OFDB list from web...");

        for (String medium : OfdbManager.MEDIUMS) {
            for (String indexed : OfdbManager.INDEXED) {
                collectOFDBDataForMedium(medium, indexed);
            }
        }

    }

    private void collectOFDBDataForMedium(String medium, String indexed) {
        boolean emptyPage = false;
        for (int position = 0; !emptyPage; position += OfdbManager.OFDB_PAGE_SIZE) {
            for (int attempt = 0; (attempt == 0) || (emptyPage && attempt < OfdbManager.OFDB_ATTEMPTS); ++attempt) {
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
                    try {
                        Thread.sleep(OfdbManager.OFDB_RETRY_INTERVAL_MILLIS);
                    } catch (InterruptedException e) {
                        log.error(e);
                    }
                } else {
                    log.info("Collected " + count + " films from ofdb.");
                    films.forEach(this::addFilm);
                }
            }
            //let's take it easy on the old OFDB
            try {
                Thread.sleep(OfdbManager.OFDB_INTERVAL_MILLIS);
            } catch (InterruptedException e) {
                log.error(e);
            }
        }
    }

}
