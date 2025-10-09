package com.example.aichatservice.apis;

import com.example.aichatservice.dto.EmotionAnalysisResponse;
import com.example.dto.AI.EmotionAnalysisRequest;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "emotion-service", url = "http://localhost:1239")
public interface EmotionServiceClient {
    @PostMapping("/api/v1/emotions/analyze")
    EmotionAnalysisResponse analyze(@RequestBody EmotionAnalysisRequest request);
}
