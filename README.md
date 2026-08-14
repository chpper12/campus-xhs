# 校园小红书 (campus-xiaohongshu)

一个仿小红书风格的 PC 端校园社交平台 MVP，支持笔记浏览、发布、点赞、评论、AI 润色、通知等功能。

## 项目结构（Monorepo）

```
compus-xiaohongshu/
├── campus-xiaohongshu/            # 后端（Spring Boot 3 + MyBatis-Plus）
│   ├── sql/init.sql               # 数据库初始化脚本
│   └── src/main/java/com/chenpperr/xhs/
├── campus-xiaohongshu-frontend/   # 前端（Vue 3 + Vite + TS）
│   └── src/
│       ├── api/                   # Axios 接口封装
│       ├── router/                # 路由 + 鉴权守卫
│       ├── stores/                # Pinia 状态管理
│       ├── utils/                 # Axios 拦截器
│       └── views/                 # 页面组件
├── API_DOC.md                     # 后端接口文档（含 Postman 测试指南）
└── DESIGN.md                      # 设计说明
```

## 技术栈

| 端 | 技术 |
|----|------|
| 前端 | Vue 3 (Composition API + `<script setup>`) · Vite · TypeScript · Tailwind CSS · Element Plus · Axios · Pinia · Vue Router |
| 后端 | Spring Boot 3 · MyBatis-Plus · MySQL · Redis · Spring Security + JWT · 阿里云 OSS · DeepSeek AI |
| 主题色 | 小红书红 `#FF2442` |

## 快速开始

### 前置环境

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis（可选，需按配置）

### 1. 初始化数据库

```bash
mysql -u root -p < campus-xiaohongshu/sql/init.sql
```

### 2. 配置后端密钥

复制密钥模板到本地（**此文件已被 .gitignore 排除，不会提交到 Git**）：

```
campus-xiaohongshu/src/main/resources/application-local.yml
```

> 默认已生成该文件供本地使用。首次克隆仓库后需自行创建，并填入真实数据库密码、OSS 密钥、AI Key、JWT 密钥。模板结构见 `application.yml` 中的 `${...}` 占位符。

### 3. 启动后端

```bash
cd campus-xiaohongshu
./mvnw spring-boot:run      # Windows 用 mvnw.cmd
```

后端默认端口 `8081`。

### 4. 启动前端

```bash
cd campus-xiaohongshu-frontend
npm install
npm run dev
```

前端默认端口 `3000`，已配置代理将 `/api` 转发到 `http://localhost:8081`。

访问 http://localhost:3000

### 5. 测试账号

- `test / 123456`
- `test2 / 123456`

## 功能清单

- 登录 / 注册
- 首页瀑布流（分类筛选 + 无限滚动）
- 笔记详情（图片轮播 + 评论 + 点赞）
- 笔记发布（多图上传 + AI 智能润色 + 标签）
- 个人主页 / 编辑资料
- 通知中心（点赞、评论通知 + 已读标记）
- 关键词搜索

## 环境变量说明

后端敏感配置均通过环境变量注入（真实值在 `application-local.yml`）：

| 环境变量 | 说明 |
|----------|------|
| `MYSQL_PASSWORD` | MySQL 密码 |
| `REDIS_PASSWORD` | Redis 密码 |
| `OSS_ACCESS_KEY_ID` | 阿里云 OSS AccessKey ID |
| `OSS_ACCESS_KEY_SECRET` | 阿里云 OSS AccessKey Secret |
| `AI_API_KEY` | AI 大模型 API Key |
| `JWT_SECRET` | JWT 签名密钥（≥256 位） |

## License

内部学习项目，仅供交流。
