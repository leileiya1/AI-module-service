package com.example.aichatservice.dto.dialogue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 多人格对话完成后的响应体 DTO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DialogueResponse {

    /**
     * 完整的对话历史记录。
     * 列表中的每一项都是一位人格的发言。
     */
    private List<String> conversationHistory;

    /**
     * 对整场对话的最终总结。
     */
    private String summary;
}