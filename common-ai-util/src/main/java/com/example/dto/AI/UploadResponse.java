package com.example.dto.AI;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadResponse {
    private Long documentId;
    private String message;
}
