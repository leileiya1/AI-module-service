package com.example.personainsightservice.service.impl;

import com.example.dto.AI.*;
import com.example.personainsightservice.entity.StyleAnalysisJob;
import com.example.personainsightservice.mapper.StyleAnalysisJobMapper;
import com.example.personainsightservice.service.AsyncStyleAnalyzer;
import com.example.personainsightservice.service.InsightService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class InsightServiceImpl implements InsightService {

    private final StyleAnalysisJobMapper jobMapper;
    private final AsyncStyleAnalyzer asyncAnalyzer;
    private final ObjectMapper objectMapper;

    @Override
    public CreateJobResponse createJob(CreateJobRequest request, String userId) {
        String jobId = UUID.randomUUID().toString();
        log.info("【洞察服务】用户 '{}' 正在创建新的分析任务, JobID: {}", userId, jobId);

        StyleAnalysisJob job = new StyleAnalysisJob();
        job.setJobId(jobId);
        job.setUserId(userId);
        job.setStatus(JobStatus.PENDING);
        job.setSourceType(SourceType.valueOf(request.getType().toUpperCase()));
        job.setSourceContent(request.getContent());

        jobMapper.insert(job);
        log.info("【洞察服务】[JOB_ID:{}] 任务已创建并存入数据库，状态: PENDING", jobId);

        // 触发异步分析
        asyncAnalyzer.analyze(jobId);
        // 立即更新状态为 PROCESSING
        job.setStatus(JobStatus.PROCESSING);
        jobMapper.updateById(job);

        return new CreateJobResponse(jobId, "任务已提交，正在后台分析中。");
    }

    @Override
    public JobStatusResponse getJobStatus(String jobId, String userId) {
        StyleAnalysisJob job = jobMapper.findByJobId(jobId)
                .orElseThrow(() -> new RuntimeException("任务不存在: " + jobId));

        // 权限校验
        if (!job.getUserId().equals(userId)) {
            throw new SecurityException("无权查看此任务状态");
        }

        JsonNode resultNode = null;
        if (job.getResultJson() != null) {
            try {
                resultNode = objectMapper.readTree(job.getResultJson());
            } catch (Exception e) {
                log.error("解析结果JSON失败", e);
            }
        }

        return JobStatusResponse.builder()
                .jobId(job.getJobId())
                .status(job.getStatus())
                .errorMessage(job.getErrorMessage())
                .result(resultNode)
                .build();
    }
}
