package com.eudes.semanticMake.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that is used to define the vocabulary structure defined by the client in JSON format
 * @author Eudes Souza
 * @since 10/2017
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Vocabulary {

    /**
     * Vocabulary URI
     */
    private String uri;
    /**
     * Vocabulary prefix
     */
    private String prefix;
    /**
     * List of Pair - different values for (predicate, value)
     */
    private List<Pair> pairs = new ArrayList<>();


}
