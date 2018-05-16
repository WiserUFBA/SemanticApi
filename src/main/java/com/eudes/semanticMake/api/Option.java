package com.eudes.semanticMake.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Classe que mantem o id e o valor da opção
 * <br>Esta classe possui a estrutura correta para ser usada para criar a opção que pode ser aplicada em um select HTML
 * <br>A estrutura desta classe é usada para montar o código JSON interpretável pelo select
 * @author Eudes Souza
 * @since 10/2017
 */
@Data
@AllArgsConstructor
public class Option {
    /**
     * String holding the option id
     */
    @ApiModelProperty(notes = "Predicate identifier")
    private String id;
    /**
     * String holding option text
     */
    @ApiModelProperty(notes = "Option text")
    private String text;
}
