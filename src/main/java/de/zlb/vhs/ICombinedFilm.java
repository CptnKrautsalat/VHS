package de.zlb.vhs;

import de.zlb.vhs.ofdb.FilmEntry;

import java.util.stream.Stream;

public interface ICombinedFilm<E extends ComparableFilmEntry> {
    FilmEntry getOfdbEntry();
    Stream<E> getCatalogEntries();
    boolean isEmpty();
    boolean hasOfdbEntry();
}
