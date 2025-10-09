package com.example.memoryservice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dto.AI.*;
import com.example.memoryservice.entity.Memory;

import java.util.List;

/**
 * 记忆服务的业务逻辑接口。
 * <p>
 * 定义了记忆服务的核心能力：添加记忆和搜索记忆。
 * Controller 层将依赖此接口，而不是具体的实现类，以实现低耦合。
 */
public interface MemoryService {

    /**
     * 为指定用户和人格添加一条新记忆。
     *
     * @param userId    用户ID
     * @param personaId 人格ID
     * @param request   包含记忆内容的请求
     */
    void addMemory(String userId, String personaId, AddMemoryRequest request);

    /**
     * 在指定用户和人格的记忆库中，根据查询文本搜索最相关的记忆。
     *
     * @param userId    用户ID
     * @param personaId 人格ID
     * @param request   包含查询文本和topK的请求
     * @return 一个按相似度降序排列的记忆列表
     */
    List<SearchMemoryResponse> searchMemory(String userId, String personaId, SearchMemoryRequest request);

    InsightResponse getInsights(String userId, String personaId); // ✨ 新增

    // ✨【新增】分页获取记忆列表
    Page<MemoryDto> listMemories(String userId, String personaId, Page<Memory> pageRequest);

    // ✨【新增】删除单条记忆
    void deleteMemory(String userId, Long memoryId);

    // ✨【新增】置顶/取消置顶单条记忆
    MemoryDto toggleMemoryPinStatus(String userId, Long memoryId, boolean pinStatus);
}
