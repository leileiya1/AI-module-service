package com.example.aichatservice.dto.dialogue;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

/**
 * 启动一次多个人格对话的请求体 DTO。
 */
@Data
public class DialogueRequest {

    /**
     * 对话的主题或开场白。
     * 例如："我正在考虑是否要辞职去创业。"
     */
    @NotBlank(message = "对话主题(topic)不能为空")
    private String topic;

    /**
     * 参与对话的人格ID列表。
     * 列表的顺序将决定人格的发言顺序。
     */
    @NotEmpty(message = "参与者列表(participantPersonaIds)不能为空")
    @Size(min = 2, message = "至少需要两位参与者")
    private List<String> participantPersonaIds;

    /**
     * 对话的最大轮次（每个参与者发言一次算一轮）。
     * 例如，如果有2个参与者，maxTurns=3，则总共会进行 2 * 3 = 6 次发言。
     */
    @NotNull(message = "最大轮次(maxTurns)不能为空")
    @Min(value = 1, message = "最大轮次至少为1")
    @Max(value = 5, message = "为防止滥用，最大轮次不能超过5")
    private Integer maxTurns;
}