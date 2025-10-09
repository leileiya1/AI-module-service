package com.example.aichatservice.service;

import com.example.dto.post.CommentReq;

/**
 * 陪伴式评论生成服务的接口定义。
 * <p>
 * 该接口定义了服务契约，将服务的“做什么”与“怎么做”分离。
 * 任何实现此接口的类都需要提供 generateComment 方法的具体逻辑。
 * 这种设计提高了代码的模块化、可测试性和可扩展性。
 */
public interface CompanionService {

    /**
     * 根据评论请求和可选的人设ID，生成一条评论。
     *
     * @param req       包含帖子内容、用户名等信息的评论请求对象。
     * @param personaId 用户希望使用的AI人设的唯一ID。如果为null或空，则使用系统默认人设。
     * @return 生成的评论文本字符串。
     */
    String generateComment(CommentReq req, String personaId, String userId);
}
