package com.example.aichatservice.controller;

import com.example.aichatservice.service.CompanionService;
import com.example.dto.post.CommentReq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/companion")
@Slf4j
@RequiredArgsConstructor
public class CompanionController {

    // ✨ 依赖注入的是接口，而不是具体的实现类
    private final CompanionService companionService;


    @PostMapping("/comment")
    public String makeWarmComment(
            @RequestBody CommentReq req,
            @RequestParam(name = "personaId", required = false) String personaId, // personaId 保持可选
            @RequestHeader("X-User-Id") String userId) { // ✨ 从请求头获取用户ID
        log.info("接收到用户 '{}' 的评论生成请求, personaId: '{}'", userId, req.userName());

        try {
            // ✨ 将 userId 传递给 Service 层
            return companionService.generateComment(req, personaId, userId);
        } catch (Exception e) {
            log.error("为用户 '{}' 生成评论时发生未知错误", userId, e);
            return "抱歉，我的大脑好像出了一点小问题，稍后再试试吧！";
        }
    }
}
