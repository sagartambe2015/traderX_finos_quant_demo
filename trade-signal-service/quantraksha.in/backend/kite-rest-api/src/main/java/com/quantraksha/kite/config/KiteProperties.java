package com.quantraksha.kite.config;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "kite")
public record KiteProperties(
        @NotBlank(message = "set KITE_API_KEY") String apiKey,
        @NotBlank(message = "set KITE_API_SECRET") String apiSecret,
        String tokenFile,
        String uiUrl,
        List<String> corsOrigins) {
}
