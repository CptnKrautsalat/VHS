package de.zlb.vhs.ofdb;

import de.zlb.vhs.catalog.LibraryCatalogEntry;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class CombinedFilm {
    private FilmEntry ofdbEntry;
    private final Set<LibraryCatalogEntry> libraryCatalogEntries = new HashSet<>();

    public CombinedFilm(FilmEntry ofdbEntry) {
        this.ofdbEntry = ofdbEntry;
    }

    public CombinedFilm() {}

    public void addLibraryCatalogEntry(LibraryCatalogEntry entry) {
        this.libraryCatalogEntries.add(entry);
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
