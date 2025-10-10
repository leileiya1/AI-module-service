package com.example.feedbackservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.ai.FeedbackRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * FeedbackRecord表的数据访问接口。
 * 继承MyBatis-Plus的BaseMapper，自动获得强大的CRUD能力。
 */

public interface FeedbackRecordMapper extends BaseMapper<FeedbackRecord> {

    /**
     * 【为未来预留】自定义查询：导出用于微调的高质量数据。
     * 我们只选择 LIKED 和 EDITED 的数据，因为它们代表了用户的积极偏好。
     * @param userId 用户ID
     * @param personaId 人格ID
     * @return 高质量反馈记录列表
     */
    List<FeedbackRecord> findGoodFeedbackForFinetuning(@Param("userId") String userId, @Param("personaId") String personaId);
}
