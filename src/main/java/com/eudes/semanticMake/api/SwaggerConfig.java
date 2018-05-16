package com.eudes.semanticMake.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static springfox.documentation.builders.PathSelectors.regex;

/**
 * Class to make the configuration of the Swagger that shows the operation of the REST methods
 * @author Eudes Souza
 * @since 11/2017
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket productApi() {
        /**
         * Method responsible to configure the package of the controller and its methods
         */
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.eudes.semanticMake.api"))
                .paths(regex("/resources.*"))
                .build()
                .apiInfo(metaData());
    }

    /**
     * Method responsible to customize the Swagger
     * @return Returns the data the we'll showed on Swagger
     */
    private ApiInfo metaData() {
        ApiInfo apiInfo = new ApiInfo(
                "Semantic API",
                "Spring Boot REST Semantic API",
                "1.0",
                "Terms of service",
                new Contact("Eudes Souza", "https://springframework.guru/about/", "eudesdionatas@gmail.com"),
                "Apache License Version 2.0",
                "https://www.apache.org/licenses/LICENSE-2.0");
        return apiInfo;
    }
}
