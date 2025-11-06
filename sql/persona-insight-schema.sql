-- Persona Insight Service Database Schema
-- 数据库: persona_insight_db
-- 描述: 用户人格洞察和风格分析

-- 创建用户画像表
CREATE TABLE IF NOT EXISTS user_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL UNIQUE,
    personality_traits JSONB,  -- 人格特征数据
    interests JSONB,  -- 兴趣爱好
    preferences JSONB,  -- 偏好设置
    demographic_info JSONB,  -- 人口统计信息
    create_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 创建风格分析任务表
CREATE TABLE IF NOT EXISTS style_analysis_jobs (
    id BIGSERIAL PRIMARY KEY,
    job_id VARCHAR(255) NOT NULL UNIQUE,
    user_id VARCHAR(255) NOT NULL,
    persona_id VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',  -- PENDING, PROCESSING, COMPLETED, FAILED
    analysis_type VARCHAR(50) NOT NULL,  -- WRITING_STYLE, COMMUNICATION_PATTERN, PERSONALITY
    input_data JSONB,  -- 输入数据
    result JSONB,  -- 分析结果
    confidence_score DECIMAL(5, 4),  -- 置信度分数 (0-1)
    error_message TEXT,
    create_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_analysis_status CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'))
);

-- 创建人格特征表
CREATE TABLE IF NOT EXISTS personality_traits (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    persona_id VARCHAR(255),
    trait_category VARCHAR(100) NOT NULL,  -- 特征类别（如Big Five维度）
    trait_name VARCHAR(100) NOT NULL,
    trait_value DECIMAL(5, 4),  -- 特征值 (0-1)
    confidence DECIMAL(5, 4),  -- 置信度
    evidence TEXT,  -- 证据说明
    create_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 创建写作风格表
CREATE TABLE IF NOT EXISTS writing_styles (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    persona_id VARCHAR(255),
    style_dimension VARCHAR(100) NOT NULL,  -- 风格维度
    style_value JSONB,  -- 风格特征值
    examples TEXT[],  -- 示例文本
    frequency INTEGER DEFAULT 1,  -- 出现频率
    create_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_writing_styles UNIQUE (user_id, persona_id, style_dimension)
);

-- 创建行为模式表
CREATE TABLE IF NOT EXISTS behavior_patterns (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    persona_id VARCHAR(255),
    pattern_type VARCHAR(100) NOT NULL,  -- 模式类型
    pattern_data JSONB NOT NULL,  -- 模式数据
    occurrence_count INTEGER DEFAULT 1,  -- 发生次数
    last_occurrence TIMESTAMP WITH TIME ZONE,
    create_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_user_profiles_user_id ON user_profiles(user_id);

CREATE INDEX IF NOT EXISTS idx_style_analysis_jobs_job_id ON style_analysis_jobs(job_id);
CREATE INDEX IF NOT EXISTS idx_style_analysis_jobs_user_id ON style_analysis_jobs(user_id);
CREATE INDEX IF NOT EXISTS idx_style_analysis_jobs_status ON style_analysis_jobs(status);
CREATE INDEX IF NOT EXISTS idx_style_analysis_jobs_create_time ON style_analysis_jobs(create_time DESC);

CREATE INDEX IF NOT EXISTS idx_personality_traits_user_id ON personality_traits(user_id);
CREATE INDEX IF NOT EXISTS idx_personality_traits_persona_id ON personality_traits(persona_id);
CREATE INDEX IF NOT EXISTS idx_personality_traits_category ON personality_traits(trait_category);

CREATE INDEX IF NOT EXISTS idx_writing_styles_user_id ON writing_styles(user_id);
CREATE INDEX IF NOT EXISTS idx_writing_styles_persona_id ON writing_styles(persona_id);

CREATE INDEX IF NOT EXISTS idx_behavior_patterns_user_id ON behavior_patterns(user_id);
CREATE INDEX IF NOT EXISTS idx_behavior_patterns_persona_id ON behavior_patterns(persona_id);
CREATE INDEX IF NOT EXISTS idx_behavior_patterns_type ON behavior_patterns(pattern_type);

-- 注释
COMMENT ON TABLE user_profiles IS '用户画像表，存储综合的用户特征';
COMMENT ON TABLE style_analysis_jobs IS '风格分析任务表';
COMMENT ON COLUMN style_analysis_jobs.analysis_type IS '分析类型: WRITING_STYLE-写作风格, COMMUNICATION_PATTERN-沟通模式, PERSONALITY-人格分析';

COMMENT ON TABLE personality_traits IS '人格特征详细表';
COMMENT ON COLUMN personality_traits.trait_category IS '特征类别，如大五人格（Big Five）的各个维度';

COMMENT ON TABLE writing_styles IS '写作风格表';
COMMENT ON TABLE behavior_patterns IS '行为模式表';
