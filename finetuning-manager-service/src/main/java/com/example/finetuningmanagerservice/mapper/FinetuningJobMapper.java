package com.example.finetuningmanagerservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.ai.FinetuningJob;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

public interface FinetuningJobMapper extends BaseMapper<FinetuningJob> {
    Optional<FinetuningJob> findByJobId(@Param("jobId") String jobId);
}
