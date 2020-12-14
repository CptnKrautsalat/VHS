package de.zlb.vhs.ofdb;

import de.zlb.vhs.csv.FilmVersionEntryBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

public class FilmVersionEntry {

	public static final Logger log = LogManager.getLogger(FilmVersionEntry.class);

	private static final String[] GERMAN_LANGUAGE_COUNTRIES = {"Deutschland", "Österreich", "Schweiz"};

	public final FilmEntry film;
	public final String medium;
	public String publisher;
	public String country;
	public String rating;
	public final String link;
	
	public FilmVersionEntry(FilmEntry film, String title, String link) {
		super();
		this.film = film;
		this.medium = extractMedium(title);
		this.publisher = extractPublisher(title);
		this.country = extractCountry(title);
		this.rating = extractRating(title);
		this.link = OfdbManager.OFDB_LINK_PREFIX + link;
	}

	public FilmVersionEntry(FilmEntry film, FilmVersionEntryBean bean) {
		super();
		this.film = film;
		this.medium = bean.medium;
		this.publisher = bean.publisher;
		this.country = bean.country;
		this.rating = bean.rating;
		this.link = bean.versionLink;
	}
	
	public FilmVersionEntryBean toBean() {
		return new FilmVersionEntryBean(film.title, String.valueOf(film.year), film.link, medium, publisher, country,
				rating, link, film.getImdbLink(), film.getDirectorsAsString(), film.getAlternativeTitlesAsString());
	}

	public boolean isVHS() {
		return medium.equals("Video");
	}

	public boolean isDigital() {
		return isDVD() || isBluRay();
	}

	public boolean isDVD() {
		return medium.equals("DVD");
	}

	public boolean isBluRay() {
		return medium.equals("Blu-ray Disc");
	}

	public boolean isGermanLanguage() {
		return Arrays.asList(GERMAN_LANGUAGE_COUNTRIES).contains(country);
	}
	
	private String extractMedium (String title) {
		int index = title.indexOf(":");
		return title.substring(0, index);
	}

	private String extractPublisher (String title) {
		int lastCommaIndex = title.lastIndexOf(",");
		int index1 = title.indexOf(':');
		int index2 = title.lastIndexOf('(');
		if (lastCommaIndex != -1 && lastCommaIndex < index2 && index2 != title.indexOf('(')) {
			String titleWithoutRating = title.substring(0, lastCommaIndex);
			index2 = titleWithoutRating.lastIndexOf('(');
		}
		return title.substring(index1 + 2, index2 - 1);
	}

	private String extractCountry (String title) {
		int lastCommaIndex = title.lastIndexOf(",");
		int index1 = title.lastIndexOf('(');
		int index2 = title.lastIndexOf(')');
		if (lastCommaIndex != -1 && lastCommaIndex < index2 && index2 != title.indexOf(')')) {
			String titleWithoutRating = title.substring(0, lastCommaIndex);
			index1 = titleWithoutRating.lastIndexOf('(');
			index2 = titleWithoutRating.lastIndexOf(')');
		}
		return (index1 == -1 || index2 == -1) ? "" : title.substring(index1 + 1, index2);
	}

	private String extractRating (String title) {
		int index1 = title.lastIndexOf(',');
		int index2 = title.lastIndexOf('(');
		boolean noRating = index1 < index2 && index2 == title.indexOf('(');
		String rating = (noRating || index1 == -1 || index1 == title.length() - 1) ? "" : title.substring(index1 + 2);
		return rating.endsWith("\\") ? "" : rating;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((link == null) ? 0 : link.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FilmVersionEntry other = (FilmVersionEntry) obj;
		if (link == null) {
			return other.link == null;
		} else return link.equals(other.link);
	}

	@Override
	public String toString() {
		return "FilmVersionEntry [medium=" + medium + ", publisher=" + publisher + ", country=" + country + ", rating="
				+ rating + ", link=" + link + "]";
	}
	
}
