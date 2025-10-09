package com.example.personainsightservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.example.personainsightservice.mapper")
@EnableAsync // 开启异步支持
public class PersonaInsightServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PersonaInsightServiceApplication.class, args);
    }

}
