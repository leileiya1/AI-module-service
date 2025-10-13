package com.example.dto.AI;

import lombok.Data;

import java.util.Map;

/**
 * 用于创建和更新 Persona 的数据传输对象 (DTO)。
 * 使用 DTO 可以让我们控制API的输入，进行数据校验，并避免直接暴露数据库实体。
 */
@Data
public class PersonaDto {

    // @NotBlank(message = "人设名称不能为空")
    // @Size(max = 50, message = "人设名称不能超过50个字符")
    private String name;

    // @NotBlank(message = "角色定位不能为空")
    private String role;

    private String tone;
    private String style;

    // @NotEmpty(message = "评论模板不能为空")
    private Map<String, Object> commentTemplates;
    private boolean memoryEnabled;
    private Long knowledgeBaseId;

    /**
     * 【核心字段】存储由 finetuning-manager-service 训练生成的、专属的微调模型ID。
     * 如果此字段不为空，系统将优先使用这个模型进行对话生成。
     * 默认为 null，表示使用通用的基础模型。
     */
    private String fineTunedModelId;
}
