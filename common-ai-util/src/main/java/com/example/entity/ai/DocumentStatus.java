package com.example.entity.ai;

/**
 * 文档处理状态的枚举
 */
public enum DocumentStatus {
    UPLOADING,  // 上传中
    PROCESSING, // 处理中 (解析、切分、向量化)
    COMPLETED,  // 处理完成
    FAILED      // 处理失败
}
