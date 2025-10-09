package com.example.entity.ai;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.dto.AI.JobStatus;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("finetuning_jobs")
public class FinetuningJob {
    @TableId(type = IdType.AUTO) private Long id;
    private String jobId;
    private String providerJobId;
    private String userId;
    private String personaId;
    private JobStatus status;
    private String baseModel;
    private String fineTunedModelId;
    private String trainingFileId;
    private String errorMessage;
    private OffsetDateTime createTime;
    private OffsetDateTime updateTime;
}
