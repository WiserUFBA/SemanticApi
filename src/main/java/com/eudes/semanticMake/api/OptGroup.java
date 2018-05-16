package com.eudes.semanticMake.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * A class that holds a group of predicates of a vocabulary
 * @author Eudes Souza
 * @since 10/2017
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OptGroup {
    /**
     * String that holds the title of the predicate group
     */
    @ApiModelProperty(notes = "Título do grupo criado")
    private String text;
    /**
     * Option list where each option has a predicate
     */
    @ApiModelProperty(notes = "Lista de predicados do grupo")
    private List<Option> children;
}
