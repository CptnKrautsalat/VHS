package de.zlb.vhs.ofdb.stats;

import de.zlb.vhs.ofdb.FilmEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;

public class StatsCollector {

    public static final Logger log = LogManager.getLogger(StatsCollector.class);

    private final OFDBFilmStats stats = new OFDBFilmStats();

    public OFDBFilmStats collectStats(Collection<FilmEntry> films) {

        films.forEach(f -> f.addToStats(stats));

        stats.toLines().forEach(log::trace);

        return stats;
    }
}
