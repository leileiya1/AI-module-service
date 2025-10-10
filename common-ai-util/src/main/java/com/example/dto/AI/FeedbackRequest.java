package com.example.dto.AI;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 接收前端或外部服务提交反馈时使用的数据传输对象。
 * 包含了JSR-303校验注解，以确保入参的合法性。
 */
@Data
public class FeedbackRequest {

    @NotBlank(message = "人格ID(personaId)不能为空")
    private String personaId;

    @NotBlank(message = "上下文(sourceContext)不能为空")
    private String sourceContext;

    @NotBlank(message = "AI原始回答(aiResponse)不能为空")
    private String aiResponse;

    @NotNull(message = "反馈类型(feedbackType)不能为空")
    // 使用正则表达式限制传入的字符串必须是 "LIKED", "DISLIKED", "EDITED" 中的一个
    @jakarta.validation.constraints.Pattern(regexp = "LIKED|DISLIKED|EDITED", message = "无效的反馈类型")
    private String feedbackType;

    // editedContent 是可选的，只有在 feedbackType 为 EDITED 时才需要
    private String editedContent;
}
