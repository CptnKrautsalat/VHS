package de.zlb.vhs.ofdb;

import de.zlb.vhs.csv.FilmVersionEntryBean;
import de.zlb.vhs.csv.LetterboxdEntryBean;
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
        expected.add("Book of Eli, The");
        expected.add("Book of Eli");
        expected.add("Zukunft der Welt liegt in seinen Händen, Die");
        expected.add("Die Zukunft der Welt liegt in seinen Händen");
        expected.add("Zukunft der Welt liegt in seinen Händen");
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
        expected.add("nächste Dimension des Grauens, Die");
        expected.add("nächste Dimension des Grauens");
        expected.add("Die nächste Dimension des Grauens");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testGenerateTitleVariations5() {
        Set<String> actual = new FilmEntry("American Pie 2", "").titles;
        Set<String> expected = new HashSet<>();
        expected.add("American Pie 2");
        expected.add("American Pie");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testGenerateLetterboxdBean() {
        FilmVersionEntryBean inputBean = new FilmVersionEntryBean();
        inputBean.setTitle("Wunder von Manhattan, Das");
        inputBean.setYear("1994");
        inputBean.setDirectors("Les Mayfield");
        inputBean.setImdbLink("http://www.imdb.com/Title?0110527");
        inputBean.setAlternativeTitles("");
        FilmEntry film = new FilmEntry(inputBean);
        LetterboxdEntryBean expected = new LetterboxdEntryBean("tt0110527", "Wunder von Manhattan", "1994", "Les Mayfield");
        Assertions.assertEquals(expected, film.generateLetterboxdBean());
    }
}
