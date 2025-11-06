-- Knowledge Service Database Schema
-- 数据库: knowledge_service_db
-- 描述: 存储知识库文档和文档块

-- 确保安装了 pgvector 扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- 创建文档表
CREATE TABLE IF NOT EXISTS documents (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    persona_id VARCHAR(255),
    title VARCHAR(500) NOT NULL,
    file_name VARCHAR(500) NOT NULL,
    file_size BIGINT,
    content_type VARCHAR(100),
    status VARCHAR(50) DEFAULT 'PENDING',  -- PENDING, PROCESSING, COMPLETED, FAILED
    total_chunks INTEGER DEFAULT 0,
    create_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 创建文档块表
CREATE TABLE IF NOT EXISTS document_chunks (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    persona_id VARCHAR(255),
    chunk_index INTEGER NOT NULL,
    content TEXT NOT NULL,
    embedding vector(1536),  -- 向量维度根据实际使用的模型调整
    metadata JSONB,  -- 存储额外的元数据
    create_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_document_chunks_document FOREIGN KEY (document_id)
        REFERENCES documents(id) ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_documents_user_id ON documents(user_id);
CREATE INDEX IF NOT EXISTS idx_documents_persona_id ON documents(persona_id);
CREATE INDEX IF NOT EXISTS idx_documents_status ON documents(status);
CREATE INDEX IF NOT EXISTS idx_documents_create_time ON documents(create_time DESC);

CREATE INDEX IF NOT EXISTS idx_document_chunks_document_id ON document_chunks(document_id);
CREATE INDEX IF NOT EXISTS idx_document_chunks_user_id ON document_chunks(user_id);
CREATE INDEX IF NOT EXISTS idx_document_chunks_persona_id ON document_chunks(persona_id);
CREATE INDEX IF NOT EXISTS idx_document_chunks_chunk_index ON document_chunks(chunk_index);

-- 创建向量相似度搜索索引
CREATE INDEX IF NOT EXISTS idx_document_chunks_embedding_hnsw ON document_chunks
USING hnsw (embedding vector_cosine_ops);

-- 注释
COMMENT ON TABLE documents IS '知识库文档表';
COMMENT ON COLUMN documents.status IS '文档处理状态: PENDING-待处理, PROCESSING-处理中, COMPLETED-已完成, FAILED-失败';

COMMENT ON TABLE document_chunks IS '文档分块表，存储向量化的文档片段';
COMMENT ON COLUMN document_chunks.chunk_index IS '文档块在原文档中的顺序';
COMMENT ON COLUMN document_chunks.embedding IS '1536维向量嵌入，用于语义搜索';
COMMENT ON COLUMN document_chunks.metadata IS 'JSON格式的元数据，如页码、章节等';
