# 管理端 CLAUDE.md — project-suda-admin-vue3

## 技术栈
- Vue 3.5 + TypeScript 5.6 + Vite 6
- Element Plus 2.9 + Tailwind CSS 3.4
- Pinia 2.x（状态管理）+ Vue Router 4.x
- Axios 1.x（请求/响应拦截器）
- ECharts 5 + vue-echarts（数据可视化）
- 品牌色：深蓝 `#1a56db` / 强调色琥珀 `#f59e0b`

## 环境变量配置
文件：`.env`（从 `.env.example` 复制）
| 变量 | 说明 | 默认值 |
|------|------|--------|
| `VITE_API_BASE_URL` | API 基础路径 | `/api` |
| `VITE_SERVER_URL` | 后端服务地址 | `http://localhost:8080/admin` |
| `VITE_WS_URL` | WebSocket 地址 | `ws://localhost:8080/ws/` |
| `VITE_DELETE_PERMISSIONS` | 删除权限 | `true` |

## 目录结构
src/
├── api/
│   ├── request.ts          # Axios 封装（拦截器）
│   └── modules/
│       ├── auth.ts         # 登录/登出
│       ├── employee.ts     # 员工 CRUD
│       ├── category.ts     # 分类 CRUD
│       ├── dish.ts         # 菜品 CRUD + 口味
│       ├── setmeal.ts      # 套餐 CRUD + 菜品关联
│       ├── order.ts        # 订单状态管理
│       ├── report.ts       # 数据统计报表
│       ├── workspace.ts    # 工作台 KPI
│       ├── ai.ts           # SSE 流式 AI 问答（原生 fetch）
│       ├── message.ts      # WebSocket 消息
│       ├── shop.ts         # 营业状态
│       └── common.ts       # 文件上传
├── components/             # 全局组件
│   ├── layout/             # AppSidebar / AppNavbar / AppBreadcrumb
│   ├── StatCard.vue        # KPI 卡片
│   ├── ImageUpload.vue     # 图片上传
│   └── EmptyState.vue      # 空状态
├── composables/            # 组合式函数
│   ├── usePagination.ts    # 分页逻辑
│   ├── useChart.ts         # ECharts 封装
│   └── useTheme.ts         # 暗色模式
├── layouts/                # DefaultLayout
├── router/                 # 路由 + 导航守卫
├── stores/                 # Pinia
│   ├── app.ts              # 侧边栏折叠、暗色模式
│   └── user.ts             # 用户信息、token
├── styles/                 # 设计 Token + 动画系统
├── utils/                  # cookies / auth / date
└── views/
    ├── ai/index.vue        # AI 智能分析页（SSE 流式问答 + 经营诊断）
    ├── dashboard/          # 工作台
    ├── statistics/         # 数据统计（ECharts + Excel 导出）
    ├── order/              # 订单管理（7 状态 Tab）
    ├── dish/               # 菜品管理（CRUD + 图片上传 + 口味）
    ├── setmeal/            # 套餐管理
    ├── category/           # 分类管理
    ├── employee/           # 员工管理
    ├── message/            # 来单/催单消息
    └── login/              # 登录页（Canvas 粒子背景）

## 路由速查
| 路径 | 页面 | 说明 |
|------|------|------|
| `/login` | 登录页 | Canvas 粒子背景 |
| `/dashboard` | 工作台 | KPI 卡片 + 图表 |
| `/statistics` | 数据统计 | ECharts + Excel 导出 |
| `/order` | 订单管理 | 7 状态 Tab |
| `/dish` | 菜品管理 | CRUD + 图片上传 |
| `/setmeal` | 套餐管理 | CRUD + 菜品选择 |
| `/category` | 分类管理 | CRUD + 排序 |
| `/employee` | 员工管理 | CRUD + 管理员保护 |
| `/ai` | AI 智能分析 | SSE 流式问答 + 诊断 |
| `/message` | 来单提醒 | WebSocket 实时消息 |

## Pinia Stores
| Store | 文件 | 状态 |
|-------|------|------|
| app | `app.ts` | `sidebarCollapsed`、`isDarkMode` |
| user | `user.ts` | `userInfo`、`token`、`roles` |

## AI 页面开发要点
- `src/api/modules/ai.ts`：原生 fetch 读取 SSE 流
- `src/views/ai/index.vue`：LangChain4j SSE 流式问答 + 经营诊断（对接 DeepSeek 官方 API）
- 暗色模式：`html.dark` CSS 全覆盖

## 构建命令
```bash
npm run dev              # 开发环境 → localhost:8888
npm run build            # 生产构建
npm run build:staging    # 预发布构建
npm run preview          # 预览构建结果
npm run lint             # ESLint 检查
npm run format           # Prettier 格式化
```

## 常见陷阱
1. **图片上传**：使用 `VITE_API_BASE_URL` 拼接 `/common/upload`
2. **WebSocket**：需要在登录后建立连接，clientId 存储在 localStorage
3. **暗色模式**：通过 `document.documentElement.classList.toggle('dark')` 切换
