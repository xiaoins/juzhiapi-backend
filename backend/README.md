# Juzhi API Platform - 后端

AI 聚合站后端服务，提供用户认证、余额计费、API Key 管理、AI 模型转发等功能。

## 技术栈

- **Java 21** + **Spring Boot 3.2**
- **MyBatis Plus** - ORM
- **Spring Security + JWT** - 认证鉴权
- **MySQL 8.0** - 数据库
- **Redis** - 缓存/限流
- **OkHttp** - HTTP客户端(调用AI网关)

## 项目结构

```
src/main/java/com/aiplatform/
├── JuzhiApiApplication.java    # 启动类
├── common/                     # 统一响应、错误码
├── config/                     # 配置类(Security, MyBatis, CORS)
├── controller/                 # 控制器
│   ├── AuthController          # 注册/登录
│   ├── UserController          # 用户信息
│   ├── WalletController        # 钱包
│   ├── ApiKeyController        # API Key
│   ├── ChatController           # 网页聊天(SSE)
│   ├── ModelController         # 模型列表
│   ├── OpenAIController        # OpenAI兼容API(/v1/*)
│   └── HealthController        # 健康检查
├── admin/                      # 管理后台控制器
├── dto/                        # 请求DTO
├── entity/                     # 数据库实体
├── exception/                  # 异常处理
├── mapper/                     # MyBatis Mapper
├── service/                    # 服务接口
│   └── impl/                   # 服务实现
├── security/                   # JWT认证
├── utils/                      # 工具类
└── vo/                         # 响应VO
```

## 快速开始

### 1. 前置条件

- JDK 21+
- MySQL 8.0
- Redis
- Maven 3.8+

### 2. 初始化数据库

```bash
mysql -u root -p < src/resources/db/schema.sql
```

### 3. 配置环境变量

```bash
cp .env.example .env
# 编辑 .env 文件，填入实际配置
```

### 4. 启动项目

```bash
# 方式一: IDE 运行 JuzhiApiApplication.main()

# 方式二: 命令行
mvn spring-boot:run
```

## API 接口文档

### 认证模块 `/api/auth`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/register | 用户注册 |
| POST | /api/auth/login | 用户登录 |
| GET | /api/auth/me | 当前用户信息 |
| POST | /api/auth/logout | 退出登录 |

### 钱包模块 `/api/wallet`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/wallet | 钱包信息 |
| GET | /api/wallet/logs | 钱包流水 |

### API Key 模块 `/api/api-keys`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/api-keys | 创建Key |
| GET | /api/api-keys | Key列表 |
| DELETE | /api/api-keys/{id} | 删除Key |
| PUT | /api/api-keys/{id}/disable | 禁用Key |
| PUT | /api/api-keys/{id}/enable | 启用Key |

### 聊天模块 `/api/chat` (SSE流式)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/chat/session | 创建会话 |
| GET | /api/chat/sessions | 会话列表 |
| POST | /api/chat/send | 发送消息(流式) |
| GET | /api/chat/messages/{id} | 会话消息 |
| DELETE | /api/chat/session/{id} | 删除会话 |

### OpenAI 兼容 API `/v1/*`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /v1/models | 模型列表 |
| POST | /v1/chat/completions | 聊天补全(支持stream) |

### 管理后台 `/api/admin/*`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/admin/users | 用户列表 |
| PUT | /api/admin/users/{id}/status | 更新用户状态 |
| PUT | /api/admin/users/balance | 调整余额 |
| GET | /api/admin/models | 模型管理 |
| POST | /api/admin/models | 创建模型 |
| PUT | /api/admin/models/{id} | 编辑模型 |
| DELETE | /api/admin/models/{id} | 删除模型 |
| GET | /api/admin/logs | 调用日志 |
| GET | /api/admin/orders | 订单管理 |
| PUT | /api/admin/orders/{id}/process | 处理订单 |

### 健康检查

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /health | 存活检查 |
| GET | /ready | 就绪检查(DB+Redis) |

## 默认管理员账号

- **用户名**: `admin`
- **密码**: `admin123`

> 首次部署后请立即修改密码！

## 计费说明

- 内部单位: credits (1元 = 10000 credits)
- 扣费公式: `费用 = input_tokens/1000 * input_price + output_tokens/1000 * output_price`
- 流式调用: 首字返回后扣费
