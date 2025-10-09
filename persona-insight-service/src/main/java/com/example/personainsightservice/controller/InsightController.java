package com.example.personainsightservice.controller;

import com.example.dto.AI.CreateJobRequest;
import com.example.dto.AI.CreateJobResponse;
import com.example.dto.AI.JobStatusResponse;
import com.example.personainsightservice.service.InsightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/insight")
@Slf4j
@RequiredArgsConstructor
public class InsightController {

    private final InsightService insightService;

    @PostMapping("/jobs")
    public ResponseEntity<CreateJobResponse> createJob(
            @RequestBody CreateJobRequest request,
            @RequestHeader("X-User-Id") String userId) {
        log.info("【API入口】收到风格分析任务创建请求, UserID: {}", userId);
        CreateJobResponse response = insightService.createJob(request, userId);
        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<JobStatusResponse> getJobStatus(
            @PathVariable String jobId,
            @RequestHeader("X-User-Id") String userId) {
        log.info("【API入口】收到任务状态查询请求, JobID: {}", jobId);
        JobStatusResponse response = insightService.getJobStatus(jobId, userId);
        return ResponseEntity.ok(response);
    }
}