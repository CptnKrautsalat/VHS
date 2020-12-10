package de.zlb.vhs;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class SortedManager<E extends ISortableEntry> {

    private static final Logger log = LogManager.getLogger(SortedManager.class);

    private final Multimap<String, E> entriesByYear = HashMultimap.create();
    private final Multimap<String, E> entriesByDirector = HashMultimap.create();
    private final Multimap<String, E> entriesByTitle = HashMultimap.create();

    public void addEntry(E entry) {
        entriesByYear.put(entry.getYear(), entry);
        entry.getDirectors().forEach(d -> entriesByDirector.put(d, entry));
        entry.getTitles().forEach(t -> entriesByTitle.put(t, entry));
    }

    public Stream<E> getEntriesWithYear(String year) {
        if (year.isBlank()) {
            return Stream.empty();
        }

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

    public Stream<E> getEntriesWithDirector(String director) {
        return entriesByDirector.get(director).stream();
    }

    public Stream<E> getEntriesWithTitle(String title) {
        return entriesByTitle.get(title).stream();
    }

    public Stream<E> getAllPossibleMatches(ISortableEntry entry) {
        List<Stream<E>> matches = new LinkedList<>();
        matches.add(getEntriesWithYear(entry.getYear()));
        entry.getDirectors().forEach(d -> matches.add(getEntriesWithDirector(d)));
        entry.getTitles().forEach(t -> matches.add(getEntriesWithTitle(t)));
        return matches.stream().flatMap(Function.identity());
    }
}
