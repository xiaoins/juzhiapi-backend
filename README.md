# Juzhi API Platform (AI 聚合站)

AI API 聚合中转平台 — 统一接口调用多家 AI 模型（OpenAI / DeepSeek / 通义千问 / Gemini / Claude / 豆包等）

## 功能特性

- 统一 OpenAI 兼容 API（`/v1/chat/completions`）
- 用户注册登录 + JWT 认证
- 余额充值与消耗计费
- API Key 管理（多 Key 隔离）
- 管理后台（用户管理 / 模型配置 / 订单查看）
- Redis 缓存 + 限流保护

## 技术栈

| 组件 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2 + Java 21 |
| ORM | MyBatis Plus 3.5 |
| 安全认证 | Spring Security + JWT |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis 7 |

## 快速开始

### 方式一：Docker Compose（推荐，一键全量部署）

```bash
# 克隆项目
git clone <your-repo-url>
cd juzhiAPI

# 一键启动（MySQL + Redis + 后端服务）
docker compose up -d --build

# 查看日志
docker compose logs -f app
```

访问：http://localhost:8080/health

默认账号：**admin** / **password**

### 方式二：本地开发（Windows / Mac / Linux）

#### 前置要求

- JDK 21+
- Maven 3.8+
- MySQL 8.0（或用 Docker 单独跑 `docker run -d --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=sulei1124 mysql:8.0`）
- Redis（或用 Docker 单独跑 `docker run -d --name redis -p 6379:6379 redis:7-alpine`）

#### 启动步骤

1. 创建数据库：
   ```sql
   CREATE DATABASE ai_platform DEFAULT CHARACTER SET utf8mb4;
   ```

2. **无需手动导入 SQL！** 应用启动时会自动执行 `schema.sql` 初始化数据库表和初始数据。

   > 原理：Spring Boot 通过 Java 直接读取 SQL 文件内容发送给 MySQL，
  > 完全不经过 shell，因此 `$2a$10$...` 等 BCrypt 哈希值不会被破坏。

3. 启动后端：
   ```bash
   cd backend
   mvn spring-boot:run
   ```

4. 验证：
   ```
   浏览器打开 http://localhost:8080/health
   应返回 {"status":"ok"}
   ```

### 方式三：打包为 JAR 部署到 Linux 服务器

```bash
# 本地打包
cd backend
mvn clean package -DskipTests

# 上传 JAR 到服务器
scp target/juzhi-api-1.0.0.jar user@server:/opt/juzhi/

# 服务器上运行（确保 MySQL 和 Redis 可达）
java -jar /opt/juzhi/juzhi-api-1.0.0.jar
```

## 接口说明

| 分类 | 路径 | 说明 |
|------|------|------|
| 健康 | `GET /health` | 健康检查（公开） |
| 就绪 | `GET /ready` | DB/Redis 就绪状态（公开） |
| 注册 | `POST /api/auth/register` | 用户注册（公开） |
| 登录 | `POST /api/auth/login` | 用户登录返回 JWT（公开） |
| 模型列表 | `GET /api/models` | AI 模型列表（需认证） |
| 钱包 | `GET /api/wallet` | 余额查询（需认证） |
| OpenAI 兼容 | `GET /v1/models` | OpenAI 格式模型列表 |
| AI 对话 | `POST /v1/chat/completions` | OpenAI 格式对话 |

## 项目结构

```
juzhiAPI/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/
│   │   └── com/aiplatform/
│   │       ├── config/         # 配置类（Security, CORS, JWT）
│   │       ├── controller/     # REST 控制器
│   │       ├── entity/         # 实体类
│   │       ├── service/        # 业务逻辑层
│   │       ├── mapper/         # MyBatis Mapper
│   │       ├── common/         # 通用响应 R / ErrorCode
│   │       ├── vo/             # 视图对象
│   │       └── dto/            # 数据传输对象
│   ├── src/main/resources/
│   │   ├── application.yml     # 主配置文件
│   │   └── db/schema.sql       # 自动初始化 SQL
│   ├── pom.xml                 # Maven 配置
│   └── Dockerfile              # 容器镜像构建
├── docker-compose.yml          # 一键编排（MySQL+Redis+App）
└── README.md                   # 本文档
```
