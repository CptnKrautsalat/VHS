package de.zlb.vhs;

import de.zlb.vhs.catalog.LibraryCatalogEntry;

import java.util.HashSet;
import java.util.Set;

public abstract class ComparableFilmEntry implements ISortableEntry {

    public abstract String getMainTitle();
    public abstract Set<String> getAlternativeTitles();
    public abstract Set<String> getGeneratedTitles();
    public abstract CombinedFilm getFilm();

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

    public boolean matchesDirector(String director) {
        return getDirectors().isEmpty()
                || getDirectors()
                .stream()
                .anyMatch(director::equalsIgnoreCase);
    }

    public boolean matchesTitlesAndDirectors (ComparableFilmEntry other) {
        //skip all the slow stuff if it has been tested before
        if (getFilm() != null && other.getFilm() != null && getFilm() == other.getFilm()) {
            return true;
        }

        boolean matchesTitles = matchesTitles(other, true, true);
        if (!matchesTitles) {
            return false;
        }

        return getDirectors().isEmpty() || other.getDirectors().isEmpty()
                || other.getDirectors()
                .stream()
                .anyMatch(this::matchesDirector);
    }

    public boolean matchesYear(String year, boolean strict) {
        if (year.equals(getYear())) {
            return true;
        }

        if (strict) {
            return false;
        }

        try {
            int year1 = Integer.parseInt(year);
            int year2 = Integer.parseInt(getYear());
            return Math.abs(year1 - year2) <= 1;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public Set<String> getTitles() {
        Set<String> titles = new HashSet<>();
        titles.add(getMainTitle());
        titles.addAll(getAlternativeTitles());
        titles.addAll(getGeneratedTitles());
        return titles;
    }

    private boolean titlesMatch(String title1, String title2) {
        return title1.equalsIgnoreCase(title2);
    }
}
