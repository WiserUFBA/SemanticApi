package com.eudes.semanticMake.api;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PrefixedPair {

    /**
     * String to hold the PrefixedPair prefix
     */
    private String prefix;

    /**
     * String to hold the PreixedPair uri
     */
    private String uri;

    /**
     * Pair to hold the PreixedPair pair
     */
    Pair pair;
}
