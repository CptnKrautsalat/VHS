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
        return matchesYear(year, strict, 1);
    }

    public boolean matchesYear(String year, boolean strict, int maxDifference) {
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
            return Math.abs(year1 - year2) <= maxDifference;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public double matchScore(ComparableFilmEntry other) {
        double score = 0d;

        if (!getYear().isEmpty() && !other.getYear().isEmpty()) {
            score += matchesYear(other.getYear(), true) ? 1 : 0;
            score += matchesYear(other.getYear(), false) ? .75 : 0;
            score += matchesYear(other.getYear(), false, 2) ? .25 : 0;
        }

        if (!getDirectors().isEmpty() && !other.getDirectors().isEmpty()) {
            double directorScore = 0d;
            for (String director : other.getDirectors()) {
                directorScore += matchesDirector(director) ? 1 : 0;
            }
            score += directorScore / (getDirectors().size() * other.getDirectors().size());
        }

        score += titlesMatch(getMainTitle(), getMainTitle()) ? 1 : 0;

        double titleScore = 0d;
        for (String title1 : other.getTitles()) {
            titleScore += titlesMatch(title1, getMainTitle()) ? 1 : 0;
            for (String title2 : getAlternativeTitles()) {
                titleScore += titlesMatch(title1, title2) ? 1 : 0;
            }
            for (String title2 : getGeneratedTitles()) {
                titleScore += titlesMatch(title1, title2) ? 1 : 0;
            }
        }
        score += titleScore / (getTitles().size() * other.getTitles().size());

        return score;
    }

    public Set<FilmEntry> findBestMatches(Set<FilmEntry> matches) {
        if (matches.size() <= 1) {
            return matches;
        }
        List<AbstractMap.SimpleImmutableEntry<Double, FilmEntry>> sorted = matches
                .stream()
                .map(m -> new AbstractMap.SimpleImmutableEntry<>(matchScore(m) * -1, m))
                .sorted(Comparator.comparingDouble(AbstractMap.SimpleImmutableEntry::getKey))
                .collect(Collectors.toList());
        double topScore = sorted.get(0).getKey();

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
