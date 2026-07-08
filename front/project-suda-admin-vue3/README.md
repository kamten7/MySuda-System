# 速达外卖 管理后台 (Suda Admin Vue3)

基于 **Vue 3 + TypeScript + Vite + Element Plus + Tailwind CSS** 的现代化后台管理系统。

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue 3 (Composition API) | 3.5+ |
| 构建 | Vite | 6.x |
| 语言 | TypeScript | 5.6 |
| UI 库 | Element Plus | 2.9+ |
| 状态管理 | Pinia | 2.x |
| 路由 | Vue Router | 4.x |
| HTTP | Axios | 1.x |
| 图表 | ECharts | 5.x |
| CSS | Tailwind CSS | 3.4 |

## 项目结构

```
src/
├── api/              # API 接口层 (Axios + 拦截器 + 类型定义)
│   ├── modules/      # 按业务模块拆分 API (auth/employee/category/dish/order/...)
│   └── types/        # API 响应类型 (ApiResponse, PageResult)
├── assets/           # 静态资源 (图片/音频/字体)
├── components/       # 全局组件
│   ├── layout/       # 布局组件 (Sidebar/Navbar/Breadcrumb)
│   ├── StatCard.vue  # KPI 统计卡片
│   ├── EmptyState.vue
│   └── ImageUpload.vue
├── composables/      # 组合式函数 (usePagination/useChart/useTheme)
├── layouts/          # 页面布局 (DefaultLayout)
├── router/           # 路由配置 + 守卫
├── stores/           # Pinia 状态管理 (app/user)
├── styles/           # 全局样式 (Tailwind/Element Plus 覆盖/过渡动画)
├── utils/            # 工具函数 (cookies/auth/date)
└── views/            # 页面组件
    ├── login/        # 登录
    ├── dashboard/    # 工作台
    ├── order/        # 订单管理
    ├── dish/         # 菜品管理
    ├── setmeal/      # 套餐管理
    ├── category/     # 分类管理
    ├── employee/     # 员工管理
    ├── statistics/   # 数据统计
    ├── message/      # 消息通知
    └── error/        # 404 等错误页
```

## 快速开始

### 前置条件

- Node.js >= 18
- 后端服务运行在 `http://localhost:8080`

### 安装与运行

```bash
# 进入项目目录
cd front/project-suda-admin-vue3

# 安装依赖
npm install

# 复制环境变量（如需自定义）
cp .env.example .env

# 启动开发服务器 → http://localhost:8888
npm run dev

# 生产构建
npm run build
```

### 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `VITE_API_BASE_URL` | API 前缀（与 Vite proxy 一致） | `/api` |
| `VITE_SERVER_URL` | 后端服务地址 | `http://localhost:8080/admin` |
| `VITE_WS_URL` | WebSocket 地址 | `ws://localhost:8080/ws/` |
| `VITE_DELETE_PERMISSIONS` | 是否显示删除操作 | `true` |

### 开发代理

Vite 开发服务器自动代理：
- `/api/*` → `http://localhost:8080/admin/*`（去除 `/api` 前缀）

## 认证机制

- JWT Token 存储在 cookie (`token`)
- 请求头：`token: <jwt_token>`（非 `Authorization: Bearer`）
- 后端密钥：`admin-secret-key: itcast`，TTL 2 小时
- 路由守卫：无 token → 重定向 `/login`

## 订单状态流转

```
1: 待付款 → 2: 待接单 → 3: 待派送 → 4: 派送中 → 5: 已完成
                       ↘ 6: 已取消
```

管理员操作：接单、拒单、派送、完成、取消。

## 迁移说明（从旧前端）

旧前端位于 `front/project-suda-admin-vue-ts/`（Vue 2 + Element UI + Vue CLI）。

### 主要变更

| 旧 | 新 |
|----|----|
| Vue 2.6 + Class Component | Vue 3.5 + Composition API `<script setup>` |
| Element UI 2.x | Element Plus 2.9+ |
| Vuex 3 | Pinia 2 |
| Vue Router 3 | Vue Router 4 |
| Vue CLI / Webpack | Vite |
| SCSS only | Tailwind CSS + SCSS |
| vue-property-decorator | 原生 Composition API |

### 兼容性

- ✅ 100% API 兼容 — 所有后端接口路径、请求格式、响应格式保持不变
- ✅ JWT 认证方式不变 — `token` header
- ✅ 开发代理路径不变 — `/api/*` → `/admin/*`
- ✅ 环境变量逻辑不变 — `VITE_DELETE_PERMISSIONS`

### 功能覆盖

| 页面 | 状态 | 说明 |
|------|------|------|
| 登录 | ✅ | 品牌设计，表单验证 |
| 工作台 | ✅ | KPI 卡片 + 订单概览 + 菜品/套餐总览 |
| 订单管理 | ✅ | 7 状态 Tab，搜索/筛选，状态流转，详情抽屉 |
| 菜品管理 | ✅ | CRUD + 图片上传 + 口味管理 + 批量删除 |
| 套餐管理 | ✅ | CRUD + 图片上传 + 菜品选择对话框 |
| 分类管理 | ✅ | CRUD + 启用/禁用 |
| 员工管理 | ✅ | CRUD + 启用/禁用 + 管理员保护 |
| 数据统计 | ✅ | ECharts 图表（营业额/用户/订单/Top10）+ Excel 导出 |
| 消息通知 | ⚠️ | UI 已实现，等待后端 `/messages/*` 接口 |
| 404 | ✅ | 品牌化 404 页面 |
| 主题切换 | ✅ | 亮色/暗色模式 |
