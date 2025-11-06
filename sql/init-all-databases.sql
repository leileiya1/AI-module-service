-- 初始化所有数据库的主脚本
-- 用途: 一次性创建所有数据库和用户
-- 使用方法: psql -U postgres -f init-all-databases.sql

-- ============================================
-- 创建数据库
-- ============================================

-- Memory Service
CREATE DATABASE memory_service_db
    WITH
    OWNER = admin
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- Knowledge Service
CREATE DATABASE knowledge_service_db
    WITH
    OWNER = admin
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- Feedback Service
CREATE DATABASE feedback_service_db
    WITH
    OWNER = admin
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- Finetuning Service
CREATE DATABASE finetuning_service_db
    WITH
    OWNER = admin
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- Persona Insight Service
CREATE DATABASE persona_insight_db
    WITH
    OWNER = admin
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- ============================================
-- 创建用户 (如果不存在)
-- ============================================

-- 检查用户是否存在，不存在则创建
DO
$$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_catalog.pg_roles
      WHERE  rolname = 'admin') THEN

      CREATE USER admin WITH PASSWORD 'admin';
   END IF;
END
$$;

-- 授予权限
GRANT ALL PRIVILEGES ON DATABASE memory_service_db TO admin;
GRANT ALL PRIVILEGES ON DATABASE knowledge_service_db TO admin;
GRANT ALL PRIVILEGES ON DATABASE feedback_service_db TO admin;
GRANT ALL PRIVILEGES ON DATABASE finetuning_service_db TO admin;
GRANT ALL PRIVILEGES ON DATABASE persona_insight_db TO admin;

-- ============================================
-- 安装扩展
-- ============================================

-- 为每个需要向量搜索的数据库安装 pgvector 扩展

\c memory_service_db
CREATE EXTENSION IF NOT EXISTS vector;
GRANT ALL ON SCHEMA public TO admin;

\c knowledge_service_db
CREATE EXTENSION IF NOT EXISTS vector;
GRANT ALL ON SCHEMA public TO admin;

-- ============================================
-- 提示信息
-- ============================================

\echo '============================================'
\echo '数据库初始化完成！'
\echo '============================================'
\echo '已创建以下数据库:'
\echo '  - memory_service_db'
\echo '  - knowledge_service_db'
\echo '  - feedback_service_db'
\echo '  - finetuning_service_db'
\echo '  - persona_insight_db'
\echo ''
\echo '用户: admin'
\echo '密码: admin'
\echo ''
\echo '请继续执行各个服务的schema脚本:'
\echo '  psql -U admin -d memory_service_db -f memory-service-schema.sql'
\echo '  psql -U admin -d knowledge_service_db -f knowledge-service-schema.sql'
\echo '  psql -U admin -d feedback_service_db -f feedback-service-schema.sql'
\echo '  psql -U admin -d finetuning_service_db -f finetuning-service-schema.sql'
\echo '  psql -U admin -d persona_insight_db -f persona-insight-schema.sql'
\echo '============================================'
