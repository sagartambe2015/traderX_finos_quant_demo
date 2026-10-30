package com.quantraksha.kite.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger UI at /swagger-ui.html; the raw spec is at /v3/api-docs. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI kiteOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Kite REST API")
                .version("0.1.0")
                .description("REST API over Zerodha Kite Connect for the React dashboard. "
                        + "Log in via /api/status → loginUrl, then POST /api/session."));
    }
}
