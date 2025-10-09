package com.example.apis;

import com.example.entity.ai.FeedbackRecord;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "feedback-service", url = "http://localhost:1240", path = "/api/v1")
public interface FeedbackServiceClient {
    // 假设 feedback-service 提供了这个接口来获取高质量数据
    @GetMapping("/feedback/export")
    List<FeedbackRecord> getGoodFeedbackFor(@RequestParam("userId") String userId, @RequestParam("personaId") String personaId);
}
