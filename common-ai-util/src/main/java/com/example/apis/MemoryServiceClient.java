package com.example.apis;

import com.example.dto.AI.AddMemoryRequest;
import com.example.dto.AI.InsightResponse;
import com.example.dto.AI.SearchMemoryRequest;
import com.example.dto.AI.SearchMemoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "memory-service", url = "http://localhost:1236", path = "/api/v1/memory")
public interface MemoryServiceClient {

    @PostMapping("/{userId}/{personaId}")
    void addMemory(@PathVariable("userId") String userId, @PathVariable("personaId") String personaId, @RequestBody AddMemoryRequest request);

    @PostMapping("/{userId}/{personaId}/search")
    List<SearchMemoryResponse> searchMemory(@PathVariable("userId") String userId, @PathVariable("personaId") String personaId, @RequestBody SearchMemoryRequest request);

    @GetMapping("/{userId}/{personaId}/insights")
    InsightResponse getInsights(@PathVariable("userId") String userId, @PathVariable("personaId") String personaId);
}
