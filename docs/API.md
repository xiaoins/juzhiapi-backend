# Juzhi API Platform - 接口文档

> **Base URL**: `http://localhost:8080`
> **认证方式**: JWT Bearer Token（Header: `Authorization: Bearer <token>`）
> **内容类型**: `application/json`
> **时间格式**: `yyyy-MM-dd HH:mm:ss`（如 `2024-01-15 10:30:00`）

---

## 目录

- [通用说明](#通用说明)
  - [统一响应格式](#统一响应格式)
  - [分页格式](#分页格式)
  - [错误码](#错误码)
  - [认证规则](#认证规则)
- [公开接口（无需认证）](#公开接口无需认证)
  - [健康检查](#1-健康检查)
  - [用户注册](#2-用户注册)
  - [用户登录](#3-用户登录)
  - [OpenAI 模型列表](#4-openai-模型列表-v1models)
- [用户接口（需JWT认证）](#用户接口需jwt认证)
  - [获取当前用户信息](#5-获取当前用户信息-apiauthme)
  - [修改密码](#6-修改密码-apiuserpassword)
  - [获取AI模型列表](#7-获取ai模型列表-apimodels)
  - [获取模型详情](#8-获取模型详情-apimodelsid)
  - [钱包信息](#9-钱包信息-apiwallet)
  - [钱包流水](#10-钱包流水-apiwalletlogs)
  - [创建充值订单](#11-创建充值订单-apiwalletrecharge)
  - [我的充值订单列表](#12-我的充值订单列表-apiwalletorders)
  - [API Key 管理](#13-api-key-管理apiaipkeys)
  - [聊天会话管理](#14-聊天会话管理-apichatsession)
  - [发送消息](#15-发送消息-apichatsend-sse流式)
  - [获取消息记录](#16-获取消息记录-apichatmessagessessionid)
- [OpenAI 兼容接口](#openai-兼容接口)
  - [聊天补全](#15-chat-completions-v1chatcompletions)
- [管理员接口（需ADMIN角色）](#管理员接口需admin角色)
  - [用户管理](#16-用户管理-apiadminusers)
  - [模型管理](#17-模型管理-apiadminmodels)
  - [订单管理](#18-订单管理-apiadminorders)
  - [调用日志](#19-调用日志-apiadminlogs)

---

## 通用说明

### 统一响应格式

所有接口返回统一的 `R<T>` 结构：

```json
{
  "code": 200,          // 状态码，200=成功，其他=失败
  "msg": "操作成功",     // 提示消息
  "data": { ... }       // 业务数据，部分接口为 null
}
```

### 分页格式

```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "records": [...],   // 数据列表
    "total": 100,        // 总记录数
    "current": 1,        // 当前页码
    "size": 20           // 每页大小
  }
}
```

**分页参数**（Query 参数，适用于所有列表接口）：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| current | int | 1 | 页码 |
| size | int | 20 | 每页条数 |

### 错误码

| code | 说明 |
|------|------|
| 200 | 操作成功 |
| 400 | 参数错误 |
| 401 | 未登录或Token已过期 |
| 403 | 无权限访问 |
| 402 | 余额不足，请先充值 |
| 429 | 请求过于频繁，请稍后再试 |
| 1001 | 用户不存在 |
| 1002 | 用户已被禁用 |
| 1003 | 密码错误 |
| 1004 | 用户名已存在 |
| 1005 | 邮箱已被注册 |
| 1006 | 原密码错误 |
| 2001 | 钱包不存在 |
| 2002 | 余额不足 |
| 3001 | API Key无效 |
| 3002 | API Key已被禁用 |
| 3003 | API Key调用次数已达上限 |
| 4001 | 模型不存在或已下线 |
| 4002 | 模型已停用 |
| 4003 | AI服务调用失败 |
| 5001 | 会话不存在 |
| 5002 | 会话不属于当前用户 |
| 6001 | 订单不存在 |
| 6002 | 订单状态异常 |
| 5000 | 服务器内部错误 |

### 认证规则

| 路径规则 | 认证方式 |
|----------|----------|
| `/api/auth/**` | 公开（无需Token） |
| `/v1/**` | 公开（使用API Key或JWT） |
| `/health`, `/ready` | 公开 |
| **其他所有路径** | 需要Header: `Authorization: Bearer <jwt_token>` |

---

## 公开接口（无需认证）

### 1. 健康检查

检查服务是否存活。

**请求**

```
GET /health
```

**响应示例**

```json
{ "status": "ok" }
```

---

### 2. 就绪检查

检查数据库和Redis连接状态。

**请求**

```
GET /ready
```

**响应示例**

```json
{
  "status": "ok",
  "checks": {
    "database": { "status": "ok" },
    "redis": { "status": "ok" }
  }
}
```

---

### 3. 用户注册

注册新用户账号。

**请求**

```
POST /api/auth/register
Content-Type: application/json
```

**请求体**

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| username | string | 是 | 3~50字符 | 用户名 |
| password | string | 是 | 6~50字符 | 密码 |
| email | string | 否 | - | 邮箱地址 |

**请求示例**

```json
{
  "username": "testuser",
  "password": "123456",
  "email": "test@example.com"
}
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

**错误响应**

```json
{ "code": 1004, "msg": "用户名已存在" }
{ "code": 1005, "msg": "邮箱已被注册" }
```

---

### 4. 用户登录

用户登录获取JWT Token。

**请求**

```
POST /api/auth/login
Content-Type: application/json
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名 |
| password | string | 是 | 密码 |

**请求示例**

```json
{
  "username": "admin",
  "password": "password"
}
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresIn": 86400,
    "user": {
      "id": 1,
      "username": "admin",
      "email": null,
      "phone": null,
      "avatar": null,
      "role": "ADMIN",
      "status": 1,
      "createdAt": "2024-01-15 10:30:00"
    }
  }
}
```

**响应字段 — UserInfoVO**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 用户ID |
| username | string | 用户名 |
| email | string? | 邮箱 |
| phone | string? | 手机号 |
| avatar | string? | 头像URL |
| role | string | 角色：`USER` 或 `ADMIN` |
| status | integer | 状态：1=正常, 0=禁用 |
| createdAt | datetime | 注册时间 |

> ⚠️ **注意**：获取到 token 后，后续所有需要认证的接口都应在 Header 中携带：
> ```
> Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
> ```

---

### 5. OpenAI 模型列表 (`/v1/models`)

返回 OpenAI 格式的可用模型列表，兼容 ChatGPT / Cursor 等客户端。

**请求**

```
GET /v1/models
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "object": "list",
    "data": [
      { "id": "deepseek-chat", "object": "model", "created": 1705312800, "owned_by": "deepseek" },
      { "id": "gpt-4o-mini", "object": "model", "created": 1705312800, "owned_by": "openai" },
      { "id": "claude-3-5-sonnet-20240620", "object": "model", "created": 1705312800, "owned_by": "anthropic" }
    ]
  }
}
```

---

## 用户接口（需JWT认证）

> 以下所有接口需要在 Header 中携带：`Authorization: Bearer <token>`

### 6. 获取当前用户信息

**请求**

```
GET /api/auth/me
Authorization: Bearer <token>
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "email": "admin@example.com",
    "phone": null,
    "avatar": null,
    "role": "ADMIN",
    "status": 1,
    "createdAt": "2024-01-15 10:30:00"
  }
}
```

---

### 7. 退出登录

前端清除本地存储的 Token 即可。

**请求**

```
POST /api/auth/logout
Authorization: Bearer <token>
```

**响应**

```json
{ "code": 200, "msg": "操作成功" }
```

---

### 8. 修改密码

**请求**

```
PUT /api/user/password
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| oldPassword | string | 是 | 当前密码 |
| newPassword | string | 是 | 新密码 |

**请求示例**

```json
{ "oldPassword": "123456", "newPassword": "newpass789" }
```

**响应**

```json
{ "code": 200, "msg": "操作成功" }
```

---

### 9. 获取当前用户详情

**请求**

```
GET /api/user/info
Authorization: Bearer <token>
```

**响应**

同 [UserInfoVO](#4-用户登录) 格式。

---

### 10. 获取AI模型列表

返回所有启用的模型及价格信息。

**请求**

```
GET /api/models
Authorization: Bearer <token>
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "displayName": "DeepSeek V3",
      "modelName": "deepseek-chat",
      "provider": "DeepSeek",
      "inputPrice": 1,
      "outputPrice": 2,
      "sort": 1,
      "enabled": 1,
      "recommended": 1
    },
    {
      "id": 2,
      "displayName": "DeepSeek R1",
      "modelName": "deepseek-reasoner",
      "provider": "DeepSeek",
      "inputPrice": 5,
      "outputPrice": 15,
      "sort": 2,
      "enabled": 1,
      "recommended": 1
    }
  ]
}
```

**响应字段 — ModelVO**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 模型ID |
| displayName | string | 显示名称 |
| modelName | string | 模型名称（用于调用API） |
| provider | string | 供应商 |
| inputPrice | long | 输入价格 (credits/千tokens) |
| outputPrice | long | 输出价格 (credits/千tokens) |
| sort | integer | 排序权重（越小越靠前） |
| enabled | integer | 是否启用：1=是, 0=否 |
| recommended | integer | 是否推荐：1=是, 0=否 |

---

### 11. 获取模型详情

**请求**

```
GET /api/models/{id}
Authorization: Bearer <token>
```

**路径参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 模型ID |

**响应**

同 [ModelVO](#10-获取ai模型列表) 格式。

---

### 12. 钱包信息

**请求**

```
GET /api/wallet
Authorization: Bearer <token>
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "userId": 1,
    "balance": 998000,
    "totalRecharge": 1000000,
    "totalUsed": 2000
  }
}
```

**响应字段 — WalletVO**

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | long | 用户ID |
| balance | long | 当前余额 (credits) |
| totalRecharge | long | 累计充值金额 |
| totalUsed | long | 累计消费金额 |

---

### 13. 钱包流水

**请求**

```
GET /api/wallet/logs?current=1&size=20
Authorization: Bearer <token>
```

**Query 参数**

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| current | int | 1 | 页码 |
| size | int | 20 | 每页条数 |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "type": "USAGE",
        "amount": -50,
        "beforeBalance": 1000000,
        "afterBalance": 999950,
        "remark": "AI对话消耗",
        "createdAt": "2024-01-15 12:00:00"
      },
      {
        "id": 2,
        "type": "RECHARGE",
        "amount": 1000000,
        "beforeBalance": 0,
        "afterBalance": 1000000,
        "remark": "初始赠送",
        "createdAt": "2024-01-15 10:30:00"
      }
    ],
    "total": 2,
    "current": 1,
    "size": 20
  }
}
```

**响应字段 — WalletLogVO**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 流水ID |
| type | string | 类型：`RECHARGE`=充值, `USAGE`=消耗, `ADMIN_ADD`=管理员增加, `ADMIN_DEDUCT`=管理员扣减 |
| amount | long | 变动金额（正数=增加，负数=减少） |
| beforeBalance | long | 变动前余额 |
| afterBalance | long | 变动后余额 |
| remark | string | 备注 |
| createdAt | datetime | 创建时间 |

---

### 11. 创建充值订单 (`/api/wallet/recharge`)

用户提交充值请求，生成待支付订单。管理员确认后 credits 到账。

**请求**

```
POST /api/wallet/recharge
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| amount | decimal | **是** | 1~100,000 | 充值金额（元） |
| payType | string | **是** | 非空 | 支付方式：`ALIPAY` / `WECHAT` / `BANK_TRANSFER` / `MANUAL` |

**请求示例**

```json
{ "amount": 100.00, "payType": "MANUAL" }
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "id": 1,
    "userId": 2,
    "orderNo": "RCD202605081658001234",
    "amount": 100.00,
    "credits": 100000,
    "status": "PENDING",
    "payType": "MANUAL",
    "paidAt": null,
    "remark": "用户自助充值",
    "createdAt": "2026-05-08 16:58:00"
  }
}
```

> 兑换比例：**1 元 = 1000 credits**
> 订单状态为 `PENDING`，需管理员在后台确认后到账

**响应字段 — RechargeOrder**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 订单ID |
| userId | long | 用户ID |
| orderNo | string | 订单号（格式：`RCD` + 时间戳 + 用户ID尾数） |
| amount | decimal | 充值金额(元) |
| credits | long | 获得的 credits 数量 |
| status | string | 状态：`PENDING`=待处理, `PAID`=已到账, `CANCELLED`=已取消 |
| payType | string? | 支付方式 |
| paidAt | datetime? | 支付/到账时间 |
| remark | string? | 备注 |
| createdAt | datetime | 创建时间 |

---

### 12. 我的充值订单列表 (`/api/wallet/orders`)

查看当前用户的充值历史记录。

**请求**

```
GET /api/wallet/orders?current=1&size=20&status=
Authorization: Bearer <token>
```

**Query 参数**

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| current | int | 1 | 页码 |
| size | int | 20 | 每页条数 |
| status | string | 否 | 按状态筛选：`PENDING`/`PAID`/`CANCELLED` |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 2,
        "orderNo": "RCD202605081658001234",
        "amount": 100.00,
        "credits": 100000,
        "status": "PAID",
        "payType": "MANUAL",
        "paidAt": "2026-05-08 17:00:00",
        "remark": "用户自助充值",
        "createdAt": "2026-05-08 16:58:00"
      }
    ],
    "total": 1,
    "current": 1,
    "size": 20
  }
}
```

---

### 13. API Key 管理

#### 14.1 创建 API Key

**请求**

```
POST /api/api-keys
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 否 | Key名称/备注 |

**请求示例**

```json
{ "name": "我的开发Key" }
```

**响应示例**

```json
{
  "code": 200,
  "msg": "创建成功，请妥善保管",
  "data": {
    "id": 1,
    "name": "我的开发Key",
    "keyPrefix": "sk-juzhi",
    "apiKey": "sk-juzhi-a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6",
    "status": 1,
    "totalCalls": 0,
    "totalCost": 0,
    "lastUsedAt": null,
    "createdAt": "2024-01-15 12:00:00"
  }
}
```

> ⚠️ **重要**: `apiKey` 完整值仅在创建时返回一次，请务必保存！

**响应字段 — ApiKeyVO**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | Key ID |
| name | string | 名称 |
| keyPrefix | string | Key 前缀 |
| apiKey | string | 完整 Key（仅创建时返回） |
| status | integer | 状态：1=启用, 0=禁用 |
| totalCalls | long | 总调用次数 |
| totalCost | long | 总消耗 credits |
| lastUsedAt | datetime? | 最后使用时间 |
| createdAt | datetime | 创建时间 |

#### 14.2 获取 API Key 列表

**请求**

```
GET /api/api-keys?current=1&size=20
Authorization: Bearer <token>
```

**响应**

> 注意：列表接口返回的 `apiKey` 为空（仅显示 `keyPrefix`），出于安全考虑不暴露完整 Key。

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "records": [
      { "id": 1, "name": "我的开发Key", "keyPrefix": "sk-juzhi", "apiKey": null, "status": 1, ... }
    ],
    "total": 1, "current": 1, "size": 20
  }
}
```

#### 14.3 删除 API Key

**请求**

```
DELETE /api/api-keys/{id}
Authorization: Bearer <token>
```

**路径参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | Key ID |

**响应**

```json
{ "code": 200, "msg": "操作成功" }
```

#### 14.4 禁用 API Key

**请求**

```
PUT /api/api-keys/{id}/disable
Authorization: Bearer <token>
```

**响应**

```json
{ "code": 200, "msg": "操作成功" }
```

#### 14.5 启用 API Key

**请求**

```
PUT /api/api-keys/{id}/enable
Authorization: Bearer <token>
```

**响应**

```json
{ "code": 200, "msg": "操作成功" }
```

---

### 14. 聊天会话管理

#### 14.1 创建会话

**请求**

```
POST /api/chat/session
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| modelName | string | 否 | 关联的模型名称 |
| title | string | 否 | 会话标题 |

> 不传 body 则创建空白会话。

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "id": 42,
    "title": "新对话",
    "modelName": "deepseek-chat",
    "createdAt": "2024-01-15 12:00:00",
    "updatedAt": "2024-01-15 12:00:00"
  }
}
```

**响应字段 — SessionVO**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 会话ID |
| title | string | 标题 |
| modelName | string? | 关联模型名 |
| createdAt | datetime | 创建时间 |
| updatedAt | datetime | 更新时间 |

#### 14.2 获取会话列表

**请求**

```
GET /api/chat/sessions?current=1&size=20
Authorization: Bearer <token>
```

**响应**

分页数据，records 中每项为 [SessionVO](#151-创建会话) 格式。

#### 14.3 删除会话

**请求**

```
DELETE /api/chat/session/{sessionId}
Authorization: Bearer <token>
```

**路径参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| sessionId | long | 会话ID |

**响应**

```json
{ "code": 200, "msg": "操作成功" }
```

---

### 15. 发送消息 (SSE 流式)

向 AI 发送聊天消息，支持 Server-Sent Events 流式返回。

**请求**

```
POST /api/chat/send
Content-Type: application/json
Accept: text/event-stream
Authorization: Bearer <token>
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| sessionId | long | 否 | 会话ID（不传则自动创建新会话） |
| modelName | string | **是** | 模型名称（如 `deepseek-chat`、`gpt-4o-mini`） |
| message | string | **是** | 消息内容 |

**请求示例**

```json
{
  "sessionId": 42,
  "modelName": "deepseek-chat",
  "message": "你好，请介绍一下你自己"
}
```

**响应格式 (SSE)**

```
data:{"id":"chatcmpl-xxx","object":"chat.completion.chunk","created":1705312800,"model":"deepseek-chat","choices":[{"index":0,"delta":{"content":"你好"},"finish_reason":null}]}

data:{"id":"chatcmpl-xxx","object":"chat.completion.chunk","created":1705312800,"model":"deepseek-chat","choices":[{"index":0,"delta":{"content":"！"},"finish_reason":null}]}

data:{"id":"chatcmpl-xxx","object":"chat.completion.chunk","created":1705312800,"model":"deepseek-chat","choices":[{"index":0,"delta":{},"finish_reason":"stop"},"usage":{"prompt_tokens":15,"completion_tokens":8,"total_tokens":23}]}

data:[DONE]
```

> 前端处理方式参考：
> ```javascript
> const eventSource = new EventSource(url); // 注意：SSE POST 需用 fetch + ReadableStream
> // 推荐：使用 fetch API 处理 SSE 流
> ```

---

### 16. 获取消息记录

**请求**

```
GET /api/chat/messages/{sessionId}?current=1&size=50
Authorization: Bearer <token>
```

**路径参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| sessionId | long | 会话ID |

**Query 参数**

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| current | int | 1 | 页码 |
| size | int | 50 | 每页条数（默认较大以加载全部历史） |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "records": [
      {
        "id": 101,
        "sessionId": 42,
        "role": "user",
        "content": "你好，请介绍一下你自己",
        "modelName": "deepseek-chat",
        "tokenCount": 8,
        "createdAt": "2024-01-15 12:00:05"
      },
      {
        "id": 102,
        "sessionId": 42,
        "role": "assistant",
        "content": "你好！我是DeepSeek，一个人工智能助手...",
        "modelName": "deepseek-chat",
        "tokenCount": 25,
        "createdAt": "2024-01-15 12:00:08"
      }
    ],
    "total": 2, "current": 1, "size": 50
  }
}
```

**响应字段 — MessageVO**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 消息ID |
| sessionId | long | 所属会话ID |
| role | string | 角色：`user` 或 `assistant` |
| content | string | 消息内容 |
| modelName | string | 使用模型 |
| tokenCount | integer | Token 数量（估算） |
| createdAt | datetime | 创建时间 |

---

## OpenAI 兼容接口

> 这些接口兼容 OpenAI API 格式，可直接用于 ChatGPT、Cursor、Cherry Studio 等客户端。
> 认证方式：Header `Authorization: Bearer <api_key>` 或 `<jwt_token>`

### 17. Chat Completions (`/v1/chat/completions`)

OpenAI 格式的聊天补全接口，支持流式和非流式模式。

**请求**

```
POST /v1/chat/completions
Content-Type: application/json
Authorization: Bearer <api_key或jwt>
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| model | string | **是** | 模型名称（如 `deepseek-chat`、`gpt-4o-mini`） |
| messages | array | **是** | 消息数组 |
| stream | boolean | 否 | 是否流式输出（默认 false） |

**messages 数组项**

| 字段 | 类型 | 说明 |
|------|------|------|
| role | string | 角色：`system`、`user`、`assistant` |
| content | string | 消息内容 |

**请求示例**

```json
{
  "model": "deepseek-chat",
  "stream": true,
  "messages": [
    {"role": "system", "content": "你是一个有用的助手"},
    {"role": "user", "content": "你好"}
  ]
}
```

**流式响应 (SSE)**

```
data:{"id":"chatcmpl-xxx","object":"chat.completion.chunk","created":1705312800,"model":"deepseek-chat","choices":[{"index":0,"delta":{"content":"你好"},"finish_reason":null}]}

...

data:[DONE]
```

**最终 usage chunk（最后一个 chunk 包含用量统计）**

```
data:{"id":"chatcmpl-xxx","object":"chat.completion.chunk","created":1705312800,"model":"deepseek-chat","choices":[{"index":0,"delta":{},"finish_reason":"stop"}],"usage":{"prompt_tokens":15,"completion_tokens":8,"total_tokens":23}}
```

**usage 对象**

| 字段 | 类型 | 说明 |
|------|------|------|
| prompt_tokens | int | 输入 Token 数 |
| completion_tokens | int | 输出 Token 数 |
| total_tokens | int | 总 Token 数 |

**错误响应**

```json
{ "error": { "code": 4001, "message": "模型不存在或已下线" } }
{ "error": { "code": 402, "message": "余额不足，请先充值" } }
{ "error": { "code": 4003, "message": "AI服务调用失败" } }
```

---

## 管理员接口（需 ADMIN 角色）

> 以下接口除 JWT 认证外，还要求当前用户角色为 `ADMIN`。

### 18. 用户管理 (`/api/admin/users`)

#### 18.1 获取用户列表（分页）

**请求**

```
GET /api/admin/users?current=1&size=20
Authorization: Bearer <admin_token>
```

**响应**

分页数据，records 中每项为 [UserInfoVO](#4-用户登录) 格式。

#### 18.2 获取用户详情

**请求**

```
GET /api/admin/users/{id}
Authorization: Bearer <admin_token>
```

**路径参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 用户ID |

#### 18.3 启用/禁用用户

**请求**

```
PUT /api/admin/users/{id}/status?status={status}
Authorization: Bearer <admin_token>
```

**路径参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 用户ID |

**Query 参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| status | integer | 1=启用, 0=禁用 |

**响应**

```json
{ "code": 200, "msg": "操作成功" }
```

#### 18.4 调整用户余额

**请求**

```
PUT /api/admin/users/balance
Content-Type: application/json
Authorization: Bearer <admin_token>
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | **是** | 目标用户ID |
| amount | long | **是** | 变动金额（正数=增加，负数=扣除） |
| remark | string | **是** | 备注/原因 |

**请求示例**

```json
{ "userId": 2, "amount": 50000, "remark": "新用户奖励" }
```

**响应**

```json
{ "code": 200, "msg": "操作成功" }
```

---

### 19. 模型管理 (`/api/admin/models`)

#### 19.1 获取模型列表（分页，含未启用的）

**请求**

```
GET /api/admin/models?current=1&size=20
Authorization: Bearer <admin_token>
```

**响应**

分页数据，records 中包含完整的 AiModel 实体对象。

#### 19.2 创建模型

**请求**

```
POST /api/admin/models
Content-Type: application/json
Authorization: Bearer <admin_token>
```

**请求体**

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| displayName | string | **是** | - | 显示名称 |
| modelName | string | **是** | - | 模型标识符（如 `gpt-4o-mini`） |
| provider | string | **是** | - | 供应商（如 `OpenAI`、`DeepSeek`） |
| inputPrice | long | 否 | 0 | 输入价(credits/千tokens) |
| outputPrice | long | 否 | 0 | 输出价(credits/千tokens) |
| sort | integer | 否 | 0 | 排序权重 |
| enabled | integer | 否 | 1 | 1=启用, 0=禁用 |
| recommended | integer | 否 | 0 | 1=推荐, 0=普通 |

**请求示例**

```json
{
  "displayName": "GPT-4o",
  "modelName": "gpt-4o",
  "provider": "OpenAI",
  "inputPrice": 15,
  "outputPrice": 60,
  "enabled": 1,
  "recommended": 1
}
```

#### 19.3 更新模型

**请求**

```
PUT /api/admin/models/{id}
Content-Type: application/json
Authorization: Bearer <admin_token>
```

**路径参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 模型ID |

**请求体**: 同 [创建模型](#192-创建模型)

#### 19.4 删除模型

**请求**

```
DELETE /api/admin/models/{id}
Authorization: Bearer <admin_token>
```

**响应**

```json
{ "code": 200, "msg": "操作成功" }
```

---

### 20. 订单管理 (`/api/admin/orders`)

#### 20.1 获取订单列表（分页）

**请求**

```
GET /api/admin/orders?current=1&size=20&userId=&status=
Authorization: Bearer <admin_token>
```

**Query 参数**

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| current | int | 1 | 页码 |
| size | int | 20 | 每页条数 |
| userId | long | 否 | 按用户ID筛选 |
| status | string | 否 | 按状态筛选：`PENDING`/`PAID`/`CANCELLED` |

**响应**

分页数据，records 中为 RechargeOrder 实体对象：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 订单ID |
| userId | long | 用户ID |
| orderNo | string | 订单号 |
| amount | decimal | 支付金额 |
| credits | long | 获得积分 |
| status | string | 状态 |
| payType | string? | 支付方式 |
| paidAt | datetime? | 支付时间 |
| createdAt | datetime | 创建时间 |

#### 20.2 处理订单（确认支付/取消）

**请求**

```
PUT /api/admin/orders/{id}/process
Content-Type: application/json
Authorization: Bearer <admin_token>
```

**路径参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 订单ID |

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | string | **是** | `PAID`=确认充值, `CANCELLED`=取消 |

> 当状态设为 `PAID` 时，系统会自动将对应 credits 充值到用户钱包。

**请求示例**

```json
{ "status": "PAID" }
```

**响应**

```json
{ "code": 200, "msg": "操作成功" }
```

---

### 21. 调用日志 (`/api/admin/logs`)

**请求**

```
GET /api/admin/logs?current=1&size=20&userId=&modelName=&status=
Authorization: Bearer <admin_token>
```

**Query 参数**

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| current | int | 1 | 页码 |
| size | int | 20 | 每页条数 |
| userId | long | 否 | 按用户ID筛选 |
| modelName | string | 否 | 按模型名模糊搜索 |
| status | string | 否 | 按状态筛选：`SUCCESS`/`FAILED` |

**响应**

分页数据，records 中为 UsageLogVO：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 日志ID |
| userId | long | 用户ID |
| username | string | 用户名 |
| apiKeyId | long | 使用的API Key ID |
| keyPrefix | string | Key前缀 |
| sessionId | long? | 会话ID |
| modelName | string | 模型名 |
| provider | string | 供应商 |
| promptTokens | int | 输入Token数 |
| completionTokens | int | 输出Token数 |
| totalTokens | int | 总Token数 |
| cost | long | 消耗 credits |
| status | string | `SUCCESS` / `FAILED` |
| errorMessage | string? | 错误信息 |
| requestIp | string | 请求IP |
| latencyMs | int | 耗时(ms) |
| createdAt | datetime | 调用时间 |

---

## 附录：快速测试命令

```bash
# ===== 1. 登录获取Token =====
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"password"}' | jq -r '.data.token')

echo "Token: $TOKEN"

# ===== 2. 获取模型列表 =====
curl -s http://localhost:8080/api/models \
  -H "Authorization: Bearer $TOKEN" | jq

# ===== 3. 查看钱包余额 =====
curl -s http://localhost:8080/api/wallet \
  -H "Authorization: Bearer $TOKEN" | jq

# ===== 4. 创建API Key =====
curl -s -X POST http://localhost:8080/api/api-keys \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"name":"测试Key"}' | jq

# ===== 5. 创建会话 =====
SESSION=$(curl -s -X POST http://localhost:8080/api/chat/session \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"modelName":"deepseek-chat"}')
echo "$SESSION" | jq

SESSION_ID=$(echo "$SESSION" | jq -r '.data.id')

# ===== 6. 发送消息（非流式预览） =====
curl -s -N -X POST http://localhost:8080/api/chat/send \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d "{\"sessionId\":$SESSION_ID,\"modelName\":\"deepseek-chat\",\"message\":\"你好\"}"

# ===== 7. OpenAI兼容接口测试 =====
curl -s -N -X POST http://localhost:8080/v1/chat/completions \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"model":"deepseek-chat","stream":true,"messages":[{"role":"user","content":"hello"}]}'

# ===== 8. 创建充值订单 =====
curl -s -X POST http://localhost:8080/api/wallet/recharge \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"amount":100,"payType":"MANUAL"}' | jq

# ===== 9. 查看我的充值订单 =====
curl -s "http://localhost:8080/api/wallet/orders" \
  -H "Authorization: Bearer $TOKEN" | jq
```
