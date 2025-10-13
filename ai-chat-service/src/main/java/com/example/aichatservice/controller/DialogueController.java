package com.example.aichatservice.controller;

import com.example.aichatservice.dto.dialogue.DialogueRequest;
import com.example.aichatservice.dto.dialogue.DialogueResponse;
import com.example.aichatservice.service.DialogueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 多人格对话功能的API控制器。
 */
@RestController
@RequestMapping("/api/v1/dialogues")
@Slf4j
@RequiredArgsConstructor
public class DialogueController {

    private final DialogueService dialogueService;

    /**
     * 启动一次多个人格之间的对话。
     * 这是一个同步阻塞接口，会等待所有对话轮次完成后才返回结果。
     *
     * @param request 包含对话主题、参与者ID列表和轮次的请求体
     * @param userId  发起对话的用户ID，从请求头获取
     * @return 包含完整对话历史和最终总结的响应
     */
    @PostMapping("/start")
    public ResponseEntity<DialogueResponse> startDialogue(
            @Valid @RequestBody DialogueRequest request,
            @RequestHeader("X-User-Id") String userId) {

        log.info("【API入口】收到用户 '{}' 启动人格对话的请求...", userId);
        DialogueResponse response = dialogueService.initiateDialogue(request, userId);
        log.info("【API入口】用户 '{}' 的人格对话请求处理完成。", userId);
        return ResponseEntity.ok(response);
    }
}
