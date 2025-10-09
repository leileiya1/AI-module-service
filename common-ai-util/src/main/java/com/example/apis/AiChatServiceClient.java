package com.example.apis;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "ai-chat-service", url = "http://localhost:1235", path = "/api/v1")
public interface AiChatServiceClient {
    // ai-chat-service 需要提供这个内部回调接口
    @PutMapping("/internal/personas/{personaId}/model")
    void updatePersonaModel(@PathVariable String personaId, @RequestBody Map<String, String> payload);
}
