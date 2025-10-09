package com.example.entity.ai;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("knowledge_bases")
public class KnowledgeBase {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String userId;
    private OffsetDateTime createTime;
}
