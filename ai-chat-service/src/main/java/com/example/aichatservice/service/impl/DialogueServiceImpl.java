package com.example.aichatservice.service.impl;

import com.example.aichatservice.dto.dialogue.DialogueRequest;
import com.example.aichatservice.dto.dialogue.DialogueResponse;
import com.example.aichatservice.entity.Persona;
import com.example.aichatservice.service.CompanionService;
import com.example.aichatservice.service.DialogueService;
import com.example.aichatservice.service.PersonaManagementService;
import com.example.dto.post.CommentReq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 多人格对话服务的实现类。
 * 扮演“导演”的角色，调度不同的人格轮流发言。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DialogueServiceImpl implements DialogueService {

    // 复用我们已有的、功能强大的单次评论生成服务
    private final CompanionService companionService;
    // 用于根据ID获取人格的详细信息
    private final PersonaManagementService personaService;
    // 用于最后的总结
    private final ChatClient chatClient;

    @Override
    public DialogueResponse initiateDialogue(DialogueRequest request, String userId) {
        long t0 = System.currentTimeMillis();
        log.info("【人格对话-启动】用户 '{}' 发起了一场关于 '{}' 的多方对话。参与者: {}, 轮次: {}",
                userId, request.getTopic(), request.getParticipantPersonaIds(), request.getMaxTurns());

        // 初始化对话历史，并将主题作为第一条消息
        List<String> conversationHistory = new ArrayList<>();
        conversationHistory.add("--- 对话主题 ---\n" + request.getTopic());

        int totalTurns = request.getParticipantPersonaIds().size() * request.getMaxTurns();

        // 循环进行多轮对话
        for (int i = 0; i < totalTurns; i++) {
            // 1. 确定当前轮次的发言者
            String currentPersonaId = request.getParticipantPersonaIds().get(i % request.getParticipantPersonaIds().size());
            Persona currentPersona = personaService.getPersonaById(currentPersonaId);

            log.info("【人格对话-轮次 {}/{}] 发言者: '{}' (ID: {})", i + 1, totalTurns, currentPersona.getName(), currentPersonaId);

            // 2. 【核心】构建一个特殊的 "postContent"
            // 将到目前为止的全部对话历史，拼接成一个字符串，作为当前人格的“输入帖子”
            String currentPostContent = conversationHistory.stream().collect(Collectors.joining("\n\n"));

            // 3. 伪造一个 CommentReq 对象来调用我们现有的 companionService
            CommentReq pseudoReq = new CommentReq(
                    currentPersona.getName(), // 让AI知道它现在的名字
                    currentPostContent,
                    null, // imageUrls
                    "请仔细阅读以上的对话历史和主题，并严格以你的身份和视角，对当前对话发表你的看法、提出问题或给出建议。", // specificDetail 作为核心指令
                    null,
                    "最多1个",
                    500 // 允许更长的回复
            );

            // 4. 调用 companionService 为当前人格生成回应
            // 这里传入的 personaId 是当前轮到的发言者，确保使用正确的记忆、知识库和模型
            String response = companionService.generateComment(pseudoReq, currentPersonaId, userId);

            // 5. 将新生成的回应格式化后，加入对话历史
            String formattedResponse = String.format("--- 来自 '%s' 的回应 ---\n%s", currentPersona.getName(), response);
            conversationHistory.add(formattedResponse);
            log.debug("【人格对话-回应】'{}' 的回应: {}", currentPersona.getName(), response);
        }

        // 6. 对整场对话进行最终总结
        log.info("【人格对话-总结】所有轮次已结束，开始生成最终总结...");
        String summary = generateSummary(conversationHistory);

        log.info("【人格对话-完成】对话已完成，总耗时: {}ms", System.currentTimeMillis() - t0);
        return new DialogueResponse(conversationHistory, summary);
    }

    /**
     * 调用LLM对整个对话历史进行总结，提炼出核心观点和建议。
     * @param history 完整的对话历史
     * @return 总结性文本
     */
    private String generateSummary(List<String> history) {
        String fullConversation = history.stream().collect(Collectors.joining("\n\n"));
        String prompt = """
        你是一个专业的会议纪要员和分析师。
        请仔细阅读以下多个人格之间的完整对话，并完成两项任务：
        1.  **核心观点**: 分点总结每一位人格的核心观点和立场。
        2.  **综合建议**: 基于所有人的观点，为发起对话的用户提供一个中立、全面、可行的行动建议。
        你的输出要清晰、有条理。

        对话记录如下：
        ---
        %s
        ---
        """.formatted(fullConversation);

        try {
            return chatClient.prompt().user(prompt).call().content();
        } catch (Exception e) {
            log.error("【人格对话-总结】生成总结时发生错误！", e);
            return "生成对话总结失败。";
        }
    }
}
