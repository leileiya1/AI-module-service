package com.example.dto.AI;

import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class CreateInsightJobRequest {
    private String type;
    private String content;
}
