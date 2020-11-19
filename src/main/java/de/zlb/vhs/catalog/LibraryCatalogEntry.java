package de.zlb.vhs.catalog;

import de.zlb.vhs.ofdb.CombinedFilm;
import de.zlb.vhs.ofdb.csv.LibraryCatalogEntryBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LibraryCatalogEntry {

    private static final Logger log = LogManager.getLogger(LibraryCatalogEntry.class);

    public static final String EMPTY_DIRECTOR_PLACEHOLDER = "NN (OHNE_DNDNR)";
    public static final Pattern YEAR_PATTERN = Pattern.compile("\\d{4}");

    //this should cover English, German, Italian, Spanish and French
    private static final String[] LEADING_ARTICLES = { "The ", "A ", "An ", "Der ", "Die" , "Das", "Ein ", "Eine ",
            "Il ", "La ", "Lo ", "L'", "I ", "Le ", "Gli ", "Un ", "Una ", "Uno ", "El ", "Los ", "Las ", "Les ", "Une "};

    private static final String[] CAST_AND_CREW_POSITIONS = { "Schauspiel", "Komp", "Kamera", "Drehbuch", "Buch", "Prod",
            "Inter", "Musik", "musik", "Darst", "Vorl", "Sonst", "precher", "Mitarb", "Text", "Moderat", "Sänger", "Tänzer",
            "Choreo", "Name", "Star", "Komment", "Gesang", "Hrsg", "Red", "Projekt", "Mit"};

    private static final String[] DIRECTOR_PHRASES = { "Film von ", "film by ", "film di "};

    public static final String VHS_FORMAT_NAME = "ad";

    public LibraryCatalogEntryBean bean;
    private CombinedFilm film;

    public final Set<String> titles = new HashSet<>();
    public final Set <String> directors = new HashSet<>();
    public String year;
    public String physicalFormat;

    public LibraryCatalogEntry(LibraryCatalogEntryBean bean) {
        this.bean = bean;
        this.titles.add(extractMainTitle(bean.title));
        this.titles.addAll(extractAlternativeTitles(bean.alternativeTitles));
        this.directors.addAll(extractDirectors(bean.director, bean.castAndCrew));
        this.year = extractYear(bean.comments);
        this.physicalFormat = bean.physicalForm;
    }

    LibraryCatalogEntry() {}

    public String getMediaNumber() {
        return bean.mediaNumber;
    }

    public boolean tryToSetFilm(CombinedFilm film) {
        if (this.film != null) {
            this.film.merge(film);
            return false;
        }
        this.film = film;
        return true;
    }

    public CombinedFilm getFilm() {
        return film;
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

    public boolean isLinkedToFilm() {
        return film != null;
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

    public boolean isVhs() {
        return physicalFormat.equals(VHS_FORMAT_NAME);
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
                .filter(s -> s.contains("Orig"))
                .collect(Collectors.toList());
        Optional<String> split = splits.size() > 1
                ? splits.stream().filter(s -> s.trim().startsWith("Orig")).findFirst()
                : splits.stream().findFirst();
        if (split.isPresent()) {
            String[] sections = split.get().split("Orig");
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

        result.addAll(extractDirectorsFromCastAndCrew(castAndCrew));

        return result;
    }

    private Set<String> extractDirectorsFromCastAndCrew(String castAndCrew) {
        Set<String> result = new HashSet<>();
        if (!castAndCrew.isEmpty()) {
            boolean descriptionContainsDirectorTitle = containsDirector(castAndCrew);
            boolean descriptionContainsDirectorPhrase = containsDirectorPhrase(castAndCrew);
            String[] people = castAndCrew.split(";");
            for (String p : people) {
                if (containsDirector(p)) {
                    String clean = p.startsWith("[")
                            ? p.replaceFirst("\\[", "")
                            : p;
                    String[] sections = clean.split("[\\[:]");
                    if (sections.length > 1) {
                        String possibleDirector = containsDirector(sections[1])
                                ? sections[0].trim()
                                : sections[1].trim();
                        if (!possibleDirector.isEmpty()) {
                            result.add(possibleDirector);
                        }
                    } else {
                        String directorWithoutPosition = Arrays.stream(p.split(" "))
                                .filter(s -> !containsDirector(s))
                                .collect(Collectors.joining(" "));
                        if (!directorWithoutPosition.contains("]")) {
                            result.add(directorWithoutPosition);
                        }
                    }
                } else {
                    result.addAll(extractDirectorsFromPoorlyFormattedDescription(p, descriptionContainsDirectorTitle,
                            descriptionContainsDirectorPhrase));
                }
            }
        }
        return result;
    }

    private Set<String> extractDirectorsFromPoorlyFormattedDescription(String description,
                                                                       boolean containsDirectorTitle,
                                                                       boolean containDirectorPhrase) {
        Set<String> result = new HashSet<>();
        Arrays.stream(description.split("\\.{3}"))
                .forEach(section -> {
                    if (containDirectorPhrase && containsDirectorPhrase(section)) {
                        Optional<Integer> index = Arrays.stream(DIRECTOR_PHRASES)
                                .filter(section::contains)
                                .map(p -> section.indexOf(p) + p.length())
                                .findFirst();
                        if (index.isPresent()) {
                            String director = section.substring(index.get()).trim();
                            if (!director.isEmpty()) {
                                result.add(extractDirectorName(director));
                            }
                        }
                    } else if ((containsDirectorTitle && containsDirector(section))
                            || (!containsDirectorTitle && !section.matches("[\\[:]") && !containsAnyPosition(section))) {
                        if (!section.isEmpty()) {
                            result.add(section.trim());
                        }
                    }
                });

        return result;
    }

    private String extractDirectorName(String director) {
        if (!(director.contains(".") || director.contains(","))) {
            return director;
        }
        int index1 = director.indexOf('.');
        int index2 = director.indexOf(',');
        int index = Math.min(index1, index2);
        if (index == -1) {
            index = Math.max(index1, index2);
        }
        return director.substring(0, index);
    }

    private boolean containsDirector(String subject) {
        return subject.contains("Regie") || subject.contains("Regisseur") || subject.contains("Filmregisseur");
    }

    private boolean containsDirectorPhrase(String subject) {
        return Arrays.stream(DIRECTOR_PHRASES).anyMatch(subject::contains);
    }

    private boolean containsAnyPosition(String subject) {
        return Arrays.stream(CAST_AND_CREW_POSITIONS).anyMatch(subject::contains);
    }
}
