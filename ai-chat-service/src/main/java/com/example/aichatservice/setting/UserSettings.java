package com.example.aichatservice.setting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 用户设置模型。
 * 用于存储用户的个性化配置，例如默认的AI人设。
 * userId 是唯一的，确保每个用户只有一条设置记录。
 */
@Document(collection = "user_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettings {

    /**
     * 这里我们直接使用 userId 作为文档的ID，
     * 因为每个用户的设置是唯一的。
     */
    @Id
    private String userId;

    /**
     * 用户设置的默认人设ID。
     */
    private String defaultPersonaId;
}
