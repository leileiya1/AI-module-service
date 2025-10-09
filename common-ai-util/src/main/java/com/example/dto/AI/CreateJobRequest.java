package com.example.dto.AI;

import lombok.Data;

@Data
public class CreateJobRequest {
    private String type; // "TEXT" or "URL"
    private String content; // 文本内容或URL链接
}
