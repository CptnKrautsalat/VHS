package de.zlb.vhs.csv;

import com.opencsv.bean.CsvBindByName;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
@EqualsAndHashCode
public class FilmVersionEntryBean {

	@CsvBindByName(column = "Titel")
	private String title;
	@CsvBindByName(column = "Jahr")
	private String year;
	@CsvBindByName(column = "Filmlink")
	private String filmLink;
	@CsvBindByName(column = "Medium")
	private String medium;
	@CsvBindByName(column = "label")
	private String publisher;
	@CsvBindByName(column = "land")
	private String country;
	@CsvBindByName(column = "Freigabe")
	private String rating;
	@CsvBindByName(column = "Fassungslink")
	private String versionLink;
	@CsvBindByName(column = "Imdb_link")
	private String imdbLink;
	@CsvBindByName(column = "Regie")
	private String directors;
	@CsvBindByName(column = "Alternative_titel")
	private String alternativeTitles;

}
