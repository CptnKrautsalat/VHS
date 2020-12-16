package de.zlb.vhs.ofdb.web;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Rating {

    ANY("-", ""),
    UNKNOWN("NB", "unbekannt"),
    FREE_FOR_ALL("OA", "FSK o.A."),
    FSK_6("6", "FSK 6"),
    FSK_12("12", "FSK 12"),
    FSK_16("16", "FSK 16"),
    FSK_18("18", "FSK 18"),
    ADULTS_ONLY("KJ", "FSK Keine Jugendfreigabe/ab 18"),
    SPIO_JK_1("J1", "SPIO/JK: strafrechtlich unbedenklich"),
    SPIO_JK_2("J2", "SPIO/JK: keine schwere Jugendgefährdung"),
    LEGAL("JG", "juristisch geprüft"),
    UNRATED("NG", "ungeprüft"),
    EDUCATIONAL("IL", "Info-/Lehr-Programm");

    private final String search;
    private final String version;
}
