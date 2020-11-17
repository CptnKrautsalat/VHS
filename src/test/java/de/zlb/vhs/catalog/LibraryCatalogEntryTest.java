package de.zlb.vhs.catalog;

import de.zlb.vhs.ofdb.csv.LibraryCatalogEntryBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class LibraryCatalogEntryTest {

    private final LibraryCatalogEntry subject = new LibraryCatalogEntry();

    @Test
    public void testExtractMainTitle1() {
        String title = subject.extractMainTitle("¬Der¬ mit dem Wolf tanzt : [Video]");
        Assertions.assertEquals("mit dem Wolf tanzt, Der", title);
    }

    @Test
    public void testExtractMainTitle2() {
        String title = subject.extractMainTitle("Flashdance : What a feeling ; [Video]");
        Assertions.assertEquals("Flashdance : What a feeling", title);
    }

    @Test
    public void testExtractMainTitle3() {
        String title = subject.extractMainTitle("¬Die¬ unglaubliche Maschine Mensch");
        Assertions.assertEquals("unglaubliche Maschine Mensch, Die", title);
    }

    @Test
    public void testExtractAlternativeTitles() {
        List<String> titles = subject.extractAlternativeTitles("Not tonight, Josephine! | Some like it hot");
        List<String> expected = new LinkedList<>();
        expected.add("Not tonight, Josephine!");
        expected.add("Some like it hot");
        Assertions.assertEquals(titles, expected);
    }

    @Test
    public void testExtractYear() {
        String year = subject.extractYear("Deutsche Synchronfassung,dt.,Orig.: USA, 1983");
        Assertions.assertEquals(year, "1983");
    }

    @Test
    public void testExtractDirectors1() {
        String director = "Powell, Michael (118741675)";
        String castAndCrew = "Emeric Pressburger [Regie, Drehbuch, Darst., Prod.]";
        Set<String> expected = new HashSet<>();
        expected.add("Michael Powell");
        expected.add("Emeric Pressburger");
        Assertions.assertEquals(expected, subject.extractDirectors(director, castAndCrew));
    }

    @Test
    public void testExtractDirectors2() {
        String director = "NN (OHNE_DNDNR)";
        String castAndCrew = "";
        Set<String> expected = new HashSet<>();
        Assertions.assertEquals(expected, subject.extractDirectors(director, castAndCrew));
    }
}
