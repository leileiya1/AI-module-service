package com.example.aichatservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableFeignClients(value = {"com.example.apis","com.example.aichatservice.apis"})
@EnableMongoAuditing // ✨ 启用MongoDB的自动审计功能（创建/更新时间戳）
public class AiChatServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiChatServiceApplication.class, args);
    }

}
