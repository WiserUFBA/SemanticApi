package com.eudes.semanticMake;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe responsável por executar a API
 */
@SpringBootApplication
public class SemanticApiApplication {

	/**
	 * Mathod that run the API
	 * @param args By default not need be passed
	 */
	public static void main(String[] args) {
		SpringApplication.run(SemanticApiApplication.class, args);
	}
}
