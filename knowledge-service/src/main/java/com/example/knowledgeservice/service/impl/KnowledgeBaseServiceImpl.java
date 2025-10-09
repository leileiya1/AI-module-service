package com.example.knowledgeservice.service.impl;

import com.example.dto.AI.*;
import com.example.exception.ResourceNotFoundException;
import com.example.knowledgeservice.entity.DocumentChunk;
import com.example.entity.ai.DocumentStatus;
import com.example.entity.ai.KnowledgeBase;
import com.example.entity.ai.KnowledgeDocument;
import com.example.knowledgeservice.mapper.DocumentChunkMapper;
import com.example.knowledgeservice.mapper.KnowledgeBaseMapper;
import com.example.knowledgeservice.mapper.KnowledgeDocumentMapper;
import com.example.knowledgeservice.service.KnowledgeBaseService;
import com.pgvector.PGvector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final KnowledgeDocumentMapper knowledgeDocumentMapper;
    private final DocumentChunkMapper documentChunkMapper;
    private final EmbeddingModel embeddingModel;
    private final AsyncDocumentProcessor asyncProcessor;

    @Override
    @Transactional
    public KnowledgeBase createKnowledgeBase(CreateKbRequest request, String userId) {
        log.info("【知识库服务】用户 '{}' 正在创建新知识库，名称: '{}'", userId, request.getName());
        KnowledgeBase kb = new KnowledgeBase();
        kb.setName(request.getName());
        kb.setDescription(request.getDescription());
        kb.setUserId(userId);
        kb.setCreateTime(OffsetDateTime.now());
        knowledgeBaseMapper.insert(kb);
        log.info("【知识库服务】知识库 '{}' 创建成功，ID: {}", kb.getName(), kb.getId());
        return kb;
    }

    @Override
    @Transactional
    public UploadResponse uploadDocumentAndStartProcessing(Long kbId, String userId, MultipartFile file) throws IOException {
        // 1. 权限校验：检查知识库是否存在且属于该用户
        KnowledgeBase kb = knowledgeBaseMapper.selectById(kbId);
        if (kb == null || !kb.getUserId().equals(userId)) {
            throw new SecurityException("无权操作此知识库");
        }

        // 2. 创建文档记录，初始状态为 PROCESSING
        log.info("【知识库服务】[KB_ID:{}] 开始处理上传文件 '{}'", kbId, file.getOriginalFilename());
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setKbId(kbId);
        doc.setFileName(file.getOriginalFilename());
        doc.setFileSize(file.getSize());
        doc.setStatus(DocumentStatus.PROCESSING);
        doc.setUpdateTime(OffsetDateTime.now());
        knowledgeDocumentMapper.insert(doc);
        log.info("【知识库服务】[KB_ID:{}] 文件 '{}' 记录已创建，ID: {}，状态: PROCESSING", kbId, doc.getFileName(), doc.getId());

        // 3. 【核心改动】在HTTP请求结束前，立即将文件内容读入内存
        byte[] fileContent = file.getBytes();
        log.debug("【知识库服务】[DOC_ID:{}] 文件内容已成功读取到内存，大小: {} bytes", doc.getId(), fileContent.length);

        asyncProcessor.processDocument(doc.getId(), fileContent);
        // 4. 立即返回文档ID，让客户端可以轮询状态
        return new UploadResponse(doc.getId(), "文件上传成功，已开始后台处理。");
    }




    @Override
    public DocumentStatusResponse getDocumentStatus(Long docId, String userId) {
        KnowledgeDocument doc = knowledgeDocumentMapper.selectById(docId);
        // 【新增健壮性】处理查询的 docId 不存在的情况，修复您日志中的 NullPointerException
        if (doc == null) {
            log.warn("【知识库服务】尝试查询不存在的文档状态, DOC_ID: {}", docId);
            throw new ResourceNotFoundException("ID为 " + docId + " 的文档未找到。");
        }

        KnowledgeBase kb = knowledgeBaseMapper.selectById(doc.getKbId());
        if (kb == null || !kb.getUserId().equals(userId)) {
            throw new SecurityException("无权查看此文档状态");
        }
        return DocumentStatusResponse.builder()
                .documentId(doc.getId())
                .status(doc.getStatus())
                .errorMessage(doc.getErrorMessage())
                .build();
    }

    @Override
    public List<SearchResult> search(Long kbId, SearchRequest request, String userId) {
        log.info("【知识库服务】[KB_ID:{}] 开始语义搜索, TopK: {}", kbId, request.getTopK());
        // 1. 向量化查询文本
        float[] targetVector = embeddingModel.embed(request.getQuery());

        // 2. 调用 Mapper 进行搜索
        List<DocumentChunk> results = documentChunkMapper.searchSimilarChunks(kbId, new PGvector(targetVector), request.getTopK());

        // 3. 转换为 DTO 返回
        return results.stream()
                .map(chunk -> new SearchResult(chunk.getContent(), chunk.getDocId(), chunk.getDistance()))
                .collect(Collectors.toList());
    }
}
