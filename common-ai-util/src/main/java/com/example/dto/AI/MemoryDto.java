package com.example.dto.AI;

import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 用于API返回的、详细的记忆数据传输对象。
 */
@Data
public class MemoryDto {
    /**
     * 记忆的唯一ID。
     */
    private Long id;

    /**
     * 记忆的文本内容。
     */
    private String content;

    /**
     * 是否被置顶。
     */
    private Boolean pinned;

    /**
     * 记忆的创建时间。
     */
    private OffsetDateTime createTime;
}
