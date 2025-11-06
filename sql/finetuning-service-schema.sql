-- Finetuning Manager Service Database Schema
-- 数据库: finetuning_service_db
-- 描述: 管理模型微调任务

-- 创建微调任务表
CREATE TABLE IF NOT EXISTS finetuning_jobs (
    id BIGSERIAL PRIMARY KEY,
    job_id VARCHAR(255) NOT NULL UNIQUE,  -- 内部任务ID
    provider_job_id VARCHAR(255),  -- AI提供商返回的任务ID
    user_id VARCHAR(255) NOT NULL,
    persona_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',  -- PENDING, PREPARING, TRAINING, COMPLETED, FAILED
    base_model VARCHAR(100) NOT NULL,  -- 基础模型名称
    fine_tuned_model_id VARCHAR(255),  -- 微调后的模型ID
    training_file_id VARCHAR(255),  -- 训练文件ID
    training_samples_count INTEGER DEFAULT 0,  -- 训练样本数量
    hyperparameters JSONB,  -- 超参数配置
    error_message TEXT,  -- 错误信息
    create_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    -- 约束
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'PREPARING', 'TRAINING', 'COMPLETED', 'FAILED', 'CANCELLED'))
);

-- 创建训练数据集表
CREATE TABLE IF NOT EXISTS training_datasets (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    persona_id VARCHAR(255) NOT NULL,
    dataset_file_path VARCHAR(500),  -- 数据集文件路径
    file_size BIGINT,
    sample_count INTEGER,  -- 样本数量
    format VARCHAR(50) DEFAULT 'JSONL',  -- 文件格式
    status VARCHAR(50) DEFAULT 'PREPARING',  -- PREPARING, READY, UPLOADED, FAILED
    create_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_training_datasets_job FOREIGN KEY (job_id)
        REFERENCES finetuning_jobs(id) ON DELETE CASCADE
);

-- 创建微调任务日志表
CREATE TABLE IF NOT EXISTS finetuning_job_logs (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL,
    log_level VARCHAR(20) NOT NULL,  -- INFO, WARNING, ERROR
    message TEXT NOT NULL,
    details JSONB,
    create_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_finetuning_job_logs_job FOREIGN KEY (job_id)
        REFERENCES finetuning_jobs(id) ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_finetuning_jobs_job_id ON finetuning_jobs(job_id);
CREATE INDEX IF NOT EXISTS idx_finetuning_jobs_user_id ON finetuning_jobs(user_id);
CREATE INDEX IF NOT EXISTS idx_finetuning_jobs_persona_id ON finetuning_jobs(persona_id);
CREATE INDEX IF NOT EXISTS idx_finetuning_jobs_user_persona ON finetuning_jobs(user_id, persona_id);
CREATE INDEX IF NOT EXISTS idx_finetuning_jobs_status ON finetuning_jobs(status);
CREATE INDEX IF NOT EXISTS idx_finetuning_jobs_create_time ON finetuning_jobs(create_time DESC);

CREATE INDEX IF NOT EXISTS idx_training_datasets_job_id ON training_datasets(job_id);
CREATE INDEX IF NOT EXISTS idx_training_datasets_user_persona ON training_datasets(user_id, persona_id);

CREATE INDEX IF NOT EXISTS idx_finetuning_job_logs_job_id ON finetuning_job_logs(job_id);
CREATE INDEX IF NOT EXISTS idx_finetuning_job_logs_create_time ON finetuning_job_logs(create_time DESC);

-- 注释
COMMENT ON TABLE finetuning_jobs IS '模型微调任务表';
COMMENT ON COLUMN finetuning_jobs.job_id IS '内部唯一任务ID';
COMMENT ON COLUMN finetuning_jobs.provider_job_id IS 'AI提供商（如智谱AI）返回的任务ID';
COMMENT ON COLUMN finetuning_jobs.status IS '任务状态: PENDING-待处理, PREPARING-准备中, TRAINING-训练中, COMPLETED-已完成, FAILED-失败, CANCELLED-已取消';
COMMENT ON COLUMN finetuning_jobs.hyperparameters IS 'JSON格式的超参数配置';

COMMENT ON TABLE training_datasets IS '训练数据集表';
COMMENT ON COLUMN training_datasets.format IS '数据集格式，通常为JSONL';

COMMENT ON TABLE finetuning_job_logs IS '微调任务日志表';
