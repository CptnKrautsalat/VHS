package de.zlb.vhs.cdh;

import de.zlb.vhs.ICombinedFilm;
import de.zlb.vhs.ofdb.FilmEntry;
import lombok.AllArgsConstructor;

import java.util.stream.Stream;

@AllArgsConstructor
public class CdhCombinedFilm implements ICombinedFilm<CdhEntry> {

    private final FilmEntry ofdbEntry;
    private final CdhEntry catalogEntry;

    @Override
    public FilmEntry getOfdbEntry() {
        return ofdbEntry;
    }

    @Override
    public Stream<CdhEntry> getCatalogEntries() {
        return Stream.of(catalogEntry);
    }

    @Override
    public boolean isEmpty() {
        return catalogEntry == null;
    }

    @Override
    public boolean hasOfdbEntry() {
        return ofdbEntry != null;
    }
}
