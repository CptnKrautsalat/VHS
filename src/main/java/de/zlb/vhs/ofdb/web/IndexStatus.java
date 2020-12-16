package de.zlb.vhs.ofdb.web;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum IndexStatus {

    ANY("A"),
    NOT_INDEXED("N"),
    INDEXED("J");

    private final String search;
}
