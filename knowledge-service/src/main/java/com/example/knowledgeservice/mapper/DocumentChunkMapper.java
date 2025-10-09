package com.example.knowledgeservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.knowledgeservice.entity.DocumentChunk;
import com.pgvector.PGvector;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DocumentChunkMapper extends BaseMapper<DocumentChunk> {
    List<DocumentChunk> searchSimilarChunks(
            @Param("kbId") Long kbId,
            @Param("targetVector") PGvector targetVector,
            @Param("topK") int topK
    );
}
