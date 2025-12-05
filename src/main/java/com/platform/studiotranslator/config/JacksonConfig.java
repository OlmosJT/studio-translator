package com.platform.studiotranslator.config;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    @Bean
    public JsonMapperBuilderCustomizer jsonMapperCustomizer() {
        return builder -> builder
                .findAndAddModules()
                .defaultDateFormat(new StdDateFormat().withColonInTimeZone(true));
    }
}
