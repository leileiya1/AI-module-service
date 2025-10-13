package com.example.apis;

import com.example.dto.AI.CreateFinetuningJobRequest;
import com.example.dto.AI.CreateFinetuningJobResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "finetuning-manager-service", url = "http://localhost:1241", path = "/api/v1")
public interface FinetuningManagerClient {

    @PostMapping("/finetuning/jobs")
    CreateFinetuningJobResponse createJob(@RequestBody CreateFinetuningJobRequest request);
}
