package com.example.aichatservice.dto;

import lombok.Data;

/**
 * 【Feign DTO】用于接收来自 emotion-analysis-service 的响应。
 * 这里的 emotion 字段使用 String 类型，以实现服务间的松耦合。
 */
@Data
public class EmotionAnalysisResponse {
    /**
     * 分析出的情绪标签，如 "JOY", "SADNESS" 等。
     * 使用 String 类型是为了健壮性，即使 emotion-analysis-service 未来增加了新的情绪类型，
     * 本服务也不会因为反序列化失败而崩溃。
     */
    private String emotion;

    /**
     * AI模型对此次判断的置信度。
     */
    private float confidence;
}
