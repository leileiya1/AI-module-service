package com.example.finetuningmanagerservice.service;

import org.springframework.core.io.Resource;

/**
 * 智谱AI微调API接口
 * 文档: <a href="https://open.bigmodel.cn/dev/api/fine-tuning">...</a>
 */
public interface ZhiPuAiApi {

    /**
     * 上传微调训练文件
     * @param file JSONL格式的训练文件
     * @param purpose 固定为 "fine-tune"
     * @return 文件上传响应
     */
    FileUploadResponse uploadFile(Resource file, String purpose);

    /**
     * 创建微调任务
     * @param request 微调任务请求
     * @return 微调任务响应
     */
    FineTuningJobResponse createFineTuningJob(FineTuningJobRequest request);

    /**
     * 查询微调任务详情
     * @param jobId 任务ID
     * @return 微调任务响应
     */
    FineTuningJobResponse retrieveFineTuningJob(String jobId);

    /**
     * 文件上传响应
     */
    record FileUploadResponse(
            String id,
            String object,
            Long bytes,
            Long createdAt,
            String filename,
            String purpose
    ) {}

    /**
     * 微调任务请求
     */
    record FineTuningJobRequest(
            String trainingFile,
            String model,
            Integer epochs,
            Double learningRateMultiplier
    ) {
        // 使用默认参数的构造器
        public FineTuningJobRequest(String trainingFile, String model) {
            this(trainingFile, model, null, null);
        }
    }

    /**
     * 微调任务响应
     */
    record FineTuningJobResponse(
            String id,
            String object,
            String model,
            Long createdAt,
            Long finishedAt,
            String fineTunedModel,
            String status,
            ErrorInfo error,
            String trainingFile
    ) {}

    /**
     * 错误信息
     */
    record ErrorInfo(
            String message,
            String code
    ) {}
}
