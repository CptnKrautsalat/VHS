package de.zlb.vhs.ofdb;

import java.util.HashSet;
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
                "ofdbTitle=" + ofdbEntry.title +
                ", libraryCatalogEntries=" + libraryCatalogEntries +
                '}';
    }
}
