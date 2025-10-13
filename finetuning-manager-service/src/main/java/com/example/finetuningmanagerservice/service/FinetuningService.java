package com.example.finetuningmanagerservice.service;


import com.example.dto.AI.CreateJobResponse;
import com.example.dto.AI.JobStatus;
import com.example.entity.ai.FinetuningJob;
import com.example.finetuningmanagerservice.dto.CreateJobRequest;
import com.example.finetuningmanagerservice.mapper.FinetuningJobMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FinetuningService {

    private final FinetuningJobMapper jobMapper;
    private final FinetuningProcessor processor;

    public CreateJobResponse createJob(CreateJobRequest request) {
        String jobId = UUID.randomUUID().toString();
        log.info("【微调服务】收到用户 '{}' 对人格 '{}' 的微调请求，创建JobID: {}",
                request.getUserId(), request.getPersonaId(), jobId);

        FinetuningJob job = new FinetuningJob();
        job.setJobId(jobId);
        job.setUserId(request.getUserId());
        job.setPersonaId(request.getPersonaId());
        job.setBaseModel(request.getBaseModel());
        job.setStatus(JobStatus.PENDING);
        jobMapper.insert(job);
        processor.process(jobId);
        return new CreateJobResponse(jobId, "微调任务已成功提交，正在后台处理。");
    }
}
