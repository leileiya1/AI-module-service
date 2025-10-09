package com.example.apis;

import com.example.dto.AI.KnowledgeSearchRequest;
import com.example.dto.AI.KnowledgeSearchResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "knowledge-service", url = "http://localhost:1237")
public interface KnowledgeServiceClient {

    @PostMapping("/api/v1/knowledge-bases/{kbId}/search")
    List<KnowledgeSearchResult> searchKnowledge(@PathVariable("kbId") Long kbId, @RequestBody KnowledgeSearchRequest request, @RequestHeader("X-User-Id") String userId);
}