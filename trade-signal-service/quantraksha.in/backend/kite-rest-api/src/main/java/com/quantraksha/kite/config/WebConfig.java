package com.quantraksha.kite.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final KiteProperties props;

    public WebConfig(KiteProperties props) {
        this.props = props;
    }

    /** Spring rejects requests whose Origin is not listed, so other web pages can't read the account. */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(props.corsOrigins().toArray(String[]::new))
                .allowedMethods("GET", "POST", "DELETE");
    }
}
