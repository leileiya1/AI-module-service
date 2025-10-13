package com.example.aichatservice.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI模型客户端的统一配置类 (简化版)。
 * 我们现在统一使用智普AI，所以只需要一个默认的ChatClient。
 * 我们可以通过 ChatClient.builder() 来获取由Spring AI自动配置好的客户端构建器。
 */
@Configuration
public class AiClientConfig {

    /**
     * 创建一个默认的 ChatClient Bean。
     * @param builder Spring AI 自动配置的 ChatClient.Builder
     * @return 默认的 ChatClient 实例
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        // 我们可以为所有调用设置一些默认行为，例如：
        // .defaultSystem("...")
        // .defaultTemperature(...)
        return builder.build();
    }
}
