package de.zlb.vhs.ofdb;

import de.zlb.vhs.ofdb.csv.FilmVersionEntryBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FilmVersionEntryTest {

    @Test
    public void testExtractCountry() {
        String title = "DVD: Universal Pictures Home Video (Australien), M (mature audiences)";
        FilmVersionEntry subject = new FilmVersionEntry(null, title, null);
        Assertions.assertEquals("Australien", subject.country);
    }

    @Test
    public void testExtractPublisher() {
        String title = "DVD: Universal Pictures Home Video (Australien), M (mature audiences)";
        FilmVersionEntry subject = new FilmVersionEntry(null, title, null);
        Assertions.assertEquals("Universal Pictures Home Video", subject.publisher);
    }

    @Test
    public void testFixBrokenEntry1() {
        FilmVersionEntryBean bean = new FilmVersionEntryBean();
        bean.country = "mature audiences";
        bean.medium = "DVD";
        bean.rating = "M (mature audiences)";
        bean.publisher = "Universal Pictures Home Video (Australien), M";
        FilmVersionEntry subject = new FilmVersionEntry(null, bean);
        subject.fixBrokenEntry();
        Assertions.assertEquals("Universal Pictures Home Video", subject.publisher);
        Assertions.assertEquals("DVD", subject.medium);
        Assertions.assertEquals("M (mature audiences)", subject.rating);
        Assertions.assertEquals("Australien", subject.country);
    }

    @Test
    public void testFixBrokenEntry2() {
        FilmVersionEntryBean bean = new FilmVersionEntryBean();
        bean.country = "USA";
        bean.medium = "Video";
        bean.rating = "Inc. (USA)";
        bean.publisher = "NYUE Enterprises, Inc.";
        FilmVersionEntry subject = new FilmVersionEntry(null, bean);
        subject.fixBrokenEntry();
        Assertions.assertEquals("NYUE Enterprises, Inc.", subject.publisher);
        Assertions.assertEquals("Video", subject.medium);
        Assertions.assertEquals("", subject.rating);
        Assertions.assertEquals("USA", subject.country);
    }
}
