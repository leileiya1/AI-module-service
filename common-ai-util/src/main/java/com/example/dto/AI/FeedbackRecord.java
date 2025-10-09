package com.example.dto.AI;

import lombok.Data;

@Data
public class FeedbackRecord {
    private String sourceContext;
    private String aiResponse;
    private String feedbackType;
    private String editedContent;
}
