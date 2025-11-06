-- Feedback Service Database Schema
-- 数据库: feedback_service_db
-- 描述: 存储用户反馈数据

-- 创建反馈记录表
CREATE TABLE IF NOT EXISTS feedback_records (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    persona_id VARCHAR(255) NOT NULL,
    source_context TEXT,  -- 用户输入的上下文
    ai_response TEXT NOT NULL,  -- AI的响应
    feedback_type VARCHAR(50) NOT NULL,  -- LIKED, DISLIKED, EDITED
    edited_content TEXT,  -- 用户编辑后的内容（仅当feedback_type为EDITED时）
    create_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    -- 约束
    CONSTRAINT chk_feedback_type CHECK (feedback_type IN ('LIKED', 'DISLIKED', 'EDITED'))
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_feedback_records_user_id ON feedback_records(user_id);
CREATE INDEX IF NOT EXISTS idx_feedback_records_persona_id ON feedback_records(persona_id);
CREATE INDEX IF NOT EXISTS idx_feedback_records_user_persona ON feedback_records(user_id, persona_id);
CREATE INDEX IF NOT EXISTS idx_feedback_records_feedback_type ON feedback_records(feedback_type);
CREATE INDEX IF NOT EXISTS idx_feedback_records_create_time ON feedback_records(create_time DESC);

-- 创建组合索引用于导出优质反馈
CREATE INDEX IF NOT EXISTS idx_feedback_records_for_finetuning
ON feedback_records(user_id, persona_id, feedback_type, create_time DESC)
WHERE feedback_type IN ('LIKED', 'EDITED');

-- 注释
COMMENT ON TABLE feedback_records IS '用户反馈记录表';
COMMENT ON COLUMN feedback_records.feedback_type IS '反馈类型: LIKED-喜欢, DISLIKED-不喜欢, EDITED-编辑';
COMMENT ON COLUMN feedback_records.source_context IS '用户输入的原始上下文';
COMMENT ON COLUMN feedback_records.ai_response IS 'AI生成的响应内容';
COMMENT ON COLUMN feedback_records.edited_content IS '用户编辑后的内容，用于模型微调';
