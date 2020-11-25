package de.zlb.vhs.ofdb.web;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class WebUtilTest {

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
        Optional<AdditionalOfdbData> actual = WebUtil.getAdditionalOfdbData("https://ssl.ofdb.de/film/14405,Die-Fantome-des-Hutmachers");
        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(expected, actual.get());
    }
}
