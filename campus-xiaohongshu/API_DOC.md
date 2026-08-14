# 校园小红书 - 接口文档 & Postman 测试指南

> 基础地址：`http://localhost:8081`
>
> 所有接口（除登录外）都需要在 Headers 中携带：
> ```
> Authorization: Bearer {登录后返回的token}
> ```

---

## 一、认证模块（Auth）

### 1.1 用户登录

```
POST /api/v1/auth/login
```

| 项目 | 说明 |
|------|------|
| 认证 | ❌ 不需要 Token |
| Content-Type | application/json |

**请求体：**

```json
{
    "username": "test",
    "password": "123456"
}
```

**响应体：**

```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiJ9...",
        "userId": 1,
        "username": "test",
        "nickname": "测试用户"
    }
}
```

**Postman 配置：**

```
Method:  POST
URL:     http://localhost:8081/api/v1/auth/login
Headers: Content-Type: application/json
Body:    raw → JSON → {"username":"test","password":"123456"}
```

---

## 二、笔记模块（Post）

### 2.1 笔记列表（分页 + 分类筛选）

```
GET /api/v1/posts
```

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| current | Long | 否 | 1 | 当前页码 |
| size | Long | 否 | 10 | 每页条数 |
| category | String | 否 | — | 分区筛选（如"美食"、"穿搭"） |

**响应体：**

```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "total": 5,
        "list": [
            {
                "id": 1,
                "title": "食堂新品红烧肉测评",
                "category": "美食",
                "coverUrl": "https://via.placeholder.com/400x300?text=food1",
                "likeCount": 42,
                "liked": true,
                "author": {
                    "userId": 1,
                    "nickname": "测试用户",
                    "avatar": "https://via.placeholder.com/100"
                },
                "createTime": "2026-07-12T10:30:00"
            }
        ]
    }
}
```

**Postman 配置：**

```
Method:  GET
URL:     http://localhost:8081/api/v1/posts?current=1&size=10
Headers: Authorization: Bearer {token}
```

分类筛选：

```
GET http://localhost:8081/api/v1/posts?current=1&size=10&category=美食
```

---

### 2.2 笔记详情

```
GET /api/v1/posts/{id}
```

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 笔记 ID（URL 路径参数） |

**响应体：**

```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "id": 1,
        "title": "食堂新品红烧肉测评",
        "category": "美食",
        "content": "今天去食堂发现了一个超好吃的窗口...",
        "imageUrls": ["https://via.placeholder.com/400x300?text=food1"],
        "likeCount": 42,
        "liked": true,
        "commentCount": 3,
        "author": {
            "userId": 1,
            "nickname": "测试用户",
            "avatar": "https://via.placeholder.com/100"
        },
        "createTime": "2026-07-12T10:30:00"
    }
}
```

**Postman 配置：**

```
Method:  GET
URL:     http://localhost:8081/api/v1/posts/1
Headers: Authorization: Bearer {token}
```

---

### 2.3 发布笔记

```
POST /api/v1/posts
```

| 参数 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| title | String | 是 | @NotBlank, @Size(max=50) | 笔记标题 |
| category | String | 是 | @NotBlank | 分区（如"美食"、"穿搭"、"推荐"） |
| content | String | 是 | @NotBlank, @Size(max=1000) | 笔记正文 |
| imageUrls | List\<String\> | 否 | — | 图片 URL 列表 |

**请求体：**

```json
{
    "title": "Postman测试笔记",
    "category": "推荐",
    "content": "这是我用Postman发布的第一条笔记！",
    "imageUrls": ["https://via.placeholder.com/400x300?text=test"]
}
```

**响应体：**

```json
{
    "code": 200,
    "msg": "success",
    "data": 7
}
```

> `data` 返回新笔记的 ID

**Postman 配置：**

```
Method:  POST
URL:     http://localhost:8081/api/v1/posts
Headers: Authorization: Bearer {token}
         Content-Type: application/json
Body:    raw → JSON → {"title":"...","category":"...","content":"...","imageUrls":["..."]}
```

---

### 2.4 删除笔记

```
DELETE /api/v1/posts/{id}
```

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 笔记 ID（URL 路径参数） |

**权限：** 只有笔记作者可以删除自己的笔记，否则返回 403。

**响应体：**

```json
{
    "code": 200,
    "msg": "success",
    "data": null
}
```

**Postman 配置：**

```
Method:  DELETE
URL:     http://localhost:8081/api/v1/posts/7
Headers: Authorization: Bearer {token}
```

---

## 三、点赞模块（Like）

### 3.1 点赞 / 取消点赞（Toggle）

```
POST /api/v1/posts/{postId}/like
```

| 参数 | 类型 | 说明 |
|------|------|------|
| postId | Long | 笔记 ID（URL 路径参数） |

**逻辑：** 第一次调用 → 点赞（返回 `true`）；第二次调用 → 取消（返回 `false`）

**响应体：**

```json
{ "code": 200, "msg": "success", "data": true }
```

**Postman 配置：**

```
Method:  POST
URL:     http://localhost:8081/api/v1/posts/1/like
Headers: Authorization: Bearer {token}
```

---

### 3.2 查询当前用户是否已点赞

```
GET /api/v1/posts/{postId}/liked
```

**响应体：**

```json
{ "code": 200, "msg": "success", "data": true }
```

**Postman 配置：**

```
Method:  GET
URL:     http://localhost:8081/api/v1/posts/1/liked
Headers: Authorization: Bearer {token}
```

---

### 3.3 查询点赞数

```
GET /api/v1/posts/{postId}/like-count
```

**响应体：**

```json
{ "code": 200, "msg": "success", "data": 42 }
```

**Postman 配置：**

```
Method:  GET
URL:     http://localhost:8081/api/v1/posts/1/like-count
Headers: Authorization: Bearer {token}
```

---

## 四、评论模块（Comment）

### 4.1 评论列表（分页）

```
GET /api/v1/posts/{postId}/comments
```

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| postId | Long | 是 | — | 笔记 ID（URL 路径参数） |
| current | Long | 否 | 1 | 当前页码 |
| size | Long | 否 | 10 | 每页条数 |

**响应体：**

```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "total": 3,
        "list": [
            {
                "id": 1,
                "postId": 1,
                "content": "红烧肉yyds！",
                "author": {
                    "userId": 1,
                    "nickname": "测试用户",
                    "avatar": "https://via.placeholder.com/100"
                },
                "createTime": "2026-07-12T10:30:00"
            }
        ]
    }
}
```

**Postman 配置：**

```
Method:  GET
URL:     http://localhost:8081/api/v1/posts/1/comments?current=1&size=10
Headers: Authorization: Bearer {token}
```

---

### 4.2 发表评论

```
POST /api/v1/posts/{postId}/comments
```

| 参数 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| postId | Long | 是 | — | 笔记 ID（URL 路径参数） |
| content | String | 是 | @NotBlank, @Size(max=500) | 评论内容（请求体） |

**请求体：**

```json
{
    "content": "看起来好好吃！明天也去尝尝～"
}
```

**响应体：**

```json
{ "code": 200, "msg": "success", "data": 8 }
```

> `data` 返回新评论的 ID

**Postman 配置：**

```
Method:  POST
URL:     http://localhost:8081/api/v1/posts/1/comments
Headers: Authorization: Bearer {token}
         Content-Type: application/json
Body:    raw → JSON → {"content":"看起来好好吃！明天也去尝尝～"}
```

---

### 4.3 删除评论

```
DELETE /api/v1/posts/{postId}/comments/{commentId}
```

| 参数 | 类型 | 说明 |
|------|------|------|
| postId | Long | 笔记 ID（URL 路径参数，RESTful 语义） |
| commentId | Long | 评论 ID（URL 路径参数） |

**权限：** 只有评论作者可以删除自己的评论，否则返回 403。

**响应体：**

```json
{ "code": 200, "msg": "success", "data": null }
```

**Postman 配置：**

```
Method:  DELETE
URL:     http://localhost:8081/api/v1/posts/1/comments/8
Headers: Authorization: Bearer {token}
```

---

## 五、通知模块（Notice）

> 通知由系统自动创建：当其他用户点赞或评论你的笔记时，自动生成通知。
>
> 当前用户查看不到自己触发的通知，只能看到别人发给自己的通知。

### 5.1 通知列表（分页）

```
GET /api/v1/notifications
```

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| current | Long | 否 | 1 | 当前页码 |
| size | Long | 否 | 10 | 每页条数 |

**响应体：**

```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "total": 2,
        "list": [
            {
                "id": 2,
                "type": "comment",
                "content": "小明同学 评论了你的笔记「食堂新品红烧肉测评」：红烧肉看着就好吃！",
                "fromUser": {
                    "userId": 2,
                    "nickname": "小明同学",
                    "avatar": "https://via.placeholder.com/100"
                },
                "postId": 1,
                "isRead": false,
                "createTime": "2026-07-13T21:45:56"
            },
            {
                "id": 1,
                "type": "like",
                "content": "小明同学 赞了你的笔记「食堂新品红烧肉测评」",
                "fromUser": {
                    "userId": 2,
                    "nickname": "小明同学",
                    "avatar": "https://via.placeholder.com/100"
                },
                "postId": 1,
                "isRead": false,
                "createTime": "2026-07-13T21:42:03"
            }
        ]
    }
}
```

**Postman 配置：**

```
Method:  GET
URL:     http://localhost:8081/api/v1/notifications?current=1&size=10
Headers: Authorization: Bearer {token}
```

---

### 5.2 标记已读

```
PUT /api/v1/notifications/{id}/read
```

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 通知 ID（列表中返回的 id 字段） |

**响应体：**

```json
{ "code": 200, "msg": "success", "data": null }
```

**Postman 配置：**

```
Method:  PUT
URL:     http://localhost:8081/api/v1/notifications/1/read
Headers: Authorization: Bearer {token}
```

---

## 六、接口汇总表

| # | Method | Path | 说明 | 需要Token |
|---|--------|------|------|-----------|
| 1 | POST | `/api/v1/auth/login` | 用户登录 | ❌ |
| 2 | GET | `/api/v1/posts` | 笔记列表（分页+分类） | ✅ |
| 3 | GET | `/api/v1/posts/{id}` | 笔记详情 | ✅ |
| 4 | POST | `/api/v1/posts` | 发布笔记 | ✅ |
| 5 | DELETE | `/api/v1/posts/{id}` | 删除笔记 | ✅ |
| 6 | POST | `/api/v1/posts/{postId}/like` | 点赞/取消 | ✅ |
| 7 | GET | `/api/v1/posts/{postId}/liked` | 是否已赞 | ✅ |
| 8 | GET | `/api/v1/posts/{postId}/like-count` | 点赞数 | ✅ |
| 9 | GET | `/api/v1/posts/{postId}/comments` | 评论列表 | ✅ |
| 10 | POST | `/api/v1/posts/{postId}/comments` | 发表评论 | ✅ |
| 11 | DELETE | `/api/v1/posts/{postId}/comments/{commentId}` | 删除评论 | ✅ |
| 12 | GET | `/api/v1/notifications` | 通知列表 | ✅ |
| 13 | PUT | `/api/v1/notifications/{id}/read` | 标记已读 | ✅ |

---

## 七、完整测试流程

### 第一阶段：登录拿 Token

```
POST /api/v1/auth/login
Body: {"username":"test","password":"123456"}
→ 保存 token1（userId=1，帖子作者）

POST /api/v1/auth/login
Body: {"username":"test2","password":"123456"}
→ 保存 token2（userId=2，其他用户）
```

### 第二阶段：笔记模块

```
GET  /api/v1/posts                          → 验证列表返回 5 条预置数据
GET  /api/v1/posts?category=美食             → 验证分类筛选
GET  /api/v1/posts/1                        → 验证详情（含 author、liked、commentCount）
POST /api/v1/posts                          → 发布新笔记，拿到新 ID
GET  /api/v1/posts/{新ID}                    → 验证新笔记详情
DELETE /api/v1/posts/{新ID}                  → 删除新笔记
GET  /api/v1/posts/{新ID}                    → 验证返回 404
```

### 第三阶段：点赞模块

```
POST /api/v1/posts/1/like                   → 点赞（用 token2）
GET  /api/v1/posts/1/liked                  → 验证返回 true
GET  /api/v1/posts/1/like-count             → 验证计数 +1
POST /api/v1/posts/1/like                   → 取消点赞
GET  /api/v1/posts/1/like-count             → 验证计数 -1
```

### 第四阶段：评论模块

```
GET  /api/v1/posts/1/comments               → 验证列表返回预置评论
POST /api/v1/posts/1/comments               → 发表新评论，拿到评论 ID
GET  /api/v1/posts/1/comments               → 验证新评论出现
DELETE /api/v1/posts/1/comments/{评论ID}     → 删除新评论
GET  /api/v1/posts/1/comments               → 验证评论已删除
```

### 第五阶段：通知模块

```
POST /api/v1/posts/1/like                   → 用 token2 点赞（触发通知）
GET  /api/v1/notifications                  → 用 token1 查通知（应有 1 条点赞通知）
POST /api/v1/posts/1/comments               → 用 token2 评论（触发通知）
GET  /api/v1/notifications                  → 用 token1 查通知（应有 2 条）
PUT  /api/v1/notifications/1/read           → 用 token1 标记已读
GET  /api/v1/notifications                  → 验证 isRead 变为 true
```

---

## 八、统一响应格式

```json
{
    "code": 200,      // 状态码
    "msg": "success", // 提示信息
    "data": {}        // 数据（成功时有值，失败时为 null）
}
```

| 状态码 | 含义 | 场景 |
|--------|------|------|
| 200 | 成功 | 正常返回 |
| 400 | 参数错误 | @Valid 校验失败（如标题为空） |
| 401 | 未认证 | Token 无效/过期/未携带 |
| 403 | 无权限 | 删除他人的帖子/评论 |
| 404 | 资源不存在 | 帖子已被删除 |
| 500 | 服务器异常 | 未知错误 |
