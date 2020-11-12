package de.zlb.vhs.ofdb.csv;

import com.opencsv.bean.CsvBindByName;

public class LibraryCatalogEntryBean {
    @CsvBindByName(column = "phyform")
    public String physicalForm;
    @CsvBindByName(column = "akds")
    public String akds;
    @CsvBindByName(column = "mediennr")
    public String mediaNumber;
    @CsvBindByName(column = "signatur")
    public String signature;
    @CsvBindByName(column = "sigel")
    public String sigel;
    @CsvBindByName(column = "fachst")
    public String department;
    @CsvBindByName(column = "regie_gndnr")
    public String director;
    @CsvBindByName(column = "verantwortlichkeitsangabe")
    public String castAndCrew;
    @CsvBindByName(column = "titel")
    public String title;
    @CsvBindByName(column = "weitere_titel")
    public String alternativeTitles;
    @CsvBindByName(column = "anmerkungen")
    public String comments;
    @CsvBindByName(column = "veröffentlichung")
    public String release;
    @CsvBindByName(column = "sprachen")
    public String languages;
    @CsvBindByName(column = "umfang_dauer")
    public String length;
    @CsvBindByName(column = "erwart")
    public String acquisition;
    @CsvBindByName(column = "genres")
    public String genres;
    @CsvBindByName(column = "ausl2010bis20")
    public String rentals2010to2020;
    @CsvBindByName(column = "ausl2020")
    public String rentals2020;
    @CsvBindByName(column = "exstat")
    public String exstat;
    @CsvBindByName(column = "austat")
    public String austat;

    public LibraryCatalogEntryBean() {}
}
