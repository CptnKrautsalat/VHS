package de.zlb.vhs.csv;

import com.opencsv.bean.CsvBindByName;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class LetterboxdEntryBean {

    @CsvBindByName(column = "imdbID")
    private String imdbId;
    @CsvBindByName(column = "Title")
    private String title;
    @CsvBindByName(column = "Year")
    private String year;
    @CsvBindByName(column = "Directors")
    private String directors;
}
