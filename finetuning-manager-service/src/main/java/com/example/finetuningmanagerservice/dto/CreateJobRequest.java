package com.example.finetuningmanagerservice.dto;

import lombok.Data;

@Data
public class CreateJobRequest {
    private String userId;
    private String personaId;
    private String baseModel = "glm-4-0520"; // 默认使用glm-4作为微调基础模型
}
