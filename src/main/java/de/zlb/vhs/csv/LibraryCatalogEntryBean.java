package de.zlb.vhs.csv;

import com.opencsv.bean.CsvBindByName;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LibraryCatalogEntryBean)) return false;
        LibraryCatalogEntryBean that = (LibraryCatalogEntryBean) o;
        return physicalForm.equals(that.physicalForm) &&
                akds.equals(that.akds) &&
                mediaNumber.equals(that.mediaNumber) &&
                signature.equals(that.signature) &&
                sigel.equals(that.sigel) &&
                department.equals(that.department) &&
                director.equals(that.director) &&
                castAndCrew.equals(that.castAndCrew) &&
                title.equals(that.title) &&
                alternativeTitles.equals(that.alternativeTitles) &&
                comments.equals(that.comments) &&
                release.equals(that.release) &&
                languages.equals(that.languages) &&
                length.equals(that.length) &&
                acquisition.equals(that.acquisition) &&
                genres.equals(that.genres) &&
                rentals2010to2020.equals(that.rentals2010to2020) &&
                rentals2020.equals(that.rentals2020) &&
                exstat.equals(that.exstat) &&
                austat.equals(that.austat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(physicalForm, akds, mediaNumber, signature, sigel, department, director, castAndCrew, title,
                alternativeTitles, comments, release, languages, length, acquisition, genres, rentals2010to2020, rentals2020,
                exstat, austat);
    }
}
