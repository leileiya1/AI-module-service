package com.example.emotionanalysisservice.controller;

import com.example.dto.AI.EmotionAnalysisRequest;
import com.example.dto.AI.EmotionAnalysisResponse;
import com.example.emotionanalysisservice.service.EmotionAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/emotions")
@Slf4j
@RequiredArgsConstructor
public class EmotionAnalysisController {

    private final EmotionAnalysisService emotionAnalysisService;

    @PostMapping("/analyze")
    public ResponseEntity<EmotionAnalysisResponse> analyze(@RequestBody EmotionAnalysisRequest request) {
        log.info("【API入口】收到情绪分析请求...");
        EmotionAnalysisResponse response = emotionAnalysisService.analyzeEmotion(request.getText());
        return ResponseEntity.ok(response);
    }
}
