package de.zlb.vhs.ofdb.web;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Genre {

    ANY("-"),
    ADVENTURE("Abenteuer"),
    ACTION("Action"),
    AMATEUR("Amateur"),
    ANIMATION("Animation"),
    ANIME("Anime"),
    BIOGRAPHY("Biografie"),
    DOCUMENTARY("Dokumentation"),
    DRAMA("DRAMA"),
    EASTERN("Eastern"),
    EROTIC("Erotik"),
    ESSAY("Essayfilm"),
    EXPERIMENTAL("Experimentalfilm"),
    FANTASY("Fantasy"),
    GOTHIC_HORROR("Grusel"),
    HARDCORE("Hardcore"),
    HEIMATFILM("Heimatfilm"),
    HISTORY("Historienfilm"),
    HORROR("Horror"),
    MARTIAL_ARTS("Kampfsport"),
    DISASTER("Katastrophenfilm"),
    FAMILY("Kinder-%2FFamilienfilm"),
    COMEDY("Komödie"),
    WAR("Krieg"),
    CRIME("Krimi"),
    SHORT("Kurzfilm"),
    ROMANCE("Liebe%2FRomantik"),
    MONDO("Mondo"),
    MUSICAL("Musikfilm"),
    MYSTERY("Mystery"),
    SCIENCE_FICTION("Science-Fiction"),
    SERIAL("Serial"),
    SEX("Sex"),
    SPLATTER("Splatter"),
    SPORTS("Sportfilm"),
    SILENT("Stummfilm"),
    TV_MOVIE("TV-Film"),
    TV_MINI_SERIES("TV_Mini-Serie"),
    TV_PILOT("TV-Pilotfilm"),
    TV_SHOW("TV-Serie"),
    THRILLER("Thriller"),
    ANIMAL("Tierfilm"),
    WEB_MINI_SERIES("Webminiserie"),
    WEB_SHOW("Webserie"),
    WESTERN("Western");


    private final String search;
}
