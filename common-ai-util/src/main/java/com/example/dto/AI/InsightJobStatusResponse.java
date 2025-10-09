package com.example.dto.AI;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class InsightJobStatusResponse {
    private String jobId;
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    private String errorMessage;
    private JsonNode result;
}
