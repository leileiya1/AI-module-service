package com.example.aichatservice.service;

import com.example.aichatservice.dto.dialogue.DialogueRequest;
import com.example.aichatservice.dto.dialogue.DialogueResponse;

/**
 * 多人格对话服务的业务逻辑接口。
 */
public interface DialogueService {

    /**
     * 根据请求发起并完成一次多个人格之间的对话。
     *
     * @param request 包含对话主题、参与者和轮次的请求
     * @param userId  发起对话的用户ID
     * @return 包含完整对话历史和最终总结的响应
     */
    DialogueResponse initiateDialogue(DialogueRequest request, String userId);
}
