package com.example.knowledgeservice.controller;

import com.example.dto.AI.*;
import com.example.entity.ai.KnowledgeBase;
import com.example.knowledgeservice.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/knowledge-bases")
@Slf4j
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    @PostMapping
    public ResponseEntity<KnowledgeBase> createKnowledgeBase(
            @RequestBody CreateKbRequest request,
            @RequestHeader("X-User-Id") String userId) {
        log.info("【API入口】收到创建知识库请求, UserID: {}", userId);
        KnowledgeBase kb = knowledgeBaseService.createKnowledgeBase(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(kb);
    }

    @PostMapping("/{kbId}/documents")
    public ResponseEntity<UploadResponse> uploadDocument(
            @PathVariable("kbId") Long kbId,
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("file") MultipartFile file) throws Exception {
        log.info("【API入口】收到文档上传请求, KB_ID: {}, FileName: {}", kbId, file.getOriginalFilename());
        UploadResponse response = knowledgeBaseService.uploadDocumentAndStartProcessing(kbId, userId, file);
        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/documents/{docId}/status")
    public ResponseEntity<DocumentStatusResponse> getDocumentStatus(
            @PathVariable("docId") Long docId,
            @RequestHeader("X-User-Id") String userId) {
        log.info("【API入口】收到查询文档状态请求, DOC_ID: {}", docId);
        DocumentStatusResponse status = knowledgeBaseService.getDocumentStatus(docId, userId);
        return ResponseEntity.ok(status);
    }

    @PostMapping("/{kbId}/search")
    public ResponseEntity<List<SearchResult>> search(
            @PathVariable("kbId") Long kbId,
            @RequestBody SearchRequest request,
            @RequestHeader("X-User-Id") String userId) {
        log.info("【API入口】收到知识库搜索请求, KB_ID: {}", kbId);
        List<SearchResult> results = knowledgeBaseService.search(kbId, request, userId);
        return ResponseEntity.ok(results);
    }
}
