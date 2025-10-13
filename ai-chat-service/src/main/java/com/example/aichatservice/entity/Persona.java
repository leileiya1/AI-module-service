package com.example.aichatservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

/**
 * AI 人设（Persona）数据模型
 * 该类映射到 MongoDB 中的 "personas" 集合。
 * 每个文档代表一个独立、可配置的AI角色。
 */
@Document(collection = "personas") // 指定在MongoDB中对应的集合名称
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Persona {

    /**
     * 人设的唯一ID，由MongoDB自动生成。
     */
    @Id
    private String id;

    /**
     * 关键字段：创建此人设的用户ID。
     * 我们将为其创建索引以优化查询。
     */
    @Indexed
    private String userId;

    /**
     * 人设名称，用于展示和区分，例如："毒舌朋友"、"温柔姐姐"。
     */
    private String name;

    /**
     * 人设的角色定位，一句话描述其核心身份。
     * 这部分会直接影响AI的自我认知。
     */
    private String role;

    /**
     * 人设的说话语气，用于指导AI生成文本的感情色彩。
     */
    private String tone;

    /**
     * 人设的语言风格，例如：多用比喻、喜欢用短句等。
     */
    private String style;

    /**
     * 专属的评论模板。
     * Key: 场景名 (e.g., "celebrate", "comfort")
     * Value: 该场景下的话术模板字符串
     * 这是实现人设差异化评论的核心。
     */
    private Map<String, Object> commentTemplates;

    /**
     * 【新增】是否为此人格启用长期记忆功能。
     */
    private boolean memoryEnabled;

    /**
     * 【新增】此人格关联的知识库ID。
     * 如果为 null 或 0，则不使用知识库。
     */
    private Long knowledgeBaseId;

    /**
     * 【核心字段】存储由 finetuning-manager-service 训练生成的、专属的微调模型ID。
     * 如果此字段不为空，系统将优先使用这个模型进行对话生成。
     * 默认为 null，表示使用通用的基础模型。
     */
    private String fineTunedModelId;

    /**
     * 记录创建时间。
     */
    @CreatedDate
    private Instant createdAt;

    /**
     * 记录最后更新时间。
     */
    @LastModifiedDate
    private Instant updatedAt;
}
