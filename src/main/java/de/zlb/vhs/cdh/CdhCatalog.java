package de.zlb.vhs.cdh;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import de.zlb.vhs.SortedManager;
import de.zlb.vhs.catalog.LibraryCatalogEntry;
import de.zlb.vhs.csv.CSVListHandler;
import de.zlb.vhs.csv.CdhEntryBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class CdhCatalog extends SortedManager<CdhEntry> {

    private static final Logger log = LogManager.getLogger(CdhCatalog.class);

    private final List<CdhEntryBean> beans = new LinkedList<>();
    private final CSVListHandler<CdhEntryBean> csvHandler = new CSVListHandler<>('#');

    public void readDataFromFiles() {
        csvHandler.readListFromDirectory("input/cdh", this::addBeansToCatalog, CdhEntryBean.class);
        log.info("{} cdh catalog entries loaded from {} beans.", getAllEntries().count(), beans.size());
        analyzeCatalog();
    }

    public void writeDataToFiles() {

        getAllEntries().forEach(CdhEntry::updateBean);

        try {
            csvHandler.writeListToCSVFile(beans, "output/cdh/films.csv");
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            log.error("Failed to write cdh film list.", e);
        }
    }

    private void addBeansToCatalog(List<CdhEntryBean> beans) {
        this.beans.addAll(beans);
        beans.forEach(this::addBeanToCatalog);
    }

    private void addBeanToCatalog(CdhEntryBean bean) {
        addEntry(new CdhEntry(bean));
    }

    private void analyzeCatalog() {
        long withoutYear = getAllEntries().filter(e -> !e.hasYear()).count();
        long withoutDirector = getAllEntries().filter(e -> !e.hasDirector()).count();
        log.info("Cdh catalog has {} entries, {} without year, {} without director.",
                getAllEntries().count(), withoutYear, withoutDirector);
    }

    public List<CdhEntry> createUnidentifiedButCompleteEntryList() {
        return getAllEntries()
                .filter(e -> e.hasYear() && e.hasDirector() && e.getFilm() == null)
                .collect(Collectors.toList());
    }

}
