package de.zlb.vhs.catalog;

import de.zlb.vhs.ofdb.csv.LibraryCatalogEntryBean;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LibraryCatalogEntry {

    public static final String EMPTY_DIRECTOR_PLACEHOLDER = "NN (OHNE_DNDNR)";
    public static final Pattern YEAR_PATTERN = Pattern.compile("\\d{4}");
    //this should cover English, German, Italian, Spanish and French
    private static final String[] LEADING_ARTICLES = { "The ", "A ", "An ", "Der ", "Die" , "Das", "Ein ", "Eine ",
            "Il ", "La ", "Lo ", "L'", "I ", "Le ", "Gli ", "Un ", "Una ", "Uno ", "El ", "Los ", "Las ", "Les ", "Une "};

    public LibraryCatalogEntryBean bean;

    public final Set<String> titles = new HashSet<>();
    public final Set <String> directors = new HashSet<>();
    public String year;

    public LibraryCatalogEntry(LibraryCatalogEntryBean bean) {
        this.bean = bean;
        this.titles.add(extractMainTitle(bean.title));
        this.titles.addAll(extractAlternativeTitles(bean.alternativeTitles));
        this.directors.addAll(extractDirectors(bean.director, bean.castAndCrew));
        this.year = extractYear(bean.comments);
    }

    LibraryCatalogEntry() {}

    public String getMediaNumber() {
        return bean.mediaNumber;
    }

    @Override
    public String toString() {
        return "LibraryCatalogEntry{" +
                "titles=" + titles +
                ", directors=" + directors +
                ", year='" + year + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LibraryCatalogEntry)) return false;
        LibraryCatalogEntry that = (LibraryCatalogEntry) o;
        return bean.equals(that.bean) &&
                titles.equals(that.titles) &&
                directors.equals(that.directors) &&
                year.equals(that.year);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bean, titles, directors, year);
    }

    public boolean matchesTitle(String title) {
        return titles
                .stream()
                .anyMatch(title::equalsIgnoreCase);
    }

    public boolean matchesDirector(String director) {
        return directors
                .stream()
                .anyMatch(director::equalsIgnoreCase);
    }

    public boolean matchesYear(String year) {
        return year.equals(this.year);
    }

    public boolean hasYear() {
        return !year.isEmpty();
    }

    public boolean hasDirector() {
        return !directors.isEmpty();
    }

    public boolean matchesTitlesAndDirectors (LibraryCatalogEntry other) {
        boolean matchesTitles = other.titles
                .stream()
                .anyMatch(this::matchesTitle);
        if (!matchesTitles) {
            return false;
        }

        return other.directors
                .stream()
                .anyMatch(this::matchesDirector);
    }

    String extractMainTitle(String title) {
        String result = title;

        //get rid of format suffix
        String[] sections = title.split("[:;]");
        if (sections.length != 1 && sections[sections.length - 1].contains("[")) {
            int lastSemicolon = title.lastIndexOf(';');
            int lastColon = title.lastIndexOf(':');
            result = title
                    .substring(0, Math.max(lastColon, lastSemicolon))
                    .trim();
        }

        //move article to the end
        if (result.contains("¬")) {
            sections = result.split("¬");
            if (sections.length == 3) {
                result = sections[2].trim() + ", " + sections[1].trim();
            }
        }

        return result;
    }

    Set<String> extractAlternativeTitles(String titles) {
        return titles.isEmpty() ? Collections.emptySet() : Arrays.stream(titles.split("\\|"))
                .map(String::trim)
                .flatMap(t -> Stream.of(t, moveLeadingArticles(t)))
                .collect(Collectors.toSet());
    }

    private String moveLeadingArticles(String title) {
        Optional<String> article = Arrays.stream(LEADING_ARTICLES)
                .filter(title::startsWith)
                .findFirst();

        return article
                .map(s -> title.replaceFirst(s, "") + ", " + s.trim())
                .orElse(title);

    }

    String extractYear(String miscData) {
        String year = "";
        List<String> splits = Arrays.stream(miscData.split("\\|"))
                .filter(s -> s.contains("Orig.:"))
                .collect(Collectors.toList());
        Optional<String> split = splits.size() > 1
                ? splits.stream().filter(s -> s.trim().startsWith("Orig.:")).findFirst()
                : splits.stream().findFirst();
        if (split.isPresent()) {
            String[] sections = split.get().split("Orig\\.:");
            if (sections.length > 1) {
                String[] subSections = sections[sections.length - 1].split(",");
                Optional<String> possibleYear = Arrays.stream(subSections)
                        .map(String::trim)
                        .filter(YEAR_PATTERN.asPredicate())
                        .findFirst();
                if (possibleYear.isPresent()) {
                    year = possibleYear.get();
                }
            }
        }

        if (year.length() > 4) {
            Optional<String> possibleYear = Arrays.stream(year.split("\\D"))
                    .filter(YEAR_PATTERN.asPredicate())
                    .findFirst();
            if (possibleYear.isPresent()) {
                return possibleYear.get();
            }
        }

        return year;
    }

    Set<String> extractDirectors (String director, String castAndCrew) {
        Set<String> result = new HashSet<>();

        if (!(director.isEmpty() || director.equals(EMPTY_DIRECTOR_PLACEHOLDER))) {
            String directorWithoutId = director.split("\\(")[0].trim();
            if (directorWithoutId.contains(",")) {
                String[] sections = directorWithoutId.split(",");
                result.add(sections[1].trim() + " " + sections[0].trim());
            } else {
                result.add(directorWithoutId);
            }
        }

        if (!castAndCrew.isEmpty()) {
            String[] people = castAndCrew.split(";");
            for (String p : people) {
                if (p.contains("[")) {
                    String[] sections = p.split("\\[");
                    if (sections.length > 1 && sections[1].contains("Regie")) {
                        result.add(sections[0].trim());
                    }
                }
            }
        }

        return result;
    }
}
