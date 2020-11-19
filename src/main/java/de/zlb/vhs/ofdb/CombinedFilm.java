package de.zlb.vhs.ofdb;

import de.zlb.vhs.catalog.LibraryCatalogEntry;
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

    public void addLibraryCatalogEntry(LibraryCatalogEntry entry) {
        if (this != entry.getFilm() && entry.tryToSetFilm(this)) {
            this.libraryCatalogEntries.add(entry);
        }
    }

    public void merge(CombinedFilm other) {
        log.trace("Merging {} into {}.", other, this);
        this.libraryCatalogEntries.addAll(other.libraryCatalogEntries);
        other.libraryCatalogEntries.clear();
        if (!this.hasOfdbEntry() && other.hasOfdbEntry()) {
            setOfdbEntry(other.ofdbEntry);
            other.ofdbEntry = null;
        }
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
