package com.quantraksha.kite.config;

import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.boot.autoconfigure.gson.GsonBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Gson can't reflect into java.time on Java 17, so each type needs its own ISO-8601 serializer: 2026-10-29, 09:15:30. */
@Configuration
public class GsonConfig {

    @Bean
    public GsonBuilderCustomizer javaTimeSerializers() {
        return builder -> builder
                .registerTypeAdapter(LocalDate.class,
                        (JsonSerializer<LocalDate>) (date, type, context) -> new JsonPrimitive(date.toString()))
                .registerTypeAdapter(LocalTime.class,
                        (JsonSerializer<LocalTime>) (time, type, context) -> new JsonPrimitive(time.toString()))
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonSerializer<LocalDateTime>) (dateTime, type, context) -> new JsonPrimitive(dateTime.toString()));
    }
}
