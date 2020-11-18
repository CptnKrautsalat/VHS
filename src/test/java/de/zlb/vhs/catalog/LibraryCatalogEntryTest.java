package de.zlb.vhs.catalog;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
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
        Set<String> titles = subject.extractAlternativeTitles("Not tonight, Josephine! | Some like it hot");
        Set<String> expected = new HashSet<>();
        expected.add("Not tonight, Josephine!");
        expected.add("Some like it hot");
        Assertions.assertEquals(expected, titles);
    }

    @Test
    public void testExtractAlternativeTitles2() {
        Set<String> titles = subject.extractAlternativeTitles("The legend of Bagger Vance");
        Set<String> expected = new HashSet<>();
        expected.add("The legend of Bagger Vance");
        expected.add("legend of Bagger Vance, The");
        Assertions.assertEquals(expected, titles);
    }

    @Test
    public void testExtractYear1() {
        String year = subject.extractYear("Deutsche Synchronfassung,dt.,Orig.: USA, 1983");
        Assertions.assertEquals(year, "1983");
    }

    @Test
    public void testExtractYear2() {
        String year = subject.extractYear("Ländercode: 2 | Orig.: USA/Deutschland, 2000 | Verschiedene Trailer. - Filmdokumentationen. - Filmkommentare: Regisseur, Produzent. - Filmografien: Schauspieler, Regisseur, Produzent.");
        Assertions.assertEquals(year, "2000");
    }

    @Test
    public void testExtractYear3() {
        String year = subject.extractYear("Ländercode: 2 PAL | Orig.: USA, 2012. - Nach dem Roman von Mark O'Brien | Untertitel: dt., engl., franz., türk. u.a.");
        Assertions.assertEquals(year, "2012");
    }

    @Test
    public void testExtractYear4() {
        String year = subject.extractYear("Ländercode: 2 | Orig.: Deutschland, 1984 (8., 11., 12., 17., und 19. Dezember 1984)");
        Assertions.assertEquals(year, "1984");
    }

    @Test
    public void testExtractYear5() {
        String year = subject.extractYear("Bonus: Diamond Sutra (Orig.: VR China 2012, Regie: Bi Gan] | Orig.: VR China, 2015 | Untertitel: englisch");
        Assertions.assertEquals(year, "2015");
    }

    @Test
    public void testExtractYear6() {
        String year = subject.extractYear("Engl. | Ländercode: 2 + 4 | Orig.: Großbritannien/USA, 2. Januar 1983");
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

    @Test
    public void testExtractDirectors3() {
        String director = "He Ping (OHNE_DNDNR)";
        String castAndCrew = "[Regie führt He Ping. Die Hauptrollen spielen Zhang Fengyi, Wang Xue Qi und Yang Kueimei]";
        Set<String> expected = new HashSet<>();
        expected.add("He Ping");
        Assertions.assertEquals(expected, subject.extractDirectors(director, castAndCrew));
    }
}
