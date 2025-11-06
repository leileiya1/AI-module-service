# AI-Module 智能人格化聊天系统

<div align="center">

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.0.0-blue.svg)](https://spring.io/projects/spring-cloud)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.2-green.svg)](https://spring.io/projects/spring-ai)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14+-336791.svg)](https://www.postgresql.org/)
[![MongoDB](https://img.shields.io/badge/MongoDB-5.0+-green.svg)](https://www.mongodb.com/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

**🚀 基于Spring Cloud的企业级AI对话平台 | 集成智谱AI GLM-4 | 支持向量记忆、知识库RAG、情绪分析、个性化微调**

[快速开始](#快速开始) • [在线文档](#api文档) • [架构设计](#系统架构) • [贡献指南](#贡献指南)

</div>

---

## 📖 项目简介

AI-Module 是一个基于微服务架构的**智能人格化聊天系统**，集成了先进的AI能力，包括对话管理、记忆存储、知识库、人格分析、情绪分析、视觉识别等功能。系统采用Spring Cloud生态，提供高可用、可扩展的企业级AI服务。

## ✨ 核心特性

<table>
  <tr>
    <td>🧠 <b>向量记忆系统</b></td>
    <td>基于pgvector的长期记忆存储与相似度检索，支持记忆置顶功能</td>
  </tr>
  <tr>
    <td>📚 <b>RAG知识库</b></td>
    <td>文档解析、智能分块、向量化存储与语义搜索，支持10MB文档</td>
  </tr>
  <tr>
    <td>🤖 <b>AI对话管理</b></td>
    <td>集成智谱AI GLM-4模型，支持多人格切换和上下文管理</td>
  </tr>
  <tr>
    <td>😊 <b>情绪分析</b></td>
    <td>实时识别对话情绪、趋势分析和情绪可视化</td>
  </tr>
  <tr>
    <td>👤 <b>人格洞察</b></td>
    <td>用户行为分析、人格特征提取、写作风格识别</td>
  </tr>
  <tr>
    <td>🎨 <b>模型微调</b></td>
    <td>基于用户反馈数据的个性化模型训练与管理</td>
  </tr>
  <tr>
    <td>👁️ <b>视觉分析</b></td>
    <td>图像内容理解、视觉场景分析、图文结合对话</td>
  </tr>
  <tr>
    <td>🔍 <b>分布式追踪</b></td>
    <td>Zipkin链路追踪，便于监控、调试和性能优化</td>
  </tr>
</table>

## 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                      Gateway Service                         │
│                      (API网关层)                              │
└──────────────────────┬──────────────────────────────────────┘
                       │
        ┌──────────────┼──────────────┐
        │              │              │
        ▼              ▼              ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│ AI Chat      │ │ Memory       │ │ Knowledge    │
│ Service      │ │ Service      │ │ Service      │
│ (核心对话)    │ │ (记忆管理)    │ │ (知识库)      │
└──────────────┘ └──────────────┘ └──────────────┘
        │              │              │
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│ Persona      │ │ Emotion      │ │ Vision       │
│ Insight      │ │ Analysis     │ │ Analysis     │
│ (人格洞察)    │ │ (情绪分析)    │ │ (视觉分析)    │
└──────────────┘ └──────────────┘ └──────────────┘
        │              │              │
┌──────────────┐ ┌──────────────┐
│ Feedback     │ │ Finetuning   │
│ Service      │ │ Manager      │
│ (反馈管理)    │ │ (微调管理)    │
└──────────────┘ └──────────────┘
        │
        ▼
┌─────────────────────────────────────────────────┐
│            Common AI Util                        │
│      (公共工具模块 - Feign客户端)                   │
└─────────────────────────────────────────────────┘
```

## 技术栈

### 后端框架
- **Java** - 21 (LTS)
- **Spring Boot** - 3.5.6
- **Spring Cloud** - 2025.0.0
- **Spring Cloud Alibaba** - 2025.0.0.0-preview
- **Spring AI** - 1.0.2

### 数据存储
- **PostgreSQL** - 关系型数据库 + pgvector扩展（向量搜索）
- **MongoDB** - 文档型数据库（用于AI对话服务）

### AI能力
- **智谱AI (ZhipuAI)** - GLM-4模型
- **Zai SDK** - 0.0.6

### 数据访问
- **MyBatis Plus** - 3.5.14
- **Spring Data MongoDB** - 数据访问层

### 服务治理
- **Spring Cloud OpenFeign** - 服务间通信
- **Spring Cloud Gateway** - API网关

### 监控追踪
- **Micrometer** - 应用监控
- **Zipkin** - 分布式链路追踪
- **Spring Boot Actuator** - 健康检查和指标

### 文档工具
- **SpringDoc OpenAPI** - 2.8.9 (Swagger UI)

### 工具库
- **Lombok** - 简化代码
- **PGvector** - PostgreSQL向量扩展

## 模块说明

### 1. common-ai-util
**公共工具模块**
- 提供Feign客户端接口，供各服务间调用
- 统一的DTO和实体类定义
- 通用异常处理
- 集成Zipkin链路追踪
- 集成SpringDoc API文档

### 2. gateway-service
**API网关服务**
- 端口：待配置
- 统一入口，路由转发
- 负载均衡
- 认证授权（规划中）

### 3. ai-chat-service
**AI对话核心服务**
- 端口：1235
- 数据库：MongoDB
- 功能：
  - 基于智谱AI的对话生成
  - 多人格（Persona）管理
  - 对话历史管理
  - 用户设置管理
  - 内部API调用

主要API：
- `CompanionController` - 伴侣对话
- `DialogueController` - 通用对话
- `PersonaManagementController` - 人格管理
- `UserSettingsController` - 用户设置

### 4. memory-service
**记忆管理服务**
- 端口：1236
- 数据库：PostgreSQL (memory_service_db)
- 功能：
  - 对话记忆存储
  - 基于pgvector的向量相似度搜索
  - 记忆置顶功能
  - 智能记忆检索（优先返回置顶记忆）

核心表：
- `memories` - 存储对话记忆和向量嵌入

### 5. knowledge-service
**知识库服务**
- 端口：1237
- 数据库：PostgreSQL (knowledge_service_db)
- 功能：
  - 文档上传和解析（支持最大10MB）
  - 文档分块处理
  - 向量化存储
  - 知识检索

核心表：
- `document_chunks` - 文档块存储

### 6. persona-insight-service
**人格洞察服务**
- 端口：待配置
- 数据库：PostgreSQL
- 功能：
  - 用户行为分析
  - 人格特征提取
  - 写作风格分析
  - 分析任务管理

核心表：
- `style_analysis_jobs` - 风格分析任务

### 7. emotion-analysis-service
**情绪分析服务**
- 端口：待配置
- 功能：
  - 对话情绪识别
  - 情绪趋势分析
  - 情绪可视化数据

### 8. vision-analysis-service
**视觉分析服务**
- 端口：待配置
- 功能：
  - 图像内容理解
  - 视觉场景分析
  - 图文结合对话

### 9. feedback-service
**用户反馈服务**
- 端口：待配置
- 数据库：PostgreSQL
- 功能：
  - 收集用户反馈（点赞、编辑）
  - 导出优质对话数据
  - 为模型微调提供数据源

核心表：
- `feedback_records` - 反馈记录

### 10. finetuning-manager-service
**模型微调管理服务**
- 端口：待配置
- 数据库：PostgreSQL
- 功能：
  - 微调任务创建和管理
  - 训练数据准备
  - 微调状态跟踪
  - 模型版本管理

核心表：
- `finetuning_jobs` - 微调任务

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.8+
- PostgreSQL 14+ (需安装pgvector扩展)
- MongoDB 5.0+
- Docker (可选，用于部署Zipkin)

### 安装步骤

#### 1. 克隆项目
```bash
git clone <repository-url>
cd AI-module
```

#### 2. 配置数据库

##### PostgreSQL配置
```bash
# 安装pgvector扩展
CREATE EXTENSION vector;

# 创建数据库
CREATE DATABASE memory_service_db;
CREATE DATABASE knowledge_service_db;
CREATE DATABASE feedback_service_db;
CREATE DATABASE finetuning_service_db;
CREATE DATABASE persona_insight_db;

# 执行初始化脚本（见 sql 目录）
psql -U admin -d memory_service_db -f sql/memory-service-schema.sql
psql -U admin -d knowledge_service_db -f sql/knowledge-service-schema.sql
psql -U admin -d feedback_service_db -f sql/feedback-service-schema.sql
psql -U admin -d finetuning_service_db -f sql/finetuning-service-schema.sql
psql -U admin -d persona_insight_db -f sql/persona-insight-schema.sql
```

##### MongoDB配置
```bash
# 创建数据库和用户
use ai_dynamic_personality_service
db.createUser({
  user: "root",
  pwd: "SAPiece@zll9",
  roles: [{role: "readWrite", db: "ai_dynamic_personality_service"}]
})
```

#### 3. 配置智谱AI API Key

在各服务的`application.yaml`中配置您的API密钥：
```yaml
spring:
  ai:
    zhipuai:
      api-key: your-api-key-here
```

需要配置的服务：
- ai-chat-service
- memory-service
- knowledge-service

#### 4. 启动Zipkin（可选）
```bash
docker run -d -p 9411:9411 openzipkin/zipkin
```

#### 5. 构建项目
```bash
mvn clean install
```

#### 6. 启动服务

按以下顺序启动：
```bash
# 1. 启动网关服务
cd gateway-service
mvn spring-boot:run

# 2. 启动核心服务
cd ai-chat-service
mvn spring-boot:run

# 3. 启动其他服务（可根据需要启动）
cd memory-service && mvn spring-boot:run
cd knowledge-service && mvn spring-boot:run
cd emotion-analysis-service && mvn spring-boot:run
# ... 其他服务
```

## 配置说明

### 端口分配

| 服务 | 端口 | 说明 |
|-----|------|------|
| gateway-service | 待配置 | API网关 |
| ai-chat-service | 1235 | AI对话服务 |
| memory-service | 1236 | 记忆管理 |
| knowledge-service | 1237 | 知识库服务 |
| emotion-analysis-service | 待配置 | 情绪分析 |
| vision-analysis-service | 待配置 | 视觉分析 |
| feedback-service | 待配置 | 反馈服务 |
| finetuning-manager-service | 待配置 | 微调管理 |
| persona-insight-service | 待配置 | 人格洞察 |

### 数据库连接配置

修改各服务的`application.yaml`：
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/your_database
    username: your_username
    password: your_password
```

### Zipkin配置

所有服务已配置Zipkin追踪：
```yaml
management:
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

## API文档

启动服务后，访问各服务的Swagger UI文档：

- AI Chat Service: http://localhost:1235/swagger-ui.html
- Memory Service: http://localhost:1236/swagger-ui.html
- Knowledge Service: http://localhost:1237/swagger-ui.html

## 数据库设计

详细的数据库表结构请参考 `sql` 目录下的SQL脚本文件。

### 核心表说明

#### memories (记忆表)
```sql
- id: 主键
- user_id: 用户ID
- persona_id: 人格ID
- content: 记忆内容
- embedding: 向量嵌入 (pgvector)
- pinned: 是否置顶
- create_time: 创建时间
- analyzed: 是否已分析
```

#### feedback_records (反馈表)
```sql
- id: 主键
- user_id: 用户ID
- persona_id: 人格ID
- source_context: 原始上下文
- ai_response: AI响应
- feedback_type: 反馈类型 (LIKED, EDITED, DISLIKED)
- edited_content: 编辑后内容
- create_time: 创建时间
```

#### finetuning_jobs (微调任务表)
```sql
- id: 主键
- job_id: 任务ID
- provider_job_id: 提供商任务ID
- user_id: 用户ID
- persona_id: 人格ID
- status: 任务状态
- base_model: 基础模型
- fine_tuned_model_id: 微调后模型ID
- training_file_id: 训练文件ID
- error_message: 错误信息
- create_time/update_time: 时间戳
```

## 开发指南

### 新增服务

1. 在父pom.xml中添加module
2. 创建子模块并继承父POM
3. 添加必要的依赖（参考现有服务）
4. 实现Controller、Service、Mapper
5. 配置application.yaml
6. 在common-ai-util中添加Feign客户端（如需被其他服务调用）

### 服务间调用

使用Feign客户端（在common-ai-util模块中定义）：
```java
@Autowired
private MemoryServiceClient memoryServiceClient;

// 调用记忆服务
List<Memory> memories = memoryServiceClient.searchMemories(request);
```

### 向量搜索

使用pgvector进行相似度搜索：
```java
// 1. 使用自定义TypeHandler处理向量字段
@TableField(typeHandler = PGvectorTypeHandler.class)
private PGvector embedding;

// 2. 在Mapper XML中使用向量距离运算符
SELECT * FROM memories
WHERE user_id = #{userId}
ORDER BY embedding <=> #{targetVector}::vector
LIMIT 10;
```

## 部署指南

### Docker部署（推荐）

1. 构建镜像
```bash
# 构建所有服务
mvn clean package -DskipTests

# 为每个服务构建Docker镜像
cd ai-chat-service
docker build -t ai-chat-service:latest .
```

2. 使用Docker Compose启动
```bash
docker-compose up -d
```

### 传统部署

1. 打包
```bash
mvn clean package -DskipTests
```

2. 运行
```bash
java -jar target/ai-chat-service-0.0.1-SNAPSHOT.jar
```

## 监控和运维

### 健康检查

所有服务都暴露了Actuator端点：
```
GET http://localhost:{port}/actuator/health
```

### 链路追踪

访问Zipkin UI查看服务调用链：
```
http://localhost:9411
```

### 日志

各服务日志级别在application.yaml中配置：
```yaml
logging:
  level:
    com.example: DEBUG
```

## 常见问题

### 1. pgvector扩展安装失败
确保PostgreSQL版本 >= 11，并按照官方文档安装pgvector扩展：
```bash
git clone https://github.com/pgvector/pgvector.git
cd pgvector
make
make install
```

### 2. 智谱AI API调用失败
- 检查API Key是否正确
- 确认账户余额充足
- 查看网络连接是否正常

### 3. 服务间调用失败
- 确认Feign客户端配置正确
- 检查服务是否都已启动
- 查看Zipkin追踪定位问题

### 4. MongoDB连接失败
- 检查MongoDB服务是否运行
- 确认用户名密码正确
- URL编码特殊字符（如@需要编码为%40）

## 未来规划

- [ ] 完善API网关的认证授权
- [ ] 实现服务注册与发现（Nacos）
- [ ] 添加配置中心
- [ ] 完善单元测试和集成测试
- [ ] 实现CI/CD流程
- [ ] 添加分布式事务支持
- [ ] 实现消息队列集成
- [ ] 添加缓存层（Redis）
- [ ] 完善监控告警系统

## 贡献指南

欢迎提交Issue和Pull Request！

## 许可证

[待添加]

## 联系方式

项目维护者：周磊磊
微信：SAPieces
email:zhouleileisapiece@gmail.com,17685219818@163.com
