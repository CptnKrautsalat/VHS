package de.zlb.vhs.csv;

import com.opencsv.bean.CsvBindByName;
import de.zlb.vhs.cdh.CdhCombinedFilm;
import de.zlb.vhs.cdh.CdhEntry;
import de.zlb.vhs.ofdb.FilmEntry;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class CdhEntryBean {
    @CsvBindByName(column = "Nr")
    private String number;
    @CsvBindByName(column = "IMDb Link")
    private String imdbLink;
    @CsvBindByName(column = "IDTitel normalisiert")
    private String normalizedTitle;
    @CsvBindByName(column = "Produktionsjahr")
    private String year;
    @CsvBindByName(column = "Gattung")
    private String genre;
    @CsvBindByName(column = "Ursprungsland")
    private String country;
    @CsvBindByName(column = "Regie")
    private String director;
    @CsvBindByName(column = "Regie1")
    private String director1;
    @CsvBindByName(column = "Regie2")
    private String director2;
    @CsvBindByName(column = "Regie3")
    private String director3;
    @CsvBindByName(column = "Regie4")
    private String director4;
    @CsvBindByName(column = "Regie5")
    private String director5;
    @CsvBindByName(column = "UID")
    private String uid;
    @CsvBindByName(column = "IDTitel")
    private String title;

    @CsvBindByName(column = "OFDB Link")
    private String ofdbLink;
    @CsvBindByName(column = "OFDB Jahr")
    private String ofdbYear;
    @CsvBindByName(column = "Ofdb Regie")
    private String ofdbDirectors;
    @CsvBindByName(column = "OFDB Haupttitel")
    private String ofdbTitle;
    @CsvBindByName(column = "OFDB Alternativtitel")
    private String ofdbAlternativeTitles;

    public void update(FilmEntry filmEntry) {
        imdbLink = filmEntry.getImdbLink();
        ofdbLink = filmEntry.link;
        ofdbYear = filmEntry.getYear();
        ofdbTitle = filmEntry.getTitle();
        ofdbDirectors = filmEntry.getDirectorsAsString();
        ofdbAlternativeTitles = filmEntry.getAlternativeTitlesAsString();
    }
}
