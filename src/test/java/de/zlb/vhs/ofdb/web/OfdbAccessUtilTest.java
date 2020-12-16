package de.zlb.vhs.ofdb.web;

import de.zlb.vhs.ofdb.FilmEntry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class OfdbAccessUtilTest {

    @Test
    public void testGetAdditionalOfdbData() {
        Set<String> titles = new HashSet<>();
        titles.add("Hatter's Ghost, The");
        titles.add("Phantome des Hutmachers, Die");
        titles.add("Fantômes du chapelier, Les");
        Set<String> directors = new HashSet<>();
        directors.add("Claude Chabrol");
        String imdbLink = "http://www.imdb.com/Title?0083925";
        AdditionalOfdbData expected = new AdditionalOfdbData(directors, titles, imdbLink);
        Optional<AdditionalOfdbData> actual = OfdbAccessUtil.getAdditionalOfdbData("https://ssl.ofdb.de/film/14405,Die-Fantome-des-Hutmachers");
        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(expected, actual.get());
    }

    @Test
    public void testGetFilmForDirectorAndYear() {
        String url = OfdbAccessUtil.generateOfdbUrlForSpecificSearch("1972", "Ronald Neame");
        try {
            Set<FilmEntry> films = OfdbAccessUtil.generateOFDBList(url);
            Assertions.assertEquals(films.size(), 1);
            Assertions.assertTrue(films.stream().anyMatch(f -> f.link.equals("https://ssl.ofdb.de/film/6268,Die-Höllenfahrt-der-Poseidon")));

        } catch (IOException e) {
            Assertions.fail();
        }
    }
}
