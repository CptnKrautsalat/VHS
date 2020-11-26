package de.zlb.vhs.csv;

import com.opencsv.bean.CsvBindByName;

public class FilmVersionEntryBean {

	@CsvBindByName(column = "Titel")
	public String title;
	@CsvBindByName(column = "Jahr")
	public String year;
	@CsvBindByName(column = "Filmlink")
	public String filmLink;
	@CsvBindByName(column = "Medium")
	public String medium;
	@CsvBindByName(column = "label")
	public String publisher;
	@CsvBindByName(column = "land")
	public String country;
	@CsvBindByName(column = "Freigabe")
	public String rating;
	@CsvBindByName(column = "Fassungslink")
	public String versionLink;
	@CsvBindByName(column = "Imdb_link")
	public String imdbLink;
	@CsvBindByName(column = "Regie")
	public String directors;
	@CsvBindByName(column = "Alternative_titel")
	public String alternativeTitles;

	public FilmVersionEntryBean(String title, String year, String filmLink, String medium, String publisher,
			String country, String rating, String versionLink, String imdbLink, String directors, String alternativeTitles) {
		super();
		this.title = title;
		this.year = year;
		this.filmLink = filmLink;
		this.medium = medium;
		this.publisher = publisher;
		this.country = country;
		this.rating = rating;
		this.versionLink = versionLink;
		this.imdbLink = imdbLink;
		this.directors = directors;
		this.alternativeTitles = alternativeTitles;
	}

	public FilmVersionEntryBean() {}

	@Override
	public String toString() {
		return "FilmVersionEntryBean [title=" + title + ", year=" + year + ", filmLink=" + filmLink + ", medium="
				+ medium + ", publisher=" + publisher + ", country=" + country + ", rating=" + rating + ", versionLink="
				+ versionLink + "]";
	}

}
