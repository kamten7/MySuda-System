<h1 align="center">
  <img src="https://img.shields.io/badge/Vue-3.5-4FC08D?logo=vuedotjs&logoColor=white" alt="Vue 3">
  <img src="https://img.shields.io/badge/TypeScript-5.6-3178C6?logo=typescript&logoColor=white" alt="TypeScript">
  <img src="https://img.shields.io/badge/Vite-6.x-646CFF?logo=vite&logoColor=white" alt="Vite">
  <img src="https://img.shields.io/badge/Element_Plus-2.9-409EFF?logo=element&logoColor=white" alt="Element Plus">
  <br><br>
  速达外卖 · 管理后台
</h1>

<p align="center">
  <strong>Vue 3 + TypeScript + Vite + Element Plus + Tailwind CSS 现代化后台管理系统</strong>
</p>

<p align="center">
  <a href="#-简介">简介</a> ·
  <a href="#-技术栈">技术栈</a> ·
  <a href="#-项目结构">项目结构</a> ·
  <a href="#-快速开始">快速开始</a> ·
  <a href="#-认证机制">认证</a> ·
  <a href="#-主题系统">主题</a> ·
  <a href="#-功能覆盖">功能</a> ·
  <a href="#-从旧前端迁移">迁移</a>
</p>

---

## 📖 简介

速达外卖管理后台是基于 **Vue 3 Composition API** 构建的现代化 SPA 后台管理系统，为商家提供员工管理、菜品/套餐 CRUD、订单流转、数据统计与报表导出、**AI 智能运营诊断**等全方位运营能力。

### 设计原则

- **`<script setup lang="ts">`** — 简洁的 Composition API 风格
- **暗色模式优先** — `html.dark` 下零白色残留，Element Plus + Tailwind 全覆盖
- **类型安全** — TypeScript 严格模式，API 请求/响应全类型化
- **原子化 CSS** — Tailwind CSS 实用优先，减少自定义样式

---

## 🛠 技术栈

| 类别 | 技术 | 版本 | 说明 |
|------|------|------|------|
| **框架** | Vue 3 | 3.5+ | Composition API + `<script setup>` |
| **构建工具** | Vite | 6.x | 极速 HMR，ESBuild 预构建 |
| **语言** | TypeScript | 5.6 | 类型安全，IDE 智能提示 |
| **UI 组件库** | Element Plus | 2.9+ | 企业级桌面端组件库 |
| **CSS 框架** | Tailwind CSS | 3.4 | 实用优先的原子化 CSS |
| **状态管理** | Pinia | 2.x | Vue 3 官方推荐状态管理 |
| **路由** | Vue Router | 4.x | SPA 路由，懒加载 + 导航守卫 |
| **HTTP 客户端** | Axios | 1.x | 请求/响应拦截器，统一错误处理 |
| **图表** | ECharts + vue-echarts | 5.5 / 7.0 | 数据统计可视化 |
| **日期处理** | dayjs | 1.11 | 轻量级日期库 |
| **进度条** | NProgress | 0.2 | 页面加载进度指示 |
| **Cookie** | js-cookie | 3.0 | Token 及用户信息持久化 |
| **代码规范** | ESLint + Prettier + Husky | — | 代码格式化 + Git Hooks |
| **自动导入** | unplugin-auto-import | 0.18 | Vue/Element Plus API 自动导入 |
| **组件注册** | unplugin-vue-components | 0.27 | Element Plus 组件按需加载 |

---

## 📁 项目结构

```
src/
├── api/                          # API 接口层
│   ├── index.ts                  #   Axios 实例（请求/响应拦截器）
│   ├── modules/                  #   按业务模块拆分的 API
│   │   ├── auth.ts               #     认证（登录/登出）
│   │   ├── employee.ts           #     员工管理
│   │   ├── category.ts           #     分类管理
│   │   ├── dish.ts               #     菜品管理
│   │   ├── setmeal.ts            #     套餐管理
│   │   ├── order.ts              #     订单管理
│   │   ├── statistics.ts         #     数据统计
│   │   ├── common.ts             #     公共接口（文件上传等）
│   │   ├── shop.ts               #     店铺状态
│   │   └── ai.ts                 #     AI 智能问答（SSE 流式）
│   └── types/                    #   API 响应类型
│       ├── ApiResponse.ts        #     Result<T> { code, msg, data }
│       └── PageResult.ts         #     PageResult { total, records }
│
├── assets/                       # 静态资源（图片/音频/字体）
│
├── components/                   # 全局组件
│   ├── layout/                   #   布局组件
│   │   ├── AppSidebar.vue        #     侧边栏导航
│   │   ├── AppNavbar.vue         #     顶部导航栏
│   │   └── AppBreadcrumb.vue     #     面包屑导航
│   ├── StatCard.vue              #   KPI 统计卡片
│   ├── EmptyState.vue            #   空状态占位组件
│   └── ImageUpload.vue           #   图片上传组件（MinIO/OSS）
│
├── composables/                  # 组合式函数
│   ├── usePagination.ts          #   分页逻辑封装
│   ├── useChart.ts               #   ECharts 图表封装
│   └── useTheme.ts               #   主题切换逻辑
│
├── layouts/                      # 页面布局
│   └── DefaultLayout.vue         #   默认后台布局（侧边栏+顶栏+内容区）
│
├── router/                       # 路由
│   ├── index.ts                  #   路由实例
│   ├── routes.ts                 #   路由配置（懒加载）
│   └── guards.ts                 #   导航守卫（NProgress + Token 校验）
│
├── stores/                       # Pinia 状态管理
│   ├── app.ts                    #   应用状态（主题/侧边栏/首次进入动画）
│   └── user.ts                   #   用户状态（Token/登录/用户信息）
│
├── styles/                       # 全局样式
│   ├── tailwind.css              #   Tailwind 基础层
│   ├── index.scss                #   全局 SCSS 变量与重置
│   └── element-override.scss     #   Element Plus 样式覆盖（含暗色模式）
│
├── utils/                        # 工具函数
│   ├── cookies.ts                #   Cookie 读写封装
│   ├── auth.ts                   #   认证工具函数
│   └── date.ts                   #   dayjs 日期格式化
│
└── views/                        # 页面组件
    ├── login/                    #   登录页（Canvas 粒子背景）
    ├── client-login/             #   客户端登录页
    ├── dashboard/                #   工作台（KPI 卡片 + 图表概览）
    ├── order/                    #   订单管理（7 状态 Tab + 搜索/筛选 + 详情抽屉）
    ├── dish/                     #   菜品管理（CRUD + 图片上传 + 口味管理）
    ├── dish/edit.vue             #   菜品编辑页
    ├── setmeal/                  #   套餐管理（CRUD + 图片上传 + 菜品选择对话框）
    ├── setmeal/edit.vue          #   套餐编辑页
    ├── category/                 #   分类管理（CRUD + 排序控制 + 启用/禁用）
    ├── employee/                 #   员工管理（CRUD + 管理员保护）
    ├── employee/edit.vue         #   员工编辑页
    ├── statistics/               #   数据统计（ECharts + Excel 导出）
    ├── message/                  #   消息通知（WebSocket 实时推送）
    ├── ai/                       #   🆕 AI 智能分析（SSE 流式问答 + 经营诊断）
    └── error/                    #   404 等错误页面
```

---

## 🚀 快速开始

### 前置条件

- **Node.js** >= 18
- **后端服务**运行在 `http://localhost:8080`（启动方式见[后端 README](../../backend/suda-take-out/README.md)）

### 安装与运行

```bash
# 进入项目目录
cd front/project-suda-admin-vue3

# 安装依赖
npm install

# 复制环境变量模板（如需自定义）
cp .env.example .env

# 启动开发服务器 → http://localhost:8888
npm run dev

# 生产构建
npm run build

# 预览生产构建
npm run preview
```

### 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `VITE_API_BASE_URL` | API 请求前缀（与 Vite proxy 一致） | `/api` |
| `VITE_SERVER_URL` | 后端服务地址 | `http://localhost:8080/admin` |
| `VITE_WS_URL` | WebSocket 地址 | `ws://localhost:8080/ws/` |
| `VITE_DELETE_PERMISSIONS` | 是否显示删除操作按钮 | `true` |

### 开发代理

Vite 开发服务器自动代理 API 请求：

```
/api/*  →  http://localhost:8080/admin/*  （去除 /api 前缀）
```

---

## 🔐 认证机制

```
┌──────────┐     ┌──────────────┐     ┌──────────────┐
│  Login    │────▶│  Cookie      │────▶│  Axios        │
│  POST     │     │  token=xxx   │     │  Header:      │
│  /employee│     │              │     │  token: xxx   │
│  /login   │     │              │     │              │
└──────────┘     └──────────────┘     └──────────────┘
```

- **Token 存储**：Cookie `token`
- **请求头**：`token: <jwt_token>`（非标准 `Authorization: Bearer`）
- **后端密钥**：`admin-secret-key`，Token 有效期 2 小时
- **路由守卫**：`router.beforeEach` 检查 Token → 无 Token 重定向 `/login`

---

## 🎨 主题系统

### 品牌色

| 角色 | 色值 | 用途 |
|------|------|------|
| 品牌蓝 | `#1a56db` | 主按钮、链接、侧边栏高亮 |
| 强调红 | `#d32f2f` | 登录按钮、删除操作 |
| 辅助金 | `#f59e0b` | 警告提示、强调标记 |

### 暗色模式

- **切换方式**：`html` 元素添加/移除 `dark` class
- **覆盖范围**：Element Plus 组件 + Tailwind 工具类 + 页面级 scoped 样式
- **持久化**：用户选择存入 Pinia + Cookie，刷新保持

### 登录页特效

Canvas 粒子系统：卡片为波源，250 粒子涟漪扩散，浅蓝→深蓝渐变，响应窗口 resize。

---

## 📋 订单状态流转

```
1: 待付款 ──支付成功──▶ 2: 待接单 ──商家接单──▶ 3: 待派送
    │                                                   │
    │ 超时 15min / 用户取消                               │ 商家派送
    ▼                                                   ▼
6: 已取消                                       4: 派送中
                                                     │
                                               凌晨 1:00 自动完成
                                                     ▼
                                                5: 已完成
```

管理员操作：**接单** → **拒单** → **派送** → **完成**，按钮按状态动态显示。

---

## 📊 功能覆盖

| 页面 | 状态 | 功能说明 |
|------|------|---------|
| 🔐 登录 | ✅ 完成 | 品牌设计 + Canvas 粒子背景，表单验证，JWT Token 签发 |
| 👤 客户端登录 | ✅ 完成 | 面向 C 端用户的登录入口 |
| 📈 工作台 | ✅ 完成 | KPI 统计卡片 + 订单概览 + 菜品/套餐总览 |
| 📋 订单管理 | ✅ 完成 | 7 状态 Tab 切换，搜索/筛选，状态流转操作，详情抽屉 |
| 🍜 菜品管理 | ✅ 完成 | CRUD + 图片上传 (MinIO/OSS) + 口味管理 + 批量删除 + 启售/停售 |
| 📦 套餐管理 | ✅ 完成 | CRUD + 图片上传 + 菜品选择对话框 + 多对多关联 |
| 📂 分类管理 | ✅ 完成 | CRUD + 排序控制 + 启用/禁用 |
| 👷 员工管理 | ✅ 完成 | CRUD + 启用/禁用 + 管理员账号保护 |
| 📊 数据统计 | ✅ 完成 | ECharts 图表（营业额/用户/订单趋势/Top10）+ Excel 导出 |
| 💬 消息通知 | ✅ 完成 | WebSocket 实时接收来单/催单通知 |
| 🤖 AI 智能分析 | ✅ 完成 | LangChain4j 集成，自然语言数据查询 + 一键经营诊断，SSE 流式输出 |
| 🌓 主题切换 | ✅ 完成 | 亮色/暗色模式，Element Plus + Tailwind 全覆盖 |
| ❌ 404 | ✅ 完成 | 品牌化 404 错误页面 |

---

## 🔄 从旧前端迁移

旧前端位于 `front/project-suda-admin-vue-ts/`（Vue 2 + Element UI + Vue CLI + Vuex + vuex-module-decorators）。

### 主要变更

| 维度 | 旧 (Vue 2) | 新 (Vue 3) |
|------|-----------|-----------|
| 框架 | Vue 2.6 + Class Component | Vue 3.5 + Composition API `<script setup>` |
| UI 库 | Element UI 2.x | Element Plus 2.9+ |
| 状态管理 | Vuex 3 + vuex-module-decorators | Pinia 2 |
| 路由 | Vue Router 3 | Vue Router 4 |
| 构建工具 | Vue CLI 5 + Webpack 5 | Vite 6 |
| 样式方案 | SCSS only | Tailwind CSS + SCSS |
| 组件风格 | `@Component` 装饰器 | 原生 Composition API |

### 兼容性保证

- ✅ **100% API 兼容** — 所有后端接口路径、请求格式、响应格式保持不变
- ✅ **JWT 认证不变** — `token` header
- ✅ **开发代理不变** — `/api/*` → `/admin/*`
- ✅ **环境变量兼容** — `VITE_DELETE_PERMISSIONS`

---

## 📄 相关文档

- [根项目 README](../../README.md) — 项目整体介绍
- [后端 API 文档](http://localhost:8080/doc.html) — Knife4j 在线接口文档
- [后端 README](../../backend/suda-take-out/README.md) — 后端详细说明
