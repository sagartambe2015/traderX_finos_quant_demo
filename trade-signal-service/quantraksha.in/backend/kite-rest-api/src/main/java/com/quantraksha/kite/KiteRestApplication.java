package com.quantraksha.kite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class KiteRestApplication {
//https://kite.trade/connect/login?api_key=t84dt5r1xj0ftdoo&v=3"
//}
    public static void main(String[] args) {
        SpringApplication.run(KiteRestApplication.class, args);
    }
}
