package com.example.finetuningmanagerservice.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 自定义文件上传响应类（SDK 无现成，基于智谱 /v4/files API 响应）
 * 包含 id、filename、status 等关键字段
 *
 * @author YourName
 * @version 1.0
 */
public class FileUploadResponse {
    @JsonProperty("id")
    private String id; // 文件 ID

    @JsonProperty("filename")
    private String filename; // 文件名

    @JsonProperty("status")
    private String status; // 状态，如 "uploaded"

    // Getter 和 Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
