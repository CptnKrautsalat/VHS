package de.zlb.vhs.ofdb;

public class FilmVersionEntry {
	public final FilmEntry film;
	public final String title;
	public final String medium;
	public final String publisher;
	public final String country;
	public final String rating;
	public final String link;
	
	public FilmVersionEntry(FilmEntry film, String title, String link) {
		super();
		this.film = film;
		this.title = title;
		this.medium = extractMedium(title);
		this.publisher = extractPublisher(title);
		this.country = extractCountry(title);
		this.rating = extractRating(title);
		this.link = link;
	}
	
	public FilmVersionEntryBean toBean() {
		return new FilmVersionEntryBean(film.title, String.valueOf(film.year), film.link, medium, publisher, country, rating, link);
	}
	
	private final String extractMedium (String title) {
		int index = title.indexOf(":");
		return title.substring(0, index);
	}
	
	private final String extractPublisher (String title) {
		int index1 = title.indexOf(':');
		int index2 = title.lastIndexOf('(');
		return title.substring(index1 + 2, index2 - 1);
	}
	
	private final String extractCountry (String title) {
		int index1 = title.indexOf('(');
		int index2 = title.lastIndexOf(')');
		return title.substring(index1 + 1, index2);
	}
	
	private final String extractRating (String title) {
		int index1 = title.lastIndexOf(',');
		return title.substring(index1 + 2, title.length());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((link == null) ? 0 : link.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
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
			if (other.link != null)
				return false;
		} else if (!link.equals(other.link))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FilmVersionEntry [medium=" + medium + ", publisher=" + publisher + ", country=" + country + ", rating="
				+ rating + ", link=" + link + "]";
	}
	
}
