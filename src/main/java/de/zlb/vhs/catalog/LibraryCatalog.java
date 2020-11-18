package de.zlb.vhs.catalog;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import de.zlb.vhs.ofdb.FilmEntry;
import de.zlb.vhs.ofdb.csv.CSVListHandler;
import de.zlb.vhs.ofdb.csv.FilmVersionEntryBean;
import de.zlb.vhs.ofdb.csv.LibraryCatalogEntryBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LibraryCatalog {

    private static final Logger log = LogManager.getLogger(LibraryCatalog.class);

    private final Set<LibraryCatalogEntryBean> beans = new HashSet<>();
    private final Multimap<String, LibraryCatalogEntry> entriesByYear = HashMultimap.create();
    private final Multimap<String, LibraryCatalogEntry> entriesByMediaNumber = HashMultimap.create();
    private final Multimap<String, LibraryCatalogEntry> entriesByTitle = HashMultimap.create();
    private final Multimap<String, LibraryCatalogEntry> entriesByDirector = HashMultimap.create();

    private final CSVListHandler<LibraryCatalogEntryBean> libraryCsvListHandler = new CSVListHandler<>(';');

    public void readDataFromFiles() {
        libraryCsvListHandler.readListFromDirectory("input/zlb", this::addBeansToLibraryCatalog, LibraryCatalogEntryBean.class);
        log.info("{} library catalog entries loaded from {} beans.", entriesByYear.size(), beans.size());
        analyzeCatalog();
    }

    private void addBeansToLibraryCatalog(List<LibraryCatalogEntryBean> beans) {
        this.beans.addAll(beans);
        beans.forEach(this::addBeanToLibraryCatalog);
    }

    private void addBeanToLibraryCatalog(LibraryCatalogEntryBean bean) {
        LibraryCatalogEntry entry = new LibraryCatalogEntry(bean);
        entriesByYear.put(entry.year, entry);
        entriesByMediaNumber.put(entry.getMediaNumber(), entry);
        entry.directors.forEach(d -> entriesByDirector.put(d, entry));
        entry.titles.forEach(t -> entriesByTitle.put(t, entry));
    }

    public Stream<LibraryCatalogEntry> getAllEntries() {
        return entriesByYear.values().stream();
    }

    public Stream<LibraryCatalogEntry> getEntriesWithYear(String year) {
        return entriesByYear.get(year).stream();
    }

    private void analyzeCatalog() {
        long withoutYear = entriesByYear.get("").size();
        long withoutDirector = getAllEntries().filter(e -> !e.hasDirector()).count();
        log.info("Library catalog has {} entries, {} without year, {} without director.",
                getAllEntries().count(), withoutYear, withoutDirector);
    }

    public void writeToFiles() {
        log.info("Writing library catalog data to CSV files...");
        writeFilmListToFile(getAllEntries().filter(e -> !e.hasDirector()).collect(Collectors.toSet()), "output/zlb/no_director.csv");
        writeFilmListToFile(entriesByYear.get(""), "output/zlb/no_year.csv");
        log.info("...done writing!");
    }

    private void writeFilmListToFile(Collection<LibraryCatalogEntry> entries, String fileName) {
        List<LibraryCatalogEntryBean> beans = entries
                .stream()
                .map(e -> e.bean)
                .collect(Collectors.toList());
        try {
            log.info("Writing {} beans to file {}...", beans.size(), fileName);
            libraryCsvListHandler.writeListToCSVFile(beans, fileName);
            log.info("... done writing to file {}!", fileName);
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            log.error("Failed to write to file {}!", fileName, e);
        }
    }

}
