# 校园小红书 MVP 设计白皮书 V5（最终版）

> **项目名称**：campus-xiaohongshu  
> **技术栈**：Spring Boot 3.x + Java 17 + MyBatis-Plus + MySQL + Redis + Hutool + 阿里云OSS  
> **基础包名**：`com.chenpperr.xhs`  
> **项目结构**：单模块 + 包名区分  
> **设计版本**：V5（最终版）  
> **更新日期**：2026-07-11

---

## 版本更新记录

| 版本 | 更新内容 |
|------|----------|
| V4 | 1. 增加分区功能（category字段）<br>2. 优化AI润色为独立同步接口<br>3. 极简弱化鉴权（删除Spring Security，改为轻量拦截器）<br>4. 补充侧边栏接口（通知、用户笔记列表） |
| V5 | 1. 增加一级评论区功能（Comment模块）<br>2. 增加阿里云OSS图片上传功能<br>3. 项目结构优化为单模块+包名区分 |

---

## 一、项目结构规划

### 1.1 整体结构

```
campus-xiaohongshu/
└── src/main/java/com/chenpperr/xhs/
    ├── common/          # 通用组件（统一响应、状态码、分页封装）
    ├── exception/       # 异常处理
    ├── utils/           # 工具类
    ├── entity/          # 数据库实体类
    ├── dto/             # 前端→后端参数对象
    ├── vo/              # 后端→前端视图对象
    ├── config/          # 配置类
    ├── interceptor/     # 拦截器
    ├── annotation/      # 自定义注解
    ├── resolver/        # 参数解析器
    ├── controller/      # 控制器层
    ├── service/         # 业务逻辑层
    │   └── impl/
    └── mapper/          # 数据访问层
```

---

### 1.2 各包职责说明

| 包名 | 职责 | 包含文件 |
|------|------|----------|
| `common` | 统一响应体、状态码、分页封装 | Result、ResultCode、PageResult |
| `exception` | 自定义业务异常、全局异常处理 | BusinessException、GlobalExceptionHandler |
| `utils` | 工具类 | RedisUtil |
| `entity` | 数据库实体类（与表结构一一对应） | User、Post、PostLike、Comment |
| `dto` | 前端请求参数对象 | PostPublishDTO、AiPolishDTO、CommentPublishDTO |
| `vo` | 后端响应视图对象 | PostCardVO、PostDetailVO、UserSimpleVO、AiPolishVO、CommentVO、NoticeVO、UploadVO |
| `config` | Spring配置类 | RedisConfig、WebMvcConfig、OssConfig |
| `interceptor` | 轻量级拦截器 | UserIdInterceptor |
| `annotation` | 自定义注解 | CurrentUserId |
| `resolver` | 参数解析器 | UserIdArgumentResolver |
| `controller` | 控制器层（接收请求、调用Service） | PostController、LikeController、CommentController、UploadController、NoticeController |
| `service` | 业务逻辑层（接口+实现） | PostService、LikeService、CommentService、AiService、OssService 及其实现类 |
| `mapper` | 数据访问层（继承BaseMapper） | UserMapper、PostMapper、PostLikeMapper、CommentMapper |

---

### 1.3 完整目录树

```
src/main/java/com/chenpperr/xhs/
├── common/
│   ├── Result.java
│   ├── ResultCode.java
│   └── PageResult.java
│
├── exception/
│   ├── BusinessException.java
│   └── GlobalExceptionHandler.java
│
├── utils/
│   └── RedisUtil.java
│
├── entity/
│   ├── User.java
│   ├── Post.java
│   ├── PostLike.java
│   └── Comment.java
│
├── dto/
│   ├── PostPublishDTO.java
│   ├── AiPolishDTO.java
│   └── CommentPublishDTO.java
│
├── vo/
│   ├── PostCardVO.java
│   ├── PostDetailVO.java
│   ├── UserSimpleVO.java
│   ├── AiPolishVO.java
│   ├── CommentVO.java
│   ├── NoticeVO.java
│   └── UploadVO.java
│
├── config/
│   ├── RedisConfig.java
│   ├── WebMvcConfig.java
│   └── OssConfig.java
│
├── interceptor/
│   └── UserIdInterceptor.java
│
├── annotation/
│   └── CurrentUserId.java
│
├── resolver/
│   └── UserIdArgumentResolver.java
│
├── controller/
│   ├── PostController.java
│   ├── LikeController.java
│   ├── CommentController.java
│   ├── UploadController.java
│   └── NoticeController.java
│
├── service/
│   ├── PostService.java
│   ├── LikeService.java
│   ├── CommentService.java
│   ├── AiService.java
│   ├── OssService.java
│   └── impl/
│       ├── PostServiceImpl.java
│       ├── LikeServiceImpl.java
│       ├── CommentServiceImpl.java
│       ├── AiServiceImpl.java
│       └── OssServiceImpl.java
│
└── mapper/
    ├── UserMapper.java
    ├── PostMapper.java
    ├── PostLikeMapper.java
    └── CommentMapper.java
```

---

## 二、核心功能与前后端映射表

| 用户操作 | 前端动作 | API路径 | Method | 处理逻辑简述 |
|----------|----------|---------|--------|-------------|
| **获取首页笔记** | 进入首页/切换分区Tab/下拉刷新 | `/api/v1/posts` | GET | 分页查询笔记列表，支持category筛选 |
| **查看笔记详情** | 点击笔记卡片 | `/api/v1/posts/{id}` | GET | 查询笔记完整内容（含图片、AI润色文本、标签） |
| **AI润色** | 编辑内容后点击"AI帮我润色"按钮 | `/api/v1/posts/ai-polish` | POST | 同步调用AI接口，返回润色文本和标签，用户预览满意后再发布 |
| **发布笔记** | 编辑图文，选择分区，点击发布 | `/api/v1/posts` | POST | 保存笔记到数据库（可包含AI润色后的内容和标签） |
| **上传图片** | 选择图片文件，点击上传 | `/api/v1/upload/image` | POST | 上传图片到阿里云OSS，返回图片外链URL |
| **点赞/取消点赞** | 点击爱心按钮 | `/api/v1/likes` | POST | Redis Hash记录点赞状态，原子性切换 |
| **发表评论** | 在笔记详情页输入评论，点击发送 | `/api/v1/comments` | POST | 保存评论到数据库 |
| **查看评论列表** | 进入笔记详情页，滚动到评论区 | `/api/v1/comments` | GET | 查询该笔记下的一级评论列表（按时间倒序） |
| **获取通知** | 点击侧边栏通知 | `/api/v1/notices` | GET | 返回硬编码的校园通知列表 |
| **查看用户笔记** | 点击侧边栏"我"的页面 | `/api/v1/posts/user/{userId}` | GET | 查询指定用户发布的笔记列表 |

---

## 三、核心API接口文档规范

### 3.1 全局统一响应格式 `Result<T>`

```json
{
  "code": 200,
  "msg": "success",
  "data": {}
}
```

**状态码定义**：

| code | 含义 | 说明 |
|------|------|------|
| `200` | 成功 | 请求正常处理 |
| `400` | 参数错误 | DTO校验失败 |
| `401` | 未认证 | 缺少userId Header |
| `500` | 服务器异常 | 未捕获的运行时异常 |

### 3.2 鉴权说明（极简模式）

**不再使用Spring Security + JWT**，改为轻量级拦截器：

- 前端每个请求Header携带 `userId: 1`（MVP阶段固定值）
- `UserIdInterceptor` 拦截所有 `/api/**` 请求，将userId存入ThreadLocal
- Controller通过 `@CurrentUserId Long userId` 注解获取当前用户ID
- 部分接口不需要userId（如获取笔记列表、AI润色、通知等）

### 3.3 接口详细文档

---

#### 3.3.1 获取首页笔记列表（瀑布流分页 + 分区筛选）

**GET** `/api/v1/posts`

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| `page` | Query | Integer | ❌ | 页码，默认1 |
| `size` | Query | Integer | ❌ | 每页条数，默认10 |
| `category` | Query | String | ❌ | 分区筛选，如"推荐"、"美食"、"穿搭"等，不传则查询全部 |

**分区枚举值**：

| category值 | 说明 |
|------------|------|
| `推荐` | 推荐（默认） |
| `穿搭` | 穿搭 |
| `美食` | 美食 |
| `职场` | 职场 |
| `情感` | 情感 |
| `家居` | 家居 |
| `游戏` | 游戏 |
| `旅行` | 旅行 |
| `健身` | 健身 |
| `视频` | 视频 |

**响应数据 (PageResult\<PostCardVO\>)**：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 100,
    "list": [
      {
        "id": 1,
        "title": "食堂新品测评",
        "category": "美食",
        "coverUrl": "https://xxx.com/cover1.jpg",
        "likeCount": 42,
        "liked": false,
        "author": {
          "userId": 1,
          "nickname": "测试用户",
          "avatar": "https://xxx.com/avatar.jpg"
        },
        "createTime": "2026-07-10 14:30:00"
      }
    ]
  }
}
```

---

#### 3.3.2 查看笔记详情

**GET** `/api/v1/posts/{id}`

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 笔记ID |

**响应数据 (PostDetailVO)**：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "title": "食堂新品测评",
    "category": "美食",
    "content": "今天去食堂发现...",
    "polishedContent": "今天漫步校园食堂，意外发现...",
    "tags": ["#食堂", "#美食", "#校园生活"],
    "imageUrls": ["https://xxx.com/img1.jpg", "https://xxx.com/img2.jpg"],
    "likeCount": 42,
    "liked": false,
    "commentCount": 5,
    "author": {
      "userId": 1,
      "nickname": "测试用户",
      "avatar": "https://xxx.com/avatar.jpg"
    },
    "createTime": "2026-07-10 14:30:00"
  }
}
```

---

#### 3.3.3 AI润色（独立同步接口）

**POST** `/api/v1/posts/ai-polish`

**说明**：用户编辑内容后，点击"AI帮我润色"按钮调用此接口。同步返回润色结果，用户预览满意后再发布笔记。

**请求参数 (AiPolishDTO)**：

```json
{
  "content": "今天去食堂发现了一个超好吃的窗口，红烧肉特别香"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `content` | String | ✅ | 用户输入的原始文本，1-1000字 |

**响应数据 (AiPolishVO)**：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "polishedContent": "今天漫步校园食堂，意外发现了一个令人惊喜的美食窗口。那里的红烧肉色泽红润，香气四溢，让人垂涎欲滴。",
    "tags": ["#食堂", "#美食", "#红烧肉", "#校园生活"]
  }
}
```

---

#### 3.3.4 发布笔记

**POST** `/api/v1/posts`

**请求头**：`userId: 1`

**请求参数 (PostPublishDTO)**：

```json
{
  "title": "食堂新品测评",
  "category": "美食",
  "content": "今天去食堂发现了一个超好吃的窗口...",
  "polishedContent": "今天漫步校园食堂，意外发现...",
  "tags": ["#食堂", "#美食", "#校园生活"],
  "imageUrls": ["https://xxx.com/img1.jpg", "https://xxx.com/img2.jpg"]
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `title` | String | ✅ | 笔记标题，1-50字 |
| `category` | String | ✅ | 分区，如"美食"、"穿搭"等 |
| `content` | String | ✅ | 用户原始内容，1-1000字 |
| `polishedContent` | String | ❌ | AI润色后的内容 |
| `tags` | Array\<String\> | ❌ | AI提取的标签 |
| `imageUrls` | Array\<String\> | ❌ | 图片URL列表，最多9张 |

**响应数据**：

```json
{
  "code": 200,
  "msg": "发布成功",
  "data": {
    "postId": 1
  }
}
```

---

#### 3.3.5 图片上传（阿里云OSS）

**POST** `/api/v1/upload/image`

**说明**：上传单张图片到阿里云OSS，返回图片外链URL。

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| `file` | Body | MultipartFile | ✅ | 图片文件，支持jpg/png/gif/webp，最大5MB |

**响应数据 (UploadVO)**：

```json
{
  "code": 200,
  "msg": "上传成功",
  "data": {
    "url": "https://your-bucket.oss-cn-hangzhou.aliyuncs.com/images/2026/07/11/abc123.jpg",
    "filename": "abc123.jpg"
  }
}
```

---

#### 3.3.6 点赞/取消点赞

**POST** `/api/v1/likes`

**请求头**：`userId: 1`

**请求参数**：

```json
{
  "postId": 1
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `postId` | Long | ✅ | 笔记ID |

**响应数据**：

```json
{
  "code": 200,
  "msg": "点赞成功",
  "data": {
    "liked": true,
    "likeCount": 43
  }
}
```

**Redis实现**：
- Key格式：`post:likes:{postId}` (Hash)
- Field：`{userId}`，Value：`1`
- 点赞：`HSET`，取消：`HDEL`
- 点赞数：`HLEN`，是否已赞：`HEXISTS`

---

#### 3.3.7 发表评论

**POST** `/api/v1/comments`

**请求头**：`userId: 1`

**请求参数 (CommentPublishDTO)**：

```json
{
  "postId": 1,
  "content": "看起来好好吃！明天也去尝尝～"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `postId` | Long | ✅ | 笔记ID |
| `content` | String | ✅ | 评论内容，1-500字 |

**响应数据**：

```json
{
  "code": 200,
  "msg": "评论成功",
  "data": {
    "commentId": 1,
    "content": "看起来好好吃！明天也去尝尝～",
    "author": {
      "userId": 1,
      "nickname": "测试用户",
      "avatar": "https://xxx.com/avatar.jpg"
    },
    "createTime": "2026-07-11 15:30:00"
  }
}
```

---

#### 3.3.8 获取评论列表

**GET** `/api/v1/comments`

**说明**：查询某篇笔记下的一级评论列表，按时间倒序排列。

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| `postId` | Query | Long | ✅ | 笔记ID |
| `page` | Query | Integer | ❌ | 页码，默认1 |
| `size` | Query | Integer | ❌ | 每页条数，默认20 |

**响应数据 (PageResult\<CommentVO\>)**：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 5,
    "list": [
      {
        "id": 3,
        "postId": 1,
        "content": "看起来好好吃！明天也去尝尝～",
        "author": {
          "userId": 2,
          "nickname": "美食达人",
          "avatar": "https://xxx.com/avatar2.jpg"
        },
        "createTime": "2026-07-11 15:30:00"
      }
    ]
  }
}
```

---

#### 3.3.9 获取系统通知（假数据）

**GET** `/api/v1/notices`

**响应数据**：

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "title": "🎉 欢迎来到校园小红书！",
      "content": "这里是属于我们的校园生活分享平台，快来发布你的第一条笔记吧～",
      "type": "system",
      "createTime": "2026-07-11 10:00:00"
    },
    {
      "id": 2,
      "title": "📢 校园美食节即将开幕",
      "content": "本周五下午2点，学校食堂将举办美食节活动，欢迎大家来打卡！",
      "type": "activity",
      "createTime": "2026-07-10 15:30:00"
    },
    {
      "id": 3,
      "title": "🔥 热门话题：期末复习攻略",
      "content": "期末考试周来袭，快来分享你的独家复习秘籍～",
      "type": "topic",
      "createTime": "2026-07-09 09:00:00"
    },
    {
      "id": 4,
      "title": "✨ 新功能上线：AI智能润色",
      "content": "发布笔记时可以使用AI帮你润色文案啦，让分享更精彩！",
      "type": "system",
      "createTime": "2026-07-08 12:00:00"
    }
  ]
}
```

---

#### 3.3.10 查询用户发布的笔记列表

**GET** `/api/v1/posts/user/{userId}`

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| `userId` | Long | 用户ID |

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| `page` | Query | Integer | ❌ | 页码，默认1 |
| `size` | Query | Integer | ❌ | 每页条数，默认10 |

**响应数据 (PageResult\<PostCardVO\>)**：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 5,
    "list": [
      {
        "id": 1,
        "title": "食堂新品测评",
        "category": "美食",
        "coverUrl": "https://xxx.com/cover1.jpg",
        "likeCount": 42,
        "liked": false,
        "author": {
          "userId": 1,
          "nickname": "测试用户",
          "avatar": "https://xxx.com/avatar.jpg"
        },
        "createTime": "2026-07-10 14:30:00"
      }
    ]
  }
}
```

---

## 四、MySQL数据库表结构

### 4.1 用户表 `user`

```sql
-- 用户表
CREATE TABLE `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '登录账号',
    `password`    VARCHAR(100) NOT NULL COMMENT '登录密码',
    `nickname`    VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '昵称',
    `avatar`      VARCHAR(500) NOT NULL DEFAULT '' COMMENT '头像URL',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 预置测试用户
INSERT INTO `user` (`id`, `username`, `password`, `nickname`, `avatar`)
VALUES (1, 'test', '123456', '测试用户', 'https://via.placeholder.com/100');
```

### 4.2 笔记表 `post`

```sql
-- 笔记表
CREATE TABLE `post` (
    `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '笔记ID',
    `user_id`          BIGINT        NOT NULL COMMENT '作者ID',
    `title`            VARCHAR(100)  NOT NULL COMMENT '笔记标题',
    `category`         VARCHAR(20)   NOT NULL DEFAULT '推荐' COMMENT '分区：推荐/穿搭/美食/职场/情感/家居/游戏/旅行/健身/视频',
    `content`          TEXT          NOT NULL COMMENT '用户原始内容',
    `polished_content` TEXT          DEFAULT NULL COMMENT 'AI润色后的内容',
    `tags`             VARCHAR(500)  DEFAULT NULL COMMENT 'AI提取的标签，JSON数组格式',
    `image_urls`       TEXT          DEFAULT NULL COMMENT '图片URL列表，JSON数组格式',
    `like_count`       INT           NOT NULL DEFAULT 0 COMMENT '点赞数（冗余字段）',
    `comment_count`    INT           NOT NULL DEFAULT 0 COMMENT '评论数（冗余字段）',
    `status`           TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：0-草稿 1-已发布 2-已删除',
    `create_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category` (`category`),
    KEY `idx_create_time` (`create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记表';
```

### 4.3 点赞记录表 `post_like`

```sql
-- 点赞记录表（持久化备份，运行时以Redis为准）
CREATE TABLE `post_like` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `post_id`     BIGINT   NOT NULL COMMENT '笔记ID',
    `user_id`     BIGINT   NOT NULL COMMENT '用户ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞记录表（持久化备份）';
```

### 4.4 评论表 `comment`

```sql
-- 一级评论表
CREATE TABLE `comment` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `post_id`     BIGINT       NOT NULL COMMENT '笔记ID',
    `user_id`     BIGINT       NOT NULL COMMENT '评论者用户ID',
    `content`     VARCHAR(500) NOT NULL COMMENT '评论内容',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    PRIMARY KEY (`id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='一级评论表';
```

### 4.5 预置测试数据

```sql
-- 预置测试笔记数据
INSERT INTO `post` (`user_id`, `title`, `category`, `content`, `polished_content`, `tags`, `image_urls`, `like_count`, `comment_count`) VALUES
(1, '食堂新品红烧肉测评', '美食', '今天去食堂发现了一个超好吃的窗口，红烧肉特别香，强烈推荐！', '今天漫步校园食堂，意外发现了一个令人惊喜的美食窗口。那里的红烧肉色泽红润，香气四溢，让人垂涎欲滴。强烈推荐给各位同学！', '["#食堂","#美食","#红烧肉","#校园生活"]', '["https://via.placeholder.com/400x300?text=food1"]', 42, 3),
(1, '图书馆自习打卡', '推荐', '图书馆五楼靠窗位置太棒了，阳光正好，学习效率翻倍！', '图书馆五楼靠窗的位置简直是学习的绝佳圣地。温暖的阳光洒在书桌上，让人心情愉悦，学习效率也随之翻倍。强烈推荐！', '["#图书馆","#自习","#学习","#校园生活"]', '["https://via.placeholder.com/400x300?text=library1"]', 28, 1),
(1, '今日穿搭分享', '穿搭', '今天穿了新买的卫衣，搭配牛仔裤，简约又好看～', '今日穿搭分享：一件新入手的卫衣，搭配经典牛仔裤，简约而不失时尚感，轻松打造休闲校园风。', '["#穿搭","#卫衣","#校园风","#日常穿搭"]', '["https://via.placeholder.com/400x300?text=outfit1"]', 35, 2),
(1, '操场夜跑打卡', '健身', '今晚跑了5公里，出汗的感觉真舒服！坚持锻炼，保持好身材。', '今晚在操场完成了5公里夜跑，大汗淋漓的感觉真是畅快淋漓！坚持锻炼，保持健康好身材，一起动起来吧！', '["#健身","#夜跑","#运动","#校园生活"]', '["https://via.placeholder.com/400x300?text=run1"]', 19, 0),
(1, '宿舍游戏开黑', '游戏', '周末和室友一起开黑打游戏，太快乐了！', '周末时光，和室友们一起开黑打游戏，欢声笑语中度过了一段快乐的时光。游戏虽好，也要注意休息哦！', '["#游戏","#室友","#周末","#开黑"]', '["https://via.placeholder.com/400x300?text=game1"]', 23, 1);

-- 预置测试评论数据
INSERT INTO `comment` (`post_id`, `user_id`, `content`) VALUES
(1, 1, '红烧肉yyds！'),
(1, 3, '这个窗口在哪里呀？求具体位置！'),
(1, 2, '看起来好好吃！明天也去尝尝～'),
(2, 2, '五楼确实安静，推荐！'),
(3, 1, '好看！求链接～'),
(3, 3, '简约风太赞了！'),
(5, 2, '什么游戏？带我一个！');
```

---

## 五、技术实现要点备忘

| 模块 | 实现要点 |
|------|----------|
| **多模块依赖** | xhs-service依赖xhs-pojo和xhs-common，pom.xml中配置模块依赖 |
| **轻量鉴权** | `UserIdInterceptor`从Header读取`userId`存入ThreadLocal |
| **分区筛选** | `GET /api/v1/posts`增加`category`查询参数 |
| **AI润色** | 独立同步接口`POST /api/v1/posts/ai-polish` |
| **Redis点赞** | Key=`post:likes:{postId}`，Hash结构 |
| **评论功能** | 一级评论，按时间倒序，更新`post.comment_count` |
| **图片上传** | 阿里云OSS SDK，配置endpoint/accessKeyId/accessKeySecret/bucketName |
| **分页查询** | MyBatis-Plus的`Page<T>`分页 |
| **统一响应** | `@RestControllerAdvice` + `Result<T>` |

### 阿里云OSS配置说明

在 `application.yml` 中配置：

```yaml
aliyun:
  oss:
    endpoint: oss-cn-hangzhou.aliyuncs.com
    access-key-id: your-access-key-id
    access-key-secret: your-access-key-secret
    bucket-name: your-bucket-name
    url-prefix: https://your-bucket-name.oss-cn-hangzhou.aliyuncs.com
```

**注意事项**：
1. 需要在阿里云控制台开通OSS服务
2. 创建Bucket时选择"公共读"权限
3. 配置跨域规则（CORS）允许前端域名访问
4. AccessKey建议使用RAM子账号，仅授予OSS权限

---

## 六、接口汇总表

| 序号 | 接口路径 | Method | 说明 | 需要userId |
|------|----------|--------|------|-----------|
| 1 | `/api/v1/posts` | GET | 获取首页笔记列表（支持category筛选） | ❌ |
| 2 | `/api/v1/posts/{id}` | GET | 查看笔记详情 | ❌ |
| 3 | `/api/v1/posts/ai-polish` | POST | AI润色（同步） | ❌ |
| 4 | `/api/v1/posts` | POST | 发布笔记 | ✅ |
| 5 | `/api/v1/upload/image` | POST | 上传图片到阿里云OSS | ❌ |
| 6 | `/api/v1/likes` | POST | 点赞/取消点赞 | ✅ |
| 7 | `/api/v1/comments` | POST | 发表评论 | ✅ |
| 8 | `/api/v1/comments` | GET | 获取评论列表（按时间倒序） | ❌ |
| 9 | `/api/v1/notices` | GET | 获取系统通知（假数据） | ❌ |
| 10 | `/api/v1/posts/user/{userId}` | GET | 查询用户发布的笔记 | ❌ |

---

## 七、数据库表汇总

| 表名 | 说明 | 记录数（预置） |
|------|------|---------------|
| `user` | 用户表 | 1条 |
| `post` | 笔记表 | 5条 |
| `post_like` | 点赞记录表 | 0条 |
| `comment` | 一级评论表 | 7条 |

---

**状态**：⏳ 待最终审批  
**下一步**：审批通过后开始编码实现
