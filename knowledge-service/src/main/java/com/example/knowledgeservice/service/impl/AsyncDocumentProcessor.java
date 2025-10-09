package com.example.knowledgeservice.service.impl;

import com.example.entity.ai.DocumentStatus;
import com.example.entity.ai.KnowledgeDocument;
import com.example.knowledgeservice.component.DocumentProcessor;
import com.example.knowledgeservice.entity.DocumentChunk;
import com.example.knowledgeservice.mapper.DocumentChunkMapper;
import com.example.knowledgeservice.mapper.KnowledgeDocumentMapper;
import com.pgvector.PGvector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 专门负责异步处理文档的组件。
 * 将 @Async 方法放在这里可以确保Spring AOP代理生效。
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AsyncDocumentProcessor {

    // 这个类需要它自己的依赖
    private final KnowledgeDocumentMapper knowledgeDocumentMapper;
    private final DocumentChunkMapper documentChunkMapper;
    private final DocumentProcessor documentProcessor;
    private final EmbeddingModel embeddingModel;

    /**
     * 异步处理文档的核心方法。
     * 它现在位于独立的组件中，可以被主服务安全调用。
     * @param docId       文档ID
     * @param fileContent 文件的完整字节内容
     */
    @Async // 这个注解现在会100%生效
    @Transactional // 建议为长任务也加上事务，确保数据一致性
    public void processDocument(Long docId, byte[] fileContent) {
        log.info("【异步任务-新线程】[DOC_ID:{}] 开始处理...", docId);
        try {
            InputStream inputStream = new ByteArrayInputStream(fileContent);
            List<String> chunks = documentProcessor.parseAndChunk(inputStream);
            if (chunks.isEmpty()) {
                log.warn("【异步任务-新线程】[DOC_ID:{}] 文档解析后内容为空，处理终止。", docId);
                updateDocumentStatus(docId, DocumentStatus.FAILED, "文档内容为空或格式不支持");
                return;
            }

            log.info("【异步任务-新线程】[DOC_ID:{}] 准备向量化 {} 个文本片段...", docId, chunks.size());
            KnowledgeDocument doc = knowledgeDocumentMapper.selectById(docId);
            List<DocumentChunk> chunkEntities = chunks.stream().map(content -> {
                float[] embedding = embeddingModel.embed(content);
                DocumentChunk chunk = new DocumentChunk();
                chunk.setDocId(docId);
                chunk.setKbId(doc.getKbId());
                chunk.setContent(content);
                chunk.setEmbedding(new PGvector(embedding));
                return chunk;
            }).collect(Collectors.toList());

            log.info("【异步任务-新线程】[DOC_ID:{}] 向量化完成，准备批量插入数据库...", docId);
            chunkEntities.forEach(documentChunkMapper::insert);
            log.info("【异步任务-新线程】[DOC_ID:{}] 所有片段已成功存入数据库。", docId);

            updateDocumentStatus(docId, DocumentStatus.COMPLETED, null);

        } catch (Exception e) {
            log.error("【异步任务-新线程】[DOC_ID:{}] 处理失败！", docId, e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "未知错误";
            updateDocumentStatus(docId, DocumentStatus.FAILED, errorMessage.substring(0, Math.min(500, errorMessage.length())));
        }
    }

    private void updateDocumentStatus(Long docId, DocumentStatus status, String errorMessage) {
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setId(docId);
        doc.setStatus(status);
        doc.setErrorMessage(errorMessage);
        doc.setUpdateTime(OffsetDateTime.now());
        knowledgeDocumentMapper.updateById(doc);
        log.info("【异步任务-新线程】[DOC_ID:{}] 状态已更新为: {}", docId, status);
    }
}
