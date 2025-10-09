package com.example.memoryservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dto.AI.*;
import com.example.entity.ai.UserPersonaInsight;
import com.example.memoryservice.entity.Memory;
import com.example.memoryservice.mapper.MemoryMapper;
import com.example.memoryservice.mapper.UserPersonaInsightMapper;
import com.example.memoryservice.service.MemoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgvector.PGvector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 记忆服务业务逻辑的 MyBatis-Plus 实现类。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MemoryServiceImpl implements MemoryService {

    // 注入 Spring AI 的 EmbeddingModel，用于将文本转换为向量
    private final EmbeddingModel embeddingModel;
    // 注入 MyBatis-Plus 的 Mapper 接口，用于数据库操作
    private final MemoryMapper memoryMapper;
    private final UserPersonaInsightMapper insightMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void addMemory(String userId, String personaId, AddMemoryRequest request) {
        log.info("【记忆服务】[用户:{}, 人格:{}] 开始处理添加记忆请求。", userId, personaId);

        if (request == null || !StringUtils.hasText(request.getContent())) {
            log.warn("【记忆服务】[用户:{}, 人格:{}] 添加记忆失败：请求内容为空。", userId, personaId);
            throw new IllegalArgumentException("记忆内容不能为空");
        }

        // 1. 文本向量化
        PGvector embedding;
        try {
            // 【已更正】调用 generateEmbedding 方法，它现在直接处理 float[]
            embedding = generateEmbedding(request.getContent());
            log.info("【记忆服务】[用户:{}, 人格:{}] 文本向量化成功，向量维度: {}", userId, personaId, embedding.toArray().length);
        } catch (Exception e) {
            log.error("【记忆服务】[用户:{}, 人格:{}] 文本向量化失败！请检查AI服务配置、API Key和网络连接。", userId, personaId, e);
            throw new RuntimeException("AI模型调用失败，无法生成记忆向量。", e);
        }

        // 2. 构建实体对象
        Memory memory = new Memory();
        memory.setUserId(userId);
        memory.setPersonaId(personaId);
        memory.setContent(request.getContent());
        memory.setEmbedding(embedding);

        // 3. 存入数据库
        int insertedRows = memoryMapper.insert(memory);
        if (insertedRows <= 0) {
            log.error("【记忆服务】[用户:{}, 人格:{}] 记忆存入数据库失败！", userId, personaId);
            throw new RuntimeException("记忆数据写入数据库失败。");
        }
        log.info("【记忆服务】[用户:{}, 人格:{}] 新记忆已成功存入数据库，ID: {}", userId, personaId, memory.getId());
    }

    @Override
    public List<SearchMemoryResponse> searchMemory(String userId, String personaId, SearchMemoryRequest request) {
        // 【核心修复】在方法的最开始就进行防御性检查
        if (request == null || !StringUtils.hasText(request.getQuery())) {
            log.warn("【记忆服务-高级搜索】[用户:{}, 人格:{}] 搜索失败：查询内容为空。", userId, personaId);
            // 直接返回空列表，而不是继续执行导致空指针
            return Collections.emptyList();
        }
        log.info("【记忆服务】[用户:{}, 人格:{}] 开始处理搜索记忆请求, 查询: '{}...', TopK: {}",
                userId, personaId, request.getQuery().substring(0, Math.min(30, request.getQuery().length())), request.getTopK());

        if (!StringUtils.hasText(request.getQuery())) {
            log.warn("【记忆服务】[用户:{}, 人格:{}] 搜索记忆失败：查询内容为空。", userId, personaId);
            return Collections.emptyList();
        }

        // 1. 将查询文本向量化
        PGvector targetVector;
        try {
            // 【已更正】调用 generateEmbedding 方法，它现在直接处理 float[]
            targetVector = generateEmbedding(request.getQuery());
            log.info("【记忆服务】[用户:{}, 人格:{}] 查询文本向量化成功。", userId, personaId);
        } catch (Exception e) {
            log.error("【记忆服务】[用户:{}, 人格:{}] 查询文本向量化失败！请检查AI服务配置、API Key和网络连接。", userId, personaId, e);
            throw new RuntimeException("AI模型调用失败，无法生成查询向量。", e);
        }

        // 2. 调用 Mapper 的自定义 SQL 方法进行相似度搜索
        log.debug("【记忆服务】[用户:{}, 人格:{}] 正在数据库中执行向量相似度搜索...", userId, personaId);
        List<Memory> searchResults = memoryMapper.searchSimilarMemoriesWithPinnedPriority(userId, personaId, targetVector, request.getTopK());

        log.info("【记忆服务】[用户:{}, 人格:{}] 搜索完成，找到 {} 条相关记忆。", userId, personaId, searchResults.size());

        // 3. 将查询结果转换为 DTO 列表返回
        return searchResults.stream()
                .map(memory -> new SearchMemoryResponse(memory.getContent(), memory.getDistance()))
                .collect(Collectors.toList());
    }

    @Override
    public InsightResponse getInsights(String userId, String personaId) {
        log.info("【洞察服务】查询用户 '{}', 人格 '{}' 的洞察...", userId, personaId);
        UserPersonaInsight insight = insightMapper.selectOne(
                new LambdaQueryWrapper<UserPersonaInsight>()
                        .eq(UserPersonaInsight::getUserId, userId)
                        .eq(UserPersonaInsight::getPersonaId, personaId)
        );

        if (insight == null || !StringUtils.hasText(insight.getInsightsJson())) {
            log.info("【洞察服务】未找到用户 '{}', 人格 '{}' 的洞察。", userId, personaId);
            return null;
        }

        try {
            InsightAnalysisResult result = objectMapper.readValue(insight.getInsightsJson(), InsightAnalysisResult.class);
            InsightResponse response = new InsightResponse();
            response.setThemes(result.getThemes());
            log.info("【洞察服务】成功获取用户 '{}' 的洞察: {}", userId, response.getThemes());
            return response;
        } catch (Exception e) {
            log.error("【洞察服务】解析洞察JSON失败 for user '{}'", userId, e);
            return null;
        }
    }

    @Override
    public Page<MemoryDto> listMemories(String userId, String personaId, Page<Memory> pageRequest) {
        log.info("【记忆管理】用户 '{}' 正在分页查询人格 '{}' 的记忆列表, 页码: {}, 大小: {}",
                userId, personaId, pageRequest.getCurrent(), pageRequest.getSize());

        Page<Memory> memoryPage = memoryMapper.selectPage(pageRequest,
                new LambdaQueryWrapper<Memory>()
                        .eq(Memory::getUserId, userId)
                        .eq(Memory::getPersonaId, personaId)
                        .orderByDesc(Memory::getPinned, Memory::getCreateTime) // 置顶的优先显示，然后按创建时间倒序
        );

        // 将 Page<Memory> 转换为 Page<MemoryDto>
        Page<MemoryDto> dtoPage = new Page<>(memoryPage.getCurrent(), memoryPage.getSize(), memoryPage.getTotal());
        List<MemoryDto> dtoList = memoryPage.getRecords().stream().map(this::convertToDto).collect(Collectors.toList());
        dtoPage.setRecords(dtoList);

        log.info("【记忆管理】查询成功，返回 {} 条记忆。", dtoList.size());
        return dtoPage;
    }

    @Override
    public void deleteMemory(String userId, Long memoryId) {
        log.info("【记忆管理】用户 '{}' 正在尝试删除记忆 ID: {}", userId, memoryId);
        Memory memory = memoryMapper.selectById(memoryId);
        // 安全校验：确保用户只能删除自己的记忆
        if (memory == null || !memory.getUserId().equals(userId)) {
            throw new SecurityException("无权删除此记忆或记忆不存在。");
        }
        int deletedRows = memoryMapper.deleteById(memoryId);
        if (deletedRows > 0) {
            log.info("【记忆管理】记忆 ID: {} 已被用户 '{}' 成功删除。", memoryId, userId);
        }
    }

    @Override
    public MemoryDto toggleMemoryPinStatus(String userId, Long memoryId, boolean pinStatus) {
        log.info("【记忆管理】用户 '{}' 正在尝试将记忆 ID: {} 的置顶状态设置为: {}", userId, memoryId, pinStatus);
        Memory memory = memoryMapper.selectById(memoryId);
        // 安全校验
        if (memory == null || !memory.getUserId().equals(userId)) {
            throw new SecurityException("无权操作此记忆或记忆不存在。");
        }

        memory.setPinned(pinStatus);
        memoryMapper.updateById(memory);
        log.info("【记忆管理】记忆 ID: {} 的置顶状态已更新为: {}", memoryId, pinStatus);

        return convertToDto(memory);
    }

    // 辅助方法：将 Memory 实体转换为 MemoryDto
    private MemoryDto convertToDto(Memory memory) {
        if (memory == null) return null;
        MemoryDto dto = new MemoryDto();
        BeanUtils.copyProperties(memory, dto);
        return dto;
    }

    /**
     * 【已更正】辅助方法：封装文本到向量的转换过程。
     *
     * @param text 需要向量化的文本
     * @return PGvector 对象
     */
    private PGvector generateEmbedding(String text) {
        log.debug("【AI调用】准备调用 EmbeddingModel.embed(String)，输入文本: '{}...'", text.substring(0, Math.min(30, text.length())));

        // 【核心更正】embeddingModel.embed(text) 直接返回 float[]
        float[] embeddingArray = embeddingModel.embed(text);

        // 无需再进行 List<Double> 到 float[] 的转换，代码更简洁
        return new PGvector(embeddingArray);
    }
}