# CLAUDE.md — 速达外卖项目

## 项目身份
全栈外卖平台：Spring Boot 3.4.3 + Vue 3.5 + Uni-app (Vue 2) 微信小程序。详细文档见各子项目 README 及子 CLAUDE.md。

## 开发环境要求
| 环境 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | 后端必需 |
| Maven | 3.6+ | 后端构建 |
| Node.js | 18+ | 管理端必需 |
| MySQL | 8.0+ | 数据库（开发环境端口 3308） |
| Redis | 5.0+ | 端口 6380，密码 123456，database 1 |
| HBuilderX | 最新 | 小程序开发必需 |
| 微信开发者工具 | 最新 | 小程序预览 |

## 核心约束（全局生效）
1. **Java 字符串**：中文引号用单引号 `'今天'`，否则编译失败
2. **Vue 2 小程序**：`mapState` 必须在 `computed` 里，不在 `methods` 里
3. **Redis**：端口 6380，密码 123456，database 1
4. **小程序单位**：使用 rpx（750rpx 设计稿），px 值 × 2 = rpx
5. **JWT 双通道**：管理端 Header `token`（密钥 kamten），用户端 Header `authentication`（密钥 itheima）

## 环境配置步骤
1. **后端配置**：
    - 复制 `backend/suda-take-out/suda-server/src/main/resources/application-dev.yml.example` → `application-dev.yml`
    - 填入真实的 MySQL、Redis、微信支付、AI API Key 等配置
2. **管理端配置**：
    - 复制 `front/project-suda-admin-vue3/.env.example` → `.env`
    - 确认 `VITE_SERVER_URL=http://localhost:8080/admin`
3. **小程序配置**：
    - 修改 `utils/env.js` 中的 `baseUrl`（默认 `http://localhost:8080`）

## AI 提供商配置
项目当前主用 **Agnes AI**（`apihub.agnes-ai.com`），同时保留了 DeepSeek 和豆包（火山引擎）的配置入口。用户端 + 管理端统一使用 LangChain4j。

| 框架 | 端 | 配置路径 | Base URL 格式 |
|------|------|---------|-------------|
| LangChain4j 1.6.0 | 用户端 + 管理端 | `suda.langchain4j.open-ai.*` | **带** `/v1` |

## Docker Compose 基础设施
项目根目录 `docker-compose.yml` 定义了 4 个服务：
| 服务 | 镜像 | 端口 | 说明 |
|------|------|------|------|
| MySQL | `mysql:8.0` | 3308 | 数据库 `sky_take_out`，自动健康检查 |
| Redis | `redis:7-alpine` | 6380 | 密码 `123456` |
| MinIO | `minio/minio:latest` | 9010 (API) / 9011 (Console) | 对象存储 |
| MinIO Init | `minio/mc:latest` | — | 自动创建 `sky-take-out` bucket |

```bash
docker-compose up -d   # 一键启动所有基础设施
```

MySQL 端口为 3308（非默认 3306），Redis 端口为 6380（非默认 6379）。

## 启动顺序
1. **基础设施**：启动 MySQL + Redis 服务
2. **后端**：`cd backend/suda-take-out && mvn spring-boot:run -pl suda-server` → localhost:8080
3. **管理端**：`cd front/project-suda-admin-vue3 && npm run dev` → localhost:8888
4. **小程序**：HBuilderX 打开 `front/mp-weixin/project-rjwm-weixin-uniapp` → 运行到微信开发者工具

## 子项目索引
| 模块 | 路径 | 技术栈 | 详细文档 |
|------|------|--------|----------|
| 后端 | `backend/suda-take-out/` | Spring Boot 3.4.3 + MyBatis 3.0.4 + Redis + LangChain4j 1.6.0 | `backend/suda-take-out/CLAUDE.md` |
| 管理端 | `front/project-suda-admin-vue3/` | Vue 3.5 + Element Plus 2.9 + Tailwind CSS 3.4 + Pinia 2.3 + ECharts 5 | `front/project-suda-admin-vue3/CLAUDE.md` |
| 小程序 | `front/mp-weixin/project-rjwm-weixin-uniapp/` | Uni-app (Vue 2) + Vuex | `front/mp-weixin/project-rjwm-weixin-uniapp/CLAUDE.md` |

---

## 后端架构

### 三模块结构
```
backend/suda-take-out/
├── suda-common/     # 工具类、常量（5个）、异常（12个）、Result/PageResult、BaseContext、JWT/OSS工具
├── suda-pojo/       # Entity（12个）、DTO（23个）、VO（19个）
└── suda-server/     # 核心服务：Controller → Service → Mapper
```

### suda-server 包结构
```
com.suda/
├── AI/                          # AI 集成层（LangChain4j 统一框架）
│   ├── AIAgent.java             #   管理端 + 用户端 LangChain4j 模型配置
│   ├── service/                 #   UserAIIntentService / UserAIPromptBuilder（预执行架构）
│   └── tools/
│       ├── UserAITools.java              #   时间感知工具（3个方法）
│       ├── DishTools.java                #   菜品搜索工具（5个方法 + name匹配）
│       ├── RecommendTools.java           #   智能推荐工具（2个方法）
│       ├── CartTools.java                #   购物车工具（5个方法）
│       └── RecommendationContextHolder.java #   推荐上下文追踪（ThreadLocal + 静态缓冲区）
├── SudaApplication.java         # 启动类（排除 AI 自动配置，启用缓存/事务/定时任务）
├── annotation/  → AutoFill.java
├── aspect/      → AutiFillAspect.java（公共字段自动填充）
├── config/      → WebMvcConfiguration / RedisConfiguration / MinioConfiguration / WebSocketConfiguration
├── controller/
│   ├── admin/   → 10 个 Controller（含 AdminAIController）
│   ├── user/    → 9 个 Controller（含 UserAIController）
│   └── notify/  → PayNotifyController（微信支付回调）
├── handler/     → GlobalExceptionHandler.java
├── interceptor/ → JwtTokenAdminInterceptor / JwtTokenUserInterceptor
├── mapper/      → 11 个 MyBatis Mapper 接口
├── service/     → 12 个 Service 接口 + 11 个 impl + BusinessToolService（AI 工具）
├── task/        → MyTask.java / OrderTask.java（定时任务）
└── websocket/   → WebSocketServer.java（`/ws/{sid}`）
```

### 数据库表（11张）
| 表名 | 说明 | 表名 | 说明 |
|------|------|------|------|
| `employee` | 管理端员工 | `setmeal` / `setmeal_dish` | 套餐 + 菜品关联 |
| `user` | C端用户(openid) | `address_book` | 收货地址 |
| `category` | 菜品/套餐分类 | `shopping_cart` | 购物车（服务端存储） |
| `dish` / `dish_flavor` | 菜品 + 口味 | `orders` / `order_detail` | 订单主表 + 明细 |

### API 端点速查

**用户端 AI（"小速"智能点餐）** — `UserAIController` (`/user/ai`)
| 方法 | 路径 | 说明 | 超时 |
|------|------|------|------|
| POST | `/user/ai/chat` | 同步 AI 对话 | — |
| POST | `/user/ai/chat/stream` | SSE 流式 AI 对话 | 120s |
| GET | `/user/ai/cart/status` | 获取购物车状态 | — |
| DELETE | `/user/ai/history/{sessionId}` | 清空对话历史 | — |

**管理端 AI** — `AdminAIController` (`/admin/ai`)
| 方法 | 路径 | 说明 | 超时 |
|------|------|------|------|
| POST | `/admin/ai/query/stream` | 自然语言数据查询（SSE 流式） | 120s |
| POST | `/admin/ai/diagnose` | 一键经营诊断报告（SSE 流式） | 180s |

**WebSocket**
| 端点 | 说明 |
|------|------|
| `ws://localhost:8080/ws/{clientId}` | 来单/催单实时推送 |

### 其他关键 Controller

**管理端** (`/admin/**`)：Employee / Category / Dish / Setmeal / Order / Report / Workspace / Common / Shop

**用户端** (`/user/**`)：User / Category / Dish / Setmeal / AddressBook / ShoppingCart / Order / Shop / AI

**回调** (`/notify/**`)：PayNotify（微信支付回调）

### JWT 认证架构
| 维度 | 管理端 | 用户端 |
|------|--------|--------|
| Header | `token` | `authentication` |
| 密钥 | `kamten` | `itheima` |
| TTL | 2 小时 | 2 小时 |
| 拦截路径 | `/admin/**` | `/user/**` |
| 排除路径 | `/admin/employee/login` | `/user/user/login`、`/user/shop/status`、`/user/order/historyOrders` |

### 定时任务
| Cron | 任务 | 说明 |
|------|------|------|
| `0 * * * * ?` | 超时订单自动取消 | 每分钟，取消 >15 分钟未支付订单 |
| `0 0 1 * * ?` | 派送订单自动完成 | 凌晨 1 点，完成 >1 小时派送中订单 |

### 订单状态流转
```
待支付(1) ──支付成功──▶ 待接单(2) ──商家接单──▶ 待派送(3) ──商家派送──▶ 派送中(4)
    │                                                                    │
    │ 超时15min/用户取消                                        凌晨1:00 自动完成
    ▼                                                                    ▼
已取消(6)                                                          已完成(5)
```

---

## AI 集成详解

### 用户端："小速"智能点餐助手（LangChain4j 预执行架构）

**v3.0 架构**：Java 预执行 + AI 纯文本生成，加购 100% 可控。

**流程**：
1. `UserAIIntentService` → 正则检测意图（搜索/推荐/加购/查看购物车/清空/闲聊）
2. Java 预执行 → 直接调用 DishTools / RecommendTools / CartTools
3. `UserAIPromptBuilder` → 构建增强 Prompt（注入真实菜品数据 + [ID:xxx] 标记）
4. LangChain4j `OpenAiStreamingChatModel` → 纯文本生成（不做工具调用）
5. Java 后处理 → 推荐上下文持久化、购物车更新事件

**内置工具类**（纯 Java，不依赖 AI 模型调用）：

| 类别 | 工具 | 说明 |
|------|------|------|
| 时间 | UserAITools | getCurrentDate / getCurrentDateTime / getTimeContext |
| 菜品 | DishTools | searchDishes / getDishDetail / getCategories / getHotDishes / searchByPriceRange / findDishesByNames |
| 推荐 | RecommendTools | getRecommendations / getBudgetFriendlyDishes |
| 购物车 | CartTools | addToCart / removeFromCart / getCart / clearCart / getCartCount |

**关键设计**：加购不经过 AI。意图 `ADD_TO_CART` 时 Java 直接执行 `cartTools.addToCart()`，AI 仅参与推荐时的文案润色。

### 管理端 AI（LangChain4j 1.6.0）

`AIAgent.java` 定义两个 Agent：
1. **StreamQueryAssistant** — 自然语言数据查询，按用户问题逐一回答
2. **DiagnosisAssistant** — 一键经营诊断，自动拉取多维度数据生成报告

`BusinessToolService` 暴露 5 个 `@Tool` 方法：
| 工具 | 说明 |
|------|------|
| `getCurrentDate()` | 时间基准，所有时间查询须先调用 |
| `getTurnover(begin, end)` | 每日营业额 |
| `getOrderStatistics(begin, end)` | 订单统计（总数/有效/各状态） |
| `getSalesTop10(begin, end)` | 菜品销量 Top10 |
| `getUserStatistics(begin, end)` | 新增用户/总用户 |

### AI 会话管理
- Redis key：`ai:session:user:{userId}`
- TTL：2 天
- 最多保留 50 轮对话历史
- `UserSession` 实体封装完整的会话生命周期

---

## 管理端前端架构

### 技术栈
Vue 3.5 (Composition API `<script setup lang="ts">`) + TypeScript 5.6 + Vite 6
Element Plus 2.9 + Tailwind CSS 3.4 + Pinia 2.3 + Vue Router 4.5
Axios 1.7 + ECharts 5.5 + vue-echarts 7.0

### 目录结构
```
src/
├── api/
│   ├── request.ts          # Axios 封装（baseURL + 拦截器 + JWT token 注入）
│   └── modules/            # 12 个 API 模块：ai / auth / category / common / dish
│                           #   employee / message / order / report / setmeal / shop / workspace
├── components/
│   ├── layout/             # AppSidebar / AppNavbar / AppBreadcrumb / AppHamburger
│   │                      #   PasswordDialog / ShopStatusDialog
│   ├── StatCard.vue        # KPI 统计卡片
│   ├── ImageUpload.vue     # 图片上传（MinIO/OSS）
│   └── EmptyState.vue      # 空状态插画
├── composables/            # usePagination / useChart / useTheme
├── layouts/                # DefaultLayout（侧边栏 + 顶栏 + 内容区）
├── router/                 # routes.ts（14 路由）+ guards.ts（导航守卫）
├── stores/                 # app.ts（侧边栏/暗色模式）+ user.ts（用户/Token）
├── styles/                 # 全局样式 + 设计 Token + 动画系统 + 暗色模式
├── utils/                  # auth.ts / cookies.ts / date.ts
└── views/                  # 15 个页面
    ├── ai/index.vue        # AI 智能分析（SSE 流式问答 + 一键诊断）
    ├── login/index.vue     # 管理端登录（Canvas 粒子背景）
    ├── client-login/       # 客户端登录页
    ├── dashboard/          # 工作台（KPI 卡片 + 图表）
    ├── statistics/         # 数据统计（ECharts + Excel 导出）
    ├── order/              # 订单管理（7 状态 Tab）
    ├── dish/               # 菜品管理（CRUD + 图片 + 口味）
    ├── setmeal/            # 套餐管理（CRUD + 菜品关联）
    ├── category/           # 分类管理（CRUD + 排序）
    ├── employee/           # 员工管理（CRUD + 管理员保护）
    ├── message/            # 来单/催单消息通知
    └── error/404.vue       # 404 页面
```

### 路由速查
| 路径 | 页面 | 权限 |
|------|------|------|
| `/login` | 登录页 | 公开 |
| `/client-login` | 客户端登录 | 公开 |
| `/dashboard` | 工作台 | 需认证 |
| `/statistics` | 数据统计 | 需认证 |
| `/order` | 订单管理 | 需认证 |
| `/dish`、`/dish/edit/:id?` | 菜品管理 | 需认证 |
| `/setmeal`、`/setmeal/edit/:id?` | 套餐管理 | 需认证 |
| `/category` | 分类管理 | 需认证 |
| `/employee`、`/employee/edit/:id?` | 员工管理 | 需认证 |
| `/ai` | AI 智能分析 | 需认证 |
| `/message` | 消息通知 | 需认证 |

### Pinia Stores
| Store | 状态 |
|-------|------|
| `app` | `sidebarCollapsed`、`isDarkMode` |
| `user` | `userInfo`、`token`、`roles` |

### 环境变量（`.env`）
| 变量 | 说明 | 默认值 |
|------|------|--------|
| `VITE_API_BASE_URL` | API 前缀 | `/api` |
| `VITE_SERVER_URL` | 后端地址 | `http://localhost:8080/admin` |
| `VITE_WS_URL` | WebSocket 地址 | `ws://localhost:8080/ws/` |

### AI 页面实现
- `api/modules/ai.ts`：原生 `fetch` + `ReadableStream` 读取 SSE 流
- `views/ai/index.vue`：双模式 — 自由问答 + 一键经营诊断

---

## 小程序端架构

### 技术栈
Uni-app (Vue 2) + Vuex + SCSS + rpx 单位
编译为微信小程序，需 HBuilderX + 微信开发者工具

### 页面结构（10 页 + 3 TabBar）
```
pages/
├── index/                  # 首页（Tab 1）：店铺信息 + 分类侧边栏 + 菜品卡片 + 购物车弹窗
├── ai/                     # AI小速（Tab 2）：SSE 流式对话 + 快捷提问 + 菜品推荐卡片 + 加购
├── my/                     # 我的（Tab 3）：用户信息卡 + 地址/订单入口 + 最近订单
├── order/                  # 确认订单 + 下单成功
├── orderDetail/            # 订单详情
├── historyOrder/           # 历史订单（分页滚动加载）
├── address/                # 地址列表（原生导航栏返回）
├── addOrEditAddress/       # 新增/编辑地址（省市区三级联动）
├── nonet/                  # 无网络提示页
└── common/
    ├── Navbar/             # 自定义导航栏组件
    └── simple-address/     # 三级地址选择器（省/市/区数据）
```

### 组件（9 个）
| 组件 | 说明 |
|------|------|
| `dish-card` | 菜品卡片（图片+名称+价格+加购按钮） |
| `chat-bubble` | AI 聊天气泡（支持 Markdown + 菜品推荐卡） |
| `chat-input` | AI 聊天输入框（文本 + 快捷提问） |
| `app-loading` | 全屏加载遮罩 |
| `app-empty` | 空状态插画 |
| `app-skeleton` | 骨架屏加载 |
| `empty` | 通用空状态 |
| `reach-bottom` | 列表触底提示 |
| `uni-icons` / `uni-nav-bar` / `uni-status-bar` | Uni-app 内置组件 |

### 工具模块
| 文件 | 说明 |
|------|------|
| `utils/env.js` | API baseUrl（默认 `http://localhost:8080`） |
| `utils/request.js` | HTTP 请求封装（JWT `authentication` header） |
| `utils/stream.js` | 🆕 SSE 流式请求（`wx.request` + `enableChunked`） |
| `utils/stomp.js` | STOMP 协议封装 |
| `utils/webscoket.js` | WebSocket 客户端 |

### Vuex Store
| 状态 | 类型 | 说明 |
|------|------|------|
| `storeInfo` | Object | 店铺请求参数 |
| `shopInfo` | String | 店铺详细信息 |
| `orderListData` | Array | 购物车列表 |
| `baseUserInfo` | Object | 微信用户信息（昵称、头像） |
| `token` | String | JWT Token |
| `sessionId` | String | AI 会话 ID |
| `cartCount` | Number | 购物车角标数量 |
| `aiMessages` | Array | AI 聊天消息缓存 |
| `aiStreamingTask` | Object | 流式请求 task（用于中断） |

### 设计 Token（rpx）
| Token | 色值 | 用途 |
|-------|------|------|
| 品牌主色 | `#1a56db` | 导航栏、按钮、选中态、TabBar 选中 |
| 品牌深色 | `#1e40af` | 按钮按下态 |
| 品牌浅蓝 | `#eff6ff` / `#dbeafe` | 卡片选中背景、用户信息卡 |
| 强调色 | `#f59e0b` | 价格、标签 |
| 成功绿 | `#10b981` | 订单完成 |
| 页面背景 | `#f3f4f7` | 全局底色、导航栏背景 |
| 卡片圆角 | `16rpx` | 统一圆角 |
| 卡片阴影 | `0 4rpx 24rpx rgba(0,0,0,0.06)` | 默认阴影 |

---

## 统一响应格式
```json
// 成功
{ "code": 1, "msg": "操作成功", "data": { ... } }
// 失败
{ "code": 0, "msg": "错误描述", "data": null }
// 分页
{ "code": 1, "msg": "查询成功", "data": { "total": 100, "records": [ ... ] } }
```

## API 文档
- Knife4j：`http://localhost:8080/doc.html`
- 接口分组：管理端 `/admin/**` · 用户端 `/user/**` · 回调 `/notify/**`

## 安全机制
| 机制 | 实现 |
|------|------|
| 认证 | JWT 双通道，管理端/用户端独立密钥 + 独立 Header |
| 上下文隔离 | `BaseContext` ThreadLocal，线程级用户 ID 隔离 |
| 密码加密 | 员工密码 MD5 摘要 |
| SQL 注入 | MyBatis `#{}` 参数化查询 |
| AI 安全 | System Prompt 限制购物车操作必须用户确认 |

## 常用命令速查
```bash
# 后端
cd backend/suda-take-out && mvn clean install -DskipTests
cd suda-server && mvn spring-boot:run  # → localhost:8080

# 管理端
cd front/project-suda-admin-vue3 && npm install && npm run dev  # → localhost:8888

# 小程序：HBuilderX 打开 front/mp-weixin/project-rjwm-weixin-uniapp → 运行到微信开发者工具
```

## Maven 依赖版本速查
| 依赖 | 版本 |
|------|------|
| Spring Boot | 3.4.3 |
| MyBatis Spring Boot | 3.0.4 |
| Druid | 1.2.23 |
| PageHelper | 2.1.0 |
| Knife4j | 4.5.0 |
| JJWT | 0.12.6 |
| Fastjson2 | 2.0.53 |
| MinIO | 8.5.17 |
| Apache POI | 5.4.0 |
| 微信支付 | 0.4.8 |
| LangChain4j | 1.6.0 |
| Lombok | 1.18.36 |
| AspectJ | 1.9.22 |
