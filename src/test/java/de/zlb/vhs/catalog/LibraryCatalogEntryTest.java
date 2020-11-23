package de.zlb.vhs.catalog;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

public class LibraryCatalogEntryTest {

    private final LibraryCatalogEntry subject = new LibraryCatalogEntry();

    @Test
    public void testExtractMainTitle1() {
        Set<String> titles = subject.extractMainTitle("¬Der¬ mit dem Wolf tanzt : [Video]");
        Set<String> expected = new HashSet<>();
        expected.add("mit dem Wolf tanzt, Der");
        expected.add("Der mit dem Wolf tanzt");
        Assertions.assertEquals(expected, titles);
    }

    @Test
    public void testExtractMainTitle2() {
        Set<String> titles = subject.extractMainTitle("Flashdance : What a feeling ; [Video]");
        Set<String> expected = new HashSet<>();
        expected.add("Flashdance : What a feeling");
        Assertions.assertEquals(expected, titles);
    }

    @Test
    public void testExtractMainTitle3() {
        Set<String> titles =  subject.extractMainTitle("¬Die¬ unglaubliche Maschine Mensch");
        Set<String> expected = new HashSet<>();
        expected.add("unglaubliche Maschine Mensch, Die");
        expected.add("Die unglaubliche Maschine Mensch");
        Assertions.assertEquals(expected, titles);
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

    @Test
    public void testExtractDirectors4() {
        String director = "NN (OHNE_DNDNR)";
        String castAndCrew = "Regie: Stephen Hopkins ; Drehbuch: William Goldman ; Kamera: Vilmos Zsigmond ; Musik: John Goldsmith ; Darsteller: Val Kilmer, Michael Douglas, Bernard Hill";
        Set<String> expected = new HashSet<>();
        expected.add("Stephen Hopkins");
        Assertions.assertEquals(expected, subject.extractDirectors(director, castAndCrew));
    }

    @Test
    public void testExtractDirectors5() {
        String director = "NN (OHNE_DNDNR)";
        String castAndCrew = "Filmregisseur und Drehbuchautor: Gus Van Sant ; Kamera: Christopher Blauvelt ; Komponist: Danny Elfman ; Schauspieler: Joaquin Phoenix, Jonah Hill, Rooney Mara [und andere]";
        Set<String> expected = new HashSet<>();
        expected.add("Gus Van Sant");
        Assertions.assertEquals(expected, subject.extractDirectors(director, castAndCrew));
    }

    @Test
    public void testExtractDirectors6() {
        String director = "NN (OHNE_DNDNR)";
        String castAndCrew = "Joseph H. Lewis ; Philip Yordan Drehbuchautor/in ; John Alton Kamera ; Cornel Wilde Schauspieler/in ; Richard Conte Schauspieler/in ; Brian Donlevy Schauspieler/in ; David Raksin Komponist/in";
        Set<String> expected = new HashSet<>();
        expected.add("Joseph H. Lewis");
        Assertions.assertEquals(expected, subject.extractDirectors(director, castAndCrew));
    }

    @Test
    public void testExtractDirectors7() {
        String director = "NN (OHNE_DNDNR)";
        String castAndCrew = "Tom Hooper Regie; Ellis Kirk Drehbuchautor/in ; Tak Fujimoto Kamera ; Paul Giamatti Schauspieler/in ; Laura Linney Schauspieler/in ; John Dossett Schauspieler/in ; David G. McCullough ; Robert Lane Komponist/in";
        Set<String> expected = new HashSet<>();
        expected.add("Tom Hooper");
        Assertions.assertEquals(expected, subject.extractDirectors(director, castAndCrew));
    }

    @Test
    public void testExtractDirectors8() {
        String director = "NN (OHNE_DNDNR)";
        String castAndCrew = "[Regie: Howard E. Baker ; Peter Chung. Produzent: Catherine Winder. Drehbuch: Mark Mars ... Musik: Drew Neuman]";
        Set<String> expected = new HashSet<>();
        expected.add("Howard E. Baker");
        Assertions.assertEquals(expected, subject.extractDirectors(director, castAndCrew));
    }

    @Test
    public void testExtractDirectors9() {
        String director = "NN (OHNE_DNDNR)";
        String castAndCrew = "Gerhard Hahn Regisseur ; Roberto De Nigris Kamera ; Rosabell Laurenti Sellers Schauspieler/in ; Josephine Benini ... Schauspieler/in ; Gerd Kaeding ... Komponist/in";
        Set<String> expected = new HashSet<>();
        expected.add("Gerhard Hahn");
        Assertions.assertEquals(expected, subject.extractDirectors(director, castAndCrew));
    }

    @Test
    public void testExtractDirectors10() {
        String director = "NN (OHNE_DNDNR)";
        String castAndCrew = "ein Film von Wolfgang Becker. Mit Jürgen Vogel, Christiane Paul und Ricky Tomlinson ... Buch Wolfgang Becker & Tom Tykwer. Filmmusik Jürgen Knieper. Szenenmusik Christian Steyer";
        Set<String> expected = new HashSet<>();
        expected.add("Wolfgang Becker");
        Assertions.assertEquals(expected, subject.extractDirectors(director, castAndCrew));
    }

    @Test
    public void testExtractDirectors11() {
        String director = "NN (OHNE_DNDNR)";
        String castAndCrew = "Regie: Fritz Lang. Buch: Fritz Lang und Thea von Harbou. Darsteller: Peter Lorre ; Gustaf Gründgens ; Theo Lingen...";
        Set<String> expected = new HashSet<>();
        expected.add("Fritz Lang");
        Assertions.assertEquals(expected, subject.extractDirectors(director, castAndCrew));
    }
}
