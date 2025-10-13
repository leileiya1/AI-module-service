package com.example.aichatservice.service.impl;

import com.example.aichatservice.dto.EmotionAnalysisResponse;
import com.example.aichatservice.entity.Persona;
import com.example.aichatservice.exception.PersonaNotFoundException;
import com.example.aichatservice.service.CompanionService;
import com.example.aichatservice.service.PersonaManagementService;
import com.example.aichatservice.service.UserSettingsService;
import com.example.aichatservice.apis.EmotionServiceClient;
import com.example.apis.KnowledgeServiceClient;
import com.example.apis.MemoryServiceClient;

import com.example.apis.VisionServiceClient;
import com.example.dto.AI.*;
import com.example.dto.post.CommentReq;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CompanionService 接口的默认实现类。
 * 包含了所有核心业务逻辑：加载人设、构建Prompt、调用AI模型。
 * 通过实现接口，使得该类的具体实现可以被轻松替换或代理。
 */
@Service // 标记为Spring的服务组件
@Slf4j

public class CompanionServiceImpl implements CompanionService { // ✨ 实现接口
    // ✨【核心简化】: 现在我们只需要注入一个默认的、由Spring自动配置的ChatClient即可！

    private final ChatClient chat;
    private final PersonaManagementService personaService;
    private final UserSettingsService userSettingsService;
    private final MemoryServiceClient memoryServiceClient;
    private final KnowledgeServiceClient knowledgeServiceClient;
    private final EmotionServiceClient emotionServiceClient; // ✨ 注入情绪分析客户端
    private final VisionServiceClient visionServiceClient;

    public CompanionServiceImpl(@Qualifier("companionChatClient") ChatClient chat,
                                PersonaManagementService personaService,
                                UserSettingsService userSettingsService,
                                MemoryServiceClient memoryServiceClient,
                                KnowledgeServiceClient knowledgeServiceClient,
                                EmotionServiceClient emotionServiceClient,
                                VisionServiceClient visionServiceClient
    ) {
        this.chat = chat;
        this.personaService = personaService;
        this.userSettingsService = userSettingsService;
        this.memoryServiceClient = memoryServiceClient;
        this.knowledgeServiceClient = knowledgeServiceClient;
        this.emotionServiceClient = emotionServiceClient;
        this.visionServiceClient = visionServiceClient;

    }

    /**
     * 内部DTO，用于接收场景推理LLM调用的JSON结果。
     */
    @Data
    private static class ScenarioInferenceResult {
        @JsonProperty("scenario")
        private String scenario;
    }

    /**
     * {@inheritDoc}
     * 这是接口方法的具体实现。
     */
    @Override // ✨ 表明这是对接口方法的覆盖
    public String generateComment(CommentReq req, String personaId, String userId) {
        log.info("【评论生成-入口】用户 '{}' 开始生成评论，请求 personaId: '{}'", userId, personaId);

        // 1. 解析并决定使用哪个人设
        Persona persona = resolvePersona(personaId, userId);
        log.debug("【评论生成-步骤1】已为用户 '{}' 加载人设: '{}' (ID: {})", userId, persona.getName(), persona.getId());

        String originalQuery = req.postContent();
        StringBuilder contextBuilder = new StringBuilder();
        String imageDescription = ""; // 初始化图片描述为空字符串
        // 2. 【新增】分析图片内容
        // 我们将视觉分析作为获取上下文的第一步
        if (!CollectionUtils.isEmpty(req.imageUrls())) {
            log.info("【评论生成-视觉】检测到 {} 张图片，开始分析...", req.imageUrls().size());
            try {
                // 为简化流程，我们暂时只分析第一张图
                String imageUrl = req.imageUrls().getFirst();
                var visionRequest = new VisionAnalysisRequest(imageUrl);

                log.debug("【评论生成-视觉】正在调用 vision-service, URL: {}", imageUrl);
                var visionResponse = visionServiceClient.analyze(visionRequest);

                if (visionResponse != null && StringUtils.hasText(visionResponse.getDescription())) {
                    // 将分析结果加入到上下文构建器中
                    contextBuilder.append("\n[图片内容]:\n- ").append(visionResponse.getDescription()).append("\n");
                    log.info("【评论生成-视觉】成功获取图片描述: {}", visionResponse.getDescription());
                } else {
                    log.warn("【评论生成-视觉】视觉分析服务返回了空的描述。");
                }
            } catch (Exception e) {
                log.error("【评论生成-视觉】调用视觉分析服务时发生严重错误！将忽略图片信息继续流程。", e);
                // 即使视觉分析失败，我们也不中断主流程，保证服务的可用性
            }
        }

        // 3. 【新增】分析帖子情绪
        String emotion = "NEUTRAL"; // 默认情绪为中性
        try {
            log.info("【评论生成-步骤2.1】开始调用情绪分析服务...");
            // 使用我们定义的Feign DTO来构建请求
            EmotionAnalysisRequest emotionRequest = new EmotionAnalysisRequest(originalQuery);
            EmotionAnalysisResponse emotionResponse = emotionServiceClient.analyze(emotionRequest);

            if (emotionResponse != null && StringUtils.hasText(emotionResponse.getEmotion())) {
                emotion = emotionResponse.getEmotion().toUpperCase(); // 将返回的String转为大写，以便作为Map的Key
                log.info("【评论生成-步骤2.2】帖子情绪分析成功，结果: {}, 置信度: {}", emotion, emotionResponse.getConfidence());
            } else {
                log.warn("【评论生成-步骤2.W】情绪分析服务返回为空，将使用默认情绪NEUTRAL。");
            }
        } catch (Exception e) {
            log.error("【评论生成-步骤2.E】调用情绪分析服务失败！将使用默认情绪NEUTRAL。", e);
        }

        // 4. 场景推理
        String scenario = "neutral_reply";
        try {
            log.info("【评论生成-步骤4.1-场景】开始进行场景推理...");
            String inferencePrompt = buildScenarioInferencePrompt(originalQuery, imageDescription, emotion);
            ScenarioInferenceResult result = chat.prompt()
                    .user(inferencePrompt)
                    .call()
                    .entity(ScenarioInferenceResult.class);
            if (result != null && StringUtils.hasText(result.getScenario())) {
                scenario = result.getScenario();
                log.info("【评论生成-步骤4.2-场景】场景推理成功，结果: {}", scenario);
            }
        } catch (Exception e) {
            log.error("【评论生成-步骤4.E-场景】场景推理失败！将使用默认场景 'neutral_reply'。", e);
        }

        // 5. 【新增】检索用户长期洞察
        try {
            log.info("【评论生成-步骤3.1】开始检索用户长期洞察...");
            InsightResponse insights = memoryServiceClient.getInsights(userId, persona.getId());
            if (insights != null && !CollectionUtils.isEmpty(insights.getThemes())) {
                contextBuilder.append("\n[用户长期关注点]:\n");
                insights.getThemes().forEach(theme -> contextBuilder.append("- ").append(theme).append("\n"));
                log.info("【评论生成-步骤3.2】成功检索到用户长期洞察: {}", insights.getThemes());
            } else {
                log.info("【评论生成-步骤3.2】未找到用户长期洞察。");
            }
        } catch (Exception e) {
            log.error("【评论生成-步骤3.E】调用洞察服务失败！", e);
        }

        // 6. 【整合】如果启用了记忆，则检索相关记忆
        if (persona.isMemoryEnabled()) {
            try {
                log.info("【评论生成-步骤2.1】人格 '{}' 已启用记忆，开始检索...", persona.getName());
                SearchMemoryRequest memoryRequest = new SearchMemoryRequest();
                memoryRequest.setQuery(originalQuery);
                List<SearchMemoryResponse> memories = memoryServiceClient.searchMemory(userId, persona.getId(), memoryRequest);
                if (!CollectionUtils.isEmpty(memories)) {
                    contextBuilder.append("\n[相关记忆]:\n");
                    memories.forEach(m -> contextBuilder.append("- ").append(m.getContent()).append("\n"));
                    log.info("【评论生成-步骤2.2】成功检索到 {} 条相关记忆。", memories.size());
                } else {
                    log.info("【评论生成-步骤2.2】未找到相关记忆。");
                }
            } catch (Exception e) {
                log.error("【评论生成-步骤2.E】调用记忆服务失败！", e);
                throw new RuntimeException("Downstream memory-service failed", e);
            }
        }

        // 7. 【整合】如果关联了知识库，则检索相关知识
        if (persona.getKnowledgeBaseId() != null && persona.getKnowledgeBaseId() > 0) {
            try {
                log.info("【评论生成-步骤3.1】人格 '{}' 已关联知识库 ID: {}，开始检索...", persona.getName(), persona.getKnowledgeBaseId());
                KnowledgeSearchRequest knowledgeRequest = new KnowledgeSearchRequest();
                knowledgeRequest.setQuery(originalQuery);
                List<KnowledgeSearchResult> knowledgeChunks = knowledgeServiceClient.searchKnowledge(persona.getKnowledgeBaseId(), knowledgeRequest, userId);
                if (!CollectionUtils.isEmpty(knowledgeChunks)) {
                    contextBuilder.append("\n[相关专业知识]:\n");
                    knowledgeChunks.forEach(k -> contextBuilder.append("- ").append(k.getContent()).append("\n"));
                    log.info("【评论生成-步骤3.2】成功检索到 {} 条相关知识片段。", knowledgeChunks.size());
                } else {
                    log.info("【评论生成-步骤3.2】未找到相关专业知识。");
                }
            } catch (Exception e) {
                log.error("【评论生成-步骤3.E】调用知识库服务失败！", e);
                throw new RuntimeException("Downstream knowledge-service failed", e);
            }
        }

        // 8. 构建最终的 Prompt
        String systemPrompt = buildSystemPromptWithEmotionAndInsight(persona, req, scenario, emotion);
        String userPrompt = buildUserPromptWithContext(req, contextBuilder.toString());
        log.debug("【评论生成-步骤4】构建完成的 System Prompt:\n---\n{}\n---", systemPrompt);
        log.debug("【评论生成-步骤4】构建完成的 User Prompt (含上下文):\n---\n{}\n---", userPrompt);

        // 9. 【核心简化】动态构建调用选项并执行
        log.info("【模型选择】准备构建AI调用选项...");

        // a. 创建一个 ZhipuAiChatOptions 的构建器
        ZhiPuAiChatOptions.Builder optionsBuilder = ZhiPuAiChatOptions.builder();

        // b. 检查是否需要使用专属微调模型
        if (StringUtils.hasText(persona.getFineTunedModelId())) {
            // 如果人格有关联的专属微调模型ID，则在本次调用中指定使用该模型
            String modelId = persona.getFineTunedModelId();
            optionsBuilder.model(modelId);
            log.info("【模型选择】人格 '{}' 拥有专属微调模型 '{}'，本次调用将使用该模型。", persona.getName(), modelId);
        } else {
            // 否则，不指定模型，让其使用 application.yml 中配置的默认模型 (如 glm-4v)
            log.info("【模型选择】人格 '{}' 未指定专属模型，本次调用将使用默认模型。", persona.getName());
        }

        // c. 构建最终的调用选项
        ZhiPuAiChatOptions chatOptions = optionsBuilder.build();

        // 10. 调用大语言模型
        log.info("【评论生成-步骤5】正在调用 AI 模型生成最终评论...");
        String comment = chat.prompt()
                .options(chatOptions)
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();
        log.info("【评论生成-步骤5】AI 模型成功返回评论: '{}'", comment);

        // 11. 【整合】异步将本次互动存入记忆
        if (persona.isMemoryEnabled() && StringUtils.hasText(comment)) {
            addInteractionToMemoryAsync(userId, persona.getId(), originalQuery, comment);
        }

        return comment;
    }

    /**
     * 解析并决定使用哪个人设的策略中心。
     *
     * @param personaId 前端传入的可选 personaId
     * @param userId    当前操作的用户ID
     * @return 最终决定使用的人设对象
     */
    private Persona resolvePersona(String personaId, String userId) {
        // 策略1：如果前端明确指定了 personaId，则优先使用它。
        if (StringUtils.hasText(personaId)) {
            log.debug("策略1：使用前端指定的 personaId '{}'", personaId);
            try {
                return personaService.getPersonaById(personaId);
            } catch (PersonaNotFoundException e) {
                log.warn("指定的 personaId '{}' 未找到，将回退到默认策略。", personaId);
            }
        }

        // 策略2：尝试查找该用户的默认人设。 (✨ TODO 已实现!)
        log.debug("策略2：尝试为用户 '{}' 查找其设置的默认人设", userId);
        Optional<String> defaultPersonaIdOpt = userSettingsService.findDefaultPersonaIdForUser(userId);
        if (defaultPersonaIdOpt.isPresent()) {
            String defaultPersonaId = defaultPersonaIdOpt.get();
            try {
                log.debug("找到了用户 '{}' 的默认人设ID: '{}'，正在加载...", userId, defaultPersonaId);
                return personaService.getPersonaById(defaultPersonaId);
            } catch (PersonaNotFoundException e) {
                log.warn("用户 '{}' 的默认人设ID '{}' 无效或已被删除，将使用系统默认。", userId, defaultPersonaId);
            }
        }

        // 策略3：如果以上两步都没有找到人设，则使用系统全局的保底人设。
        log.debug("策略3：用户 '{}' 未指定人设且未设置默认人设，使用系统全局默认", userId);
        return getSystemDefaultPersona();
    }

    /**
     * 【新增】异步方法，将成功的互动添加到记忆服务。
     */
    @Async
    public void addInteractionToMemoryAsync(String userId, String personaId, String userQuery, String aiResponse) {
        try {
            log.info("【异步记忆】[用户:{}, 人格:{}] 开始异步存储本次互动...", userId, personaId);
            String memoryContent = String.format("用户提问/帖子内容摘要: %s\n我的回答: %s", userQuery, aiResponse);
            AddMemoryRequest addMemoryRequest = new AddMemoryRequest();
            addMemoryRequest.setContent(memoryContent);
            memoryServiceClient.addMemory(userId, personaId, addMemoryRequest);
            log.info("【异步记忆】[用户:{}, 人格:{}] 本次互动已成功存入记忆。", userId, personaId);
        } catch (Exception e) {
            log.error("【异步记忆】[用户:{}, 人格:{}] 异步存储记忆失败！", userId, personaId, e);
        }
    }

    /**
     * 【新增】构建带有动态上下文（记忆、知识）的 User Prompt。
     */
    private String buildUserPromptWithContext(CommentReq req, String context) {
        return """
                %s
                
                [用户昵称] %s
                [帖子内容] %s
                [可引用细节（若能抽取）] %s
                [建议的 micro_step 候选] %s
                [表情策略] %s
                """.formatted(
                StringUtils.hasText(context) ? "[上下文信息]:\n" + context : "[上下文信息]: 无",
                nvl(req.userName(), "朋友"),
                nvl(req.postContent(), ""),
                nvl(req.specificDetail(), "（从帖子自动抽取）"),
                nvl(req.microStep(), "深呼吸一分钟 / 喝水 / 列出一件能做的小事"),
                nvl(req.emojiPolicy(), "最多 1 个")
        );
    }

    /**
     * 【新增】根据情绪动态选择模板来构建System Prompt。
     */
    private String buildSystemPromptWithEmotionAndInsight(Persona persona, CommentReq req, String scenario, String emotion) {
        // 1. 定义通用规则和约束
        String guidelines = """
                - 你的回答必须自然、真诚，严格符合你的角色设定。
                - 评论要针对帖子内容，不要空洞。
                - 长度严格遵守下面的字数限制。
                - 直接输出评论文本，不要包含任何额外的解释或标记。
                """;
        int maxLen = req.maxLength() != null ? req.maxLength() : 160;

        // 2. 【核心】调用辅助方法，根据场景和情绪，智能地查找最匹配的话术模板
        String chosenTemplate = findTemplateForScenarioAndEmotion(persona, scenario, emotion);

        log.info("【模板选择】根据场景 '{}' 和情绪 '{}'，最终选择的模板是: '{}'", scenario, emotion, chosenTemplate);

        // 3. 将所有信息组合成最终的、给AI的系统指令 (System Prompt)
        return """
                [你的身份]
                你的名字是：%s
                你的角色定位是：%s
                你的语气和风格：%s
                
                [你的任务]
                你正在为一个社交帖子生成评论。请严格扮演你的角色，并使用下面提供的【专属场景模板】作为你说话的核心思路。
                你可以对模板进行润色和扩展，但必须保留其核心含义和风格。
                
                【专属场景模板】: %s
                
                [评论规则]
                %s
                评论长度严格限制在 %d 字以内。
                """.formatted(
                persona.getName(),
                persona.getRole(),
                persona.getTone() + "；" + persona.getStyle(),
                chosenTemplate,
                guidelines,
                maxLen
        );
    }

    /**
     * 【新增】一个健壮的模板查找辅助方法。
     *
     * @param persona  人格对象
     * @param scenario 场景，如 "comfort", "celebrate"
     * @param emotion  情绪，如 "SADNESS", "JOY"
     * @return 找到的最佳模板字符串
     */
    @SuppressWarnings("unchecked") // 压制类型转换警告，因为我们知道这里的逻辑
    private String findTemplateForScenarioAndEmotion(Persona persona, String scenario, String emotion) {
        // 最终的、万能的兜底模板
        String ultimateFallbackTemplate = "请根据你的角色和上下文信息，生成一条自然的、有共鸣的评论。";

        Map<String, Object> templates = persona.getCommentTemplates();
        if (templates == null || templates.isEmpty() || !StringUtils.hasText(scenario)) {
            log.warn("【模板查找】人格 '{}' 的模板库为空或场景缺失，使用系统级兜底模板。", persona.getName());
            return ultimateFallbackTemplate;
        }

        Object scenarioTemplateObject = templates.get(scenario);

        if (scenarioTemplateObject instanceof Map) {
            // 场景A: 模板是新的嵌套结构, e.g., "comfort": {"SADNESS": "...", "default": "..."}
            log.debug("【模板查找】场景 '{}' 是一个嵌套的情绪Map结构。", scenario);
            Map<String, String> emotionMap = (Map<String, String>) scenarioTemplateObject;

            // 策略1: 优先查找与当前情绪精确匹配的模板
            if (StringUtils.hasText(emotion) && emotionMap.containsKey(emotion)) {
                String specificTemplate = emotionMap.get(emotion);
                if (StringUtils.hasText(specificTemplate)) {
                    log.debug("【模板查找】成功找到情绪 '{}' 的专属模板。", emotion);
                    return specificTemplate;
                }
            }

            // 策略2: 如果找不到精确匹配，查找该场景下的 "default" 模板
            if (emotionMap.containsKey("default")) {
                String defaultTemplate = emotionMap.get("default");
                if (StringUtils.hasText(defaultTemplate)) {
                    log.debug("【模板查找】未找到情绪 '{}' 的模板，成功回退到场景 '{}' 的默认模板。", emotion, scenario);
                    return defaultTemplate;
                }
            }
            log.warn("【模板查找】场景 '{}' 的情绪Map中既没有找到 '{}'，也没有找到 'default' 模板。", scenario, emotion);

        } else if (scenarioTemplateObject instanceof String flatTemplate) {
            // 场景B: 模板是旧的扁平结构, e.g., "celebrate": "恭喜你！"
            log.debug("【模板查找】场景 '{}' 是一个扁平的字符串结构。", scenario);
            if (StringUtils.hasText(flatTemplate)) {
                return flatTemplate;
            }
        }

        // 如果连场景都找不到，或者场景下的模板为空，则返回最终兜底
        log.warn("【模板查找】在场景 '{}' 下未找到任何有效模板，使用系统级兜底模板。", scenario);
        return ultimateFallbackTemplate;
    }

    /**
     * 【新增】构建用于场景推理的专属Prompt。
     */
    private String buildScenarioInferencePrompt(String postContent, String imageDescription, String emotion) {
        String context = "帖子文本: " + postContent;
        if (StringUtils.hasText(imageDescription)) {
            context += "\n图片内容: " + imageDescription;
        }
        context += "\n分析出的情绪: " + emotion;

        return """
                你是一个顶级的社交情商专家。你的任务是根据下面提供的上下文信息，判断这段对话最适合哪一个社交场景。
                请从以下几个预定义的场景标签中选择一个最合适的：
                ["celebrate", "comfort", "encourage", "gentle_reminder", "neutral_reply"]
                
                - "celebrate": 用于分享好消息、成就、喜悦的时刻。
                - "comfort": 用于表达悲伤、失落、遇到困难需要安慰的时刻。
                - "encourage": 用于表达迷茫、缺乏动力、需要鼓励的时刻。
                - "gentle_reminder": 用于提醒注意事项或表达担忧的场景。
                - "neutral_reply": 用于中性的日常分享、提问或陈述。
                
                你的输出必须严格遵循以下JSON格式，不要添加任何解释：
                {
                  "scenario": "选择的场景标签"
                }
                
                待分析的上下文如下：
                ---
                %s
                ---
                """.formatted(context);
    }

    private Persona getSystemDefaultPersona() {
        log.debug("正在加载系统默认人设 (温情陪伴者)");
        Persona defaultPersona = new Persona();
        defaultPersona.setId("system_default_warm_companion");
        defaultPersona.setName("温情陪伴者");
        defaultPersona.setRole("温情、可靠、边界清晰的长情陪伴者");
        defaultPersona.setTone("温柔、尊重、不过度干预；像长期朋友");
        defaultPersona.setStyle("先共情再建议；多用第二人称；短句+分点；不过度热情");
        defaultPersona.setCommentTemplates(Map.of(
                "celebrate", "{name}，看完你这段分享替你开心！{specific_detail} 来之不易，能感受到你的努力与专注。愿好状态延续～如果愿意，也想听你说说：这一程里最关键的一步是什么？{emoji}",
                "comfort", "{name}，抱抱你。我读到你的{specific_detail}，能想象这段时间不容易。先允许自己慢一点、松一松，已经很勇敢了。不妨从一件可执行的小事开始，比如{micro_step}，给自己一点点可见的改善。{emoji}",
                "encourage", "{name}，能把心情写下来本身就很不容易。我注意到{specific_detail}，说明你在认真面对。也许我们可以用“一步/一天”的节奏，先做{micro_step}；我会在这里陪你一起走一小步。{emoji}",
                "gentle_reminder", "{name}，读到你的{specific_detail}，我也替你捏了把汗～或许可以提前准备{prep_suggestion}，把不确定降到最低。需要我帮你列个简单清单吗？{emoji}",
                "neutral_reply", "{name}，我看见你分享了{specific_detail}，很有生活气息。也好奇：这件事里，哪个瞬间最让你难忘？{emoji}"
        ));
        return defaultPersona;
    }


    private static String nvl(String v, String def) {
        return (v != null && !v.isBlank()) ? v : def;
    }
}
