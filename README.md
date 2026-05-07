# Juzhi API Platform (AI 聚合站)

AI API 聚合中转平台 — 统一接口调用多家 AI 模型（OpenAI / DeepSeek / 通义千问 / Gemini / Claude / 豆包等）

## 功能特性

- 统一 OpenAI 兼容 API（`/v1/chat/completions`），支持 ChatGPT / Cursor / 其他 AI 客户端直连
- 用户注册登录 + JWT 认证
- 余额充值与消耗计费（按 token 精确计费）
- API Key 管理（多 Key 隔离，独立计费）
- 管理后台（用户管理 / 模型配置 / 订单查看 / 用量日志）
- Redis 缓存 + 限流保护

## 技术栈

| 组件 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.2.5 |
| Java | JDK | 21 |
| ORM | MyBatis Plus | 3.5.5 |
| 安全认证 | Spring Security + JWT | - |
| 数据库 | MySQL | 8.0+ |
| 缓存 | Redis | 7.x |

## 快速开始

### 方式一：Docker Compose 一键部署（推荐）

> 适用于：服务器部署 / 不想手动装环境的场景，一条命令启动全部服务。

```bash
# 1. 克隆项目
git clone https://github.com/xiaoins/juzhiapi-backend.git
cd juzhiapi-backend

# 2. 一键构建并启动全部服务（MySQL + Redis + 后端 API）
docker compose up -d --build

# 3. 等待约 30 秒让服务完全启动，查看日志确认状态
docker compose logs -f app
```

当日志中出现 `Started JuzhiApiApplication` 即表示启动成功。

验证服务：
```bash
curl http://localhost:8080/health
# 预期返回: {"status":"ok"}
```

默认管理员账号：
| 账号 | 密码 | 角色 | 初始余额 |
|------|------|------|----------|
| `admin` | `password` | 管理员 | 1,000,000 credits |

> **首次登录后请及时修改密码！**

常用管理命令：
```bash
docker compose logs -f app        # 查看应用实时日志
docker compose ps                  # 查看所有容器状态
docker compose down                # 停止并删除容器
docker compose down -v             # 停止容器并清除数据卷（⚠️ 会删除数据库数据）
```

### 方式二：本地开发运行（Windows / Mac / Linux）

> 适用于：开发者本地调试、IDE 开发、二次开发。

#### 前置环境

| 工具 | 版本要求 | 安装方式 |
|------|----------|----------|
| **JDK** | 21 或更高 | [下载](https://adoptium.net/) 或 `sdk install java 21-open` |
| **Maven** | 3.8+ | [下载](https://maven.apache.org/download.cgi) 或 IDE 自带 |
| **MySQL** | 8.0+ | [下载](https://dev.mysql.com/downloads/) 或 Docker 运行 |
| **Redis** | 7.x | [下载](https://redis.io/download) 或 Docker 运行 |

如果不想单独安装 MySQL 和 Redis，可以用 Docker 快速启动：

```bash
# 启动 MySQL
docker run -d --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=sulei1124 mysql:8.0 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci

# 启动 Redis
docker run -d --name redis -p 6379:6379 redis:7-alpine
```

#### 启动步骤

```bash
# 1. 克隆项目
git clone https://github.com/xiaoins/juzhiapi-backend.git
cd juzhiapi-backend/backend

# 2. 创建数据库（只需执行一次）
mysql -u root -psulei1124 -e "CREATE DATABASE IF NOT EXISTS ai_platform DEFAULT CHARACTER SET utf8mb4;"

# 3. 启动项目（数据库表和初始数据会自动初始化，无需手动导入 SQL）
mvn spring-boot:run
```

看到以下输出即表示启动成功：
```
Started JuzhiApiApplication in x.xxx seconds
```

> **为什么不需要手动导入 schema.sql？**
>
> 项目在 `application.yml` 中配置了 `spring.sql.init.mode=always`，Spring Boot 启动时会通过 JDBC 直接读取 SQL 文件内容发送给 MySQL 执行。这种方式完全不经过 shell，因此 `$2a$10$...` 等 BCrypt 密码哈希值中的 `$` 符号不会被任何 shell 解析破坏。兼容 Windows PowerShell、Linux Bash、macOS Zsh 等所有环境。

#### 验证

浏览器打开 http://localhost:8080/health ，应返回：
```json
{"status":"ok"}
```

或测试完整登录流程：
```bash
# 注册用户
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456","email":"test@test.com"}'

# 登录获取 Token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'
```

### 方式三：打包 JAR 部署到 Linux 服务器

```bash
# 1. 本地打包
cd backend
mvn clean package -DskipTests
# 生成的 JAR 文件: target/juzhi-api-1.0.0.jar

# 2. 上传到服务器
scp target/juzhi-api-1.0.0.jar user@your-server-ip:/opt/juzhi/

# 3. 在服务器上运行（确保服务器上 MySQL 和 Redis 可达）
ssh user@your-server-ip
java -jar /opt/juzhi/juzhi-api-1.0.0.jar

# 4. 推荐使用 nohup 后台运行
nohup java -jar /opt/juzhi/juzhi-api-1.0.0.jar > app.log 2>&1 &

# 5. 或使用 systemd 管理进程（开机自启）
sudo vim /etc/systemd/system/juzhi-api.service
```

systemd 服务配置示例：
```ini
[Unit]
Description=Juzhi API Platform
After=network.target mysql.service redis.service

[Service]
Type=simple
User=root
ExecStart=/usr/bin/java -jar /opt/juzhi/juzhi-api-1.0.0.jar
WorkingDirectory=/opt/juzhi
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

## 接口说明

### 公开接口（无需认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/health` | 健康检查 |
| GET | `/ready` | DB / Redis 就绪状态 |
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/login` | 用户登录（返回 JWT Token） |
| GET | `/v1/models` | OpenAI 格式模型列表 |

### 认证接口（需 Header: `Authorization: Bearer <token>`）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/models` | AI 模型列表（含价格） |
| GET | `/api/wallet` | 当前用户余额 |
| POST | `/v1/chat/completions` | AI 对话（OpenAI 兼容格式） |

### 管理员接口（需 ADMIN 角色）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/users` | 用户列表（分页） |
| PUT | `/api/admin/users/{id}/status` | 启用/禁用用户 |
| GET | `/api/admin/models` | 模型管理 |
| GET | `/api/admin/orders` | 充值订单 |
| GET | `/api/admin/logs` | API 调用日志 |

## 项目结构

```
juzhiapi-backend/
├── backend/                          # Spring Boot 后端主模块
│   ├── src/main/java/com/aiplatform/
│   │   ├── JuzhiApiApplication.java  # 启动类
│   │   ├── config/                   # 配置类
│   │   │   ├── SecurityConfig.java   # Spring Security 安全配置
│   │   │   ├── CorsConfig.java       # 跨域配置
│   │   │   └── MybatisPlusConfig.java
│   │   ├── controller/               # REST 控制器层
│   │   │   ├── AuthController.java   # 认证相关
│   │   │   ├── ModelController.java  # 模型管理
│   │   │   ├── WalletController.java # 钱包相关
│   │   │   ├── ChatController.java   # 对话相关
│   │   │   ├── OpenAIController.java # OpenAI 兼容接口
│   │   │   └── admin/                # 管理后台控制器
│   │   ├── service/                  # 业务逻辑层
│   │   │   └── impl/                 # 服务实现
│   │   ├── mapper/                   # MyBatis Plus Mapper
│   │   ├── entity/                   # 数据库实体
│   │   ├── dto/                      # 数据传输对象（请求参数）
│   │   ├── vo/                       # 视图对象（响应数据）
│   │   ├── common/                   # 通用工具
│   │   │   ├── R.java                # 统一响应封装
│   │   │   └── ErrorCode.java        # 错误码定义
│   │   ├── security/                 # JWT 安全组件
│   │   │   ├── JwtUtil.java          # JWT 工具
│   │   │   └── JwtAuthenticationFilter.java
│   │   ├── exception/                # 全局异常处理
│   │   └── utils/                    # 工具类
│   ├── src/main/resources/
│   │   ├── application.yml           # 主配置文件
│   │   └── db/schema.sql             # 数据库初始化脚本（自动执行）
│   ├── pom.xml                       # Maven 依赖配置
│   └── Dockerfile                    # Docker 镜像构建文件
├── docker-compose.yml                # Docker Compose 编排（一键全量部署）
├── .gitignore                        # Git 忽略规则
└── README.md                         # 本文档
```

## 配置说明

核心配置位于 `backend/src/main/resources/application.yml`：

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| server.port | 8080 | 服务端口 |
| spring.datasource.url | jdbc:mysql://localhost:3306/ai_platform | 数据库地址 |
| spring.datasource.username / password | root / sulei1124 | 数据库凭据 |
| spring.data.redis.host / port | localhost / 6379 | Redis 地址 |
| jwt.secret | (内置) | JWT 签名密钥（生产环境请修改） |
| jwt.expiration | 86400000ms (24h) | Token 有效期 |
| ai-gateway.url | http://localhost:3000 | AI 网关地址 |

环境变量覆盖（适用于 Docker / 生产部署）：
```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/ai_platform
export SPRING_DATA_REDIS_HOST=redis
export JWT_SECRET=your-production-secret-key
```

## 常见问题

**Q: 启动报错 `Failed to obtain JDBC Connection`？**
A: 确保 MySQL 已启动且端口可访问。如果是 MySQL 8.0，确认 JDBC URL 中包含 `allowPublicKeyRetrieval=true`。

**Q: admin 登录密码错误？**
A: 数据库通过 `schema.sql` 自动初始化时，默认密码为 `password`（不是 `admin123`）。

**Q: 如何修改管理员密码？**
A: 登录后在管理后台操作，或直接更新数据库：`UPDATE user SET password='新BCrypt哈希' WHERE username='admin';`

**Q: 如何切换为 HTTPS？**
A: 在 `application.yml` 中添加 SSL 配置，或在 Nginx 反向代理层处理 TLS 终结。
