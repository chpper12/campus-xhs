# 校园小红书 - 前端

仿小红书 PC 端前端界面，使用 Vue 3 + Vite + TypeScript 构建。

## 技术栈

- Vue 3（Composition API + `<script setup>`）
- Vite + TypeScript
- Tailwind CSS（主题色 `#FF2442`）
- Element Plus（Modal / Toast / Form）
- Axios（请求/响应拦截器 + Bearer Token）
- Pinia（用户状态管理）
- Vue Router（路由 + 鉴权守卫）

## 目录结构

```
src/
├── api/           # 接口封装（auth / posts / comments / notifications / user）
├── router/        # 路由配置 + 鉴权守卫
├── stores/        # Pinia 状态（user）
├── utils/         # Axios 封装（拦截器）
├── views/         # 页面组件
│   ├── Login.vue
│   ├── Register.vue
│   ├── Home.vue           # 首页（Header + 侧边栏 + 分类 + 瀑布流）
│   ├── Search.vue         # 搜索页
│   ├── Profile.vue        # 个人主页
│   ├── Notifications.vue  # 通知中心
│   ├── PublishModal.vue   # 发布弹窗
│   ├── PostDetailModal.vue# 详情弹窗
│   └── EditProfileModal.vue
└── assets/styles/  # 全局样式
```

## 启动

```bash
npm install
npm run dev      # http://localhost:3000
npm run build    # 生产构建到 dist/
```

> 开发环境已配置代理：`/api` → `http://localhost:8081`（见 `vite.config.ts`）

## 代理配置

后端端口若非 `8081`，修改 `vite.config.ts` 中的 `server.proxy`：

```ts
server: {
  proxy: {
    '/api': { target: 'http://localhost:8081', changeOrigin: true }
  }
}
```
