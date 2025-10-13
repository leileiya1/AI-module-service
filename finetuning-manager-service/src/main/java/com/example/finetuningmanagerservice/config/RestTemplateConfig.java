package com.example.finetuningmanagerservice.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * RestTemplate配置类
 * 用于HTTP请求，包括调用智谱AI接口
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ofMinutes(5))
                .requestFactory(HttpComponentsClientHttpRequestFactory.class)
                .build();
    }
}
