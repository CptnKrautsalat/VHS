package de.zlb.vhs;

import java.util.Set;

public abstract class ComparableFilmEntry {

    public abstract String getMainTitle();
    public abstract Set<String> getAlternativeTitles();
    public abstract Set<String> getGeneratedTitles();

    public boolean matchesTitles(ComparableFilmEntry libraryCatalogEntry, boolean includeAltTiles, boolean includeGeneratedTitles) {

        if (libraryCatalogEntry.matchesTitle(getMainTitle(), includeAltTiles, includeGeneratedTitles)) {
            return true;
        }

        if (includeAltTiles && getAlternativeTitles().stream()
                .anyMatch(t -> libraryCatalogEntry.matchesTitle(t, includeAltTiles, includeGeneratedTitles))) {
            return true;
        }

        return includeGeneratedTitles && getGeneratedTitles().stream()
                .anyMatch(t -> libraryCatalogEntry.matchesTitle(t, includeAltTiles, includeGeneratedTitles));
    }

    public boolean matchesTitle(String title, boolean includeAltTitles, boolean includeGeneratedTitles) {
        if (titlesMatch(getMainTitle(), title)) {
            return true;
        }
        if (includeAltTitles && getAlternativeTitles().stream().anyMatch(t -> titlesMatch(t, title))) {
            return true;
        }
        return includeGeneratedTitles && getGeneratedTitles().stream().anyMatch(t -> titlesMatch(t, title));
    }

    private boolean titlesMatch(String title1, String title2) {
        return title1.equalsIgnoreCase(title2);
    }
}
