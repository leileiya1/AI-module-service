package com.example.finetuningmanagerservice.client.dto;

import lombok.Data;

@Data
public class FeedbackRecord {
    private String sourceContext;
    private String aiResponse;
    private String feedbackType;
    private String editedContent;
}
