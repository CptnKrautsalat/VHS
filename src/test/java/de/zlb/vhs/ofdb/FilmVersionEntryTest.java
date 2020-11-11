package de.zlb.vhs.ofdb;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FilmVersionEntryTest {

    @Test
    public void testExtractCountry() {
        String title = "DVD: Universal Pictures Home Video (Australien), M (mature audiences)";
        FilmVersionEntry subject = new FilmVersionEntry(null, title, null);
        Assertions.assertEquals("Australien", subject.country);
    }
}
