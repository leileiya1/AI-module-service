package com.example.dto.AI;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {
    private String content;
    private Long documentId;
    private double distance;
}
