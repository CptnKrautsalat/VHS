package de.zlb.vhs.catalog;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.zlb.vhs.ofdb.csv.CSVListHandler;
import de.zlb.vhs.ofdb.csv.LibraryCatalogEntryBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
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

}
