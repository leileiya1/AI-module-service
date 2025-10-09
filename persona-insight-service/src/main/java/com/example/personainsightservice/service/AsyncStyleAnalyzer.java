package com.example.personainsightservice.service;


import com.example.dto.AI.JobStatus;
import com.example.dto.AI.SourceType;
import com.example.dto.AI.StyleAnalysisResult;
import com.example.personainsightservice.entity.StyleAnalysisJob;
import com.example.personainsightservice.mapper.StyleAnalysisJobMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@Slf4j
public class AsyncStyleAnalyzer {

    private final ChatClient chatClient; // Spring AI 提供的强大的LLM客户端
    private final StyleAnalysisJobMapper jobMapper;
    private final ObjectMapper objectMapper;

    public AsyncStyleAnalyzer(ChatClient.Builder chatClient, StyleAnalysisJobMapper jobMapper, ObjectMapper objectMapper) {
        this.chatClient = chatClient.build();
        this.jobMapper = jobMapper;
        this.objectMapper = objectMapper;
    }

    @Async
    public void analyze(String jobId) {
        log.info("【异步分析器】[JOB_ID:{}] 开始分析任务...", jobId);
        StyleAnalysisJob job = jobMapper.findByJobId(jobId)
                .orElseThrow(() -> new RuntimeException("任务不存在: " + jobId));

        try {
            // 1. 获取文本内容
            String textContent = getTextContent(job);

            // 2. 构建 Prompt
            String prompt = buildPrompt(textContent);

            // 3. 调用 LLM 并获取结构化输出
            log.info("【异步分析器】[JOB_ID:{}] 正在调用LLM进行风格分析...", jobId);
            StyleAnalysisResult result = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(StyleAnalysisResult.class); // Spring AI 自动将JSON结果转换为Java对象

            // 4. 保存结果
            String resultJson = objectMapper.writeValueAsString(result);
            updateJobStatus(jobId, JobStatus.COMPLETED, null, resultJson);
            log.info("【异步分析器】[JOB_ID:{}] 分析成功并已保存结果。", jobId);

        } catch (Exception e) {
            log.error("【异步分析器】[JOB_ID:{}] 分析失败！", jobId, e);
            updateJobStatus(jobId, JobStatus.FAILED, e.getMessage(), null);
        }
    }

    private String getTextContent(StyleAnalysisJob job) throws Exception {
        if (job.getSourceType() == SourceType.URL) {
            log.info("【异步分析器】[JOB_ID:{}] 正在从URL抓取内容: {}", job.getJobId(), job.getSourceContent());
            // 使用 Jsoup 抓取并提取纯文本
            return Jsoup.connect(job.getSourceContent()).get().body().text();
        }
        return job.getSourceContent();
    }

    // 这是整个服务的灵魂：Prompt Engineering
    private String buildPrompt(String textContent) {
        // 为了防止文本过长，我们截取一部分进行分析
        String excerpt = textContent.substring(0, Math.min(textContent.length(), 2000));

        return """
                你是一个语言风格分析专家。请仔细阅读以下文本，并以JSON格式总结出作者的人格特质。
                
                你需要分析并填充以下字段：
                - "name": 根据内容，给这个人格起一个简洁、贴切的名字 (例如: "科技爱好者", "生活分享家")。
                - "role": 一句话描述这个人格的角色定位。
                - "tone": 总结作者的语气 (例如: "幽默、犀利", "温柔、鼓励")。
                - "style": 总结作者的语言风格 (例如: "多用短句和表情包", "喜欢用专业术语和比喻")。
                - "commentTemplates": 基于以上分析，为“庆祝(celebrate)”和“安慰(comfort)”这两个场景，模仿作者的风格，各生成一句话术模板。模板中必须包含 "{name}", "{specific_detail}", "{micro_step}", "{emoji}" 这些占位符。
                
                严格按照下面的JSON格式输出，不要包含任何额外的解释或Markdown标记。
                
                {
                  "name": "...",
                  "role": "...",
                  "tone": "...",
                  "style": "...",
                  "commentTemplates": {
                    "celebrate": "...",
                    "comfort": "..."
                  }
                }
                
                待分析的文本如下：
                ---
                %s
                ---
                """.formatted(excerpt);
    }

    private void updateJobStatus(String jobId, JobStatus status, String errorMessage, String resultJson) {
        StyleAnalysisJob job = new StyleAnalysisJob();
        job.setJobId(jobId);
        job.setStatus(status);
        job.setErrorMessage(errorMessage);
        job.setResultJson(resultJson);
        job.setUpdateTime(OffsetDateTime.now());
        jobMapper.update(job, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<StyleAnalysisJob>()
                .eq(StyleAnalysisJob::getJobId, jobId));
    }
}
