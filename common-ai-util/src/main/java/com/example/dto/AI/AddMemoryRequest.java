package com.example.dto.AI;

import lombok.Data;

/**
 * 添加记忆的请求体 DTO。
 * <p>
 * 它代表了调用者希望服务“记住”的一段文本信息。
 */
@Data
public class AddMemoryRequest {

    /**
     * 需要被记住并向量化的核心文本内容。
     * 例如："用户A在帖子B下评论说他很喜欢Spring框架。"
     */
    private String content;
}