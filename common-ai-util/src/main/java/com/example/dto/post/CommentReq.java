package com.example.dto.post;

import java.util.List;

/**
 * 评论生成请求的DTO（数据传输对象）。
 * 这是外部服务调用 ai-chat-service 时使用的数据结构。
 *
 * @param userName       用户昵称
 * @param postContent    帖子的文本内容
 * @param imageUrls      【新增】帖子中包含的图片URL列表
 * @param specificDetail 可引用的细节
 * @param microStep      建议的小步骤
 * @param emojiPolicy    表情策略
 * @param maxLength      最大长度
 */
public record CommentReq(
        String userName,
        String postContent,
        List<String> imageUrls, // ✨ 新增图片URL列表
        String specificDetail,
        String microStep,
        String emojiPolicy,
        Integer maxLength
) {
}
