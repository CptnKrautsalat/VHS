package de.zlb.vhs.catalog;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import de.zlb.vhs.SortedManager;
import de.zlb.vhs.csv.CSVListHandler;
import de.zlb.vhs.csv.LibraryCatalogEntryBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LibraryCatalog extends SortedManager<LibraryCatalogEntry> {

    private static final Logger log = LogManager.getLogger(LibraryCatalog.class);

    private final Set<LibraryCatalogEntryBean> beans = new HashSet<>();
    private final Multimap<String, LibraryCatalogEntry> entriesByMediaNumber = HashMultimap.create();
    private final Multimap<String, LibraryCatalogEntry> entriesByTitle = HashMultimap.create();
    private final Multimap<String, LibraryCatalogEntry> entriesByDirector = HashMultimap.create();
    private final Multimap<String, LibraryCatalogEntry> entriesBySignaturePrefix = HashMultimap.create();

    private final CSVListHandler<LibraryCatalogEntryBean> libraryCsvListHandler = new CSVListHandler<>(';');

    public void readDataFromFiles() {
        libraryCsvListHandler.readListFromDirectory("input/zlb", this::addBeansToLibraryCatalog, LibraryCatalogEntryBean.class);
        log.info("{} library catalog entries loaded from {} beans.", getAllEntries().count(), beans.size());
        analyzeCatalog();
    }

    private void addBeansToLibraryCatalog(List<LibraryCatalogEntryBean> beans) {
        this.beans.addAll(beans);
        beans.forEach(this::addBeanToLibraryCatalog);
    }

    private void addBeanToLibraryCatalog(LibraryCatalogEntryBean bean) {
        LibraryCatalogEntry entry = new LibraryCatalogEntry(bean);
        addEntry(entry);
        entriesByMediaNumber.put(entry.getMediaNumber(), entry);
        entriesBySignaturePrefix.put(entry.signaturePrefix, entry);
        entry.directors.forEach(d -> entriesByDirector.put(d, entry));
        entry.titles.forEach(t -> entriesByTitle.put(t, entry));
    }

    private void analyzeCatalog() {
        long withoutYear = getEntriesWithYear("").count();
        long withoutDirector = getAllEntries().filter(e -> !e.hasDirector()).count();
        log.info("Library catalog has {} entries, {} without year, {} without director.",
                getAllEntries().count(), withoutYear, withoutDirector);
    }

    public Stream<LibraryCatalogEntry> getEntriesWithSignaturePrefix(String signaturePrefix) {
        return entriesBySignaturePrefix.get(signaturePrefix).stream();
    }

    public void writeToFiles() {
        log.info("Writing library catalog data to CSV files...");
        writeFilmListToFile(getAllEntries().filter(e -> !e.hasDirector()).collect(Collectors.toSet()), "output/zlb/no_director.csv");
        writeFilmListToFile(getAllEntries().filter(LibraryCatalogEntry::hasWrongYear).collect(Collectors.toSet()), "output/zlb/wrong_year.csv");
        writeFilmListToFile(getEntriesWithYear("").collect(Collectors.toSet()), "output/zlb/no_year.csv");
        writeFilmListToFile(createUnidentifiedButCompleteEntryList(), "output/zlb/mystery.csv");

        Set<LibraryCatalogEntry> indentifiedVhsTapes = collectIdentifiedVhsTapes();
        indentifiedVhsTapes.forEach(LibraryCatalogEntry::updateBean);

        writeFilmListToFile(createReplacableEntryList(indentifiedVhsTapes), "output/zlb/replace.csv");
        writeFilmListToFile(createNonReplacableEntryList(indentifiedVhsTapes), "output/zlb/digitize.csv");

        List<LibraryCatalogEntry> germanDubOnlyOnVhs = collectEntriesWithGermanDubOnlyOnVhs();
        indentifiedVhsTapes.forEach(LibraryCatalogEntry::updateBean);

        writeFilmListToFile(germanDubOnlyOnVhs, "output/zlb/language.csv");
        writeFilmListToFile(createReplacableDubbedEntryList(germanDubOnlyOnVhs), "output/zlb/language_replace.csv");
        writeFilmListToFile(createNonReplacableDubbedEntryList(germanDubOnlyOnVhs), "output/zlb/language_digitize.csv");


        log.info("...done writing!");
    }

    private List<LibraryCatalogEntry> filterAndSort(Stream<LibraryCatalogEntry> entries, Predicate<LibraryCatalogEntry> filter) {
        return entries
                .filter(filter)
                .sorted(Comparator.comparingInt(LibraryCatalogEntry::getRentalsSince2010).reversed())
                .collect(Collectors.toList());
    }

    private List<LibraryCatalogEntry> createUnidentifiedButCompleteEntryList() {
        return filterAndSort(getAllEntries(), f -> !f.isLinkedToOfdbFilm() && f.hasYear() && f.directors.size() == 1
                && !f.hasWrongYear() && !f.isTvShow() && f.isVhs() && f.signaturePrefix.startsWith("Film 10 ")
                && f.getRentalsSince2010() > 0);
    }

    private List<LibraryCatalogEntry> createNonReplacableEntryList(Collection<LibraryCatalogEntry> tapes) {
        return filterAndSort(tapes.stream(), f -> !f.getFilm().existsDigitally());
    }

    private List<LibraryCatalogEntry> createReplacableEntryList(Collection<LibraryCatalogEntry> tapes) {
        return filterAndSort(tapes.stream(), f -> f.getFilm().existsDigitally());
    }

    private List<LibraryCatalogEntry> createNonReplacableDubbedEntryList(Collection<LibraryCatalogEntry> tapes) {
        return filterAndSort(tapes.stream(), f -> !f.getFilm().germanDubExistsDigitally());
    }

    private List<LibraryCatalogEntry> createReplacableDubbedEntryList(Collection<LibraryCatalogEntry> tapes) {
        return filterAndSort(tapes.stream(), f -> f.getFilm().germanDubExistsDigitally());
    }

    private List<LibraryCatalogEntry> collectEntriesWithGermanDubOnlyOnVhs() {
        return filterAndSort(getAllEntries(), f -> f.isVhs() && f.isLinkedToOfdbFilm()
                && f.languages.contains("ger") && f.getFilm().germanDubOnlyOnVhsInLibraryCatalog());
    }

    private Set<LibraryCatalogEntry> collectIdentifiedVhsTapes() {
        return getAllEntries()
                .filter(LibraryCatalogEntry::isVhs)
                .filter(LibraryCatalogEntry::isLinkedToOfdbFilm)
                .filter(e -> e.getFilm().isOnlyOnVhsInCatalog())
                .collect(Collectors.toSet());
    }

    private void writeFilmListToFile(Collection<LibraryCatalogEntry> entries, String fileName) {
        List<LibraryCatalogEntryBean> beans = entries
                .stream()
                .map(e -> e.bean)
                .collect(Collectors.toList());
        try {
            log.info("Writing {} beans to file {}...", beans.size(), fileName);
            libraryCsvListHandler.writeListToCSVFile(beans, fileName);
            log.trace("... done writing to file {}!", fileName);
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            log.error("Failed to write to file {}!", fileName, e);
        }
    }

}
