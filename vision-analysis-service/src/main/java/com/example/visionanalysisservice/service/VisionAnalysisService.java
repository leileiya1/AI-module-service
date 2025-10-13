package com.example.visionanalysisservice.service;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.*;
import com.example.dto.AI.VisionAnalysisResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 视觉分析服务的核心业务逻辑实现。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class VisionAnalysisService {

    // 注入由Spring管理的 ZhipuAiClient 单例Bean
    private final ZhipuAiClient zhipuAiClient;

    // 从配置文件注入模型名称，便于未来更换模型
    @Value("${zhipu.model.vision}")
    private String visionModel;

    // 将Prompt指令定义为常量，便于维护
    private static final String PROMPT_INSTRUCTION = "请用一句中文完整描述这张图片，要求画面感强、细节丰富、客观准确。描述应包括：主体及动作、颜色与材质、光影与环境、空间位置与构图、前中后台元素。禁止主观评价或情感词。例：‘一只黄色的猫趴在阳光斜射的深棕色木地板中央，木纹清晰可见，左侧有一盆高大的绿色植物，背景是一扇半开的窗户，阳光透过窗棂照亮猫的毛发’。";

    /**
     * 分析图片URL并返回文字描述。
     * @param imageUrl 图片的公开URL
     * @return 包含图片文字描述的响应对象
     */
    public VisionAnalysisResponse analyzeImage(String imageUrl) {
        log.info("【视觉分析服务】收到分析请求，图片URL: {}", imageUrl);

        // 1. 输入校验，保证代码健壮性
        if (!StringUtils.hasText(imageUrl)) {
            log.error("【视觉分析服务】分析失败：图片URL为空。");
            throw new IllegalArgumentException("图片URL不能为空。");
        }

        try {
            // 2. 构建多模态消息内容
            List<MessageContent> messageContents = Arrays.asList(
                    // a. 文本指令部分
                    MessageContent.builder()
                            .type("text")
                            .text(PROMPT_INSTRUCTION)
                            .build(),
                    // b. 图片URL部分
                    MessageContent.builder()
                            .type("image_url")
                            .imageUrl(ImageUrl.builder()
                                    .url(imageUrl)
                                    .build())
                            .build()
            );

            // 3. 构建完整的聊天请求
            ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                    .model(visionModel)
                    .messages(Collections.singletonList(
                            ChatMessage.builder()
                                    .role(ChatMessageRole.USER.value())
                                    .content(messageContents)
                                    .build()
                    ))
                    .build();

            log.info("【视觉分析服务】已构建请求，准备调用智普模型: {}", visionModel);
            // 4. 调用SDK发起请求
            ChatCompletionResponse response = zhipuAiClient.chat().createChatCompletion(request);

            // 5. 【核心健壮性】对响应进行详细检查和解析
            if (response.isSuccess() && response.getData() != null &&
                response.getData().getChoices() != null && !response.getData().getChoices().isEmpty()) {

                // 安全地获取回复内容
                Object reply = response.getData().getChoices().getFirst().getMessage().getContent();
                String description = (reply != null) ? reply.toString() : "未能从AI获取有效描述。";

                log.info("【视觉分析服务】成功获取图片描述: {}", description);
                return new VisionAnalysisResponse(description);
            } else {
                // 如果API调用本身是成功的，但返回了错误码或空结果
                String errorMessage = response.getMsg() != null ? response.getMsg() : "AI服务返回未知错误。";
                log.error("【视觉分析服务】AI服务返回失败响应: {}", errorMessage);
                throw new RuntimeException("AI服务调用失败: " + errorMessage);
            }

        } catch (Exception e) {
            // 捕获所有可能的异常，如网络错误、SDK内部错误等
            log.error("【视觉分析服务】调用智普AI时发生严重异常！", e);
            throw new RuntimeException("调用视觉分析服务时发生内部错误。", e);
        }
    }
}