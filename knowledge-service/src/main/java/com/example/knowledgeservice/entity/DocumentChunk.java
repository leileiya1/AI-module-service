package com.example.knowledgeservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import com.example.knowledgeservice.handler.PGvectorTypeHandler;
import com.pgvector.PGvector;
import lombok.Data;

@Data
@TableName(value = "document_chunks", autoResultMap = true)
public class DocumentChunk {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long docId;
    private Long kbId;
    private String content;
    @TableField(typeHandler = PGvectorTypeHandler.class)
    private PGvector embedding;
    @TableField(exist = false)
    private Double distance;
}
