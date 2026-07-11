# CLAUDE.md — 速达外卖项目上下文

本文件为 Claude Code 提供项目上下文。详细文档见 `README.md`。

## 子项目索引

| 子项目 | 目录 | 说明 |
|--------|------|------|
| **后端服务** | `backend/suda-take-out/` | Spring Boot 3.4.3 + MyBatis + MySQL + Redis + Spring AI |
| **管理端** | `front/project-suda-admin-vue3/` | Vue 3 + Vite + Element Plus + Tailwind + Pinia |
| **小程序端** | `front/mp-weixin/project-rjwm-weixin-uniapp/` | Uni-app (Vue 2) 微信小程序 |

## 常用命令

### 后端

```bash
cd backend/suda-take-out
mvn clean install -DskipTests          # 构建全部模块
cd suda-server && mvn spring-boot:run  # 启动 → localhost:8080
```

API 文档：`http://localhost:8080/doc.html`（Knife4j）

### 管理端前端 (Vue 3)

```bash
cd front/project-suda-admin-vue3
npm install && npm run dev   # 开发 → localhost:8888
npm run build                # 生产构建
```

### 小程序端

**必须通过 HBuilderX 运行**（本项目无 `package.json`，不通过 npm 管理）：

1. 用 HBuilderX 打开项目目录 `front/mp-weixin/project-rjwm-weixin-uniapp`
2. 运行 → 运行到小程序模拟器 → 微信开发者工具
3. 微信开发者工具自动打开，勾选"不校验合法域名"

## 项目配置 — example 模板模式

| 模板（已提交） | 真实配置（git-ignore） |
|---|---|
| `.env.example`（根目录） | `.env` |
| `suda-server/.../application-dev.yml.example` | `application-dev.yml` |
| `front/project-suda-admin-vue3/.env.example` | `.env` |

## 后端模块

```
backend/suda-take-out/
├── suda-common/   → 工具类、常量、异常、Result/PageResult、BaseContext
├── suda-pojo/     → entity、dto、vo
└── suda-server/   → controller、service、mapper、config、interceptor、aspect、task、websocket、AI/
    ├── com.suda
    │   ├── AI/                → Spring AI 集成层
    │   │   ├── config/        → SpringAIConfig（ChatClient + 16个FunctionCallback）
    │   │   └── tools/         → UserAITools/DishTools/RecommendTools/CartTools
    │   ├── controller/
    │   │   ├── admin/         → 管理端接口（含 AdminAIController）
    │   │   ├── user/          → 用户端接口（含 UserAIController）
    │   │   └── notify/        → 支付回调
    │   ├── service/
    │   │   ├── AIService.java             → 管理端 AI 诊断服务
    │   │   ├── UserAIService.java         → 用户端 AI 点餐服务
    │   │   ├── BusinessToolService.java   → AI 经营数据分析工具
    │   │   └── impl/                      → 服务实现
    │   └── ...
```

## 管理端前端架构

见 README.md 项目结构章节，核心特点：
- Vue 3.5 + TypeScript 5.6 + Vite 6 + Element Plus 2.9 + Tailwind CSS 3.4
- 品牌色深蓝 `#1a56db`，强调色琥珀 `#f59e0b`
- 暗色模式 `html.dark` CSS 全覆盖
- 登录页 Canvas 粒子动画
- **AI 智能分析页**: `src/views/ai/index.vue`，LangChain4j SSE 流式问答 + 经营诊断
- **AI API 模块**: `src/api/modules/ai.ts`，原生 fetch SSE 流式读取

### 路由配置

| 路径 | 页面 | 说明 |
|------|------|------|
| `/login` | 登录页 | Canvas 粒子背景 |
| `/client-login` | 客户端登录 | C 端用户登录入口 |
| `/dashboard` | 工作台 | KPI 卡片 + 图表概览 |
| `/statistics` | 数据统计 | ECharts 可视化 + Excel 导出 |
| `/order` | 订单管理 | 7 状态 Tab + 搜索筛选 |
| `/dish` | 菜品管理 | CRUD + 图片上传 + 口味 |
| `/setmeal` | 套餐管理 | CRUD + 图片上传 + 菜品选择 |
| `/category` | 分类管理 | CRUD + 排序 + 启用/禁用 |
| `/employee` | 员工管理 | CRUD + 管理员保护 |
| `/message` | 消息通知 | WebSocket 实时推送 |
| `/ai` | AI 智能分析 | SSE 流式问答 + 经营诊断 |

## 小程序端架构

### 技术栈
- **框架**: Uni-app (Vue 2) + Vuex，HBuilderX 管理，编译为微信小程序
- **样式**: SCSS，`rpx` 单位系统（750rpx 设计稿）
- **无 npm 依赖**：不使用 `package.json`，所有依赖来自 HBuilderX IDE 工具链

### 目录结构（重构后）

```
front/mp-weixin/project-rjwm-weixin-uniapp/
├── pages/
│   ├── api/api.js           → 所有 API 接口定义（含 AI 相关）
│   ├── index/index.*        → 首页：分类浏览 + 菜品列表 + 购物车
│   ├── ai/index.vue         → AI 聊天页：SSE 流式对话 + 智能点餐
│   ├── my/my.vue            → 个人中心：地址管理、历史订单
│   ├── order/index.*        → 确认订单页
│   ├── order/success.vue    → 下单成功页
│   ├── historyOrder/        → 历史订单（分页）
│   ├── address/address.vue  → 地址列表
│   ├── addOrEditAddress/    → 新增/编辑地址
│   ├── nonet/index.vue      → 无网络页
│   └── common/
│       ├── Navbar/          → 自定义导航栏组件（首页+AI页使用）
│       └── simple-address/  → 省市区三级联动选择器
├── components/
│   ├── app-loading/         → 全屏加载遮罩
│   ├── app-empty/           → 空状态插画 + 提示文字
│   ├── app-skeleton/        → 骨架屏（菜品列表/分类/卡片/文本）
│   ├── dish-card/           → 菜品卡片（图片+信息+加减按钮）
│   ├── chat-bubble/         → AI 聊天气泡（用户蓝底/AI白底灰边）
│   ├── chat-input/          → AI 聊天输入区
│   └── reach-bottom/        → 上拉加载更多指示器
├── utils/
│   ├── env.js               → API baseUrl（localhost:8080）
│   ├── request.js           → HTTP 请求封装（JWT authentication header）
│   └── stream.js            → SSE 流式请求（wx.request + enableChunked）
├── store/index.js           → Vuex（含 AI 会话状态）
├── pages.json               → 路由 + TabBar 配置
├── uni.scss                 → 全局设计 Token（品牌色、间距、圆角、动画）
├── App.vue                  → 全局样式 + 动画 keyframes
└── manifest.json            → 应用配置（AppID 等）
```

### TabBar 导航（3 个 Tab）
| Tab | 页面 | 说明 |
|-----|------|------|
| 🏠 首页 | `pages/index/index` | 分类浏览 + 菜品列表 + 购物车弹窗 |
| 🤖 AI小速 | `pages/ai/index` | SSE 流式 AI 对话 + 智能点餐 |
| 👤 我的 | `pages/my/my` | 个人中心 + 地址 + 历史订单 |

### 设计系统（与管理端统一）

| Token | 值 | 用途 |
|-------|-----|------|
| 品牌主色 | `#1a56db` | 导航栏、按钮、选中态、强调 |
| 品牌深色 | `#1e40af` | 按钮按下态 |
| 品牌浅蓝 | `#eff6ff` / `#dbeafe` | 卡片浅蓝底、选中背景 |
| 强调色 | `#f59e0b` | 价格颜色、标签 |
| 成功绿 | `#10b981` | 订单完成 |
| 危险红 | `#ef4444` | 删除、角标 |
| 页面背景 | `#f3f4f7` | 全局底色 |
| 卡片背景 | `#fff` | 卡片、列表项 |
| 卡片阴影 | `0 4rpx 24rpx rgba(0,0,0,0.06)` | |
| 卡片圆角 | `16rpx` | |
| 动画缓动 | `cubic-bezier(0.4, 0, 0.2, 1)` | 弹窗/过渡 |
| 导航栏渐变 | `linear-gradient(135deg, #1e3a8a, #1a56db)` | |

## 核心架构模式（后端）

### JWT 双通道认证

- **管理端**：Header `token`，密钥 `admin-secret-key: kamten` → `JwtTokenAdminInterceptor`
- **用户端**：Header `authentication`，密钥 `user-secret-key: itheima` → `JwtTokenUserInterceptor`

### AI 集成（Spring AI + DeepSeek）

**关键文件**：
- `com.suda.AI.config.SpringAIConfig` — ChatClient 配置，16 个 FunctionCallback 注册
- `com.suda.AI.tools.UserAITools` — 时间工具（getCurrentDate/getCurrentDateTime/getTimeContext）
- `com.suda.AI.tools.DishTools` — 菜品查询工具（搜索/详情/分类/热门/价格筛选）
- `com.suda.AI.tools.RecommendTools` — 智能推荐工具
- `com.suda.AI.tools.CartTools` — 购物车操作工具
- `com.suda.service.impl.UserAIServiceImpl` — 用户 AI 服务实现
- `com.suda.controller.user.UserAIController` — 用户端 AI 控制器（同步/流式对话）
- `com.suda.controller.admin.AdminAIController` — 管理端 AI 控制器（自然语言查询/经营诊断）

**架构要点**：
- ChatClient 用 `List<FunctionCallback>` 自动收集所有 Bean，新增工具只需加 `@Bean` 即可自动生效
- AI 会话存储在 Redis：key `ai:session:user:{userId}`，TTL 2 天，最多 50 轮
- System Prompt 中 AI 名叫"小速"，遵循"推荐→询问→确认→操作"流程
- 管理端 AI 使用 LangChain4j，通过 `BusinessToolService` 提供经营数据查询工具

**用户端 AI API**：
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/user/ai/chat` | 同步对话 |
| POST | `/user/ai/chat/stream` | SSE 流式对话（120s 超时） |
| GET | `/user/ai/cart/status` | 获取购物车状态 |
| DELETE | `/user/ai/history/{sessionId}` | 清空对话历史 |

**管理端 AI API**：
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/admin/ai/query/stream` | 自然语言数据查询（SSE 流式） |
| POST | `/admin/ai/diagnose` | 一键经营诊断报告（SSE 流式） |

### 小程序 SSE 流式请求

微信小程序通过 `wx.request({ enableChunked: true })` 实现 SSE 流式读取，封装在 `utils/stream.js`。要求基础库 ≥ 2.20.1。

## ⚠️ 常见陷阱（开发时必须注意）

### 1. Vue 2 中 `mapState` 必须放在 `computed` 里，不能放在 `methods` 里

```javascript
// ❌ 错误 — 会报 "[Vue warn]: Computed property was assigned to but it has no setter"
methods: {
  ...mapState(['orderListData', 'token']),
}

// ✅ 正确
computed: {
  ...mapState(['orderListData', 'token']),
}
```

### 2. Java 字符串里的中文双引号会导致编译失败

```java
// ❌ 错误 — "今天" 中的引号被当成字符串结束符
.description("...不知道"今天"是哪一天...")

// ✅ 正确 — 用单引号或转义
.description("...不知道'今天'是哪一天...")
```

### 3. Spring 注入多个同类型 Bean 的坑

```java
// ❌ 风险 — 16 个 FunctionCallback 参数全靠参数名匹配 Bean 名，依赖 -parameters 编译标志
public ChatClient chatClient(..., FunctionCallback a, FunctionCallback b, ...)

// ✅ 安全 — 用 List<T> 自动收集所有同类型 Bean
public ChatClient chatClient(..., List<FunctionCallback> functionCallbacks)
```

### 4. Redis 端口

本地 Redis 端口是 **6380**（不是默认的 6379），密码 `123456`，database `1`。

### 5. HBuilderX 运行小程序

Uni-app 项目无 `package.json`，必须在 HBuilderX 中打开并运行。修改代码后直接在 HBuilderX 中重新编译运行即可看到效果。

### 6. 小程序 rpx 单位

所有尺寸用 `rpx`，基于 750rpx 设计稿。1rpx = 屏幕宽度/750。从管理端 px 值转换时，通常乘以 2 得到 rpx（如 `16px → 32rpx`）。
