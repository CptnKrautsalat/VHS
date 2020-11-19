package de.zlb.vhs;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.zlb.vhs.catalog.LibraryCatalogEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public abstract class SortedManager<E extends ISortableEntry> {

    private static final Logger log = LogManager.getLogger(SortedManager.class);

    private final Multimap<String, E> entriesByYear = HashMultimap.create();

    public void addEntry(E entry) {
        entriesByYear.put(entry.getYear(), entry);
    }

    public Stream<E> getEntriesWithYear(String year) {
        Set<E> result = new HashSet<>(entriesByYear.get(year));

        try {
            int yearAsNumber = Integer.parseInt(year);
            result.addAll(entriesByYear.get(String.valueOf(yearAsNumber - 1)));
            result.addAll(entriesByYear.get(String.valueOf(yearAsNumber + 1)));
        } catch (NumberFormatException e) {
            log.warn("{} is not a year!", year);
        }

        return result.stream();
    }

    public Stream<E> getAllEntries() {
        return entriesByYear.values().stream();
    }
}
