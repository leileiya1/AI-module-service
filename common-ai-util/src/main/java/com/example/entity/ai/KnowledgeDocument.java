package com.example.entity.ai;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("knowledge_documents")
public class KnowledgeDocument {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long kbId;
    private String fileName;
    private Long fileSize;
    @TableField(value = "status") // 明确指定列名
    private DocumentStatus status;
    private String errorMessage;
    private OffsetDateTime createTime;
    private OffsetDateTime updateTime;
}
