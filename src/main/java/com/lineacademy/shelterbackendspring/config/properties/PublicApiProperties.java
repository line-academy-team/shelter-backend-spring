package com.lineacademy.shelterbackendspring.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "public.api")
public class PublicApiProperties {
    private String key;
    private String baseUrl = "http://openapi.seoul.go.kr:8088";
}
