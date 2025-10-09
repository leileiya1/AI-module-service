package com.example.dto.AI;

import lombok.Data;

import java.util.Map;
@Data
public class StyleAnalysisResult {
    private String name;
    private String role;
    private String tone;
    private String style;
    private Map<String, String> commentTemplates;
}
