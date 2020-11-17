package de.zlb.vhs.catalog;

import de.zlb.vhs.ofdb.csv.CSVListHandler;
import de.zlb.vhs.ofdb.csv.LibraryCatalogEntryBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Stream;

public class LibraryCatalog {

    private static final Logger log = LogManager.getLogger(LibraryCatalog.class);

    private final Set<LibraryCatalogEntryBean> libraryCatalogBeans = new HashSet<>();
    private final Map<String, LibraryCatalogEntry> entries = new HashMap<>();

    private final CSVListHandler<LibraryCatalogEntryBean> libraryCsvListHandler = new CSVListHandler<>(';');

    public void readDataFromFiles() {
        libraryCsvListHandler.readListFromDirectory("input/zlb", this::addBeansToLibraryCatalog, LibraryCatalogEntryBean.class);
        log.info("{} library catalog entries loaded from {} beans.", entries.size(), libraryCatalogBeans.size());
    }

    private void addBeansToLibraryCatalog(List<LibraryCatalogEntryBean> beans) {
        libraryCatalogBeans.addAll(beans);
        beans.forEach(this::addBeanToLibraryCatalog);
    }

    private void addBeanToLibraryCatalog(LibraryCatalogEntryBean bean) {
        LibraryCatalogEntry existingEntry = entries.get(bean.mediaNumber);
        if (existingEntry == null) {
            entries.put(bean.mediaNumber, new LibraryCatalogEntry(bean));
        } else {
            existingEntry.addBean(bean);
        }
    }

    public Stream<LibraryCatalogEntry> getCatalogValues() {
        return entries.values().stream();
    }
}
