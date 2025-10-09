package com.example.dto.AI;

import lombok.Data;

@Data
public class FeedbackRequest {
    private String personaId;
    private String sourceContext;
    private String aiResponse;
    private String feedbackType; // "LIKED", "DISLIKED", or "EDITED"
    private String editedContent; // 仅在 feedbackType 为 "EDITED" 时需要
}
