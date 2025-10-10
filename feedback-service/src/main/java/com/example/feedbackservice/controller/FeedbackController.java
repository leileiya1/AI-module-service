package com.example.feedbackservice.controller;

import com.example.dto.AI.FeedbackRequest;
import com.example.feedbackservice.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/feedback")
@Slf4j
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    /**
     * 接收并存储一条用户反馈。
     * @param request 包含反馈所有信息的请求体
     * @param userId 操作用户ID，从请求头获取
     * @return HTTP 201 Created 状态
     */
    @PostMapping
    public ResponseEntity<Void> submitFeedback(
            @Valid @RequestBody FeedbackRequest request, // @Valid 激活DTO中的校验注解
            @RequestHeader("X-User-ID") String userId) {

        log.info("【API入口】收到来自用户 '{}' 的反馈提交请求...", userId);
        feedbackService.saveFeedback(request, userId);
        log.info("【API入口】用户 '{}' 的反馈请求处理完成。", userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}