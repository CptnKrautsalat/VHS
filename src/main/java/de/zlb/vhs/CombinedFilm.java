package de.zlb.vhs;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.zlb.vhs.catalog.LibraryCatalogEntry;
import de.zlb.vhs.ofdb.FilmEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class CombinedFilm {

    private static final Logger log = LogManager.getLogger(CombinedFilm.class);

    private FilmEntry ofdbEntry;
    private final Set<LibraryCatalogEntry> libraryCatalogEntries = new HashSet<>();

    public CombinedFilm(Optional<FilmEntry> ofdbEntry) {
        ofdbEntry.ifPresent(this::setOfdbEntry);
    }

    public void setOfdbEntry(FilmEntry ofdbEntry) {
        this.ofdbEntry = ofdbEntry;
        ofdbEntry.setFilm(this);
    }

    public CombinedFilm() {}

    public CombinedFilm addLibraryCatalogEntry(LibraryCatalogEntry entry) {
        libraryCatalogEntries.add(entry);
        CombinedFilm other = entry.getFilm();
        if (this == other) {
            return this;
        }
        if (other != null) {
            entry.setFilm(other);
            return merge(other);
        }
        entry.setFilm(this);
        return this;
    }

    public CombinedFilm merge(CombinedFilm other) {
        log.trace("Merging {} and {}.", other, this);

        if (!other.hasOfdbEntry()) {
            other.libraryCatalogEntries.forEach(e -> {
                libraryCatalogEntries.add(e);
                e.setFilm(this);
            });
            other.libraryCatalogEntries.clear();
            return this;
        }
        this.ofdbEntry = null;
        libraryCatalogEntries.forEach(e -> {
            other.libraryCatalogEntries.add(e);
            e.setFilm(other);
        });
        this.libraryCatalogEntries.clear();
        return other;
    }

    public boolean isEmpty() {
        return libraryCatalogEntries.isEmpty();
    }

    public FilmEntry getOfdbEntry() {
        return ofdbEntry;
    }

    public Stream<LibraryCatalogEntry> getLibraryCatalogEntries() {
        return libraryCatalogEntries.stream();
    }

    public boolean hasOfdbEntry () {
        return ofdbEntry != null;
    }

    public boolean isOnlyOnVhsInCatalog() {
        return libraryCatalogEntries
                .stream()
                .allMatch(LibraryCatalogEntry::isVhs);
    }

    public boolean hasUniqueLanguageOnVhs() {
        Multimap<String, LibraryCatalogEntry> sortedByLanguage = HashMultimap.create();
        libraryCatalogEntries.forEach(lce -> lce.languages.forEach(l -> sortedByLanguage.put(l, lce)));
        for (String language : sortedByLanguage.keySet()) {
            if (sortedByLanguage.get(language).stream().allMatch(LibraryCatalogEntry::isVhs)) {
                return true;
            }
        }
        return false;
    }

    public boolean existsDigitally() {
        return hasOfdbEntry() && ofdbEntry.hasDigitalRelease();
    }

    @Override
    public String toString() {
        return "CombinedFilm{" +
                "ofdbEntry=" + ofdbEntry +
                ", libraryCatalogEntries=" + libraryCatalogEntries +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CombinedFilm)) return false;
        CombinedFilm that = (CombinedFilm) o;
        return Objects.equals(ofdbEntry, that.ofdbEntry) &&
                libraryCatalogEntries.equals(that.libraryCatalogEntries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ofdbEntry, libraryCatalogEntries);
    }
}
