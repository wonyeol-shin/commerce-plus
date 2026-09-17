package com.example.commerceplus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableCaching
@EnableMethodSecurity
@EnableScheduling
public class CommerceplusApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommerceplusApplication.class, args);
    }

}
