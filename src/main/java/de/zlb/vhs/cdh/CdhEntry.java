package de.zlb.vhs.cdh;

import de.zlb.vhs.ComparableFilmEntry;
import de.zlb.vhs.TitleUtil;
import de.zlb.vhs.csv.CdhEntryBean;
import lombok.Setter;
import lombok.ToString;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@ToString
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
        this.year = extractYear(bean);

        this.directors = new HashSet<>();
        extractDirectors(bean);
        this.alternativeTitles = new HashSet<>();
        this.generatedTitles = new HashSet<>();
        generateTitles();
    }

    private String extractYear(CdhEntryBean bean) {
        String year = bean.getYear().split("[/-]")[0];
        return year.contains("?") ? "" : year;
    }

    private void generateTitles() {
        generatedTitles.addAll(TitleUtil.moveLeadingArticles(this.mainTitle));
        Arrays.stream(mainTitle.split("[.-:]"))
                .map(String::trim)
                .map(TitleUtil::moveLeadingArticles)
                .flatMap(Set::stream)
                .filter(t -> !t.isBlank())
                .forEach(generatedTitles::add);
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
