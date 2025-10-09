package com.example.memoryservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.memoryservice.entity.Memory;
import com.pgvector.PGvector;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MemoryMapper extends BaseMapper<Memory> {

    /**
     * 【升级版】向量相似度搜索，增加了“置顶优先”逻辑。
     * @param userId        用户ID
     * @param personaId     人格ID
     * @param targetVector  目标向量
     * @param topK          返回结果的总数量
     * @return 包含相似度分数的记忆列表
     */
    List<Memory> searchSimilarMemoriesWithPinnedPriority(
            @Param("userId") String userId,
            @Param("personaId") String personaId,
            @Param("targetVector") PGvector targetVector,
            @Param("topK") int topK
    );
}
