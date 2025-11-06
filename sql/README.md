# SQL 脚本说明

本目录包含AI-Module项目的所有数据库初始化脚本。

## 目录结构

```
sql/
├── README.md                           # 本文件
├── init-all-databases.sql              # 创建所有数据库的主脚本
├── memory-service-schema.sql           # 记忆服务数据库表结构
├── knowledge-service-schema.sql        # 知识库服务数据库表结构
├── feedback-service-schema.sql         # 反馈服务数据库表结构
├── finetuning-service-schema.sql       # 微调管理服务数据库表结构
└── persona-insight-schema.sql          # 人格洞察服务数据库表结构
```

## 前置要求

### PostgreSQL
- 版本: 14+
- 扩展: pgvector (用于向量搜索)

### MongoDB
- 版本: 5.0+
- 用于: ai-chat-service

## 安装 pgvector 扩展

### 方法一：使用包管理器（推荐）

**Ubuntu/Debian:**
```bash
sudo apt install postgresql-14-pgvector
```

**macOS (Homebrew):**
```bash
brew install pgvector
```

**Windows:**
下载预编译的二进制文件或从源码编译。

### 方法二：从源码编译

```bash
git clone https://github.com/pgvector/pgvector.git
cd pgvector
make
sudo make install
```

## 快速开始

### 1. 创建所有数据库

连接到PostgreSQL并执行主初始化脚本：

```bash
psql -U postgres -f init-all-databases.sql
```

此脚本会：
- 创建5个数据库
- 创建admin用户（如果不存在）
- 授予必要的权限
- 为memory-service和knowledge-service安装pgvector扩展

### 2. 创建表结构

执行各服务的schema脚本：

```bash
# Memory Service
psql -U admin -d memory_service_db -f memory-service-schema.sql

# Knowledge Service
psql -U admin -d knowledge_service_db -f knowledge-service-schema.sql

# Feedback Service
psql -U admin -d feedback_service_db -f feedback-service-schema.sql

# Finetuning Service
psql -U admin -d finetuning_service_db -f finetuning-service-schema.sql

# Persona Insight Service
psql -U admin -d persona_insight_db -f persona-insight-schema.sql
```

### 3. 配置MongoDB（用于ai-chat-service）

```bash
# 启动MongoDB
mongosh

# 创建数据库和用户
use ai_dynamic_personality_service
db.createUser({
  user: "root",
  pwd: "SAPiece@zll9",
  roles: [{role: "readWrite", db: "ai_dynamic_personality_service"}]
})
```

## 数据库列表

| 数据库名称 | 服务 | 数据库类型 | 主要功能 |
|-----------|------|----------|---------|
| memory_service_db | memory-service | PostgreSQL | 存储对话记忆和向量嵌入 |
| knowledge_service_db | knowledge-service | PostgreSQL | 存储知识库文档和文档块 |
| feedback_service_db | feedback-service | PostgreSQL | 存储用户反馈数据 |
| finetuning_service_db | finetuning-manager-service | PostgreSQL | 管理模型微调任务 |
| persona_insight_db | persona-insight-service | PostgreSQL | 用户人格洞察和分析 |
| ai_dynamic_personality_service | ai-chat-service | MongoDB | AI对话和人格数据 |

## 数据库用户凭据

**PostgreSQL:**
- 用户名: `admin`
- 密码: `admin`

**MongoDB:**
- 用户名: `root`
- 密码: `SAPiece@zll9`

**注意:** 生产环境中请务必修改这些默认密码！

## 各服务数据库详细说明

### memory-service-schema.sql
**核心表:**
- `memories` - 存储用户对话记忆，包含1536维向量嵌入
- `user_persona_insights` - 用户人格洞察数据

**特性:**
- 使用pgvector扩展进行向量相似度搜索
- HNSW索引优化查询性能
- 支持记忆置顶功能

### knowledge-service-schema.sql
**核心表:**
- `documents` - 知识库文档元数据
- `document_chunks` - 文档分块和向量嵌入

**特性:**
- 文档处理状态跟踪
- 向量相似度搜索
- 文档分块管理

### feedback-service-schema.sql
**核心表:**
- `feedback_records` - 用户反馈记录

**特性:**
- 支持三种反馈类型：LIKED, DISLIKED, EDITED
- 优化的查询索引用于导出优质反馈
- 为模型微调提供数据源

### finetuning-service-schema.sql
**核心表:**
- `finetuning_jobs` - 微调任务
- `training_datasets` - 训练数据集
- `finetuning_job_logs` - 任务日志

**特性:**
- 完整的任务状态跟踪
- 训练数据集管理
- 详细的日志记录

### persona-insight-schema.sql
**核心表:**
- `user_profiles` - 用户画像
- `style_analysis_jobs` - 风格分析任务
- `personality_traits` - 人格特征
- `writing_styles` - 写作风格
- `behavior_patterns` - 行为模式

**特性:**
- 多维度人格分析
- 写作风格识别
- 行为模式追踪

## 向量搜索说明

### 向量维度
默认使用1536维向量（OpenAI ada-002标准），根据实际使用的embedding模型调整。

### 索引类型

**HNSW (Hierarchical Navigable Small World):**
- 适合: 中小型数据集（< 1M向量）
- 优点: 查询速度快，构建相对快
- 缺点: 内存占用较大

```sql
CREATE INDEX idx_memories_embedding_hnsw ON memories
USING hnsw (embedding vector_cosine_ops);
```

**IVFFlat (Inverted File Flat):**
- 适合: 大型数据集（> 1M向量）
- 优点: 内存占用小
- 缺点: 查询速度相对慢，需要训练

```sql
CREATE INDEX idx_memories_embedding_ivfflat ON memories
USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
```

### 距离度量
- `vector_cosine_ops` - 余弦相似度（默认）
- `vector_l2_ops` - 欧几里得距离
- `vector_ip_ops` - 内积

## 常用操作

### 验证pgvector安装
```sql
SELECT * FROM pg_extension WHERE extname = 'vector';
```

### 查看向量索引
```sql
SELECT
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE indexdef LIKE '%vector%';
```

### 测试向量搜索
```sql
-- 查询最相似的10条记忆
SELECT content, 1 - (embedding <=> '[0.1, 0.2, ...]'::vector) AS similarity
FROM memories
ORDER BY embedding <=> '[0.1, 0.2, ...]'::vector
LIMIT 10;
```

### 重建索引
```sql
-- 如果索引性能下降，可以重建
REINDEX INDEX idx_memories_embedding_hnsw;
```

## 备份和恢复

### 备份单个数据库
```bash
pg_dump -U admin -d memory_service_db -F c -f memory_service_db.backup
```

### 恢复数据库
```bash
pg_restore -U admin -d memory_service_db memory_service_db.backup
```

### 备份所有数据库
```bash
pg_dumpall -U postgres > all_databases.sql
```

## 性能优化建议

1. **定期VACUUM**
```sql
VACUUM ANALYZE memories;
```

2. **监控查询性能**
```sql
EXPLAIN ANALYZE
SELECT * FROM memories
WHERE user_id = 'user123'
ORDER BY embedding <=> '[...]'::vector
LIMIT 10;
```

3. **调整PostgreSQL配置** (postgresql.conf)
```
shared_buffers = 256MB          # 根据可用内存调整
effective_cache_size = 1GB      # 根据可用内存调整
maintenance_work_mem = 64MB     # 用于索引构建
```

## 故障排查

### pgvector扩展安装失败
```sql
-- 检查扩展是否可用
SELECT * FROM pg_available_extensions WHERE name = 'vector';

-- 如果不可用，需要重新安装pgvector
```

### 向量查询慢
- 确认索引已创建
- 考虑增加`effective_cache_size`
- 对于大数据集，考虑使用IVFFlat索引

### 连接数不足
```sql
-- 查看当前连接数
SELECT count(*) FROM pg_stat_activity;

-- 修改最大连接数 (需要重启)
ALTER SYSTEM SET max_connections = 200;
```

## 维护计划

建议定期执行以下维护任务：

1. **每日**: 监控数据库性能和连接数
2. **每周**: VACUUM ANALYZE所有表
3. **每月**: 检查索引健康状况，必要时重建
4. **每季度**: 审查和优化慢查询

## 相关资源

- [PostgreSQL官方文档](https://www.postgresql.org/docs/)
- [pgvector GitHub](https://github.com/pgvector/pgvector)
- [MongoDB官方文档](https://www.mongodb.com/docs/)

## 更新日志

- 2025-11-06: 初始版本，创建所有服务的数据库schema
