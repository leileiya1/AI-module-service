package com.example.dto.AI;

import lombok.Data;

@Data
public class KnowledgeSearchRequest {
    private String query;
    private int topK = 2; // 默认从知识库中检索2条最相关的
}