package com.example.emotionanalysisservice.service;

import com.example.dto.AI.Emotion;
import com.example.dto.AI.EmotionAnalysisResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmotionAnalysisService {

    private final ChatClient chatClient;

    public EmotionAnalysisService(ChatClient.Builder chatClient) {
        this.chatClient = chatClient.build();
    }

    /**
     * 分析文本的核心情绪。
     *
     * @param text 待分析的文本
     * @return 情绪分析结果
     */
    public EmotionAnalysisResponse analyzeEmotion(String text) {
        log.info("【情绪分析】开始分析文本: '{}...'", text.substring(0, Math.min(50, text.length())));

        // 1. 构建专门用于情绪分类的 Prompt
        String prompt = buildEmotionPrompt(text);

        // 2. 调用 LLM 并将返回的 JSON 自动映射到 DTO
        try {
            EmotionAnalysisResponse response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(EmotionAnalysisResponse.class);

            log.info("【情绪分析】分析完成，情绪: {}, 置信度: {}", response.getEmotion(), response.getConfidence());
            return response;
        } catch (Exception e) {
            log.error("【情绪分析】调用LLM进行情绪分析失败！", e);
            // 如果分析失败，返回一个安全的中性情绪
            EmotionAnalysisResponse fallbackResponse = new EmotionAnalysisResponse();
            fallbackResponse.setEmotion(Emotion.NEUTRAL);
            fallbackResponse.setConfidence(0.5f);
            return fallbackResponse;
        }
    }

    private String buildEmotionPrompt(String text) {
        String excerpt = text.substring(0, Math.min(text.length(), 500));
        return """
                你是一个顶级心理学家和文本情感分析专家。
                你的任务是分析以下文本，并判断其中表达的最主要的核心情绪。
                请从以下几个预定义的情绪标签中选择一个最合适的：
                ["JOY", "SADNESS", "ANGER", "SURPRISE", "FEAR", "NEUTRAL"]
                
                你的输出必须严格遵循以下JSON格式，不要添加任何解释：
                {
                  "emotion": "选择的情绪标签",
                  "confidence": 0.95
                }
                'confidence' 是你对这个判断的置信度，范围在0.0到1.0之间。
                
                待分析的文本如下：
                ---
                %s
                ---
                """.formatted(excerpt);
    }
}
