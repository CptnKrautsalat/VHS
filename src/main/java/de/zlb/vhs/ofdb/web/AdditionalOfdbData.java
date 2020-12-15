package de.zlb.vhs.ofdb.web;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Set;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AdditionalOfdbData {
    private final Set<String> directors;
    private final Set<String> alternativeTitles;
    private final String imdbLink;
}
