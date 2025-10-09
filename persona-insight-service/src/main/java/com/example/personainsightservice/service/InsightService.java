package com.example.personainsightservice.service;

import com.example.dto.AI.CreateJobRequest;
import com.example.dto.AI.CreateJobResponse;
import com.example.dto.AI.JobStatusResponse;

public interface InsightService {
    CreateJobResponse createJob(CreateJobRequest request, String userId);

    JobStatusResponse getJobStatus(String jobId, String userId);
}
