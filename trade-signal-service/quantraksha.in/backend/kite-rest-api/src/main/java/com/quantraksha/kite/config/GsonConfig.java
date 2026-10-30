package com.quantraksha.kite.config;

import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import java.time.LocalDate;
import org.springframework.boot.autoconfigure.gson.GsonBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Gson can't reflect into java.time on Java 17, so LocalDate needs its own serializer: 2026-10-29. */
@Configuration
public class GsonConfig {

    @Bean
    public GsonBuilderCustomizer localDateSerializer() {
        return builder -> builder.registerTypeAdapter(LocalDate.class,
                (JsonSerializer<LocalDate>) (date, type, context) -> new JsonPrimitive(date.toString()));
    }
}
