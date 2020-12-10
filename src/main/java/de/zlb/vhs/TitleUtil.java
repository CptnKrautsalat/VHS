package de.zlb.vhs;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class TitleUtil {
    //this should cover English, German, Italian, Spanish and French
    public static final String[] LEADING_ARTICLES = { "The ", "A ", "An ", "Der ", "Die" , "Das", "Ein ", "Eine ",
            "Il ", "La ", "Lo ", "L'", "I ", "Le ", "Gli ", "Un ", "Una ", "Uno ", "El ", "Los ", "Las ", "Les ", "Une "};

    public static Set<String> moveLeadingArticles(String title) {
        Set<String> result = new HashSet<>();
        result.add(title.trim());
        Optional<String> article = getLeadingArticle(title);

        if (article.isPresent()) {
            String titleWithoutArticle = title.replaceFirst(article.get(), "").trim();
            result.add(titleWithoutArticle);
            result.add(titleWithoutArticle + ", " + article.get().trim());
        }

        return result;

    }

    public static Optional<String> getLeadingArticle(String title) {
        return Arrays.stream(LEADING_ARTICLES)
                .filter(title::startsWith)
                .findFirst();
    }

    public static Optional<String> getTrailingArticle(String title) {
        String[] sections = title.split(", ");
        if (sections.length == 1) {
            return Optional.empty();
        }
        String lastSection = sections[sections.length - 1];
        return Arrays.stream(LEADING_ARTICLES)
                .map(String::trim)
                .filter(lastSection::equals)
                .findFirst();
    }

    public static Optional<String> getNumberedSequelTitle(String title) {
        String[] sections = title.split("\\d+");
        if (sections[0].equals(title)) {
            return Optional.empty();
        }
        return Optional.of(sections[0].trim());
    }
}
