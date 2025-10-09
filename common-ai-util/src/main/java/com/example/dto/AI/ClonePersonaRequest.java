package com.example.dto.AI;

import lombok.Data;

@Data
public class ClonePersonaRequest {
    private String type; // "TEXT" or "URL"
    private String content;
}
