package de.zlb.vhs;

import de.zlb.vhs.ofdb.FilmEntry;

import java.util.*;
import java.util.stream.Collectors;

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

    public boolean matches(ComparableFilmEntry other) {
        //skip all the slow stuff if it has been tested before
        if (getFilm() != null && other.getFilm() != null && getFilm() == other.getFilm()) {
            return true;
        }

        if (!matchesYear(other.getYear(), false)) {
            return false;
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

        if (year.isEmpty() || getYear().isEmpty()) {
            return true;
        }

        try {
            int year1 = Integer.parseInt(year);
            int year2 = Integer.parseInt(getYear());
            return Math.abs(year1 - year2) <= 1;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public int matchScore(ComparableFilmEntry other) {
        int score = 0;
        score += matchesYear(other.getYear(), false) ? 2 : -4;
        score += matchesYear(other.getYear(), true) ? 3 : 0;

        for (String director : other.getDirectors()) {
            score += matchesDirector(director) ? 3 : -3;
        }

        score += titlesMatch(getMainTitle(), getMainTitle()) ? 1 : 0;
        for (String title1 : other.getTitles()) {
            score += titlesMatch(title1, getMainTitle()) ? 3 : 0;
            for (String title2 : getAlternativeTitles()) {
                score += titlesMatch(title1, title2) ? 2 : 0;
            }
            for (String title2 : getGeneratedTitles()) {
                score += titlesMatch(title1, title2) ? 1 : 0;
            }
        }

        return score;
    }

    public Set<FilmEntry> findBestMatches(Set<FilmEntry> matches) {
        if (matches.size() <= 1) {
            return matches;
        }
        List<AbstractMap.SimpleImmutableEntry<Integer, FilmEntry>> sorted = matches
                .stream()
                .map(m -> new AbstractMap.SimpleImmutableEntry<>(matchScore(m) * -1, m))
                .sorted(Comparator.comparingInt(AbstractMap.SimpleImmutableEntry::getKey))
                .collect(Collectors.toList());
        int topScore = sorted.get(0).getKey();

        return sorted
                .stream()
                .filter(e -> e.getKey().equals(topScore))
                .map(AbstractMap.SimpleImmutableEntry::getValue)
                .collect(Collectors.toSet());
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
