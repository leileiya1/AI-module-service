package com.example.dto.AI;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class JobStatusResponse {
    private String jobId;
    private JobStatus status;
    private String errorMessage;
    private JsonNode result; // 直接返回解析好的JSON，方便前端使用
}
