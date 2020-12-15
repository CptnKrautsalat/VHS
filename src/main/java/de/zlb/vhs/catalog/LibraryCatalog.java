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
        entry.titles.forEach(t -> entriesByTitle.put(t, entry));
    }

    private void analyzeCatalog() {
        long withoutYear = getAllEntries().filter(e -> !e.hasYear()).count();
        long withoutDirector = getAllEntries().filter(e -> !e.hasDirector()).count();
        log.info("Library catalog has {} entries, {} without year, {} without director.",
                getAllEntries().count(), withoutYear, withoutDirector);
    }

    public Stream<LibraryCatalogEntry> getEntriesWithSignaturePrefix(String signaturePrefix) {
        return entriesBySignaturePrefix.get(signaturePrefix).stream();
    }

    public void writeToFiles() {
        log.info("Writing library catalog data to CSV files...");
        writeFilmListToFile(filterAndSort(getAllEntries(), f -> f.directors.isEmpty()), "output/zlb/no_director.csv");
        writeFilmListToFile(filterAndSort(getAllEntries(), LibraryCatalogEntry::hasWrongYear), "output/zlb/wrong_year.csv");
        writeFilmListToFile(filterAndSort(getAllEntries(), f -> f.year.isEmpty()), "output/zlb/no_year.csv");
        writeFilmListToFile(createUnidentifiedButCompleteEntryList(), "output/zlb/mystery.csv");
        writeFilmListToFile(createMandatoryMysteryEntryList(), "output/zlb/mystery_mandatory.csv");

        Set<LibraryCatalogEntry> indentifiedVhsTapes = collectIdentifiedVhsTapes();
        indentifiedVhsTapes.forEach(LibraryCatalogEntry::updateBean);

        List<LibraryCatalogEntry> replacableEntryList = createReplacableEntryList(indentifiedVhsTapes);
        writeFilmListToFile(replacableEntryList, "output/zlb/replace.csv");
        List<LibraryCatalogEntry> nonReplacableEntryList = createNonReplacableEntryList(indentifiedVhsTapes);
        writeFilmListToFile(nonReplacableEntryList, "output/zlb/digitize.csv");

        List<LibraryCatalogEntry> germanDubOnlyOnVhs = collectEntriesWithGermanDubOnlyOnVhs();
        indentifiedVhsTapes.forEach(LibraryCatalogEntry::updateBean);

        writeFilmListToFile(germanDubOnlyOnVhs, "output/zlb/language.csv");
        List<LibraryCatalogEntry> replacableDubbedEntryList = createReplacableDubbedEntryList(germanDubOnlyOnVhs);
        replacableDubbedEntryList.removeIf(replacableEntryList::contains);
        writeFilmListToFile(replacableDubbedEntryList, "output/zlb/language_replace.csv");
        List<LibraryCatalogEntry> nonReplacableDubbedEntryList = createNonReplacableDubbedEntryList(germanDubOnlyOnVhs);
        nonReplacableDubbedEntryList.removeIf(nonReplacableEntryList::contains);
        writeFilmListToFile(nonReplacableDubbedEntryList, "output/zlb/language_digitize.csv");

        List<LibraryCatalogEntry> mandatoryReplacableEntryList = createMandatoryList(replacableEntryList, replacableDubbedEntryList);
        writeFilmListToFile(mandatoryReplacableEntryList, "output/zlb/replace_mandatory.csv");
        List<LibraryCatalogEntry> mandatoryNonReplacableEntryList = createMandatoryList(nonReplacableEntryList, nonReplacableDubbedEntryList);
        writeFilmListToFile(mandatoryNonReplacableEntryList, "output/zlb/digitize_mandatory.csv");

        log.info("...done writing!");
    }

    @SafeVarargs
    private List<LibraryCatalogEntry> createMandatoryList(Collection<LibraryCatalogEntry>... entries) {

        return Arrays.stream(entries)
                .flatMap(Collection::stream)
                .filter(LibraryCatalogEntry::isMandatory)
                .sorted(Comparator.comparingInt(LibraryCatalogEntry::getRentalsSince2010).reversed())
                .collect(Collectors.toList());

    }

    private List<LibraryCatalogEntry> filterAndSort(Stream<LibraryCatalogEntry> entries, Predicate<LibraryCatalogEntry> filter) {
        return entries
                .filter(filter)
                .sorted(Comparator.comparingInt(LibraryCatalogEntry::getRentalsSince2010).reversed())
                .collect(Collectors.toList());
    }

    public List<LibraryCatalogEntry> createUnidentifiedButCompleteEntryList() {
        return filterAndSort(getAllEntries(), f -> !f.isLinkedToOfdbFilm() && f.hasYear() && !f.directors.isEmpty()
                && f.isFeatureFilm() && f.isVhs());
    }

    private List<LibraryCatalogEntry> createMandatoryMysteryEntryList() {
        return filterAndSort(getAllEntries(), e -> e.isVhs() && e.isMandatory() && !e.isLinkedToOfdbFilm());
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
