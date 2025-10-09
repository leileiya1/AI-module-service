package com.example.dto.AI;

import lombok.Data;
@Data
public class SearchRequest {
    private String query;
    private int topK = 3;
}
