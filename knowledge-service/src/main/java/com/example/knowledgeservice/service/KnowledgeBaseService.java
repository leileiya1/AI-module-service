package com.example.knowledgeservice.service;

import com.example.dto.AI.*;
import com.example.entity.ai.KnowledgeBase;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface KnowledgeBaseService {
    KnowledgeBase createKnowledgeBase(CreateKbRequest request, String userId);

    UploadResponse uploadDocumentAndStartProcessing(Long kbId, String userId, MultipartFile file) throws Exception;

    DocumentStatusResponse getDocumentStatus(Long docId, String userId);

    List<SearchResult> search(Long kbId, SearchRequest request, String userId);
}
