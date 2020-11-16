package de.zlb.vhs.ofdb;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LibraryCatalogEntryTest {

    @Test
    public void testExtractMainTitle1() {
        String title = new LibraryCatalogEntry().extractMainTitle("¬Der¬ mit dem Wolf tanzt : [Video]");
        Assertions.assertEquals("mit dem Wolf tanzt, Der", title);
    }

    @Test
    public void testExtractMainTitle2() {
        String title = new LibraryCatalogEntry().extractMainTitle("Flashdance : What a feeling ; [Video]");
        Assertions.assertEquals("Flashdance : What a feeling", title);
    }

    @Test
    public void testExtractMainTitle3() {
        String title = new LibraryCatalogEntry().extractMainTitle("¬Die¬ unglaubliche Maschine Mensch");
        Assertions.assertEquals("unglaubliche Maschine Mensch, Die", title);
    }
}
