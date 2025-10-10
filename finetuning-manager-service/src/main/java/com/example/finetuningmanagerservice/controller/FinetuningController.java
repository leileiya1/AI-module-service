package com.example.finetuningmanagerservice.controller;

import com.example.dto.AI.CreateJobResponse;
import com.example.finetuningmanagerservice.dto.CreateJobRequest;
import com.example.finetuningmanagerservice.service.FinetuningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/finetuning")
@Slf4j
@RequiredArgsConstructor
public class FinetuningController {

    private final FinetuningService finetuningService;

    @PostMapping("/jobs")
    public ResponseEntity<CreateJobResponse> createJob(@RequestBody CreateJobRequest request) {
        log.info("【API入口】收到微调任务创建请求...");
        CreateJobResponse response = finetuningService.createJob(request);
        return ResponseEntity.accepted().body(response);
    }
}
