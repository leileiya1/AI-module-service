package com.example.apis;

import com.example.dto.AI.CreateInsightJobRequest;
import com.example.dto.AI.CreateJobResponse;
import com.example.dto.AI.InsightJobStatusResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "insight-service", url = "http://localhost:1238")
public interface InsightServiceClient {

    @PostMapping("/api/v1/insight/jobs")
    CreateJobResponse createJob(@RequestBody CreateInsightJobRequest request, @RequestHeader("X-User-Id") String userId);

    @GetMapping("/api/v1/insight/jobs/{jobId}")
    InsightJobStatusResponse getJobStatus(@PathVariable("jobId") String jobId, @RequestHeader("X-User-Id") String userId);
}
