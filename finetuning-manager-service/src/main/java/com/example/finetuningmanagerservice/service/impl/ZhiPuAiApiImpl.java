package com.example.finetuningmanagerservice.service.impl;

import com.example.finetuningmanagerservice.service.ZhiPuAiApi;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * 智谱AI微调API实现类
 * 基于智谱AI官方文档: https://open.bigmodel.cn/dev/api/fine-tuning
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ZhiPuAiApiImpl implements ZhiPuAiApi {

    private static final String BASE_URL = "https://open.bigmodel.cn/api/paas/v4";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${zhipu.ai.api-key}")
    private String apiKey;

    @Override
    public FileUploadResponse uploadFile(Resource file, String purpose) {
        log.info("【智谱AI】开始上传训练文件，文件名: {}", file.getFilename());

        String url = BASE_URL + "/files";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(apiKey);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file);
        body.add("purpose", purpose);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<FileUploadResponse> response = restTemplate.postForEntity(
                url, requestEntity, FileUploadResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("【智谱AI】文件上传成功，文件ID: {}", response.getBody().id());
                return response.getBody();
            }

            throw new RuntimeException("文件上传失败: " + response.getStatusCode());
        } catch (Exception e) {
            log.error("【智谱AI】文件上传失败", e);
            throw new RuntimeException("智谱AI文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public FineTuningJobResponse createFineTuningJob(FineTuningJobRequest request) {
        log.info("【智谱AI】创建微调任务，模型: {}, 训练文件: {}", request.model(), request.trainingFile());

        String url = BASE_URL + "/fine_tuning/jobs";
        HttpHeaders headers = createJsonHeaders();

        try {
            // 构建请求体
            CreateJobRequestBody requestBody = new CreateJobRequestBody(
                request.model(),
                request.trainingFile(),
                new HyperParameters(request.epochs(), request.learningRateMultiplier())
            );

            HttpEntity<CreateJobRequestBody> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<FineTuningJobResponse> response = restTemplate.postForEntity(
                url, requestEntity, FineTuningJobResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("【智谱AI】微调任务创建成功，任务ID: {}", response.getBody().id());
                return response.getBody();
            }

            throw new RuntimeException("微调任务创建失败: " + response.getStatusCode());
        } catch (Exception e) {
            log.error("【智谱AI】创建微调任务失败", e);
            throw new RuntimeException("智谱AI创建微调任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public FineTuningJobResponse retrieveFineTuningJob(String jobId) {
        log.debug("【智谱AI】查询微调任务状态，任务ID: {}", jobId);

        String url = BASE_URL + "/fine_tuning/jobs/" + jobId;
        HttpHeaders headers = createJsonHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<FineTuningJobResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, requestEntity, FineTuningJobResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }

            throw new RuntimeException("查询微调任务失败: " + response.getStatusCode());
        } catch (Exception e) {
            log.error("【智谱AI】查询微调任务失败", e);
            throw new RuntimeException("智谱AI查询微调任务失败: " + e.getMessage(), e);
        }
    }

    private HttpHeaders createJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        return headers;
    }

    /**
     * 创建微调任务的请求体
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record CreateJobRequestBody(
            String model,
            @JsonProperty("training_file") String trainingFile,
            @JsonProperty("hyperparameters") HyperParameters hyperparameters
    ) {}

    /**
     * 超参数配置
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record HyperParameters(
            @JsonProperty("n_epochs") Integer nEpochs,
            @JsonProperty("learning_rate_multiplier") Double learningRateMultiplier
    ) {}
}
