package com.example.aichatservice.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;

@Configuration
public class CompanionChatConfig {

    @Bean(name = "companionChatClient")
    public ChatClient companionChatClient(
            ChatModel chatModel,
            @Value("classpath:prompts/companion/system.md") Resource systemPrompt
    ) throws Exception {
        String system = new String(systemPrompt.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return ChatClient.builder(chatModel)
                .defaultSystem(system)   // 默认“温情陪伴者”人设
                .build();
    }
}
