# 校园小红书 - 后端

仿小红书 PC 端校园社交平台后端服务，基于 Spring Boot 3。

## 技术栈

- Spring Boot 3
- MyBatis-Plus
- MySQL 8 + Redis
- Spring Security + JWT 鉴权
- 阿里云 OSS（图片存储）
- DeepSeek AI（AI 润色）

## 目录结构

```
src/main/java/com/chenpperr/xhs/
├── annotation/      # 自定义注解（@CurrentUserId）
├── common/          # 统一响应 Result / 分页 PageResult
├── config/          # 配置类（OSS / AI / MyBatis-Plus / WebMvc）
├── controller/      # 控制器
├── dto/             # 请求/响应 DTO
├── entity/          # 实体类
├── exception/       # 全局异常处理
├── interceptor/     # 拦截器
├── mapper/          # MyBatis Mapper
├── resolver/        # 参数解析器
├── security/        # JWT / Security 配置
└── service/         # 业务逻辑
```

## 启动

### 1. 初始化数据库

```bash
mysql -u root -p < sql/init.sql
```

### 2. 配置密钥

复制 `src/main/resources/application-local.yml`（已 gitignore），填入真实密钥：

```yaml
spring:
  datasource:
    password: 你的MySQL密码
  data:
    redis:
      password: 你的Redis密码
aliyun:
  oss:
    access-key-id: xxx
    access-key-secret: xxx
ai:
  api-key: sk-xxx
jwt:
  secret: 你的JWT密钥（≥256位）
```

### 3. 运行

```bash
./mvnw spring-boot:run    # Windows: mvnw.cmd spring-boot:run
```

后端默认端口 `8081`，接口统一前缀 `/api`。

## 接口文档

完整接口文档见仓库根目录 `API_DOC.md`，统一响应格式：

```json
{ "code": 200, "msg": "success", "data": {} }
```

## 主要接口

| Method | Path | 说明 |
|--------|------|------|
| POST | `/api/v1/auth/login` | 登录 |
| POST | `/api/v1/auth/register` | 注册 |
| GET | `/api/v1/posts` | 笔记列表（分页+分类） |
| GET | `/api/v1/posts/{id}` | 笔记详情 |
| POST | `/api/v1/posts` | 发布笔记 |
| POST | `/api/v1/posts/{id}/like` | 点赞/取消 |
| GET | `/api/v1/posts/{id}/comments` | 评论列表 |
| POST | `/api/v1/posts/{id}/comments` | 发表评论 |
| GET | `/api/v1/notifications` | 通知列表 |
| PUT | `/api/v1/notifications/{id}/read` | 标记已读 |
