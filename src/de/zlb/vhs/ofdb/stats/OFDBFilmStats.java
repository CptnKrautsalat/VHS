package de.zlb.vhs.ofdb.stats;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OFDBFilmStats {
    public long films = 0;
    public long tvShows = 0;
    public long shorts = 0;
    public long features = 0;
    public long releases = 0;
    public long vhsReleases = 0;
    public long dvdReleases = 0;
    public long blurayReleases = 0;

    private final Map<String, Long> countries = new HashMap<>();

    public void incrementCountry (String country) {
        if (countries.containsKey(country)) {
            countries.compute(country, (k, v) -> (v != null) ? ++v : v);
        } else {
            countries.put(country, 1L);
        }
    }

    public List<String> toLines() {
        List<String> lines = new LinkedList<>();
        lines.add("Filme \t:\t" + films);
        lines.add("Serien \t:\t" + tvShows);
        lines.add("Kurzfilme \t:\t" + shorts);
        lines.add("Langfilme \t:\t" + features);
        lines.add("");
        lines.add("Veröffentlichungen \t:\t" + releases);
        lines.add("Videos \t:\t" + vhsReleases);
        lines.add("DVDs \t:\t" + dvdReleases);
        lines.add("Blu-Rays \t:\t" + blurayReleases);
        lines.add("");
        lines.add("Länder:");
        countries.forEach((k, v) -> lines.add(k + " \t:\t" + v));
        return lines;
    }

    @Override
    public String toString() {
        return "OFDBFilmStats{" +
                "films=" + films +
                ", tvShows=" + tvShows +
                ", shorts=" + shorts +
                ", features=" + features +
                ", releases=" + releases +
                ", vhsReleases=" + vhsReleases +
                ", dvdReleases=" + dvdReleases +
                ", blurayReleases=" + blurayReleases +
                '}';
    }
}
