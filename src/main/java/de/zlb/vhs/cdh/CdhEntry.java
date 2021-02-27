package de.zlb.vhs.cdh;

import de.zlb.vhs.CombinedFilm;
import de.zlb.vhs.ComparableFilmEntry;
import de.zlb.vhs.TitleUtil;
import de.zlb.vhs.csv.CdhEntryBean;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

public class CdhEntry extends ComparableFilmEntry {

    private final CdhEntryBean bean;

    private final String mainTitle;
    private final String year;

    private final Set<String> directors;
    private final Set<String> alternativeTitles;
    private final Set<String> generatedTitles;

    @Setter
    private CdhCombinedFilm combinedFilm;

    public CdhEntry(CdhEntryBean bean) {

        this.bean = bean;

        this.mainTitle = bean.getNormalizedTitle();
        this.year = bean.getYear().split("/")[0];

        this.directors = new HashSet<>();
        extractDirectors(bean);
        this.alternativeTitles = new HashSet<>();
        this.generatedTitles = TitleUtil.moveLeadingArticles(this.mainTitle);
    }

    private void extractDirectors(CdhEntryBean bean) {
        directors.add(bean.getDirector());
        directors.add(bean.getDirector1());
        directors.add(bean.getDirector2());
        directors.add(bean.getDirector3());
        directors.add(bean.getDirector4());
        directors.add(bean.getDirector5());
        directors.removeIf(String::isEmpty);
    }

    @Override
    public String getMainTitle() {
        return mainTitle;
    }

    @Override
    public Set<String> getAlternativeTitles() {
        return alternativeTitles;
    }

    @Override
    public Set<String> getGeneratedTitles() {
        return generatedTitles;
    }

    @Override
    public CdhCombinedFilm getFilm() {
        return combinedFilm;
    }

    @Override
    public String getYear() {
        return year;
    }

    @Override
    public Set<String> getDirectors() {
        return directors;
    }

    public void updateBean() {
        CdhCombinedFilm combinedFilm = getFilm();
        if (combinedFilm != null && combinedFilm.hasOfdbEntry()) {
            bean.update(combinedFilm.getOfdbEntry());
        }
    }
}
