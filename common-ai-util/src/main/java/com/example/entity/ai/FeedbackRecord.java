package com.example.entity.ai;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.dto.AI.FeedbackType;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 数据库 `feedback_records` 表的实体映射类。
 */
@Data
@TableName("feedback_records")
public class FeedbackRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String userId;
    private String personaId;
    private String sourceContext;
    private String aiResponse;
    private FeedbackType feedbackType;
    private String editedContent;
    private OffsetDateTime createTime;
}