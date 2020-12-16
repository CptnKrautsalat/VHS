package de.zlb.vhs.ofdb.web;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchParameters {
    @Builder.Default private boolean futureReleases = false;
    @Builder.Default private char titleStartsWith = '-';
    @Builder.Default private String title = "";
    @Builder.Default private Genre genre = Genre.ANY;
    @Builder.Default private float minimumUserRating = 0f;
    @Builder.Default private String productionCountry = "-";
    @Builder.Default private String year = "";
    @Builder.Default private String director = "";
    @Builder.Default private String actor = "";
    @Builder.Default private Medium medium = Medium.ANY;
    @Builder.Default private String publisher = "";
    @Builder.Default private String publishingCountry = "-";
    @Builder.Default private Rating rating = Rating.ANY;
    @Builder.Default private Version version = Version.ANY;
    @Builder.Default private IndexStatus indexStatus = IndexStatus.ANY;
    @Builder.Default private String info = "";
    @Builder.Default private int position = 0;

    public String toUrl() {
        return "https://ssl.ofdb.de/view.php?page=fsuche&Typ=" + (futureReleases ? "V" : "N")
                + "&AB=" + titleStartsWith
                + "&Titel=" + title
                + "&Genre=" + genre.getSearch()
                + "&Note=" + minimumUserRating
                + "&HLand=" + productionCountry
                + "&Jahr=" + year
                + "&Regie=" + director
                + "&Darsteller=" + actor
                + "&Wo=" + medium.getSearch()
                + "&Wer=" + publisher
                + "&Land=" + publishingCountry
                + "&Freigabe=" + rating.getSearch()
                + "&Cut=" + version.getSearch()
                + "&Indiziert=" + indexStatus.getSearch()
                + "&Info=" + info
                + "&Pos=" + position;
    }
}
