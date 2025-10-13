package com.example.finetuningmanagerservice.service;

import com.example.apis.AiChatServiceClient;
import com.example.apis.FeedbackServiceClient;
import com.example.dto.AI.JobStatus;
import com.example.entity.ai.FeedbackRecord;
import com.example.entity.ai.FinetuningJob;
import com.example.finetuningmanagerservice.mapper.FinetuningJobMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class FinetuningProcessor {

    private final FinetuningJobMapper jobMapper;
    private final FeedbackServiceClient feedbackServiceClient;
    private final AiChatServiceClient aiChatServiceClient;
    private final ZhiPuAiApi zhipuAiApi;

    private final ObjectMapper objectMapper;
    private static final int MIN_RECORDS_FOR_FINETUNING = 10;

    @Async
    @Transactional
    public void process(String jobId) {
        log.info("【微调处理器】[JOB_ID:{}] 开始执行微调流程...", jobId);
        FinetuningJob job = jobMapper.findByJobId(jobId).orElse(null);
        if (job == null) {
            log.error("【微调处理器】[JOB_ID:{}] 任务不存在，终止执行。", jobId);
            return;
        }
        try {
            updateJobStatus(job, JobStatus.PREPARING_DATA, null);
            log.info("【微调处理器】[JOB_ID:{}] 正在从feedback-service拉取高质量反馈数据...", jobId);
            List<FeedbackRecord> records = feedbackServiceClient.getGoodFeedbackFor(job.getUserId(), job.getPersonaId());
            if (records == null || records.size() < MIN_RECORDS_FOR_FINETUNING) {
                throw new RuntimeException("高质量反馈数据不足" + MIN_RECORDS_FOR_FINETUNING + "条。");
            }
            log.info("【微调处理器】[JOB_ID:{}] 成功拉取 {} 条反馈数据。", jobId, records.size());

            String jsonlContent = formatDataForZhipu(records);
            ByteArrayResource trainingFileResource = new ByteArrayResource(jsonlContent.getBytes(StandardCharsets.UTF_8)) {
                @Override
                public String getFilename() {
                    return "training_data.jsonl";
                }
            };

            updateJobStatus(job, JobStatus.UPLOADING_FILE, null);
            log.info("【微调处理器】[JOB_ID:{}] 正在上传格式化后的训练文件...", jobId);
            ZhiPuAiApi.FileUploadResponse fileResponse = zhipuAiApi.uploadFile(trainingFileResource, "fine-tune");
            String fileId = fileResponse.id();
            job.setTrainingFileId(fileId);
            jobMapper.updateById(job);
            log.info("【微调处理器】[JOB_ID:{}] 文件上传成功，FileID: {}", jobId, fileId);

            updateJobStatus(job, JobStatus.RUNNING, null);
            log.info("【微调处理器】[JOB_ID:{}] 正在启动微调任务...", jobId);
            var jobRequest = new ZhiPuAiApi.FineTuningJobRequest(fileId, job.getBaseModel());
            var fineTuningJob = zhipuAiApi.createFineTuningJob(jobRequest);
            String providerJobId = fineTuningJob.id();
            job.setProviderJobId(providerJobId);
            jobMapper.updateById(job);
            log.info("【微调处理器】[JOB_ID:{}] 微调任务已在AI平台启动，ProviderJobID: {}", jobId, providerJobId);

            final int MAX_POLLING_ATTEMPTS = 120;
            for (int i = 0; i < MAX_POLLING_ATTEMPTS; i++) {
                Thread.sleep(60000);
                log.debug("【微调处理器】[JOB_ID:{}] 轮询任务状态 ({}/{})", jobId, i + 1, MAX_POLLING_ATTEMPTS);
                var currentJob = zhipuAiApi.retrieveFineTuningJob(providerJobId);
                String currentStatus = currentJob.status();
                log.info("【微调处理器】[JOB_ID:{}] 当前AI平台任务状态: {}", jobId, currentStatus);

                if ("succeeded".equalsIgnoreCase(currentStatus)) {
                    String newModelId = currentJob.fineTunedModel();
                    log.info("【微调处理器】[JOB_ID:{}] 微调成功！新的专属模型ID为: {}", jobId, newModelId);

                    Map<String, String> payload = new HashMap<>();
                    payload.put("modelId", newModelId);
                    aiChatServiceClient.updatePersonaModel(job.getPersonaId(), payload);
                    log.info("【微调处理器】[JOB_ID:{}] 已成功回调ai-chat-service更新人格模型。", jobId);

                    updateJobStatus(job, JobStatus.COMPLETED, newModelId);
                    return;
                }
                if ("failed".equalsIgnoreCase(currentStatus) || "cancelled".equalsIgnoreCase(currentStatus)) {
                    String errorMessage = (currentJob.error() != null && StringUtils.hasText(currentJob.error().message()))
                            ? currentJob.error().message() : "智谱AI微调任务失败或被取消。";
                    throw new RuntimeException(errorMessage);
                }
            }
            throw new RuntimeException("微调任务轮询超时（超过2小时）。");

        } catch (Exception e) {
            log.error("【微调处理器】[JOB_ID:{}] 微调流程发生严重错误！", jobId, e);
            updateJobStatus(job, JobStatus.FAILED, e.getMessage());
        }
    }

    private String formatDataForZhipu(List<FeedbackRecord> records) {
        return records.stream()
                .filter(r -> "EDITED".equals(r.getFeedbackType()) && StringUtils.hasText(r.getEditedContent()))
                .map(r -> {
                    Map<String, String> conversation = new HashMap<>();
                    conversation.put("prompt", r.getSourceContext());
                    conversation.put("completion", r.getEditedContent());
                    try {
                        return objectMapper.writeValueAsString(conversation);
                    } catch (JsonProcessingException e) {
                        log.error("【数据格式化】序列化单条微调数据失败！", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));
    }

    private void updateJobStatus(FinetuningJob job, JobStatus status, String message) {
        job.setStatus(status);
        if (status == JobStatus.COMPLETED) {
            job.setFineTunedModelId(message);
        } else if (status == JobStatus.FAILED) {
            String safeMessage = message != null ? message.substring(0, Math.min(message.length(), 2000)) : "未知错误";
            job.setErrorMessage(safeMessage);
        }
        job.setUpdateTime(OffsetDateTime.now());
        jobMapper.updateById(job);
    }
}