package de.zlb.vhs.csv;

import com.opencsv.bean.CsvBindByName;

import java.util.Objects;

public class LetterboxdEntryBean {

    @CsvBindByName(column = "imdbID")
    public String imdbId;
    @CsvBindByName(column = "Title")
    public String title;
    @CsvBindByName(column = "Year")
    public String year;
    @CsvBindByName(column = "Directors")
    public String directors;

    public LetterboxdEntryBean(String imdbId, String title, String year, String directors) {
        this.imdbId = imdbId;
        this.title = title;
        this.year = year;
        this.directors = directors;
    }

    public LetterboxdEntryBean() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LetterboxdEntryBean)) return false;
        LetterboxdEntryBean that = (LetterboxdEntryBean) o;
        return imdbId.equals(that.imdbId) &&
                title.equals(that.title) &&
                year.equals(that.year) &&
                directors.equals(that.directors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imdbId, title, year, directors);
    }

    @Override
    public String toString() {
        return "LetterboxdEntryBean{" +
                "imdbId='" + imdbId + '\'' +
                ", title='" + title + '\'' +
                ", year='" + year + '\'' +
                ", directors='" + directors + '\'' +
                '}';
    }
}
