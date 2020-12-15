package de.zlb.vhs.csv;

import com.opencsv.bean.CsvBindByName;
import de.zlb.vhs.catalog.LibraryCatalogEntry;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class LibraryCatalogEntryBean {
    @CsvBindByName(column = "phyform")
    private String physicalForm;
    @CsvBindByName(column = "akds")
    private String akds;
    @CsvBindByName(column = "mediennr")
    private String mediaNumber;
    @CsvBindByName(column = "signatur")
    private String signature;
    @CsvBindByName(column = "sigel")
    private String sigel;
    @CsvBindByName(column = "fachst")
    private String department;
    @CsvBindByName(column = "regie_gndnr")
    private String director;
    @CsvBindByName(column = "verantwortlichkeitsangabe")
    private String castAndCrew;
    @CsvBindByName(column = "titel")
    private String title;
    @CsvBindByName(column = "weitere_titel")
    private String alternativeTitles;
    @CsvBindByName(column = "anmerkungen")
    private String comments;
    @CsvBindByName(column = "veröffentlichung")
    private String release;
    @CsvBindByName(column = "sprachen")
    private String languages;
    @CsvBindByName(column = "umfang_dauer")
    private String length;
    @CsvBindByName(column = "erwart")
    private String acquisition;
    @CsvBindByName(column = "genres")
    private String genres;
    @CsvBindByName(column = "ausl2010bis20")
    private String rentals2010to2020;
    @CsvBindByName(column = "ausl2020")
    private String rentals2020;
    @CsvBindByName(column = "exstat")
    private String exstat;
    @CsvBindByName(column = "austat")
    private String austat;
    @CsvBindByName(column = "ofdb")
    private String ofdbLink;

    public void update(LibraryCatalogEntry entry) {
        if (entry.isLinkedToOfdbFilm()) {
            ofdbLink = entry.getFilm().getOfdbEntry().link;
        }
    }
}
