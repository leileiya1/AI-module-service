package com.example.dto.AI;

import com.example.entity.ai.DocumentStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentStatusResponse {
    private Long documentId;
    private DocumentStatus status;
    private String errorMessage;
}
