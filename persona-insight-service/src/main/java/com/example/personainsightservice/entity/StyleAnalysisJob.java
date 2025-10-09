package com.example.personainsightservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.dto.AI.JobStatus;
import com.example.dto.AI.SourceType;
import lombok.Data;
import java.time.OffsetDateTime;
@Data
@TableName("style_analysis_jobs")
public class StyleAnalysisJob {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String jobId;
    private String userId;
    private JobStatus status;
    private SourceType sourceType;
    private String sourceContent;
    private String errorMessage;
    private String resultJson; // 存储分析结果的JSON字符串
    private OffsetDateTime createTime;
    private OffsetDateTime updateTime;
}
