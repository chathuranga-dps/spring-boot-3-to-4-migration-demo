package com.example.migrationdemo.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.SerializationFeature;

/**
 * MIGRATION-DEMO:
 * Jackson configuration migrated from Jackson 2 to Jackson 3 for Spring Boot 4.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public JsonMapperBuilderCustomizer jsonMapperBuilderCustomizer() {
        return builder -> builder
                .changeDefaultPropertyInclusion(inclusion ->
                        inclusion.withValueInclusion(JsonInclude.Include.NON_NULL))
                .disable(SerializationFeature.INDENT_OUTPUT);
    }

}
