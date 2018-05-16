package com.eudes.semanticMake.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that is used to define the structure of the client-defined resource in JSON format
 * @author Eudes Souza
 * @since 10/2017
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceApi {

    /**
     * URI of the created resource
     */
    private String about;
    /**
     * Prefixo do recurso criado
     */
    private String prefix;
    /**
     * Created resource type
     */
    private String name;
    /**
     * List of vocabularies used in the created resource
     */
    List<Vocabulary> vocabularies = new ArrayList<>();

}
