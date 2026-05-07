# AI 聚合站 V1 需求与设计文档

| 属性 | 内容 |
|------|------|
| 项目定位 | 低成本 AI 聚合 API 平台 |
| 目标用户 | 学生、程序员、AI 工具用户、小型团队 |
| 核心能力 | 网页聊天 + 用户 API Key + 余额计费 + 多模型聚合 + 外部工具接入 |
| 文档版本 | V1.0 |
| 文档状态 | 草稿 |

---

## 目录

- [一、项目背景](#一项目背景)
- [二、项目目标](#二项目目标)
- [三、用户角色](#三用户角色)
- [四、核心业务流程](#四核心业务流程)
- [五、功能需求](#五功能需求)
- [六、非功能需求](#六非功能需求)
- [七、系统架构设计](#七系统架构设计)
- [八、系统模块设计](#八系统模块设计)
- [九、数据库设计](#九数据库设计)
- [十、核心接口设计](#十核心接口设计)
- [十一、外部工具接入设计](#十一外部工具接入设计)
- [十二、计费设计](#十二计费设计)
- [十三、部署设计](#十三部署设计)
- [十四、Nginx 路由设计](#十四nginx-路由设计)
- [十五、V1 页面设计](#十五v1-页面设计)
- [十六、开发计划](#十六开发计划)
- [十七、推荐开发顺序](#十七推荐开发顺序)
- [十八、V1 不建议做的功能](#十八v1-不建议做的功能)
- [十九、项目风险](#十九项目风险)
- [二十、V2 扩展方向](#二十v2-扩展方向)
- [二十一、项目名称建议](#二十一项目名称建议)
- [二十二、V1 最小可交付版本总结](#二十二v1-最小可交付版本总结)

---

# 一、项目背景

目前很多国内用户想使用 Claude、OpenAI、Gemini、DeepSeek、通义、豆包等模型，但存在以下问题：

1. 国外平台注册、支付、网络使用门槛较高
2. 不同 AI 平台 API 使用方式不同
3. 很多用户不会配置 Claude Code、OpenCode、Continue、Cherry Studio 等工具
4. 个人用户希望用一个统一入口调用多个模型
5. 小团队希望有统一的 API Key、余额、账单、调用记录

因此，本项目希望做一个低成本 AI 聚合平台，为用户提供：

1. 网页端 AI 聊天
2. 统一 API 接入地址
3. 用户独立 API Key
4. 多模型选择
5. 余额充值与 token 扣费
6. 调用日志查询
7. 管理后台配置模型、价格、用户额度

---

# 二、项目目标

## 2.1 V1 核心目标

V1 不追求大而全，先完成最小商业闭环：

```
用户注册
↓
用户充值 / 后台赠送额度
↓
用户获取 API Key
↓
用户在网站或外部工具中使用
↓
系统按 token 扣费
↓
后台查看用户、余额、调用日志
```

## 2.2 项目定位

本项目不是单纯做聊天网站，而是做：

> AI API 聚合平台 + 用户计费系统 + 工具接入服务

核心卖点：

1. 一个 API Key 使用多个模型
2. 国内用户更容易使用
3. 支持 Claude Code、OpenCode、Cherry Studio、Continue 等工具
4. 支持网页聊天
5. 支持余额计费
6. 支持学生和开发者场景

---

# 三、用户角色

## 3.1 普通用户

普通用户主要使用平台：

1. 注册登录
2. 查看余额
3. 创建 API Key
4. 在网页聊天
5. 在外部工具中配置 API
6. 查看调用记录
7. 充值购买额度

## 3.2 管理员

管理员负责平台运营：

1. 管理用户
2. 给用户充值 / 扣费
3. 禁用异常用户
4. 管理模型
5. 设置模型价格
6. 查看平台调用日志
7. 配置上游 API 渠道
8. 查看系统消耗和收益

---

# 四、核心业务流程

## 4.1 用户网页聊天流程

```
用户登录
↓
进入聊天页面
↓
选择模型
↓
输入问题
↓
后端检查余额
↓
调用 AI 网关
↓
返回模型结果
↓
统计 token
↓
扣除余额
↓
保存聊天记录
```

## 4.2 用户 API Key 使用流程

```
用户登录平台
↓
进入 API Key 管理页面
↓
创建 API Key
↓
复制 Base URL 和 API Key
↓
配置到 Claude Code / OpenCode / Cherry Studio / Continue 等工具
↓
工具请求你的 API 地址
↓
系统识别用户身份
↓
检查余额和模型权限
↓
转发请求到 AI 网关
↓
返回结果
↓
统计 token 并扣费
```

## 4.3 管理员配置模型流程

```
管理员登录后台
↓
添加模型
↓
设置模型名称
↓
设置模型供应商
↓
设置输入 token 单价
↓
设置输出 token 单价
↓
设置是否启用
↓
用户端可选择该模型
```

---

# 五、功能需求

## 5.1 用户系统

### 功能说明

用户可以注册、登录、查看个人信息、查看余额和 API Key。

### 功能列表

1. 用户注册
2. 用户登录
3. 用户退出登录
4. 修改密码
5. 查看个人资料
6. 查看账户余额
7. 查看累计消耗
8. 查看 API Key

### 字段设计

| 字段 | 说明 |
|------|------|
| 用户ID | 主键 |
| 用户名 | 唯一 |
| 手机号 / 邮箱 | 登录凭证 |
| 密码 | 加密存储 |
| 头像 | URL |
| 角色 | USER / ADMIN |
| 状态 | 正常 / 禁用 |
| 注册时间 | — |
| 最后登录时间 | — |

---

## 5.2 钱包余额系统

### 功能说明

每个用户拥有一个账户余额，调用模型时按照 token 消耗扣费。

### 功能列表

1. 查看余额
2. 后台增加余额
3. 后台扣除余额
4. 调用 AI 自动扣费
5. 查看余额变动记录
6. 余额不足时禁止调用

### 余额单位建议

建议不要直接用"元"作为内部单位，而是用 **credits（点数）**：

```
1 元 = 10000 credits
```

这样后期做套餐、折扣、赠送额度会更方便。

---

## 5.3 API Key 管理系统

### 功能说明

用户可以创建自己的 API Key，用于外部工具接入。

### 功能列表

1. 创建 API Key
2. 查看 API Key
3. 删除 API Key
4. 禁用 API Key
5. 设置 API Key 名称
6. 查看 API Key 调用次数
7. 查看 API Key 消耗金额

### API Key 示例

```
Base URL:
https://api.yourdomain.com/v1

API Key:
sk-user_xxxxxxxxxxxxxxxxxxxxx
```

### 外部工具配置示例

```
OPENAI_API_BASE=https://api.yourdomain.com/v1
OPENAI_API_KEY=sk-user_xxxxxxxxxxxxxxxxxxxxx
```

---

## 5.4 网页聊天系统

### 功能说明

用户可以在网页端直接使用 AI 聊天。

### 功能列表

1. 新建对话
2. 删除对话
3. 选择模型
4. 发送消息
5. 流式输出
6. 保存聊天记录
7. 查看历史会话
8. 复制回答
9. 清空上下文

### V1 范围说明

**V1 先支持：**

- 普通文本对话
- 流式输出
- 历史记录
- 模型切换

**暂时不做：**

- 图片生成
- 文件上传
- 知识库
- Agent
- 插件系统

---

## 5.5 模型管理系统

### 功能说明

管理员可以配置平台可用模型。

### 模型类型

1. DeepSeek
2. 通义千问
3. 豆包
4. OpenAI
5. Claude
6. Gemini
7. 其他 OpenAI 兼容模型

### 模型字段

| 字段 | 说明 |
|------|------|
| 模型ID | 主键 |
| 模型显示名称 | 前端展示用 |
| 模型真实名称 | 调用上游时使用 |
| 供应商 | DeepSeek / OpenAI 等 |
| 输入 token 单价 | credits / 1K tokens |
| 输出 token 单价 | credits / 1K tokens |
| 倍率 | 价格倍率系数 |
| 是否启用 | 控制用户端是否可见 |
| 是否推荐 | 推荐模型标记 |
| 排序 | 显示排序 |
| 创建时间 | — |
| 更新时间 | — |

### 示例

```
显示名称：DeepSeek V3
真实模型：deepseek-chat
供应商：DeepSeek
输入价格：0.001 credits / 1K tokens
输出价格：0.002 credits / 1K tokens
状态：启用
```

---

## 5.6 Token 计费系统

### 功能说明

系统根据模型调用产生的 token 数量进行扣费。

### 扣费公式

```
总费用 = 输入 token 费用 + 输出 token 费用

输入 token 费用 = 输入 token 数量 / 1000 × 输入单价
输出 token 费用 = 输出 token 数量 / 1000 × 输出单价
```

### 计费举例

```
输入 token：2000
输出 token：1000

输入单价：2 credits / 1K tokens
输出单价：8 credits / 1K tokens

费用 = 2000 / 1000 × 2 + 1000 / 1000 × 8
费用 = 4 + 8
费用 = 12 credits
```

---

## 5.7 API 转发系统

### 功能说明

系统需要兼容外部 AI 工具调用。V1 优先支持 OpenAI 兼容协议。

### V1 必须支持的接口

```
GET  /v1/models
POST /v1/chat/completions
```

### 后续版本支持

```
POST /v1/messages
POST /v1/embeddings
POST /v1/images/generations
```

### 请求流程

```
外部工具请求 /v1/chat/completions
↓
读取 Authorization Bearer Token
↓
识别用户 API Key
↓
检查用户状态
↓
检查 API Key 状态
↓
检查余额
↓
检查模型是否可用
↓
转发请求到 New API / One API / LiteLLM
↓
接收返回结果
↓
统计 token
↓
扣除余额
↓
保存调用日志
↓
返回结果给外部工具
```

---

## 5.8 调用日志系统

### 功能说明

每次 AI 调用都要记录，方便用户和管理员查看。

### 用户可查看

| 字段 | 说明 |
|------|------|
| 调用时间 | — |
| 使用模型 | — |
| 输入 token | — |
| 输出 token | — |
| 消耗 credits | — |
| 调用状态 | 成功 / 失败 |

### 管理员可查看

| 字段 | 说明 |
|------|------|
| 用户ID | — |
| API Key | — |
| 模型 | — |
| 上游渠道 | — |
| 请求时间 | — |
| 响应时间 | — |
| token 消耗 | — |
| 扣费金额 | — |
| 是否成功 | — |
| 错误信息 | 失败时记录 |

---

## 5.9 充值订单系统

### V1 简化版

V1 可以先不接自动支付，先用人工充值。

```
用户联系管理员付款
↓
管理员后台给用户加 credits
↓
系统生成充值记录
```

### V2 再接支付

后期可以接：支付宝、微信支付、易支付、虎皮椒、Stripe。

---

## 5.10 管理后台

管理后台模块：

1. 用户管理
2. 余额管理
3. 订单管理
4. API Key 管理
5. 模型管理
6. 调用日志
7. 渠道管理
8. 系统配置

---

# 六、非功能需求

## 6.1 性能需求

1. 普通请求响应时间小于 3 秒
2. 流式输出首字响应时间小于 5 秒
3. 支持 100 个用户同时在线
4. V1 支持日调用 1 万次以内

## 6.2 安全需求

1. 用户密码必须加密存储（bcrypt）
2. API Key 不明文完整展示，数据库存 hash 值
3. 后台接口必须鉴权
4. 防止用户恶意刷接口
5. 限制单用户并发请求数
6. 限制单 API Key 每分钟请求数
7. 敏感配置放在环境变量中

## 6.3 风控需求

1. 用户余额不足禁止调用
2. 用户状态异常禁止调用
3. API Key 被禁用禁止调用
4. 单个用户设置每日最大消耗
5. 单个 API Key 设置最大请求频率
6. 异常高频请求自动限制

---

# 七、系统架构设计

## 7.1 总体架构

```
用户浏览器
    ↓
Vue3 前端
    ↓
Nginx
    ↓
Spring Boot 后端
    ↓
Redis / MySQL
    ↓
AI 网关 New API / One API / LiteLLM
    ↓
上游模型 API
```

## 7.2 技术栈

### 前端

| 技术 | 说明 |
|------|------|
| Vue 3 | 前端框架 |
| Vite | 构建工具 |
| Element Plus | UI 组件库 |
| Pinia | 状态管理 |
| Vue Router | 路由 |
| Axios | HTTP 请求 |
| Markdown 渲染 | 消息展示 |
| 代码高亮 | 代码块展示 |

### 后端

| 技术 | 说明 |
|------|------|
| Java 21 | 语言 |
| Spring Boot 3 | 框架 |
| Spring Security | 安全鉴权 |
| MyBatis Plus | ORM |
| JWT | Token 认证 |
| Redis | 缓存 / 限流 |
| MySQL 8 | 数据库 |

### AI 网关（三选一）

推荐优先使用 **New API** 或 **One API**，原因：中文资料多、部署简单、后台现成、适合个人快速上线。

- New API
- One API
- LiteLLM

### 部署

Docker + Docker Compose + Nginx + HTTPS + Linux 云服务器

---

# 八、系统模块设计

## 8.1 前端模块

```
src/
├── api/
│   ├── user.js
│   ├── chat.js
│   ├── wallet.js
│   ├── apiKey.js
│   └── model.js
│
├── views/
│   ├── Login.vue
│   ├── Register.vue
│   ├── Chat.vue
│   ├── Dashboard.vue
│   ├── ApiKey.vue
│   ├── Wallet.vue
│   └── Admin.vue
│
├── components/
│   ├── ChatBox.vue
│   ├── ModelSelect.vue
│   ├── MessageItem.vue
│   └── ApiKeyCard.vue
│
├── router/
├── store/
└── utils/
```

## 8.2 后端模块

```
com.xxx.aiplatform
├── controller
│   ├── AuthController
│   ├── UserController
│   ├── ChatController
│   ├── ApiKeyController
│   ├── WalletController
│   ├── ModelController
│   └── AdminController
│
├── service
│   ├── AuthService
│   ├── UserService
│   ├── ChatService
│   ├── ApiKeyService
│   ├── WalletService
│   ├── ModelService
│   └── BillingService
│
├── mapper
├── entity
├── dto
├── vo
├── config
├── security
├── common
└── utils
```

---

# 九、数据库设计

## 9.1 user 用户表

```sql
CREATE TABLE user (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    username    VARCHAR(50)  NOT NULL,
    email       VARCHAR(100),
    phone       VARCHAR(30),
    password    VARCHAR(255) NOT NULL,
    avatar      VARCHAR(255),
    role        VARCHAR(20)  DEFAULT 'USER',
    status      TINYINT      DEFAULT 1,
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email)
);
```

## 9.2 wallet 钱包表

```sql
CREATE TABLE wallet (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT NOT NULL,
    balance         BIGINT DEFAULT 0,
    total_recharge  BIGINT DEFAULT 0,
    total_used      BIGINT DEFAULT 0,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_id (user_id)
);
```

> 说明：`balance` 使用整数 credits，避免小数金额误差。

## 9.3 wallet_log 钱包流水表

```sql
CREATE TABLE wallet_log (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL,
    type            VARCHAR(30)  NOT NULL,
    amount          BIGINT       NOT NULL,
    before_balance  BIGINT       NOT NULL,
    after_balance   BIGINT       NOT NULL,
    remark          VARCHAR(255),
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP
);
```

`type` 枚举值：`RECHARGE` / `CONSUME` / `ADMIN_ADD` / `ADMIN_DEDUCT` / `REFUND`

## 9.4 api_key 用户 API Key 表

```sql
CREATE TABLE api_key (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id      BIGINT       NOT NULL,
    name         VARCHAR(100),
    api_key      VARCHAR(255) NOT NULL,
    key_prefix   VARCHAR(50),
    status       TINYINT      DEFAULT 1,
    last_used_at DATETIME,
    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_api_key (api_key)
);
```

> 安全建议：`api_key` 字段存储 hash 值，不存明文。页面仅在创建时完整展示一次。

## 9.5 ai_model 模型表

```sql
CREATE TABLE ai_model (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    display_name  VARCHAR(100) NOT NULL,
    model_name    VARCHAR(100) NOT NULL,
    provider      VARCHAR(50)  NOT NULL,
    input_price   BIGINT       DEFAULT 0,
    output_price  BIGINT       DEFAULT 0,
    sort          INT          DEFAULT 0,
    enabled       TINYINT      DEFAULT 1,
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## 9.6 chat_session 对话会话表

```sql
CREATE TABLE chat_session (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    title       VARCHAR(255),
    model_name  VARCHAR(100),
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## 9.7 chat_message 对话消息表

```sql
CREATE TABLE chat_message (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id  BIGINT       NOT NULL,
    user_id     BIGINT       NOT NULL,
    role        VARCHAR(20)  NOT NULL,
    content     TEXT         NOT NULL,
    model_name  VARCHAR(100),
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP
);
```

## 9.8 api_usage_log 调用日志表

```sql
CREATE TABLE api_usage_log (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id           BIGINT       NOT NULL,
    api_key_id        BIGINT,
    model_name        VARCHAR(100),
    provider          VARCHAR(50),
    prompt_tokens     INT          DEFAULT 0,
    completion_tokens INT          DEFAULT 0,
    total_tokens      INT          DEFAULT 0,
    cost              BIGINT       DEFAULT 0,
    status            VARCHAR(30),
    error_message     TEXT,
    request_ip        VARCHAR(100),
    created_at        DATETIME     DEFAULT CURRENT_TIMESTAMP
);
```

## 9.9 recharge_order 充值订单表

```sql
CREATE TABLE recharge_order (
    id        BIGINT          PRIMARY KEY AUTO_INCREMENT,
    user_id   BIGINT          NOT NULL,
    order_no  VARCHAR(100)    NOT NULL,
    amount    DECIMAL(10, 2)  NOT NULL,
    credits   BIGINT          NOT NULL,
    status    VARCHAR(30)     DEFAULT 'PENDING',
    pay_type  VARCHAR(30),
    paid_at   DATETIME,
    created_at DATETIME       DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no)
);
```

---

# 十、核心接口设计

## 10.1 用户认证接口

```
POST /api/auth/register
POST /api/auth/login
POST /api/auth/logout
GET  /api/auth/me
```

## 10.2 用户钱包接口

```
GET  /api/wallet
GET  /api/wallet/logs
```

## 10.3 API Key 接口

```
GET    /api/api-keys
POST   /api/api-keys
DELETE /api/api-keys/{id}
PUT    /api/api-keys/{id}/disable
PUT    /api/api-keys/{id}/enable
```

## 10.4 聊天接口

```
POST   /api/chat/session
GET    /api/chat/session
POST   /api/chat/send
GET    /api/chat/messages/{sessionId}
DELETE /api/chat/session/{sessionId}
```

## 10.5 OpenAI 兼容接口

```
GET  /v1/models
POST /v1/chat/completions
```

请求头：

```
Authorization: Bearer sk-user_xxxxxxxxxxxxx
```

请求体示例：

```json
{
  "model": "deepseek-chat",
  "messages": [
    {
      "role": "user",
      "content": "帮我解释这段 Java 代码"
    }
  ],
  "stream": true
}
```

---

# 十一、外部工具接入设计

## 11.1 提供给用户的配置

平台页面需要展示：

```
API 地址：https://api.yourdomain.com/v1
API Key：sk-user_xxxxxxxxxxxxx
兼容协议：OpenAI Compatible API
```

## 11.2 Cherry Studio 配置

```
Provider：OpenAI Compatible
API Host：https://api.yourdomain.com/v1
API Key：sk-user_xxxxxxxxxxxxx
Model：deepseek-chat / qwen-plus / gpt-xxx
```

## 11.3 Continue 配置

```json
{
  "models": [
    {
      "title": "My AI Gateway",
      "provider": "openai",
      "model": "deepseek-chat",
      "apiBase": "https://api.yourdomain.com/v1",
      "apiKey": "sk-user_xxxxxxxxxxxxx"
    }
  ]
}
```

## 11.4 OpenCode / Claude Code 说明

这类工具需确认是否支持：

1. 自定义 Base URL
2. 自定义 API Key
3. OpenAI Compatible API
4. Anthropic Compatible API

V1 先支持 OpenAI Compatible，V2 再增加 Anthropic Compatible（`POST /v1/messages`）。

---

# 十二、计费设计

## 12.1 内部单位

```
1 元 = 10000 credits
```

## 12.2 套餐设计

| 套餐 | 价格 | Credits |
|------|------|---------|
| 体验套餐 | 9.9 元 | 100,000 |
| 学生套餐 | 29 元 | 350,000 |
| 标准套餐 | 59 元 | 750,000 |
| 高级套餐 | 99 元 | 1,400,000 |

## 12.3 模型价格策略

| 模型 | 定位 | 价格策略 |
|------|------|----------|
| DeepSeek | 低成本主力模型 | 低价 |
| 通义千问 | 国内稳定模型 | 低价 |
| 豆包 | 国内低成本模型 | 低价 |
| Gemini Flash | 快速模型 | 中低价 |
| Claude Sonnet | 高质量模型 | 高价 |
| GPT 高级模型 | 高质量模型 | 高价 |

---

# 十三、部署设计

## 13.1 最小部署组件

1. MySQL
2. Redis
3. Spring Boot 后端
4. Vue3 前端
5. Nginx
6. New API / One API

## 13.2 Docker Compose 示例

```yaml
version: "3.8"

services:
  mysql:
    image: mysql:8.0
    container_name: ai_mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: root123456
      MYSQL_DATABASE: ai_platform
    ports:
      - "3306:3306"
    volumes:
      - ./mysql/data:/var/lib/mysql

  redis:
    image: redis:7
    container_name: ai_redis
    restart: always
    ports:
      - "6379:6379"

  new-api:
    image: calciumion/new-api:latest
    container_name: ai_new_api
    restart: always
    ports:
      - "3000:3000"
    volumes:
      - ./new-api/data:/data

  backend:
    image: your-ai-backend:latest
    container_name: ai_backend
    restart: always
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/ai_platform
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root123456
      SPRING_REDIS_HOST: redis
      AI_GATEWAY_URL: http://new-api:3000
    depends_on:
      - mysql
      - redis
      - new-api

  frontend:
    image: nginx:latest
    container_name: ai_frontend
    restart: always
    ports:
      - "80:80"
    volumes:
      - ./frontend/dist:/usr/share/nginx/html
      - ./nginx/default.conf:/etc/nginx/conf.d/default.conf
```

---

# 十四、Nginx 路由设计

```
https://yourdomain.com        → 前端页面
https://yourdomain.com/api    → Spring Boot 后端
https://api.yourdomain.com/v1 → OpenAI 兼容 API 接口
```

也可统一为：

```
https://yourdomain.com/v1
```

---

# 十五、V1 页面设计

## 15.1 用户端页面

1. 登录页
2. 注册页
3. 首页
4. 聊天页
5. API Key 页面
6. 余额页面
7. 调用记录页面
8. 使用文档页面

## 15.2 管理端页面

1. 管理后台首页
2. 用户管理
3. 余额管理
4. 模型管理
5. API Key 管理
6. 调用日志
7. 订单管理
8. 系统配置

---

# 十六、开发计划

## 第一阶段：基础环境搭建

**目标：** 项目能跑起来。

**任务：**
1. 创建 Spring Boot 3 项目
2. 创建 Vue3 + Vite 项目
3. 创建 MySQL 数据库
4. 创建 Redis 容器
5. 创建 Docker Compose
6. 部署 New API / One API
7. 配置 Nginx

**产出：** 后端基础项目 + 前端基础项目 + 数据库初始化脚本 + Docker Compose 文件

---

## 第二阶段：用户与登录系统

**目标：** 用户可以注册登录。

**任务：**
1. 用户注册接口
2. 用户登录接口
3. JWT 鉴权
4. 用户信息接口
5. 前端登录页面
6. 前端注册页面
7. 路由权限控制

**产出：** 用户可以正常注册、登录、退出。

---

## 第三阶段：钱包与 API Key

**目标：** 用户拥有余额和 API Key。

**任务：**
1. 创建钱包表
2. 注册时自动创建钱包
3. 钱包余额查询
4. 钱包流水查询
5. 创建 API Key
6. 删除 API Key
7. 禁用 API Key
8. API Key 页面开发

**产出：** 用户可以创建自己的 API Key，并查看余额。

---

## 第四阶段：模型管理与价格管理

**目标：** 管理员可以配置模型价格。

**任务：**
1. 模型表设计
2. 模型新增
3. 模型编辑
4. 模型启用 / 禁用
5. 模型价格设置
6. 前端模型选择组件

**产出：** 平台可以动态管理模型。

---

## 第五阶段：网页聊天功能

**目标：** 用户可以在网页端聊天。

**任务：**
1. 聊天会话表
2. 聊天消息表
3. 创建会话接口
4. 发送消息接口
5. 调用 AI 网关
6. 流式输出
7. 保存聊天记录
8. 前端聊天页面

**产出：** 用户可以在网页端选择模型并聊天。

---

## 第六阶段：OpenAI 兼容 API

**目标：** 外部工具可以调用你的平台。

**任务：**
1. 实现 `GET /v1/models`
2. 实现 `POST /v1/chat/completions`
3. 解析 Authorization Bearer Token
4. 识别用户 API Key
5. 检查余额
6. 转发请求到 AI 网关
7. 返回 OpenAI 兼容格式
8. 支持 stream 流式输出

**产出：** Cherry Studio、Continue 等工具可以接入。

---

## 第七阶段：Token 统计与扣费

**目标：** 调用后自动扣费。

**任务：**
1. 获取 prompt_tokens
2. 获取 completion_tokens
3. 获取 total_tokens
4. 根据模型价格计算费用
5. 扣除用户余额
6. 记录钱包流水
7. 记录调用日志
8. 余额不足禁止调用

**产出：** 系统具备商业闭环。

---

## 第八阶段：管理后台

**目标：** 管理员能运营平台。

**任务：**
1. 用户管理页面
2. 用户余额调整
3. 模型管理页面
4. 调用日志页面
5. API Key 管理页面
6. 订单管理页面
7. 后台权限控制

**产出：** 平台可以基本运营。

---

## 第九阶段：上线部署

**目标：** 部署到云服务器。

**任务：**
1. 购买服务器
2. 安装 Docker
3. 上传项目
4. 启动 Docker Compose
5. 配置域名
6. 配置 HTTPS
7. 测试网站聊天
8. 测试 API 调用
9. 测试扣费

**产出：** 平台正式上线。

---

# 十七、推荐开发顺序

```
1.  New API / One API 先跑通
2.  手动配置一个模型
3.  用 Postman 测试模型调用
4.  Spring Boot 做用户登录
5.  Spring Boot 做 API Key
6.  Spring Boot 做余额
7.  实现 /v1/chat/completions
8.  实现扣费
9.  Vue3 做用户页面
10. Vue3 做管理后台
11. Docker 部署
12. 域名上线
```

---

# 十八、V1 不建议做的功能

为了避免项目做崩，V1 暂不实现以下功能：

1. 知识库
2. Agent
3. 插件市场
4. 多租户企业空间
5. 复杂优惠券
6. 自动分销
7. 图片生成
8. 文件解析
9. 语音识别
10. 工作流编排

**先把核心闭环做通：**

```
用户 → 充值 → API Key → 调用 → 扣费
```

---

# 十九、项目风险

## 19.1 技术风险

| 风险 | 解决方案 |
|------|----------|
| 流式输出处理复杂 | V1 先只做 OpenAI 兼容 |
| token 统计可能不准确 | 先接 2-3 个模型，充分测试 |
| 外部工具兼容性不同 | 保留完整调用日志 |
| 不同模型返回格式不一致 | 网关层统一适配 |
| 高并发下余额扣减重复 | 扣费逻辑加事务 + Redis 限流 |

## 19.2 运营风险

| 风险 | 解决方案 |
|------|----------|
| 用户滥用 API | 设置用户每日限额 |
| 余额消耗争议 | 保留完整调用日志 |
| 上游模型价格变化 | 设置模型单独权限，方便快速调价 |
| 上游服务不稳定 | 多渠道备份，后台可快速切换 |
| 用户大量并发导致成本失控 | API Key 限流 + 每日消耗上限 |

---

# 二十、V2 扩展方向

V1 做完后，可以升级：

1. 支付宝 / 微信自动充值
2. Anthropic Compatible API
3. Claude Code 专用配置教程
4. OpenCode 专用配置教程
5. 用户套餐系统
6. 邀请返利系统
7. 团队空间
8. 企业 API Key
9. 知识库问答
10. AI 编程助手模板
11. AI 毕设助手模板
12. AI 电商文案助手

---

# 二十一、项目名称建议

面向程序员方向的名称参考：

| 名称 | 风格 |
|------|------|
| 码伴 AI | 亲切、面向开发者 |
| CodeMate AI | 英文，国际化 |
| 轻舟 AI | 轻量、快速 |
| 聚智 API | 强调聚合 |
| 星桥 AI | 有设计感 |
| 智汇 API | 强调汇聚 |

**推荐：码伴 AI**

- 定位：面向学生和初级程序员的 AI 编程助手与 API 聚合平台
- 宣传语：一个 Key，连接多个 AI 模型。

---

# 二十二、V1 最小可交付版本总结

V1 只需完成以下功能，即可上线测试并开始收费：

| 序号 | 功能模块 |
|------|----------|
| 1 | 用户注册登录 |
| 2 | 用户余额 |
| 3 | API Key 创建 |
| 4 | 模型列表 |
| 5 | 网页聊天 |
| 6 | OpenAI 兼容 API |
| 7 | token 扣费 |
| 8 | 调用日志 |
| 9 | 管理后台 |
| 10 | Docker 部署 |
