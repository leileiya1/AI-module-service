package com.example.memoryservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.memoryservice.handler.PGvectorTypeHandler;
import com.pgvector.PGvector;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@TableName(value = "memories", autoResultMap = true) // autoResultMap=true 是为了让自定义TypeHandler生效
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Memory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userId;
    private String personaId;
    private String content;

    // 【核心】指定使用我们自定义的TypeHandler来处理这个字段
    @TableField(typeHandler = PGvectorTypeHandler.class)
    private PGvector embedding;

    private OffsetDateTime createTime;
    private Boolean analyzed;
    /**
     * 【新增】是否被用户置顶。
     * true: 是, false: 否 (默认)
     */
    private Boolean pinned;
    /**
     * 这个字段不存在于数据库表中，仅用于接收“置顶优先”查询的结果。
     * 我们将在SQL中为置顶的记录赋予更高的优先级值。
     */
    @TableField(exist = false)
    private Integer priority;
    // 用于接收查询结果中的相似度分数，它不存在于表中
    @TableField(exist = false)
    private Double distance;
}
