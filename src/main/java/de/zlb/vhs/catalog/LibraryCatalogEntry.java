package de.zlb.vhs.catalog;

import de.zlb.vhs.CombinedFilm;
import de.zlb.vhs.ComparableFilmEntry;
import de.zlb.vhs.ISortableEntry;
import de.zlb.vhs.TitleUtil;
import de.zlb.vhs.csv.LibraryCatalogEntryBean;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LibraryCatalogEntry extends ComparableFilmEntry implements ISortableEntry {

    public static final String EMPTY_DIRECTOR_PLACEHOLDER = "NN (OHNE_DNDNR)";
    public static final Pattern YEAR_PATTERN = Pattern.compile("\\d{4}");
    public static final Pattern PERIOD_WITHOUT_INITIALS_PATTERS = Pattern.compile("\\w{2,}\\.");

    private static final String[] CAST_AND_CREW_POSITIONS = { "Schauspiel", "Komp", "Kamera", "Drehbuch", "Buch", "Prod",
            "Inter", "Musi", "musi", "Darst", "Vorl", "Sonst", "precher", "Mitarb", "Text", "Moderat", "Sänger", "Tänzer",
            "Choreo", "Name", "Star", "Komment", "Gesang", "Hrsg", "Red", "Projekt", "Mit"};

    private static final String[] DIRECTOR_PHRASES = { "Film von ", "film by ", "film di ", "Regie führt ", "directed by ", "Directed by "};
    private static final String[] DIRECTOR_POSITIONS = { "Regie", "Regisseur", "Name im Titel"};

    public static final String VHS_FORMAT_NAME = "ad";

    public LibraryCatalogEntryBean bean;

    private CombinedFilm film;

    private String mainTitle;
    private final Set<String> alternativeTitles = new HashSet<>();
    private final Set<String> generatedTitles = new HashSet<>();

    public final Set<String> titles = new HashSet<>();
    public final Set<String> directors = new HashSet<>();
    public final Set<String> genres = new HashSet<>();
    public final Set<String> languages = new HashSet<>();

    public String year;
    public String physicalFormat;
    public String signaturePrefix;

    public int rentalsSince2010 = 0;

    private AcquisitionMethod acquisitionMethod;

    public LibraryCatalogEntry(LibraryCatalogEntryBean bean) {
        this.bean = bean;
        this.generatedTitles.addAll(extractMainTitle(bean.getTitle()));
        this.alternativeTitles.addAll(extractAlternativeTitles(bean.getAlternativeTitles()));
        this.titles.addAll(this.generatedTitles);
        this.titles.addAll(this.alternativeTitles);
        this.directors.addAll(extractDirectors(bean.getDirector(), bean.getCastAndCrew()));
        this.genres.addAll(extractGenres(bean.getGenres()));
        this.languages.addAll(extractLanguages(bean.getLanguages()));
        this.year = extractYear(bean.getComments());
        this.signaturePrefix = extractSignaturePrefix(bean.getSignature());
        this.physicalFormat = bean.getPhysicalForm();
        this.rentalsSince2010 = Integer.parseInt(bean.getRentals2010to2020());
        this.acquisitionMethod = AcquisitionMethod.fromString(bean.getAcquisition());
    }

    LibraryCatalogEntry() {}

    public String getMediaNumber() {
        return bean.getMediaNumber();
    }

    public CombinedFilm getFilm() {
        return film;
    }

    public int getRentalsSince2010() {
        return rentalsSince2010;
    }

    public void setFilm(CombinedFilm film) {
        this.film = film;
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

    @Override
    public String getMainTitle() {
        return mainTitle;
    }

    @Override
    public Set<String> getAlternativeTitles() {
        return alternativeTitles;
    }

    @Override
    public Set<String> getGeneratedTitles() {
        return generatedTitles;
    }

    public boolean hasYear() {
        return !year.isEmpty();
    }

    public boolean hasWrongYear() {
        if (!hasYear()) {
            return false;
        }
        int yearNumber = Integer.parseInt(year);
        return yearNumber < 1870 || yearNumber > 2020;
    }

    public boolean hasDirector() {
        return !directors.isEmpty();
    }

    public boolean isLinkedToOfdbFilm() {
        return film != null && film.hasOfdbEntry();
    }

    public boolean isMandatory() {
        return acquisitionMethod == AcquisitionMethod.MANDATORY;
    }

    public boolean isVhs() {
        return physicalFormat.equals(VHS_FORMAT_NAME);
    }

    public boolean isTvShow() {
        return signaturePrefix.startsWith("Film 7 ");
    }

    public boolean isFeatureFilm() {
        return signaturePrefix.startsWith("Film 10 ");
    }

    public void updateBean() {
        bean.update(this);
    }

    static Set<String> extractLanguages(String languages) {
        return Arrays.stream(languages.split(","))
                .collect(Collectors.toSet());
    }

    String extractSignaturePrefix(String signature) {
        String noMediaFormat = signature.split(":")[0];
        String[] sections = noMediaFormat.split(" ");
        if (sections[sections.length - 1].matches("[a-z]")) {
            return noMediaFormat.substring(0, noMediaFormat.length() - 2);
        }
        return noMediaFormat;
    }

    Set<String> extractGenres(String genres) {
        return Arrays.stream(genres.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    Set<String> extractMainTitle(String title) {
        Set<String> result = new HashSet<>();
        String tempTitle = title;

        //get rid of format suffix
        String[] sections = title.split("[:;]");
        if (sections.length != 1 && sections[sections.length - 1].contains("[")) {
            int lastSemicolon = title.lastIndexOf(';');
            int lastColon = title.lastIndexOf(':');
            tempTitle = title
                    .substring(0, Math.max(lastColon, lastSemicolon))
                    .trim();
        }

        //move article to the end
        if (tempTitle.contains("¬")) {
            sections = tempTitle.split("¬");
            if (sections.length == 3) {
                tempTitle = sections[2].trim();
                result.add(sections[2].trim() + ", " + sections[1].trim());
                result.add(sections[1].trim() + " " + sections[2].trim());
            }
        }

        result.add(tempTitle);

        //unify separators
        mainTitle = tempTitle.replaceAll(" ?[:\\-] ?", " ");
        result.add(mainTitle);

        Optional<String> sequelTitle = TitleUtil.getNumberedSequelTitle(mainTitle);
        sequelTitle.ifPresent(result::add);

        //unify spelling
        result.add(tempTitle.replaceAll("ß", "ss"));

        //split weird long titles {
        sections = tempTitle.split(" [:\\-;] ");
        if (sections.length > 1) {
            Arrays.stream(sections)
                    .map(String::trim)
                    .flatMap(t -> TitleUtil.moveLeadingArticles(t).stream())
                    .forEach(result::add);
        }

        result.removeIf(String::isBlank);
        return result;
    }

    static Set<String> extractAlternativeTitles(String titles) {
        return titles.isEmpty() ? Collections.emptySet() : Arrays.stream(titles.split("\\|"))
                .map(String::trim)
                .flatMap(t -> TitleUtil.moveLeadingArticles(t).stream())
                .filter(t -> !t.isBlank())
                .map(t -> t.replaceAll(" ?[:\\-] ?", " "))
                .collect(Collectors.toSet());
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

        if (year.isEmpty()) {
            Matcher matcher = YEAR_PATTERN.matcher(miscData);
            if (matcher.find()) {
                return matcher.group();
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

        result.removeIf(String::isBlank);

        return result;
    }

    private Set<String> extractDirectorsFromCastAndCrew(String castAndCrew) {
        Set<String> result = new HashSet<>();
        if (!castAndCrew.isEmpty()) {
            boolean descriptionContainsDirectorTitle = containsDirector(castAndCrew);
            boolean descriptionContainsDirectorPhrase = containsDirectorPhrase(castAndCrew);
            String[] people = castAndCrew.split(";");
            result.addAll(extractDirectorsFromCrewArray(descriptionContainsDirectorTitle, descriptionContainsDirectorPhrase, people));
        }
        return result;
    }

    private Set<String> extractDirectorsFromCrewArray(boolean descriptionContainsDirectorTitle,
                                                      boolean descriptionContainsDirectorPhrase, String[] people) {
        Set<String> result = new HashSet<>();
        for (String p : people) {
            if (containsPeriodThatIsNotAnInitial(p)) {
                result.addAll(extractDirectorsFromCrewArray(descriptionContainsDirectorTitle,
                        descriptionContainsDirectorPhrase, p.split("\\.")));
            } else if (containsDirector(p) && !containsDirectorPhrase(p)) {
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
        return result;
    }

    private boolean containsPeriodThatIsNotAnInitial(String subject) {
        if (!subject.contains(".")) {
            return false;
        }
        return PERIOD_WITHOUT_INITIALS_PATTERS.matcher(subject).find();
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
                            || (!containsDirectorTitle && !containDirectorPhrase && !section.matches("[\\[:]") && !containsAnyPosition(section))) {
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
        return Arrays.stream(DIRECTOR_POSITIONS).anyMatch(p -> containsIgnoreCase(subject, p));
    }

    private boolean containsDirectorPhrase(String subject) {
        return Arrays.stream(DIRECTOR_PHRASES).anyMatch(p -> containsIgnoreCase(subject, p));
    }

    private boolean containsAnyPosition(String subject) {
        return Arrays.stream(CAST_AND_CREW_POSITIONS).anyMatch(p -> containsIgnoreCase(subject, p));
    }

    private static boolean containsIgnoreCase(String a, String b) {
        return a.toLowerCase().contains(b.toLowerCase());
    }

    @Override
    public String getYear() {
        return year;
    }

    @Override
    public Set<String> getDirectors() {
        return directors;
    }
}
