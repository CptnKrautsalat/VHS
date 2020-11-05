package de.zlb.vhs.ofdb;

import de.zlb.vhs.ofdb.csv.FilmVersionEntryBean;

public class FilmVersionEntry {
	public final FilmEntry film;
	public final String medium;
	public final String publisher;
	public final String country;
	public final String rating;
	public final String link;
	
	public FilmVersionEntry(FilmEntry film, String title, String link) {
		super();
		this.film = film;
		this.medium = extractMedium(title);
		this.publisher = extractPublisher(title);
		this.country = extractCountry(title);
		this.rating = extractRating(title);
		this.link = OFDBListGenerator.OFDB_LINK_PREFIX + link;
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
		return new FilmVersionEntryBean(film.title, String.valueOf(film.year), film.link, medium, publisher, country, rating, link);
	}
	
	private String extractMedium (String title) {
		int index = title.indexOf(":");
		return title.substring(0, index);
	}
	
	private String extractPublisher (String title) {
		int index1 = title.indexOf(':');
		int index2 = title.lastIndexOf('(');
		return title.substring(index1 + 2, index2 - 1);
	}
	
	private String extractCountry (String title) {
		int index1 = title.lastIndexOf('(');
		int index2 = title.lastIndexOf(')');
		return (index1 == -1 || index2 == -1) ? "" : title.substring(index1 + 1, index2);
	}
	
	private String extractRating (String title) {
		int index1 = title.lastIndexOf(',');
		return (index1 == -1 || index1 == title.length() - 1) ? "" : title.substring(index1 + 2);
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
