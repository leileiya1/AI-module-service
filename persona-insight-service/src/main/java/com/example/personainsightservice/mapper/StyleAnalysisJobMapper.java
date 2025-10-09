package com.example.personainsightservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.personainsightservice.entity.StyleAnalysisJob;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

public interface StyleAnalysisJobMapper extends BaseMapper<StyleAnalysisJob> {
    // 使用 Optional 避免空指针
    Optional<StyleAnalysisJob> findByJobId(@Param("jobId") String jobId);
}
