package com.example.dto.AI;

import lombok.Data;

import java.util.List;

@Data
public class InsightResponse {
    private List<String> themes; // 用户的长期主题/目标/兴趣点
}