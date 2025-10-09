package com.example.entity.ai;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("user_persona_insights")
public class UserPersonaInsight {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String userId;
    private String personaId;
    private String insightsJson;
    private OffsetDateTime lastAnalyzedAt;
    private OffsetDateTime createTime;
    private OffsetDateTime updateTime;
}