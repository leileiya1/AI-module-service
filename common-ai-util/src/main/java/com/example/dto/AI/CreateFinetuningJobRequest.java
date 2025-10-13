package com.example.dto.AI;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateFinetuningJobRequest {
    private String userId;
    private String personaId;
}
