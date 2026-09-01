package com.lineacademy.shelterbackendspring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableJpaAuditing
@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class ShelterBackendSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShelterBackendSpringApplication.class, args);
    }

}
