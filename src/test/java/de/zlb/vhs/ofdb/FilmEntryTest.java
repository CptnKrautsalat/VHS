package de.zlb.vhs.ofdb;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

public class FilmEntryTest {

    @Test
    public void testGenerateTitleVariations1() {
        Set<String> actual = new FilmEntry("Book of Eli - Die Zukunft der Welt liegt in seinen Händen, The", "").titles;
        Set<String> expected = new HashSet<>();
        expected.add("Book of Eli - Die Zukunft der Welt liegt in seinen Händen, The");
        expected.add("Book of Eli Die Zukunft der Welt liegt in seinen Händen, The");
        expected.add("Book of Eli");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testGenerateTitleVariations2() {
        Set<String> actual = new FilmEntry("Musik von \"The Book of Eli\", Die [Kurzfilm]", "").titles;
        Set<String> expected = new HashSet<>();
        expected.add("Musik von \"The Book of Eli\", Die");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testGenerateTitleVariations3() {
        Set<String> actual = new FilmEntry("Adaption.", "").titles;
        Set<String> expected = new HashSet<>();
        expected.add("Adaption.");
        expected.add("Adaption");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testGenerateTitleVariations4() {
        Set<String> actual = new FilmEntry("[●REC]² - Die nächste Dimension des Grauens", "").titles;
        Set<String> expected = new HashSet<>();
        expected.add("[●REC]² - Die nächste Dimension des Grauens");
        expected.add("[●REC]² Die nächste Dimension des Grauens");
        expected.add("[●REC]²");
        Assertions.assertEquals(expected, actual);
    }
}
