package de.zlb.vhs.ofdb.web;

import java.util.Objects;
import java.util.Set;

public class AdditionalOfdbData {
    public final Set<String> directors;
    public final Set<String> alternativeTitles;
    public final String imdbLink;

    public AdditionalOfdbData(Set<String> directors, Set<String> alternativeTitles, String imdbLink) {
        this.directors = directors;
        this.alternativeTitles = alternativeTitles;
        this.imdbLink = imdbLink;
    }

    @Override
    public String toString() {
        return "AdditionalOfdbData{" +
                "directors=" + directors +
                ", alternativeTitles=" + alternativeTitles +
                ", imdbLink='" + imdbLink + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AdditionalOfdbData)) return false;
        AdditionalOfdbData that = (AdditionalOfdbData) o;
        return directors.equals(that.directors) &&
                alternativeTitles.equals(that.alternativeTitles) &&
                imdbLink.equals(that.imdbLink);
    }

    @Override
    public int hashCode() {
        return Objects.hash(directors, alternativeTitles, imdbLink);
    }
}
