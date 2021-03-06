package de.zlb.vhs.ofdb;

import com.google.common.collect.Lists;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import de.zlb.vhs.SortedManager;
import de.zlb.vhs.csv.CSVListHandler;
import de.zlb.vhs.csv.FilmVersionEntryBean;
import de.zlb.vhs.csv.LetterboxdEntryBean;
import de.zlb.vhs.ofdb.web.IndexStatus;
import de.zlb.vhs.ofdb.web.Medium;
import de.zlb.vhs.ofdb.web.OfdbAccessUtil;
import de.zlb.vhs.ofdb.web.SearchParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OfdbManager extends SortedManager<FilmEntry> {

    private static final Logger log = LogManager.getLogger(OfdbManager.class);

    public static final String OFDB_LINK_PREFIX = "https://ssl.ofdb.de/";
    public static final Medium[] MEDIA = {Medium.VHS, Medium.DVD, Medium.BLU_RAY};
    public static final IndexStatus[] INDEXED = {IndexStatus.INDEXED, IndexStatus.NOT_INDEXED};

    public static final int OFDB_PAGE_SIZE = 50;
    public static final int OFDB_RETRY_INTERVAL_MILLIS = 60000;
    public static final int OFDB_ATTEMPTS = 5;
    public static final int OFDB_INTERVAL_MILLIS = 100;

    private final Map<String, FilmEntry> ofdbFilms = new HashMap<>();
    private final CSVListHandler<FilmVersionEntryBean> ofdbCsvListHandler = new CSVListHandler<>(',');
    private final CSVListHandler<LetterboxdEntryBean> letterboxdCsvListHandler = new CSVListHandler<>(',');
    private Set<FilmEntry> vhsOnly;

    public void addFilm(FilmEntry film) {
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
            log.trace("... done writing to file {}!", fileName);
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            log.error("Failed to write to file {}!", fileName, e);
        }
    }

    private void writeLetterboxdListToFiles(Collection<FilmEntry> films) {
        List<LetterboxdEntryBean> beans = films.stream()
                .filter(f -> !f.isTVShow())
                .map(FilmEntry::generateLetterboxdBean)
                .collect(Collectors.toList());
        List<List<LetterboxdEntryBean>> partitions = Lists.partition(beans, 1000);
        AtomicInteger count = new AtomicInteger(0);
        partitions.forEach(l -> {
            String fileName = "output/letterboxd/vhs_only_" + count.getAndIncrement() + ".csv";
            try {
                log.info("Writing {} films to file {}...", l.size(), fileName);
                letterboxdCsvListHandler.writeListToCSVFile(l, fileName);
                log.trace("... done writing to file {}!", fileName);
            } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
                log.error("Failed to write to file {}!", fileName, e);
            }
        });
    }

    public Stream<FilmEntry> getFilms() {
        return ofdbFilms.values().stream();
    }

    public FilmEntry getFilm(String link) {
        return ofdbFilms.get(link);
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
        vhsOnly = getVHSOnlyFilms();
    }

    public void writeToFiles() {
        log.trace("Writing OFDB data to CSV files...");
        writeFilmListToFile(ofdbFilms.values(), "output/ofdb/ofdb.csv");
        writeFilmListToFile(vhsOnly, "output/ofdb/vhs_only.csv");
        writeLetterboxdListToFiles(vhsOnly);
        log.trace("...done writing!");
    }

    public void collectOFDBData() {
        log.info("Generating OFDB list from web...");

        for (Medium medium : OfdbManager.MEDIA) {
            for (IndexStatus indexed : OfdbManager.INDEXED) {
                collectOFDBDataForMedium(medium, indexed);
            }
        }

    }

    private void collectOFDBDataForMedium(Medium medium, IndexStatus indexed) {
        boolean emptyPage = false;
        for (int position = 0; !emptyPage; position += OfdbManager.OFDB_PAGE_SIZE) {
            for (int attempt = 0; (attempt == 0) || (emptyPage && attempt < OfdbManager.OFDB_ATTEMPTS); ++attempt) {
                SearchParameters searchParameters = OfdbAccessUtil.generateParametersForGeneralSearch(medium, indexed, position);
                Set <FilmEntry> films = null;
                try {
                    films = OfdbAccessUtil.generateOFDBList(searchParameters);
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
