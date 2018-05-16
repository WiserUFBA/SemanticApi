package com.eudes.semanticMake;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import static org.springframework.web.cors.CorsConfiguration.ALL;

/**
 * Class responsible for make the CORS configuration for that the API stay avaliable to any another port ou domain
 * CORS enables external applications to make REST requests.
 */
@Configuration
public class WebConfiguration extends WebMvcConfigurerAdapter {


    /**
     * Make all mapping to allow external calls
     * @param registry The registry of mappings
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
                .addMapping("/**")
                .allowedOrigins(ALL)
                .allowedMethods(ALL)
                .allowedHeaders(ALL)
                .allowCredentials(true)
                .maxAge(3600);
    }
}