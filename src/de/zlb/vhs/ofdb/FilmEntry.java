package de.zlb.vhs.ofdb;

import de.zlb.vhs.ofdb.csv.FilmVersionEntryBean;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class FilmEntry {
	public final String title;
	public final int year;
	public final String link;
	
	private final List<FilmVersionEntry> versions = new LinkedList<FilmVersionEntry>();

	public FilmEntry(String title, String link) {
		super();
		this.title = extractTitle(title);
		this.year = extractYear(title);
		this.link = OFDBListGenerator.OFDB_LINK_PREFIX + link;
	}

	public FilmEntry(FilmVersionEntryBean filmVersionEntryBean) {
		super();
		this.title = filmVersionEntryBean.title;
		this.year = Integer.parseInt(filmVersionEntryBean.year);
		this.link = filmVersionEntryBean.filmLink;
		this.addVersion(new FilmVersionEntry(this, filmVersionEntryBean));
	}

	public void mergeVersions (FilmEntry otherFilm) {
		otherFilm.versions.forEach(this::addVersion);
	}
	
	private String extractTitle(String title) {
		int index = title.lastIndexOf('(');
		return title.substring(0, index-1);
	}
	
	private int extractYear(String title) {
		int index = title.lastIndexOf('(');
		return Integer.parseInt(title.substring(index + 1, index + 5));
	}
	
	public void addVersion(FilmVersionEntry version) {
		versions.add(version);
	}
	
	public Stream<FilmVersionEntry> getVersions() {
		return versions.stream();
	}

	@Override
	public String toString() {
		return "FilmEntry [title=" + title + ", year=" + year + ", link=" + link + ", versions=" + versions + "]";
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
		FilmEntry other = (FilmEntry) obj;
		if (link == null) {
			return other.link == null;
		} else return link.equals(other.link);
	}
}
