package com.example.dto.AI;

import lombok.Data;

@Data
public class EmotionAnalysisResponse {
    private Emotion emotion;
    private float confidence; // 置信度
}
