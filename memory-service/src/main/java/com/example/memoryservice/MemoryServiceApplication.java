package com.example.memoryservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.example.memoryservice.mapper")
@EnableScheduling // ✨ 开启定时任务
public class MemoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MemoryServiceApplication.class, args);
    }

}
