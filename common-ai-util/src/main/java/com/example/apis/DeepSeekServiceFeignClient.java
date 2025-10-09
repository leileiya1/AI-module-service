package com.example.apis;

import com.example.dto.post.CommentReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ai-chat-service", url = "http://localhost:1235", path = "/api/v1/companion")
public interface DeepSeekServiceFeignClient {
    /**
     * 调用 AI 聊天服务生成一条温暖的评论。
     * 接口已升级，以支持多用户和动态人格系统。
     *
     * @param req       评论请求体，包含帖子内容等。
     * @param userId    必需。当前操作用户的ID，将作为 HTTP 请求头 X-User-ID 发送。
     * @param personaId 可选。希望使用的AI人格ID，将作为 URL 请求参数 ?personaId=xxx 发送。
     *                  如果为 null，则 AI 服务会使用用户的默认人格或系统保底人格。
     * @return AI 服务生成的评论文本。
     */
    @PostMapping("/comment")
    String makeWarmComment(@RequestBody CommentReq req,
                           @RequestHeader("X-User-Id") Long userId,
                           @RequestParam(name = "personaId", required = false) String personaId);
}
