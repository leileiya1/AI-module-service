package com.example.feedbackservice.service;

import com.example.dto.AI.FeedbackRequest;
import com.example.dto.AI.FeedbackType;
import com.example.entity.ai.FeedbackRecord;
import com.example.feedbackservice.mapper.FeedbackRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 反馈服务的核心业务逻辑层。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRecordMapper feedbackMapper;

    /**
     * 保存一条用户反馈。
     * @param request 包含反馈所有信息的DTO
     * @param userId 操作用户ID
     * @return 保存到数据库后的完整 FeedbackRecord 实体
     */
    @Transactional
    public FeedbackRecord saveFeedback(FeedbackRequest request, String userId) {
        log.info("【反馈服务】开始处理用户 '{}' 对人格 '{}' 的反馈，类型: {}",
                userId, request.getPersonaId(), request.getFeedbackType());

        FeedbackType feedbackType = FeedbackType.valueOf(request.getFeedbackType().toUpperCase());

        // 健壮性校验：如果反馈类型是EDITED，那么editedContent字段必须有内容。
        if (feedbackType == FeedbackType.EDITED && !StringUtils.hasText(request.getEditedContent())) {
            log.error("【反馈服务】校验失败：用户 '{}' 提交了EDITED类型的反馈，但editedContent为空。", userId);
            throw new IllegalArgumentException("EDITED类型的反馈必须提供editedContent字段。");
        }

        // 1. 将DTO转换为数据库实体
        FeedbackRecord record = new FeedbackRecord();
        record.setUserId(userId);
        record.setPersonaId(request.getPersonaId());
        record.setSourceContext(request.getSourceContext());
        record.setAiResponse(request.getAiResponse());
        record.setFeedbackType(feedbackType);
        record.setEditedContent(request.getEditedContent()); // 如果不是EDITED，此值为null

        // 2. 插入数据库
        int insertedRows = feedbackMapper.insert(record);

        if (insertedRows > 0) {
            log.info("【反馈服务】用户 '{}' 的反馈记录已成功保存到数据库，记录ID: {}", userId, record.getId());
        } else {
            log.error("【反馈服务】用户 '{}' 的反馈记录存入数据库失败！", userId);
            throw new RuntimeException("反馈数据写入数据库时发生错误。");
        }

        return record;
    }
}