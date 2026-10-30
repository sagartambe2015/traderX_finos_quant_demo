package com.quantraksha.kite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class KiteRestApplication {

    public static void main(String[] args) {
        SpringApplication.run(KiteRestApplication.class, args);
    }
}
