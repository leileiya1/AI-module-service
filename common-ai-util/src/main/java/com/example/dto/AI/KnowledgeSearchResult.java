package com.example.dto.AI;

import lombok.Data;
@Data
public class KnowledgeSearchResult {
    private String content;
    private Long documentId;
    private double distance;
}
