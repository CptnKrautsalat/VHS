package de.zlb.vhs.ofdb;

import de.zlb.vhs.catalog.LibraryCatalogEntry;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class CombinedFilm {
    private FilmEntry ofdbEntry;
    private final Set<LibraryCatalogEntry> libraryCatalogEntries = new HashSet<>();

    public CombinedFilm(Optional<FilmEntry> ofdbEntry) {
        if (ofdbEntry.isPresent()) {
            this.ofdbEntry = ofdbEntry.get();
            ofdbEntry.get().film = this;
        }

    }

    public CombinedFilm() {}

    public void addLibraryCatalogEntry(LibraryCatalogEntry entry) {
        entry.film = this;
        this.libraryCatalogEntries.add(entry);
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
