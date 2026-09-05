package com.example.migrationdemo.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;

/**
 * MIGRATION-DEMO:
 * Jackson configuration migrated from Jackson 2 to Jackson 3 for Spring Boot 4.
 */
@Configuration
public class JacksonConfig {

    /*
     * MIGRATION-DEMO:
     * This customization was migrated from Spring Boot 3 / Jackson 2 to
     * Spring Boot 4 / Jackson 3.
     */
    @Bean
    public JsonMapperBuilderCustomizer jsonMapperBuilderCustomizer() {
        return builder -> builder
                .changeDefaultPropertyInclusion(inclusion ->
                        inclusion.withValueInclusion(JsonInclude.Include.NON_NULL))
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.INDENT_OUTPUT);
    }

}
