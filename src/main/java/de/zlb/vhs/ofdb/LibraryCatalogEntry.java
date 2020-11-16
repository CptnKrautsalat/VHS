package de.zlb.vhs.ofdb;

import de.zlb.vhs.ofdb.csv.LibraryCatalogEntryBean;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class LibraryCatalogEntry {
    private LibraryCatalogEntryBean bean;

    private final List<String> titles = new LinkedList<>();
    private String year;
    private String director;

    public LibraryCatalogEntry(LibraryCatalogEntryBean bean) {
        this.bean = bean;
    }

    public LibraryCatalogEntry() {
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
}
