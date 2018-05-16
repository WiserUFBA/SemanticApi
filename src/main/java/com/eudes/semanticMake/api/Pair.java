package com.eudes.semanticMake.api;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Class that is used to define the structure of the pair (predicate, value) defined by the client in JSON format
 * @author Eudes Souza
 * @since 10/2017
 */
@Data
@AllArgsConstructor
public class Pair {

    /**
     * Predicate name
     */
    private String propertyName;

    /**
     * Predicate value
     */
    private String value;

    /**
     * Flag to verify the representation of the resource
     */
    private boolean asResource;

    /**
     * String that contains the name of the property to which this property belongs
     */
    private String subPropertyOf;
}
