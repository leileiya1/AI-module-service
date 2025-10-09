package com.example.visionanalysisservice.config;

import ai.z.openapi.ZhipuAiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * 智普AI客户端配置类。
 * 负责创建并向Spring容器提供一个 ZhipuAiClient 的单例Bean。
 * 这种方式将SDK的初始化逻辑与业务逻辑完全分离，是企业级开发的最佳实践。
 */
@Slf4j
@Configuration
public class ZhipuAiConfig {

    /**
     * 从 application.yml 配置文件中注入 API Key。
     */
    @Value("${zhipu.api.key}")
    private String apiKey;

    /**
     * @Bean 注解告诉Spring，这个方法的返回值是一个需要被容器管理的Bean。
     * 其他任何需要 ZhipuAiClient 的地方，都可以通过 @Autowired 或构造函数注入来获取它。
     * @return 配置好的 ZhipuAiClient 实例
     */
    @Bean
    public ZhipuAiClient zhipuAiClient() {
        log.info("【智普AI配置】开始初始化 ZhipuAiClient...");
        if (!StringUtils.hasText(apiKey) || "sk-...".equals(apiKey)) {
            log.error("【智普AI配置】错误：API Key 未配置！请在 application.yml 中设置 zhipu.api.key。");
            // 在实际应用中，这里应该抛出异常，阻止应用启动
            throw new IllegalArgumentException("智普AI API Key 未配置！");
        }
        log.info("【智普AI配置】ZhipuAiClient 初始化成功。");
        return ZhipuAiClient.builder()
                .apiKey(apiKey)
                .build();
    }
}
