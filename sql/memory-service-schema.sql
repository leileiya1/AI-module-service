-- Memory Service Database Schema
-- 数据库: memory_service_db
-- 描述: 存储用户对话记忆和向量嵌入

-- 确保安装了 pgvector 扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- 创建记忆表
CREATE TABLE IF NOT EXISTS memories (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    persona_id VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    embedding vector(1536),  -- 向量维度根据实际使用的模型调整
    create_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    analyzed BOOLEAN DEFAULT FALSE,
    pinned BOOLEAN DEFAULT FALSE,

    -- 索引
    CONSTRAINT idx_memories_user_persona UNIQUE (user_id, persona_id, content)
);

-- 创建索引以优化查询
CREATE INDEX IF NOT EXISTS idx_memories_user_id ON memories(user_id);
CREATE INDEX IF NOT EXISTS idx_memories_persona_id ON memories(persona_id);
CREATE INDEX IF NOT EXISTS idx_memories_user_persona_combined ON memories(user_id, persona_id);
CREATE INDEX IF NOT EXISTS idx_memories_pinned ON memories(pinned);
CREATE INDEX IF NOT EXISTS idx_memories_create_time ON memories(create_time DESC);

-- 创建向量相似度搜索索引 (HNSW算法，性能更好)
CREATE INDEX IF NOT EXISTS idx_memories_embedding_hnsw ON memories
USING hnsw (embedding vector_cosine_ops);

-- 或使用 IVFFlat 索引（数据量大时更适合）
-- CREATE INDEX IF NOT EXISTS idx_memories_embedding_ivfflat ON memories
-- USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);

-- 创建用户人格洞察表
CREATE TABLE IF NOT EXISTS user_persona_insights (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    persona_id VARCHAR(255) NOT NULL,
    insight_data JSONB,  -- 存储洞察数据
    create_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_user_persona_insight UNIQUE (user_id, persona_id)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_user_persona_insights_user ON user_persona_insights(user_id);
CREATE INDEX IF NOT EXISTS idx_user_persona_insights_persona ON user_persona_insights(persona_id);

-- 注释
COMMENT ON TABLE memories IS '用户对话记忆表，存储向量化的对话内容';
COMMENT ON COLUMN memories.embedding IS '1536维向量嵌入，用于相似度搜索';
COMMENT ON COLUMN memories.pinned IS '是否置顶，置顶的记忆会优先返回';
COMMENT ON COLUMN memories.analyzed IS '是否已进行人格分析';

COMMENT ON TABLE user_persona_insights IS '用户人格洞察数据表';
COMMENT ON COLUMN user_persona_insights.insight_data IS 'JSON格式的洞察数据';
