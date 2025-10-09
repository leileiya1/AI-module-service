package com.example.memoryservice.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.dto.AI.InsightAnalysisResult;
import com.example.entity.ai.UserPersonaInsight;
import com.example.memoryservice.entity.Memory;
import com.example.memoryservice.mapper.MemoryMapper;
import com.example.memoryservice.mapper.UserPersonaInsightMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j

public class InsightAnalysisTask {

    private final MemoryMapper memoryMapper;
    private final UserPersonaInsightMapper insightMapper;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public InsightAnalysisTask(MemoryMapper memoryMapper,
                               UserPersonaInsightMapper insightMapper,
                               ChatClient.Builder chatClient,
                               ObjectMapper objectMapper) {
        this.memoryMapper = memoryMapper;
        this.insightMapper = insightMapper;
        this.chatClient = chatClient.build();
        this.objectMapper = objectMapper;

    }

    /**
     * 定时任务，每小时的第15分钟执行一次。
     * cron表达式: "秒 分 时 日 月 周"
     */
    @Scheduled(cron = "0 15 * * * ?")
    public void analyzeUserMemories() {
        log.info("【定时洞察任务】开始执行，扫描需要分析的用户记忆...");

        // 1. 查找所有包含未分析记忆的用户-人格对
        List<Map<String, Object>> pairsToAnalyze = memoryMapper.selectMaps(
                new LambdaQueryWrapper<Memory>()
                        .eq(Memory::getAnalyzed, false)
                        .groupBy(Memory::getUserId, Memory::getPersonaId)
                        .select(Memory::getUserId, Memory::getPersonaId)
        );

        if (pairsToAnalyze.isEmpty()) {
            log.info("【定时洞察任务】没有找到需要分析的新记忆，任务结束。");
            return;
        }

        log.info("【定时洞察任务】发现 {} 个需要进行记忆分析的用户-人格对。", pairsToAnalyze.size());

        for (Map<String, Object> pair : pairsToAnalyze) {
            String userId = (String) pair.get("userId");
            String personaId = (String) pair.get("personaId");
            processSinglePair(userId, personaId);
        }
    }

    private void processSinglePair(String userId, String personaId) {
        log.info("【定时洞察任务】正在处理用户 '{}', 人格 '{}' 的记忆...", userId, personaId);
        try {
            // 2. 获取该用户-人格对的所有记忆（也可以只取最近的N条）
            List<Memory> memories = memoryMapper.selectList(
                    new LambdaQueryWrapper<Memory>()
                            .eq(Memory::getUserId, userId)
                            .eq(Memory::getPersonaId, personaId)
                            .orderByDesc(Memory::getCreateTime)
                            .last("LIMIT 100") // 限制最多分析最近100条记忆
            );

            String concatenatedMemories = memories.stream()
                    .map(Memory::getContent)
                    .collect(Collectors.joining("\n---\n"));

            // 3. 构建Prompt并调用LLM
            String prompt = buildInsightPrompt(concatenatedMemories);
            InsightAnalysisResult result = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(InsightAnalysisResult.class);

            if (result != null && result.getThemes() != null && !result.getThemes().isEmpty()) {
                // 4. 保存或更新洞察结果
                String resultJson = objectMapper.writeValueAsString(result);
                UserPersonaInsight insight = insightMapper.selectOne(
                        new LambdaQueryWrapper<UserPersonaInsight>()
                                .eq(UserPersonaInsight::getUserId, userId)
                                .eq(UserPersonaInsight::getPersonaId, personaId)
                );
                if (insight == null) {
                    insight = new UserPersonaInsight();
                    insight.setUserId(userId);
                    insight.setPersonaId(personaId);
                    insight.setInsightsJson(resultJson);
                    insight.setLastAnalyzedAt(OffsetDateTime.now());
                    insightMapper.insert(insight);
                } else {
                    insight.setInsightsJson(resultJson);
                    insight.setLastAnalyzedAt(OffsetDateTime.now());
                    insightMapper.updateById(insight);
                }
                log.info("【定时洞察任务】成功为用户 '{}', 人格 '{}' 更新洞察: {}", userId, personaId, result.getThemes());

                // 5. 将已分析过的记忆标记为 analyzed = true
                List<Long> memoryIds = memories.stream().map(Memory::getId).collect(Collectors.toList());
                if (!memoryIds.isEmpty()) {
                    log.debug("【定时洞察任务】准备将 {} 条记忆标记为已分析...", memoryIds.size());
                    memoryMapper.update(null, new LambdaUpdateWrapper<Memory>()
                            .set(Memory::getAnalyzed, true)
                            .in(Memory::getId, memoryIds));
                }
            }
        } catch (Exception e) {
            log.error("【定时洞察任务】处理用户 '{}', 人格 '{}' 时发生错误", userId, personaId, e);
        }
    }

    private String buildInsightPrompt(String memoryText) {
        String excerpt = memoryText.substring(0, Math.min(memoryText.length(), 4000));
        return """
                你是一位资深的人生导师和数据分析师。请仔细分析以下一位用户的多段对话记忆记录，总结出该用户在对话中反复提及的、长期的【目标】、【兴趣】或【困境】。
                你的任务是提炼出 1 到 3 个核心主题词或短语。
                
                例如，如果对话多次提到学习编程和找工作，你应该总结出：["学习Java编程", "正在寻找软件开发工作"]
                如果对话多次提到健身和饮食，你应该总结出：["坚持每日健身打卡", "关注健康饮食"]
                
                你的输出必须严格遵循以下JSON格式，不要包含任何额外的解释或Markdown标记：
                {
                  "themes": ["主题1", "主题2", ...]
                }
                
                待分析的记忆记录如下：
                ---
                %s
                ---
                """.formatted(excerpt);
    }
}
