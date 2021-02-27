package de.zlb.vhs;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.zlb.vhs.catalog.LibraryCatalogEntry;
import de.zlb.vhs.ofdb.FilmEntry;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class CombinedFilm implements ICombinedFilm<LibraryCatalogEntry> {

    private static final Logger log = LogManager.getLogger(CombinedFilm.class);

    @Getter
    private FilmEntry ofdbEntry;
    private final Set<LibraryCatalogEntry> libraryCatalogEntries = new HashSet<>();

    public CombinedFilm(FilmEntry ofdbEntry) {
        if (ofdbEntry != null) {
            setOfdbEntry(ofdbEntry);
        }
    }

    public void setOfdbEntry(FilmEntry ofdbEntry) {
        this.ofdbEntry = ofdbEntry;
        ofdbEntry.setFilm(this);
    }

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

    @Override
    public boolean isEmpty() {
        return libraryCatalogEntries.isEmpty();
    }

    @Override
    public Stream<LibraryCatalogEntry> getCatalogEntries() {
        return libraryCatalogEntries.stream();
    }

    @Override
    public boolean hasOfdbEntry () {
        return ofdbEntry != null;
    }

    public boolean isOnlyOnVhsInCatalog() {
        return libraryCatalogEntries
                .stream()
                .allMatch(LibraryCatalogEntry::isVhs);
    }

    public boolean germanDubOnlyOnVhsInLibraryCatalog() {
        Multimap<String, LibraryCatalogEntry> sortedByLanguage = HashMultimap.create();
        libraryCatalogEntries.forEach(lce -> lce.languages.forEach(l -> sortedByLanguage.put(l, lce)));
        return sortedByLanguage.get("ger").stream().allMatch(LibraryCatalogEntry::isVhs);
    }

    public boolean existsDigitally() {
        return hasOfdbEntry() && ofdbEntry.hasDigitalRelease();
    }

    public boolean germanDubExistsDigitally() {
        return hasOfdbEntry() && ofdbEntry.hasDigitalReleaseWithGermanDub();
    }

}
