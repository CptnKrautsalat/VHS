package de.zlb.vhs.ofdb.web;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Medium {

    DVD("D", "DVD"),
    BLU_RAY("B", "Blu-ray Disc"),
    VHS("V", "Video"),
    UHD("I", "Ultra HD Blu-ray"),
    HD_DVD("H", "HD DVD"),
    UMD("U", "UMD"),
    FREE_TV("F", "Free-TV"),
    PAY_TV("P", "Pay-TV"),
    VOD("O", "Video on Demand"),
    CINEMA("K", "Kino"),
    LASER_DISC("L", "Laserdisc"),
    VCD("C", "Video-CD"),
    SUPER_8_16("S", "Super-8/16"),
    ANY("-", "");

    private final String search;
    private final String version;
}
