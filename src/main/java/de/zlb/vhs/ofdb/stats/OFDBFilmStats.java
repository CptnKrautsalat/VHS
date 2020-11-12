package de.zlb.vhs.ofdb.stats;

import java.util.*;

public class OFDBFilmStats {
    public static final String SUPERTAB = "\t\t\t\t\t: ";
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
        lines.add("Filme " + SUPERTAB + films);
        lines.add("Serien " + SUPERTAB + tvShows);
        lines.add("Kurzfilme " + SUPERTAB + shorts);
        lines.add("Langfilme " + SUPERTAB + features);
        lines.add("----------");
        lines.add("Veröffentlichungen " + SUPERTAB + releases);
        lines.add("Videos " + SUPERTAB + vhsReleases);
        lines.add("DVDs " + SUPERTAB + dvdReleases);
        lines.add("Blu-Rays " + SUPERTAB + blurayReleases);
        lines.add("----------");
        lines.add("Länder:");
        countries
                .entrySet()
                .stream()
                .sorted(Comparator.comparingLong(Map.Entry::getValue))
                .forEach(e -> lines.add(e.getKey() + SUPERTAB + e.getValue()));
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
