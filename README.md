
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
  <strong>Spring Boot 3 + Vue 3 + 微信小程序 — 全链路外卖平台解决方案</strong>
</p>

<p align="center">
  <a href="#-项目简介">项目简介</a> ·
  <a href="#-系统架构">系统架构</a> ·
  <a href="#-技术栈">技术栈</a> ·
  <a href="#-快速开始">快速开始</a> ·
  <a href="#-核心功能">核心功能</a> ·
  <a href="#-项目结构">项目结构</a> ·
  <a href="#-架构决策">架构决策</a> ·
  <a href="#-API-文档">API 文档</a>
</p>

---

## 📖 项目简介

**速达外卖 (Suda Take-Out)** 是一套**生产级**外卖业务全链路平台，采用**前后端分离 + 多端协同**架构，覆盖从商家后台管理、用户小程序点餐到订单配送完成的完整业务流程。

> 💡 本项目适用于 Spring Boot 全栈项目学习、毕业设计、课程设计等场景。

### 🎯 三端协同

<table>
<tr>
  <td width="50%">

  **🖥️ 商家管理端** `front/project-suda-admin-vue3/`

  Vue 3 + Element Plus 现代化后台管理系统，支持员工管理、菜品/套餐 CRUD、订单流转、数据统计与报表导出。

  </td>
  <td width="50%">

  **📱 用户小程序端** `front/mp-weixin/`

  Uni-app 跨端微信小程序，支持微信授权登录、菜品浏览、购物车、地址管理、在线支付、订单追踪与催单。

  </td>
</tr>
<tr>
  <td colspan="2" align="center">

  **⚙️ 后端 API 服务** `backend/suda-take-out/`

  Spring Boot 3 + MyBatis + MySQL + Redis，RESTful API，JWT 双通道认证，WebSocket 实时推送。

  </td>
</tr>
</table>

---

## 🏗 系统架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                          客户端层 (Client)                           │
├──────────────────────────┬──────────────────────────────────────────┤
│   商家管理端 (Web SPA)     │        用户小程序端 (WeChat Mini Program)    │
│   Vue 3 + Element Plus   │        Uni-app + 微信原生能力              │
│   localhost:8888          │        微信开发者工具预览                    │
└────────────┬─────────────┴────────────────────┬─────────────────────┘
             │                                  │
             │  Header: token                   │  Header: authentication
             │  (admin-secret-key)              │  (user-secret-key)
             │                                  │
┌────────────┴──────────────────────────────────┴─────────────────────┐
│                       网关 / 拦截器层 (Gateway)                       │
├──────────────────────────────────────────────────────────────────────┤
│  JwtTokenAdminInterceptor         JwtTokenUserInterceptor            │
│  路径: /admin/**                  路径: /user/**                      │
│  密钥: admin-secret-key           密钥: user-secret-key               │
│  注入: BaseContext ThreadLocal    注入: BaseContext ThreadLocal       │
└────────────────────────────┬─────────────────────────────────────────┘
                             │
┌────────────────────────────┴─────────────────────────────────────────┐
│                       业务服务层 (Business)                            │
├───────────────────────────────────────────────────────────────────────┤
│  Controller ──▶ Service ──▶ Mapper ──▶ MySQL                         │
│      │               │            │                                   │
│      │               │            └──▶ PageHelper 分页                │
│      │               │                                                │
│      │               ├──▶ Redis (Spring Cache + RedisTemplate)        │
│      │               ├──▶ 阿里云 OSS (图片存储)                        │
│      │               ├──▶ 微信支付 V3 API                              │
│      │               ├──▶ 百度地图 API (配送范围校验)                   │
│      │               └──▶ WebSocket (来单/催单实时推送)                │
│      │                                                                │
│      └──▶ AOP 切面 (@AutoFill 自动填充 createTime/updateTime 等)       │
└───────────────────────────────────────────────────────────────────────┘
                             │
┌────────────────────────────┴─────────────────────────────────────────┐
│                       基础设施层 (Infrastructure)                      │
├───────────────────────────────────────────────────────────────────────┤
│  MySQL 8.0+    Redis 5.0+    阿里云 OSS    微信支付    百度地图 API     │
└───────────────────────────────────────────────────────────────────────┘
```

### 请求链路

```
Client → Interceptor (JWT 校验 + ThreadLocal 注入)
       → Controller (参数校验)
       → Service (业务编排 + @Transactional + 缓存管理)
       → Mapper (MyBatis XML + PageHelper 分页)
       → MySQL
```

### 认证流程

```
┌─────────┐     ┌──────────────┐     ┌──────────────┐
│  Client  │────▶│  Interceptor  │────▶│  Controller   │
│ (Token)  │     │  解析 JWT     │     │  BaseContext   │
│          │     │  注入上下文    │     │  .getCurrentId │
└─────────┘     └──────────────┘     └──────────────┘

/admin/**  → JwtTokenAdminInterceptor  → admin-secret-key  → Header: token
/user/**   → JwtTokenUserInterceptor   → user-secret-key   → Header: authentication
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
| **缓存** | Redis + Spring Cache | — | 双层缓存策略 |
| **认证** | JWT (JJWT) | 0.12.6 | 双通道无状态 Token |
| **API 文档** | Knife4j | 4.5.0 | Swagger 增强，分组展示 |
| **实时通信** | WebSocket (JSR 356) | — | 来单/催单实时推送 |
| **AOP** | AspectJ | 1.9.22 | 公共字段自动填充 |
| **文件存储** | 阿里云 OSS | 3.10.2 | 图片上传与回显 |
| **JSON** | Fastjson2 | 2.0.53 | 高性能序列化 |
| **Excel** | Apache POI | 5.4.0 | 运营报表导出 |
| **微信支付** | wechatpay-apiv3 | 0.4.8 | 微信支付 V3 API |
| **AI 集成** | LangChain4j | 1.6.0 | AI Agent 智能问答 |
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
| **代码规范** | ESLint + Prettier + Husky | — | 代码格式化 + Git Hooks |

### 用户小程序端

| 类别 | 技术 | 说明 |
|------|------|------|
| **框架** | Uni-app (Vue 2) | 跨端开发，编译为微信小程序 |
| **运行环境** | 微信小程序 | 微信登录、微信支付等原生能力 |
| **实时通信** | WebSocket | 多人点餐购物车同步 |

---

## 🚀 快速开始

### 环境要求

| 组件 | 最低版本 | 说明 |
|------|---------|------|
| JDK | 17+ | 后端运行环境 |
| Maven | 3.6+ | 后端构建工具 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 5.0+ (Windows: 3.0+) | 缓存服务 |
| Node.js | 18+ | 前端构建工具链 |
| 微信开发者工具 | 最新稳定版 | 小程序预览与调试 |

### 1️⃣ 克隆项目

```bash
git clone <your-repo-url>
cd SUDAWAIMAI
```

### 2️⃣ 导入数据库

```bash
# 执行 SQL 初始化脚本
mysql -u root -p < sql/sky_take_out.sql
```

### 3️⃣ 配置环境变量

项目采用 **模板文件模式** 管理敏感配置：

```bash
# 后端配置（从模板复制并填入真实值）
cp backend/suda-take-out/suda-server/src/main/resources/application-dev.yml.example \
   backend/suda-take-out/suda-server/src/main/resources/application-dev.yml

# 管理端配置
cp front/project-suda-admin-vue3/.env.example \
   front/project-suda-admin-vue3/.env
```

编辑 `application-dev.yml`，填入数据库、Redis、OSS、微信支付等配置。详细配置说明见各子项目 README。

### 4️⃣ 启动后端

```bash
cd backend/suda-take-out

# 编译全部模块（跳过测试）
mvn clean install -DskipTests

# 启动 Spring Boot 应用
cd suda-server
mvn spring-boot:run
```

启动成功后访问：
- **API 服务**: http://localhost:8080
- **API 文档 (Knife4j)** : http://localhost:8080/doc.html

### 5️⃣ 启动管理端前端

```bash
cd front/project-suda-admin-vue3

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

浏览器访问 **http://localhost:8888**，进入商家后台管理登录页。

### 6️⃣ 启动小程序端

1. 打开**微信开发者工具**
2. 导入项目 → 选择 `front/mp-weixin/` 目录
3. 填入小程序 AppID（或使用测试号）
4. 开发者工具中预览和调试

### 📋 启动总结

| 端 | 目录 | 启动命令 | 默认地址 |
|---|------|---------|---------|
| **后端 API** | `backend/suda-take-out/` | `mvn clean install -DskipTests && cd suda-server && mvn spring-boot:run` | `http://localhost:8080` |
| **管理端** | `front/project-suda-admin-vue3/` | `npm install && npm run dev` | `http://localhost:8888` |
| **小程序** | `front/mp-weixin/` | 微信开发者工具导入 | 工具内预览 |
| **API 文档** | — | 后端启动后访问 | `http://localhost:8080/doc.html` |

> 🔧 **内网穿透（开发环境）**：微信支付回调需要外网可达 URL，推荐使用 [Cpolar](https://www.cpolar.com/) 将本地 8080 端口暴露到公网。

---

## ✨ 核心功能

### 商家管理端

| 模块 | 功能 | 技术亮点 |
|------|------|---------|
| **🔐 员工管理** | CRUD + 启用/禁用 + 登录认证 | JWT Token 签发（2h 有效期），状态控制禁止登录 |
| **📂 分类管理** | 菜品分类 / 套餐分类 CRUD | 类型区分（type=1/2），排序控制 |
| **🍜 菜品管理** | CRUD + 上下架 + 口味管理 | OSS 图片上传，口味关联表 `dish_flavor` |
| **📦 套餐管理** | CRUD + 上下架 + 菜品关联 | 多对多中间表 `setmeal_dish`，Redis 缓存 |
| **📋 订单管理** | 全生命周期流转 + 搜索/筛选 | 微信支付回调 → WebSocket 推送，超时自动取消 |
| **📊 数据统计** | 营业额/用户/订单/销量 Top10 | ECharts 可视化，MyBatis 动态 SQL 聚合查询 |
| **📥 报表导出** | 一键导出运营数据 Excel | Apache POI 生成 `.xlsx`，前端 Blob 下载 |
| **🔔 实时通知** | 来单提醒 + 催单通知 | WebSocket JSR 356，ConcurrentHashMap 会话管理 |
| **🌓 主题切换** | 亮色 / 暗色模式 | `html.dark` CSS 全覆盖，Element Plus + Tailwind 联动 |

### 用户小程序端

| 模块 | 功能 | 技术亮点 |
|------|------|---------|
| **🔑 微信登录** | 一键授权登录，新用户自动注册 | `wx.login` → openid → JWT 签发 |
| **📍 地址管理** | CRUD + 默认地址 + 省市区联动 | 百度地图地理编码 → 经纬度坐标 |
| **🛒 购物车** | 添加/修改/删除/清空 | 服务端存储（`shopping_cart` 表），跨设备同步 |
| **💳 在线支付** | 微信支付 V3 统一下单 | JSAPI 支付 + 回调验签 + 配送范围校验（5km） |
| **📦 订单追踪** | 历史订单 / 详情 / 再来一单 / 催单 | WebSocket 推送商家 |

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
│       │       ├── annotation/           #     自定义注解 (@AutoFill)
│       │       ├── aspect/               #     AOP 切面（公共字段自动填充）
│       │       ├── config/               #     配置类（MVC、OSS、Redis、WebSocket）
│       │       ├── constant/             #     常量定义
│       │       ├── context/              #     ThreadLocal 用户上下文 (BaseContext)
│       │       ├── controller/           #     控制器层
│       │       │   ├── admin/            #       管理端接口 /admin/**
│       │       │   ├── user/             #       用户端接口 /user/**
│       │       │   └── notify/           #       支付回调接口 /notify/**
│       │       ├── enumeration/          #     枚举（操作类型、订单状态）
│       │       ├── exception/            #     业务异常体系 (BaseException + 11 子类)
│       │       ├── handler/              #     全局异常处理器
│       │       ├── interceptor/          #     JWT 拦截器（管理端 + 用户端）
│       │       ├── mapper/               #     MyBatis Mapper 接口
│       │       ├── result/               #     统一响应体 Result<T> / PageResult
│       │       ├── service/              #     业务服务层
│       │       ├── task/                 #     定时任务（Spring Task）
│       │       ├── utils/                #     工具类（JWT、HttpClient）
│       │       └── websocket/            #     WebSocket 服务端 (@ServerEndpoint)
│       └── pom.xml                       #   父 POM (Spring Boot 3.4.3)
│
├── front/
│   ├── project-suda-admin-vue3/          # 🖥️ Vue 3 管理端
│   │   └── src/
│   │       ├── api/                      #     Axios 实例 + API 模块 + 类型定义
│   │       ├── components/               #     全局组件 (Sidebar/Navbar/StatCard/ImageUpload)
│   │       ├── composables/              #     组合式函数 (usePagination/useChart/useTheme)
│   │       ├── layouts/                  #     页面布局 (DefaultLayout)
│   │       ├── router/                   #     路由配置 + 导航守卫 (NProgress + Token)
│   │       ├── stores/                   #     Pinia 状态管理 (app/user)
│   │       ├── styles/                   #     全局样式 (Tailwind/Element Plus 覆盖/暗色模式)
│   │       ├── utils/                    #     工具函数 (cookies/auth/date)
│   │       └── views/                    #     页面组件 (login/dashboard/order/dish/...)
│   │
│   └── mp-weixin/                        # 📱 Uni-app 微信小程序
│       └── project-rjwm-weixin-uniapp/
│           ├── pages/                    #     页面目录
│           ├── components/               #     组件目录
│           ├── store/                    #     Vuex 状态管理
│           ├── utils/                    #     工具函数
│           ├── static/                   #     静态资源
│           ├── pages.json                #     页面全局配置
│           └── manifest.json             #     应用配置
│
├── sql/                                  # 📄 数据库初始化脚本
├── .claude/                              # 🤖 Claude Code 配置
│   ├── CLAUDE.md                         #     AI 上下文文件
│   └── settings.json                     #     项目设置
├── .env.example                          # 🔧 根环境变量模板
└── README.md                             # 📖 本文件
```

---

## 🎯 架构决策

以下是项目中的关键架构决策及其设计理由：

### 1. JWT 双通道认证

**决策**: 管理端和用户端使用**独立的 JWT 密钥和 Header 名称**。

| 维度 | 管理端 | 用户端 |
|------|--------|--------|
| Header | `token` | `authentication` |
| 密钥 | `admin-secret-key` | `user-secret-key` |
| 拦截路径 | `/admin/**` | `/user/**` |
| 签发方式 | 用户名+密码登录 | 微信 `wx.login` openid |

**理由**: 两个端的认证来源不同（密码 vs 微信授权），独立密钥可以实现更好的安全隔离，降低密钥泄露的影响范围。

### 2. ThreadLocal 用户上下文

**决策**: 拦截器解析 JWT 后将用户 ID 存入 `BaseContext`（ThreadLocal），Service 层无感获取。

```java
// 拦截器中注入
BaseContext.setCurrentId(empId);

// Service 中获取 — 无需从 Controller 传参
Long currentUserId = BaseContext.getCurrentId();
```

**理由**: 避免在每个 Service 方法签名中传递 `userId` 参数，保持业务代码简洁；ThreadLocal 天然线程安全。

### 3. AOP 自动填充公共字段

**决策**: 自定义 `@AutoFill` 注解 + AspectJ 切面自动注入 `createTime/createUser/updateTime/updateUser`。

**理由**: 四个公共字段分布在几乎所有表中，AOP 方案消除了 Service 层的大量样板代码，且集中管理填充逻辑便于维护。

### 4. 双层缓存策略

**决策**: Spring Cache 注解（套餐）+ 手动 RedisTemplate（菜品）协同工作。

- **套餐**: `@Cacheable(key="#categoryId")` / `@CacheEvict` — 声明式，精确清除
- **菜品**: 手动 `redisTemplate.opsForValue()` — 灵活性更高，通配符批量清除 `dish_*`

**理由**: 套餐数据相对稳定用声明式缓存，菜品涉及多条件查询和口味关联用编程式缓存更灵活。

### 5. WebSocket 实时推送

**决策**: JSR 356 标准 `@ServerEndpoint("/ws/{sid}")` + 静态 `ConcurrentHashMap` 会话管理。

**理由**: Spring Boot 原生 WebSocket 支持，无需引入额外中间件；`ConcurrentHashMap` 保证会话管理的线程安全。

### 6. 统一响应体设计

**决策**: `Result<T> { code: 1 成功 / 0 失败, msg, data }` + `PageResult { total, records }`。

**理由**: 前后端约定统一的响应格式，前端拦截器可统一处理错误提示、Loading 状态和分页数据渲染。

### 7. 全局异常体系

**决策**: `BaseException` 基类派生出 11 个子异常类，`@RestControllerAdvice` 统一捕获处理。

**理由**: 细粒度异常分类让前端能精确展示错误信息，SQL 唯一约束冲突自动解析为友好提示。

---

## 📡 API 文档

启动后端后访问 Knife4j 在线文档：

```
http://localhost:8080/doc.html
```

### 接口分组

| 分组 | 路径前缀 | 说明 |
|------|---------|------|
| **管理端** | `/admin/*` | 员工管理、分类管理、菜品管理、套餐管理、订单管理、数据统计、报表导出 |
| **用户端** | `/user/*` | 用户登录、地址管理、菜品浏览、购物车、订单、店铺状态 |
| **回调** | `/notify/*` | 微信支付成功/退款回调 |

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
| **支付验签** | 微信支付 V3 回调使用平台证书验证签名 |
| **路由守卫** | 前端 `router.beforeEach` 检查 Token，未认证重定向登录页 |
| **异常屏蔽** | 全局异常处理器统一包装，不暴露内部错误细节 |

---

## ⚡ 定时任务

| Cron | 任务 | 说明 |
|------|------|------|
| `0 * * * * ?` | 超时订单自动取消 | 每分钟扫描，取消超过 15 分钟未支付的订单 |
| `0 0 1 * * ?` | 派送订单自动完成 | 每天凌晨 1 点，完成超过 1 小时的派送中订单 |

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

### 关键表说明

| 表名 | 主键 | 核心字段 | 说明 |
|------|------|---------|------|
| `employee` | id | username, password(MD5), status | 管理端员工 |
| `user` | id | openid, name, phone | C端用户 |
| `category` | id | name, type(1菜品/2套餐), sort | 分类 |
| `dish` | id | name, category_id, price, image, status | 菜品 |
| `dish_flavor` | id | dish_id, name, value | 口味 |
| `setmeal` | id | name, category_id, price, image, status | 套餐 |
| `setmeal_dish` | id | setmeal_id, dish_id, copies | 套餐-菜品关联 |
| `address_book` | id | user_id, consignee, phone, detail, is_default | 收货地址 |
| `shopping_cart` | id | user_id, dish_id/setmeal_id, number, amount | 购物车 |
| `orders` | id | number(订单号), user_id, status, amount | 订单主表 |
| `order_detail` | id | order_id, name, number, amount | 订单明细 |

---

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交变更 (`git commit -m 'feat: add amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request

### Commit 规范

本项目推荐使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

- `feat:` 新功能
- `fix:` 修复 Bug
- `refactor:` 重构
- `docs:` 文档更新
- `style:` 代码格式
- `perf:` 性能优化
- `chore:` 构建/工具变更

---

## 📄 子项目文档

| 子项目 | 文档 |
|--------|------|
| 后端 API 服务 | [`backend/suda-take-out/README.md`](backend/suda-take-out/README.md) |
| 管理端前端 | [`front/project-suda-admin-vue3/README.md`](front/project-suda-admin-vue3/README.md) |
| 用户小程序端 | [`front/mp-weixin/README.md`](front/mp-weixin/README.md) |

---

## 📝 License

本项目基于 MIT 协议开源。

---

<p align="center">
  <sub>Made with ❤️ by Suda Team</sub>
</p>
