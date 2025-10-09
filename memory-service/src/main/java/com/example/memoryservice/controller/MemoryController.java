package com.example.memoryservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dto.AI.*;
import com.example.memoryservice.entity.Memory;
import com.example.memoryservice.service.MemoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 记忆服务对外 RESTful API 控制器。
 * <p>
 * 负责处理所有与记忆相关的HTTP请求，并将业务逻辑委托给 MemoryService。
 */
@RestController
@RequestMapping("/api/v1/memory")
@Slf4j
@Validated // 开启参数校验
@RequiredArgsConstructor
public class MemoryController {

    private final MemoryService memoryService;

    /**
     * API: 为指定用户和人格添加一条记忆。
     *
     * @param userId    用户ID，从URL路径中获取
     * @param personaId 人格ID，从URL路径中获取
     * @param request   包含记忆内容的JSON请求体
     * @return HTTP 201 Created 状态码
     */
    @PostMapping("/{userId}/{personaId}")
    public ResponseEntity<Void> addMemory(
            @PathVariable("userId") String userId,
            @PathVariable("personaId") String personaId,
            @RequestBody AddMemoryRequest request) {

        log.info("【API入口】收到添加记忆请求: UserID={}, PersonaID={}", userId, personaId);
        try {
            memoryService.addMemory(userId, personaId, request);
            log.info("【API入口】添加记忆请求处理完成: UserID={}, PersonaID={}", userId, personaId);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            log.error("【API入口】处理添加记忆请求时发生异常: UserID={}, PersonaID={}", userId, personaId, e);
            // 在实际应用中，这里应该有一个全局异常处理器来返回更规范的错误信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * API: 在指定用户和人格的记忆库中进行语义搜索。
     *
     * @param userId    用户ID，从URL路径中获取
     * @param personaId 人格ID，从URL路径中获取
     * @param request   包含查询文本和topK的JSON请求体
     * @return 包含最相似记忆的列表和 HTTP 200 OK 状态码
     */
    @PostMapping("/{userId}/{personaId}/search")
    public ResponseEntity<List<SearchMemoryResponse>> search(
            @PathVariable("userId") String userId,
            @PathVariable("personaId") String personaId,
            @Valid @RequestBody SearchMemoryRequest request) {

        log.info("【API入口】收到搜索记忆请求: UserID={}, PersonaID={}", userId, personaId);
        try {
            List<SearchMemoryResponse> results = memoryService.searchMemory(userId, personaId, request);
            log.info("【API入口】搜索记忆请求处理完成: UserID={}, PersonaID={}, 找到 {} 条结果", userId, personaId, results.size());
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("【API入口】处理搜索记忆请求时发生异常: UserID={}, PersonaID={}", userId, personaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{userId}/{personaId}/insights")
    public ResponseEntity<InsightResponse> getInsights(
            @PathVariable("userId") String userId,
            @PathVariable("personaId") String personaId) {
        log.info("【API入口】收到洞察查询请求: UserID={}, PersonaID={}", userId, personaId);
        InsightResponse insights = memoryService.getInsights(userId, personaId);
        if (insights == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(insights);
    }

    /**
     * 【新增API】分页获取指定用户-人格对的所有记忆。
     */
    @GetMapping("/{userId}/{personaId}")
    public ResponseEntity<Page<MemoryDto>> listMemories(
            @PathVariable("userId") String userId,
            @PathVariable("personaId") String personaId,
            @RequestParam(defaultValue = "1") @Min(1) int pageNo,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int pageSize) {

        log.info("【API入口】收到分页查询记忆请求: UserID={}, PersonaID={}", userId, personaId);
        Page<Memory> pageRequest = new Page<>(pageNo, pageSize);
        Page<MemoryDto> resultPage = memoryService.listMemories(userId, personaId, pageRequest);
        return ResponseEntity.ok(resultPage);
    }

    /**
     * 【新增API】用户删除自己的单条记忆。
     */
    @DeleteMapping("/{memoryId}")
    public ResponseEntity<Void> deleteMemory(
            @PathVariable("memoryId") Long memoryId,
            @RequestHeader("X-User-Id") String userId) {
        log.info("【API入口】收到删除记忆请求: MemoryID={}, UserID={}", memoryId, userId);
        memoryService.deleteMemory(userId, memoryId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 【新增API】置顶或取消置顶单条记忆。
     */
    @PutMapping("/{memoryId}/pin")
    public ResponseEntity<MemoryDto> pinMemory(
            @PathVariable("memoryId") Long memoryId,
            @RequestHeader("X-User-Id") String userId) {
        log.info("【API入口】收到置顶记忆请求: MemoryID={}, UserID={}", memoryId, userId);
        MemoryDto updatedMemory = memoryService.toggleMemoryPinStatus(userId, memoryId, true);
        return ResponseEntity.ok(updatedMemory);
    }

    @PutMapping("/{memoryId}/unpin")
    public ResponseEntity<MemoryDto> unpinMemory(
            @PathVariable("memoryId") Long memoryId,
            @RequestHeader("X-User-Id") String userId) {
        log.info("【API入口】收到取消置顶记忆请求: MemoryID={}, UserID={}", memoryId, userId);
        MemoryDto updatedMemory = memoryService.toggleMemoryPinStatus(userId, memoryId, false);
        return ResponseEntity.ok(updatedMemory);
    }
}
