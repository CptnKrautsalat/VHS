package de.zlb.vhs.ofdb.web;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Version {

    ANY("A"),
    UNCUT("N"),
    EXTENDED("L"),
    CUT("J"),
    MAYBE_CUT("U");

    private final String search;
}
