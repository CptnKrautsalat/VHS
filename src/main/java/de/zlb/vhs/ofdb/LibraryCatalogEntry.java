package de.zlb.vhs.ofdb;

import de.zlb.vhs.ofdb.csv.LibraryCatalogEntryBean;

import java.util.*;
import java.util.stream.Collectors;

public class LibraryCatalogEntry {
    LibraryCatalogEntryBean bean;

    final Set<String> titles = new HashSet<>();
    final Set <String> directors = new HashSet<>();
    String year;

    public LibraryCatalogEntry(LibraryCatalogEntryBean bean) {
        this.bean = bean;
        this.titles.add(extractMainTitle(bean.title));
        this.titles.addAll(extractAlternativeTitles(bean.alternativeTitles));
        this.directors.addAll(extractDirectors(bean.director, bean.castAndCrew));
        this.year = extractYear(bean.comments);
    }

    public LibraryCatalogEntry() {}

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

    List<String> extractAlternativeTitles(String titles) {
        return Arrays.stream(titles.split("\\|"))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    String extractYear(String miscData) {
        String[] sections = miscData.split("Orig\\.:");
        if (sections.length > 1) {
            String[] subSections = sections[sections.length - 1].split(",");
            if (subSections.length > 1) {
                return subSections[subSections.length - 1].trim();
            }
        }
        return "";
    }

    Set<String> extractDirectors (String director, String castAndCrew) {
        Set<String> result = new HashSet<>();

        if (!director.isEmpty()) {
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
                    if (sections[1].contains("Regie")) {
                        result.add(sections[0].trim());
                    }
                }
            }
        }

        return result;
    }
}
