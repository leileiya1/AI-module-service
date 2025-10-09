package com.example.visionanalysisservice.controller;

import com.example.dto.AI.VisionAnalysisRequest;
import com.example.dto.AI.VisionAnalysisResponse;
import com.example.visionanalysisservice.service.VisionAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vision")
@Slf4j
@RequiredArgsConstructor
public class VisionAnalysisController {

    private final VisionAnalysisService visionAnalysisService;

    @PostMapping("/analyze")
    public ResponseEntity<VisionAnalysisResponse> analyze(@RequestBody VisionAnalysisRequest request) {
        log.info("【API入口】收到视觉分析请求...");
        // Service层现在会处理所有异常，Controller层只负责调用和返回
        VisionAnalysisResponse response = visionAnalysisService.analyzeImage(request.getImageUrl());
        log.info("【API入口】视觉分析请求处理完成。");
        return ResponseEntity.ok(response);
    }
}
