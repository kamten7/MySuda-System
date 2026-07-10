
<h1 align="center">
  <br>
  <img src="https://img.shields.io/badge/Spring_Boot-3.4.3-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Vue-3.5-4FC08D?logo=vuedotjs&logoColor=white" alt="Vue 3">
  <img src="https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white" alt="Java 17">
  <img src="https://img.shields.io/badge/TypeScript-5.6-3178C6?logo=typescript&logoColor=white" alt="TypeScript">
  <img src="https://img.shields.io/badge/license-MIT-blue.svg" alt="License">
  <br><br>
  速达外卖 · Suda Take-Out
</h1>

<p align="center">
  <strong>Spring Boot 3 + Vue 3 + Uni-app 微信小程序 — 全链路 AI 外卖平台</strong>
</p>

<p align="center">
  <a href="#-项目简介">项目简介</a> ·
  <a href="#-系统架构">系统架构</a> ·
  <a href="#-技术栈">技术栈</a> ·
  <a href="#-快速开始">快速开始</a> ·
  <a href="#-核心功能">核心功能</a> ·
  <a href="#-ai-智能助手">AI 智能助手</a> ·
  <a href="#-项目结构">项目结构</a> ·
  <a href="#-架构决策">架构决策</a> ·
  <a href="#-设计系统">设计系统</a> ·
  <a href="#-API-文档">API 文档</a>
</p>

---

## 📖 项目简介

**速达外卖 (Suda Take-Out)** 是一套**生产级**外卖业务全链路平台，采用**前后端分离 + 多端协同**架构，覆盖从商家后台管理、AI 智能点餐、用户小程序下单到订单配送完成的完整业务流程。

> 💡 本项目适用于 Spring Boot 全栈项目学习、毕业设计、课程设计等场景。

### 🎯 三端协同

<table>
<tr>
  <td width="50%">

  **🖥️ 商家管理端** `front/project-suda-admin-vue3/`

  Vue 3 + Element Plus 现代化后台管理系统，支持员工管理、菜品/套餐 CRUD、订单流转、数据统计、**AI 运营诊断**与报表导出。

  </td>
  <td width="50%">

  **📱 用户小程序端** `front/mp-weixin/project-rjwm-weixin-uniapp/`

  Uni-app 跨端微信小程序，支持微信授权登录、菜品浏览、**AI 智能点餐助手"小速"**、购物车、地址管理、在线支付、订单追踪与催单。

  </td>
</tr>
<tr>
  <td colspan="2" align="center">

  **⚙️ 后端 API 服务** `backend/suda-take-out/`

  Spring Boot 3 + MyBatis + MySQL + Redis，RESTful API，JWT 双通道认证，**Spring AI + DeepSeek 大模型集成**，WebSocket 实时推送。

  </td>
</tr>
</table>

---

## 🏗 系统架构

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                              客户端层 (Client)                                     │
├──────────────────────────┬───────────────────────────────────────────────────────┤
│   商家管理端 (Web SPA)     │        用户小程序端 (WeChat Mini Program)                 │
│   Vue 3 + Element Plus   │        Uni-app (Vue 2) + 微信原生能力                    │
│   localhost:8888          │        HBuilderX → 微信开发者工具预览                      │
│   🆕 AI运营诊断            │        🆕 AI点餐助手"小速" (SSE流式对话)                   │
└────────────┬─────────────┴────────────────────┬──────────────────────────────────┘
             │                                  │
             │  Header: token                   │  Header: authentication
             │  (admin-secret-key)              │  (user-secret-key)
             │                                  │
┌────────────┴──────────────────────────────────┴──────────────────────────────────┐
│                           网关 / 拦截器层 (Gateway)                                 │
├───────────────────────────────────────────────────────────────────────────────────┤
│  JwtTokenAdminInterceptor         JwtTokenUserInterceptor                         │
│  路径: /admin/**                  路径: /user/**                                   │
│  密钥: admin-secret-key           密钥: user-secret-key                            │
│  注入: BaseContext ThreadLocal    注入: BaseContext ThreadLocal                    │
└────────────────────────────┬──────────────────────────────────────────────────────┘
                             │
┌────────────────────────────┴──────────────────────────────────────────────────────┐
│                          业务服务层 (Business)                                      │
├────────────────────────────────────────────────────────────────────────────────────┤
│  Controller ──▶ Service ──▶ Mapper ──▶ MySQL                                      │
│      │               │            │                                                │
│      │               │            └──▶ PageHelper 分页                             │
│      │               │                                                             │
│      │               ├──▶ Redis (Spring Cache + RedisTemplate + AI会话存储)        │
│      │               ├──▶ 阿里云 OSS (图片存储)                                     │
│      │               ├──▶ 微信支付 V3 API                                          │
│      │               ├──▶ Spring AI + DeepSeek (AI智能点餐)                        │
│      │               ├──▶ LangChain4j + DeepSeek (AI运营诊断)                      │
│      │               └──▶ WebSocket (来单/催单实时推送)                             │
│      │                                                                             │
│      └──▶ AOP 切面 (@AutoFill 自动填充 createTime/updateTime 等)                    │
└────────────────────────────────────────────────────────────────────────────────────┘
                             │
┌────────────────────────────┴──────────────────────────────────────────────────────┐
│                          基础设施层 (Infrastructure)                                │
├────────────────────────────────────────────────────────────────────────────────────┤
│  MySQL 8.0+    Redis 5.0+(port 6380)    阿里云 OSS    微信支付    DeepSeek API     │
└────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 🛠 技术栈

### 后端

| 类别 | 技术 | 版本 | 说明 |
|------|------|------|------|
| **核心框架** | Spring Boot | 3.4.3 | IoC 容器、自动配置、嵌入式 Tomcat |
| **JDK** | Java | 17 (LTS) | 长期支持版本 |
| **构建工具** | Maven | 3.6+ | 多模块项目依赖管理 |
| **ORM** | MyBatis + PageHelper | 3.0.4 / 2.1.0 | SQL 映射 + 透明分页 |
| **连接池** | Druid | 1.2.23 | 高性能数据库连接池 + SQL 监控 |
| **数据库** | MySQL | 8.0+ | 关系型数据库 |
| **缓存** | Redis + Spring Cache | — | 双层缓存策略（端口 6380，密码 123456，db 1） |
| **认证** | JWT (JJWT) | 0.12.6 | 双通道无状态 Token |
| **API 文档** | Knife4j | 4.5.0 | Swagger 增强，分组展示 |
| **实时通信** | WebSocket (JSR 356) | — | 来单/催单实时推送 |
| **AOP** | AspectJ | 1.9.22 | 公共字段自动填充 |
| **文件存储** | 阿里云 OSS | 3.10.2 | 图片上传与回显 |
| **JSON** | Fastjson2 | 2.0.53 | 高性能序列化 |
| **Excel** | Apache POI | 5.4.0 | 运营报表导出 |
| **微信支付** | wechatpay-apiv3 | 0.4.8 | 微信支付 V3 API |
| **AI 集成** | Spring AI | 1.0.0-M5 | 用户端 AI 点餐：16 个 FunctionCallback 工具 |
| **AI 集成** | LangChain4j | 1.6.0 | 管理端 AI 运营诊断 |
| **LLM** | DeepSeek Chat | — | 大语言模型（api.deepseek.com） |
| **工具库** | Lombok / Commons Lang3 | 1.18.36 / 3.17.0 | 简化开发 |

### 管理端前端

| 类别 | 技术 | 版本 | 说明 |
|------|------|------|------|
| **框架** | Vue 3 (Composition API) | 3.5+ | `<script setup lang="ts">` 风格 |
| **构建工具** | Vite | 6.x | 极速 HMR |
| **语言** | TypeScript | 5.6 | 类型安全 |
| **UI 组件库** | Element Plus | 2.9+ | 企业级桌面端组件 |
| **CSS 框架** | Tailwind CSS | 3.4 | 实用优先的原子化 CSS |
| **状态管理** | Pinia | 2.x | Vue 3 官方推荐 |
| **路由** | Vue Router | 4.x | SPA 路由 + 导航守卫 |
| **HTTP 客户端** | Axios | 1.x | 请求/响应拦截器 |
| **图表** | ECharts 5 + vue-echarts | 5.5 / 7.0 | 数据可视化 |
| **工具库** | dayjs / js-cookie / NProgress | — | 日期 / Cookie / 进度条 |

### 用户小程序端

| 类别 | 技术 | 说明 |
|------|------|------|
| **框架** | Uni-app (Vue 2) + Vuex | 跨端开发，编译为微信小程序 |
| **开发工具** | HBuilderX | IDE，编译运行到微信开发者工具 |
| **导航** | TabBar（3 Tab：首页/AI小速/我的） | 重构后的新导航结构 |
| **样式** | SCSS + rpx 单位 | 蓝色系设计系统，卡片化 UI |
| **AI 通信** | SSE 流式（wx.request enableChunked） | 实时流式 AI 对话 |
| **公共组件** | loading / empty / skeleton / dish-card / chat-bubble / chat-input | 6 个可复用组件 |

---

## 🚀 快速开始

### 环境要求

| 组件 | 最低版本 | 说明 |
|------|---------|------|
| JDK | 17+ | 后端运行环境 |
| Maven | 3.6+ | 后端构建工具 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 5.0+ | 缓存服务（**端口 6380**，密码 123456） |
| Node.js | 18+ | 管理端前端构建 |
| HBuilderX | 最新版 | 小程序开发 IDE（[下载](https://www.dcloud.io/hbuilderx.html)） |
| 微信开发者工具 | 最新稳定版 | 小程序预览与调试 |

### 1️⃣ 克隆项目

```bash
git clone <your-repo-url>
cd SUDAWAIMAI
```

### 2️⃣ 导入数据库

```bash
mysql -u root -p < sql/sky_take_out.sql
```

### 3️⃣ 配置环境变量

```bash
# 后端配置
cp backend/suda-take-out/suda-server/src/main/resources/application-dev.yml.example \
   backend/suda-take-out/suda-server/src/main/resources/application-dev.yml

# 管理端配置
cp front/project-suda-admin-vue3/.env.example \
   front/project-suda-admin-vue3/.env
```

### 4️⃣ 启动后端

```bash
cd backend/suda-take-out
mvn clean install -DskipTests
cd suda-server
mvn spring-boot:run
```

启动后：
- **API 服务**: http://localhost:8080
- **API 文档 (Knife4j)** : http://localhost:8080/doc.html

### 5️⃣ 启动管理端前端

```bash
cd front/project-suda-admin-vue3
npm install && npm run dev
```

浏览器访问 **http://localhost:8888**

### 6️⃣ 启动小程序端

1. 用 **HBuilderX** 打开 `front/mp-weixin/project-rjwm-weixin-uniapp`
2. 运行 → 运行到小程序模拟器 → 微信开发者工具
3. 微信开发者工具勾选 "不校验合法域名"
4. 确保后端 `localhost:8080` 已启动

### 📋 启动总结

| 端 | 目录 | 启动命令 | 默认地址 |
|---|------|---------|---------|
| **后端** | `backend/suda-take-out/` | `mvn clean install -DskipTests && cd suda-server && mvn spring-boot:run` | `http://localhost:8080` |
| **管理端** | `front/project-suda-admin-vue3/` | `npm install && npm run dev` | `http://localhost:8888` |
| **小程序** | `front/mp-weixin/project-rjwm-weixin-uniapp/` | HBuilderX → 运行到微信开发者工具 | 工具内预览 |
| **API 文档** | — | 后端启动后访问 | `http://localhost:8080/doc.html` |

---

## ✨ 核心功能

### 商家管理端

| 模块 | 功能 | 技术亮点 |
|------|------|---------|
| **🔐 员工管理** | CRUD + 启用/禁用 + 登录认证 | JWT Token 签发（2h 有效期） |
| **📂 分类管理** | 菜品分类 / 套餐分类 CRUD | 类型区分（type=1/2），排序控制 |
| **🍜 菜品管理** | CRUD + 上下架 + 口味管理 | OSS 图片上传，口味关联表 |
| **📦 套餐管理** | CRUD + 上下架 + 菜品关联 | 多对多中间表，Redis 缓存 |
| **📋 订单管理** | 全生命周期流转 + 搜索/筛选 | 微信支付回调 → WebSocket 推送 |
| **📊 数据统计** | 营业额/用户/订单/销量 Top10 | ECharts 可视化 |
| **📥 报表导出** | 一键导出运营数据 Excel | Apache POI |
| **🔔 实时通知** | 来单提醒 + 催单通知 | WebSocket JSR 356 |
| **🌓 主题切换** | 亮色 / 暗色模式 | `html.dark` CSS 全覆盖 |
| **🤖 AI 运营诊断** | 🆕 自然语言数据查询 + 一键诊断报告 | LangChain4j + DeepSeek SSE 流式 |

### 用户小程序端

| 模块 | 功能 | 技术亮点 |
|------|------|---------|
| **🔑 微信登录** | 一键授权登录 | `wx.login` → openid → JWT |
| **📋 菜品浏览** | 分类侧边栏 + 菜品卡片 + 口味选择 | 🆕 蓝色系卡片化设计，骨架屏加载 |
| **🤖 AI 点餐助手"小速"** | 🆕 SSE 流式对话 + 智能推荐 + 一键加购 | Spring AI + DeepSeek + 16 个工具函数 |
| **🛒 购物车** | 添加/修改/删除/清空 | 底部弹窗，动画过渡 |
| **📍 地址管理** | CRUD + 默认地址 + 省市区联动 | 三级联动选择器 |
| **💳 在线支付** | 微信支付 V3 | JSAPI 支付 + 回调验签 |
| **📦 订单追踪** | 历史订单 / 详情 / 再来一单 | 分页加载 + 彩色状态标签 |
| **🎨 设计系统** | 🆕 TabBar 导航 + 卡片UI + 弹窗动画 | 与管理端统一的蓝色品牌色系 |

### 订单状态流转

```
待支付 (1) ──支付成功──▶ 待接单 (2) ──商家接单──▶ 待派送 (3)
    │                                                        │
    │ 超时 15min / 用户取消                                    │ 商家派送
    ▼                                                        ▼
已取消 (6)                                            派送中 (4)
                                                           │
                                                    凌晨 1:00 自动完成
                                                           ▼
                                                      已完成 (5)
```

---

## 🤖 AI 智能助手

速达外卖集成了两套 AI 系统，分别服务商家和用户。

### 用户端："小速"智能点餐助手

用户在小程序的 **AI小速** Tab 中与 AI 对话，实现自然语言点餐。

**支持的能力：**
- 🔍 菜品搜索（按菜名、食材、口味、价格）
- 🎯 智能推荐（场景感知：天热/天冷/减肥/聚会）
- 🛒 购物车操作（用户确认后执行）
- 🕐 时间感知（自动识别早/午/晚餐时段）
- 💬 流式对话（SSE 逐字输出）

**AI 工具集（16 个 FunctionCallback）：**

| 类别 | 工具 | 说明 |
|------|------|------|
| 时间 | getCurrentDate / getCurrentDateTime / getTimeContext | 时间感知 |
| 菜品 | searchDishes / getDishDetail / getCategories / getHotDishes / searchByPriceRange | 菜品查询 |
| 推荐 | getRecommendations / getBudgetFriendlyDishes | 智能推荐 |
| 购物车 | addToCart / removeFromCart / getCart / clearCart / getCartCount | 购物车操作 |

**安全机制：** System Prompt 强制"推荐→询问→确认→操作"流程，AI 不能自主操作购物车。

**API 端点：**

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/user/ai/chat` | 同步 AI 对话 |
| POST | `/user/ai/chat/stream` | SSE 流式 AI 对话 |
| GET | `/user/ai/cart/status` | 获取购物车状态 |
| DELETE | `/user/ai/history/{sessionId}` | 清空对话历史 |

### 管理端：AI 运营诊断

管理端 AI 页面支持自然语言数据查询（"上周营业额是多少？"）和一键运营诊断报告。

---

## 📁 项目结构

```
SUDAWAIMAI/
│
├── backend/
│   └── suda-take-out/                    # ☕ 后端 Maven 多模块工程
│       ├── suda-common/                  #   公共模块：工具类、常量、异常、Result
│       ├── suda-pojo/                    #   实体模块：Entity、DTO、VO
│       ├── suda-server/                  #   服务模块：Controller、Service、Mapper
│       │   └── src/main/java/com/suda/
│       │       ├── AI/                   #     🆕 AI 集成层
│       │       │   ├── config/           #       SpringAIConfig（16个FunctionCallback注册）
│       │       │   └── tools/            #       AI工具：UserAITools/DishTools/RecommendTools/CartTools
│       │       ├── annotation/           #     自定义注解 (@AutoFill)
│       │       ├── aspect/               #     AOP 切面（公共字段自动填充）
│       │       ├── config/               #     配置类（MVC、OSS、Redis、WebSocket）
│       │       ├── context/              #     ThreadLocal 用户上下文 (BaseContext)
│       │       ├── controller/           #     控制器层
│       │       │   ├── admin/            #       管理端接口 /admin/**
│       │       │   ├── user/             #       用户端接口 /user/**（含 AI）
│       │       │   └── notify/           #       支付回调接口 /notify/**
│       │       ├── enumeration/          #     枚举（操作类型、订单状态）
│       │       ├── exception/            #     业务异常体系
│       │       ├── handler/              #     全局异常处理器
│       │       ├── interceptor/          #     JWT 拦截器（管理端 + 用户端）
│       │       ├── mapper/               #     MyBatis Mapper 接口
│       │       ├── service/              #     业务服务层
│       │       ├── task/                 #     定时任务（Spring Task）
│       │       └── websocket/            #     WebSocket 服务端
│       └── pom.xml
│
├── front/
│   ├── project-suda-admin-vue3/          # 🖥️ Vue 3 管理端
│   │   └── src/
│   │       ├── api/                      #     Axios 实例 + API 模块（含 AI 模块）
│   │       ├── components/               #     全局组件 (Sidebar/Navbar/StatCard/...)
│   │       ├── composables/              #     组合式函数 (usePagination/useChart/useTheme)
│   │       ├── layouts/                  #     页面布局 (DefaultLayout)
│   │       ├── router/                   #     路由配置 + 导航守卫
│   │       ├── stores/                   #     Pinia 状态管理 (app/user)
│   │       ├── styles/                   #     全局样式（含设计 Token、动画系统）
│   │       ├── utils/                    #     工具函数 (cookies/auth/date)
│   │       └── views/                    #     页面组件（含 AI 智能分析页）
│   │
│   └── mp-weixin/project-rjwm-weixin-uniapp/  # 📱 重构后的 Uni-app 小程序
│           ├── pages/
│           │   ├── api/api.js            #     所有 API 接口（含 AI 相关 4 个）
│           │   ├── index/                #     首页：分类侧边栏 + 菜品卡片列表 + 购物车
│           │   ├── ai/                   #     🆕 AI 对话页：SSE 流式 + 快捷提问 + 菜品推荐卡片
│           │   ├── my/                   #     个人中心 + 最近订单
│           │   ├── order/                #     确认订单 + 下单成功
│           │   ├── historyOrder/         #     历史订单（分页）
│           │   ├── address/              #     地址列表
│           │   ├── addOrEditAddress/     #     新增/编辑地址
│           │   ├── nonet/                #     无网络页
│           │   └── common/
│           │       ├── Navbar/           #     自定义导航栏（渐变蓝背景）
│           │       └── simple-address/   #     三级地址选择器
│           ├── components/
│           │   ├── app-loading/          #     🆕 全屏加载
│           │   ├── app-empty/            #     🆕 空状态
│           │   ├── app-skeleton/         #     🆕 骨架屏
│           │   ├── dish-card/            #     🆕 菜品卡片
│           │   ├── chat-bubble/          #     🆕 AI 聊天气泡
│           │   └── chat-input/           #     🆕 AI 聊天输入
│           ├── utils/
│           │   ├── env.js                #     API baseUrl
│           │   ├── request.js            #     HTTP 请求封装
│           │   └── stream.js             #     🆕 SSE 流式请求封装
│           ├── store/index.js            #     Vuex（含 AI 会话状态）
│           ├── pages.json                #     🆕 路由 + 3 TabBar 配置
│           ├── uni.scss                  #     🆕 蓝色系设计 Token
│           ├── App.vue                   #     🆕 全局动画系统
│           └── manifest.json             #     应用配置
│
├── sql/                                  # 📄 数据库初始化脚本
├── .claude/
│   ├── CLAUDE.md                         #     🤖 AI 上下文文件
│   └── settings.json                     #     项目设置
├── .env.example                          # 🔧 根环境变量模板
└── README.md                             # 📖 本文件
```

> 🆕 = 本次重构新增或大幅修改

---

## 🎨 设计系统

小程序与管理端采用**统一的蓝色品牌色系**，确保三端视觉一致。

### 品牌色彩

| Token | 色值 | 用途 |
|-------|------|------|
| 品牌主色 | `#1a56db` | 导航栏、按钮、选中态 |
| 品牌深色 | `#1e40af` | 按钮按下态 |
| 品牌浅蓝背景 | `#eff6ff` | 卡片选中背景 |
| 强调色 | `#f59e0b` | 价格、标签 |
| 成功绿 | `#10b981` | 订单完成 |
| 页面背景 | `#f3f4f7` | 全局底色 |

### 卡片规范（小程序 rpx）

| 属性 | 值 |
|------|-----|
| 圆角 | `16rpx` |
| 阴影 | `0 4rpx 24rpx rgba(0,0,0,0.06)` |
| 内边距 | `24rpx` |
| 悬浮阴影 | `0 8rpx 32rpx rgba(0,0,0,0.10)` |

### 导航栏渐变

```
linear-gradient(135deg, #1e3a8a 0%, #1a56db 100%)
```

---

## 🎯 架构决策

### 1. JWT 双通道认证

| 维度 | 管理端 | 用户端 |
|------|--------|--------|
| Header | `token` | `authentication` |
| 密钥 | `kamten` | `itheima` |
| 拦截路径 | `/admin/**` | `/user/**` |

### 2. ThreadLocal 用户上下文

拦截器解析 JWT → `BaseContext.setCurrentId()` → Service 层 `BaseContext.getCurrentId()` 获取。

### 3. AI FunctionCallback 注册模式

采用 `List<FunctionCallback>` 自动收集所有 Bean，新增工具只需添加 `@Bean` 方法即可自动生效，**避免逐个注入同类型参数导致的编译参数依赖问题**。

### 4. 双层缓存策略

- 套餐：Spring Cache 注解 `@Cacheable` / `@CacheEvict`
- 菜品：RedisTemplate 手动管理，通配符批量清除 `dish_*`

### 5. AI 会话管理

Redis key `ai:session:user:{userId}`，TTL 2 天，最多 50 轮对话历史，按用户 ID 隔离。

---

## 📡 API 文档

启动后端后访问：`http://localhost:8080/doc.html`

### 接口分组

| 分组 | 路径前缀 | 说明 |
|------|---------|------|
| **管理端** | `/admin/*` | 员工、分类、菜品、套餐、订单、统计、报表、AI |
| **用户端** | `/user/*` | 登录、地址、菜品、套餐、购物车、订单、**AI** |
| **回调** | `/notify/*` | 微信支付回调 |

### 统一响应格式

```json
// 成功
{ "code": 1, "msg": "操作成功", "data": { ... } }

// 失败
{ "code": 0, "msg": "错误描述", "data": null }

// 分页
{ "code": 1, "msg": "查询成功", "data": { "total": 100, "records": [ ... ] } }
```

---

## 🔒 安全机制

| 机制 | 实现方式 |
|------|---------|
| **认证** | JWT 双通道无状态 Token，管理端/用户端独立密钥 |
| **上下文隔离** | ThreadLocal `BaseContext`，线程级用户 ID 隔离 |
| **密码加密** | 员工密码 MD5 摘要存储 |
| **SQL 注入防护** | MyBatis `#{}` 参数化查询 |
| **AI 安全** | System Prompt 限制购物车操作必须用户确认 |

---

## ⚡ 定时任务

| Cron | 任务 | 说明 |
|------|------|------|
| `0 * * * * ?` | 超时订单自动取消 | 每分钟，取消 >15 分钟未支付订单 |
| `0 0 1 * * ?` | 派送订单自动完成 | 凌晨 1 点，完成 >1 小时派送中订单 |

---

## 🗄️ 数据库设计

### 核心 ER 关系

```
employee (员工)
  └── 登录认证

user (C端用户)
  ├── address_book (地址簿, 1:N)
  ├── shopping_cart (购物车, 1:N)
  └── orders (订单, 1:N)
        └── order_detail (订单明细, 1:N)

category (分类)
  ├── dish (菜品, 1:N)
  │     └── dish_flavor (菜品口味, 1:N)
  └── setmeal (套餐, 1:N)
        └── setmeal_dish (套餐菜品关联, N:N)
```

| 表名 | 说明 |
|------|------|
| `employee` | 管理端员工 |
| `user` | C端用户（openid） |
| `category` | 菜品/套餐分类 |
| `dish` / `dish_flavor` | 菜品 + 口味 |
| `setmeal` / `setmeal_dish` | 套餐 + 菜品关联 |
| `address_book` | 收货地址 |
| `shopping_cart` | 购物车（服务端存储） |
| `orders` / `order_detail` | 订单主表 + 明细 |

---

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交变更 (`git commit -m 'feat: add amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request

### Commit 规范

- `feat:` 新功能 | `fix:` 修复 Bug | `refactor:` 重构 | `docs:` 文档
- `style:` 代码格式 | `perf:` 性能优化 | `chore:` 构建/工具变更

---

## 📄 License

MIT

---

<p align="center">
  <sub>Made with ❤️ by Suda Team</sub>
</p>
