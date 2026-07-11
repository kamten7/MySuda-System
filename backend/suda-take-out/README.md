# 速达外卖 (Suda Take-Out)

> **基于 Spring Boot 3 + Vue 3 + 微信小程序的全链路 AI 外卖平台系统**

一个完整的外卖业务全链路平台，涵盖商家后台管理端（Web SPA）、用户小程序端与后端 RESTful API 服务，支持从菜品浏览、购物车、地址管理、微信支付下单到商家接单、订单流转、数据统计与报表导出的闭环业务流程。**集成 Spring AI + DeepSeek 大模型，提供 AI 智能点餐助手"小速"和管理端 AI 运营诊断。**

---

## 项目简介

速达外卖采用 **前后端分离 + 多端协同** 架构，包含三端：

| 端 | 技术栈 | 目录 | 说明 |
|---|--------|------|------|
| **后端服务** | Spring Boot 3 + MyBatis + MySQL + Redis + Spring AI | `backend/suda-take-out/` | RESTful API 服务，JWT 双通道认证，AI 集成 |
| **商家管理端** | Vue 3 + TypeScript + Element Plus + Tailwind | `front/project-suda-admin-vue3/` | SPA 后台管理系统，含 AI 运营诊断 |
| **用户小程序端** | Uni-app (微信小程序) | `front/mp-weixin/project-rjwm-weixin-uniapp/` | C 端用户点餐小程序，含 AI 智能点餐 |

核心技术挑战包括：JWT 双通道认证体系设计、基于 ThreadLocal 的用户上下文隔离、AOP 自动字段填充减少样板代码、WebSocket 实时推送实现来单提醒与催单通知、Spring Cache + Redis 缓存策略优化高并发菜品查询、百度地图 API 配送范围校验、微信支付 V3 API 集成及回调处理、定时任务处理超时订单取消与派送状态流转、Apache POI 生成运营数据 Excel 报表并支持前端 Blob 下载、**Spring AI + DeepSeek 16 个 FunctionCallback 工具实现 AI 智能点餐、LangChain4j 管理端 AI 运营诊断、小程序 SSE 流式对话通信。**

---

## 项目架构

```
suda-take-out/
├── suda-common/         # 公共模块 — 工具类、常量、异常定义、统一返回结果
├── suda-pojo/           # 实体模块 — 数据库实体、DTO、VO
└── suda-server/         # 服务模块 — Controller、Service、Mapper、配置、拦截器、AOP、WebSocket、AI
    ├── com.suda
    │   ├── AI/                  # 🆕 AI 集成层
    │   │   ├── config/          #     SpringAIConfig（ChatClient + 16个FunctionCallback）
    │   │   └── tools/           #     AI工具：UserAITools/DishTools/RecommendTools/CartTools
    │   ├── annotation/      # 自定义注解 (@AutoFill)
    │   ├── aspect/          # AOP 切面 (公共字段自动填充)
    │   ├── config/          # 配置类 (WebMvc、OSS、Redis、WebSocket)
    │   ├── constant/        # 常量定义
    │   ├── context/         # ThreadLocal 用户上下文
    │   ├── controller/      # 控制器层 (admin/、user/、notify/)
    │   │   ├── admin/       #   管理端接口：员工/分类/菜品/套餐/订单/统计/AI
    │   │   ├── user/        #   用户端接口：登录/地址/菜品/套餐/购物车/订单/AI
    │   │   └── notify/      #   支付回调接口
    │   ├── enumeration/     # 枚举 (操作类型、订单状态)
    │   ├── exception/       # 业务异常体系
    │   ├── handler/         # 全局异常处理器
    │   ├── interceptor/     # JWT 拦截器 (管理端/用户端)
    │   ├── json/            # JSON 序列化配置
    │   ├── mapper/          # MyBatis Mapper 接口
    │   ├── properties/      # 配置属性类
    │   ├── result/          # 统一响应体 Result<T>
    │   ├── service/         # 业务服务层
    │   │   ├── impl/        #   服务实现
    │   │   ├── AIService.java           # 管理端 AI 诊断服务
    │   │   ├── UserAIService.java       # 用户端 AI 点餐服务
    │   │   └── BusinessToolService.java # AI 经营数据分析工具服务
    │   ├── task/            # 定时任务
    │   ├── utils/           # 工具类 (JWT、HttpClient、MinioUtil)
    │   └── websocket/       # WebSocket 服务端
    └── resources/
        ├── mapper/          # MyBatis XML 映射文件
        └── application*.yml # 多环境配置
```

**分层架构说明：**

| 层次 | 职责 | 关键实现 |
|------|------|----------|
| Controller | 接收 HTTP 请求、参数校验 | 按 admin/user/notify 分组；CommonController 处理 OSS/MinIO 文件上传 |
| Interceptor | JWT Token 校验、用户身份注入 | 双通道设计 — admin 路由用 `admin-secret-key`，user 路由用 `user-secret-key` |
| Service | 业务逻辑编排 | 事务管理、缓存注解、第三方 API 调用、AI 集成 |
| Mapper | 数据持久化 | MyBatis XML 写复杂 SQL；PageHelper 自动分页 |
| AOP | 横切关注点 | `@AutoFill` 注解 + AspectJ 自动注入 createTime/updateTime/createUser/updateUser |
| AI | 大模型集成 | Spring AI ChatClient + 16 个 FunctionCallback 工具函数，LangChain4j 经营诊断 |

---

## 技术栈

### 后端技术

| 类别 | 技术 | 版本 | 用途 |
|------|------|------|------|
| 核心框架 | Spring Boot | 3.4.3 | IoC 容器、自动配置、嵌入式 Tomcat |
| JDK | Java | 17 (LTS) | 长期支持版本 |
| 构建工具 | Maven | 3.6+ | 多模块项目依赖管理 |
| 持久层 | MyBatis | 3.0.4 | SQL 与对象映射，XML 编写复杂查询 |
| 分页插件 | PageHelper | 2.1.0 | 透明分页，不侵入业务代码 |
| 连接池 | Druid | 1.2.23 | 高性能数据库连接池，内置 SQL 监控 |
| 数据库 | MySQL | 8.0+ | 关系型数据库 |
| 缓存 | Redis + Spring Cache | — | 套餐/菜品数据缓存，AI 会话存储 |
| 认证授权 | JWT (JJWT) | 0.12.6 | 无状态 Token 认证，管理端/用户端双密钥 |
| API 文档 | Knife4j | 4.5.0 | Swagger / OpenAPI 3 增强，分组展示管理端/用户端接口 |
| 实时通信 | WebSocket (JSR 356) | — | 来单提醒、客户催单实时推送 |
| AOP | AspectJ | 1.9.22 | 自定义注解 + 切面实现公共字段自动填充 |
| 文件存储 | MinIO / 阿里云 OSS | — | 菜品/套餐图片上传与回显 |
| JSON 处理 | Fastjson2 | 2.0.53 | 高性能 JSON 序列化/反序列化 |
| Excel | Apache POI | 5.4.0 | 运营数据报表导出 |
| 微信支付 | wechatpay-apiv3 | 0.4.8 | 微信支付 V3 API，Apache HttpClient 签名 |
| 地图 API | 百度地图 API | — | 地址地理编码 + 路线规划 + 配送范围校验 |
| **AI 集成** | **Spring AI** | **1.0.0-M5** | **用户端 AI 点餐：ChatClient + 16 个 FunctionCallback** |
| **AI 集成** | **LangChain4j** | **1.6.0** | **管理端 AI 运营诊断** |
| **LLM** | **DeepSeek Chat** | **—** | **大语言模型（api.deepseek.com）** |
| 工具类 | Commons Lang3 | 3.17.0 | 常用工具方法 |
| 简化代码 | Lombok | 1.18.36 | 自动生成 getter/setter/构造器 |
| 内网穿透 | Cpolar | — | 开发环境微信回调穿透 |

### 前端技术（商家管理端）

| 类别 | 技术 | 版本 | 用途 |
|------|------|------|------|
| 框架 | Vue 3 (Composition API) | 3.5+ | `<script setup lang="ts">` 风格 |
| 语言 | TypeScript | 5.6 | 类型安全，大型项目可维护性 |
| 构建 | Vite | 6.x | 极速 HMR，ESBuild 预构建 |
| UI 库 | Element Plus | 2.9+ | 企业级桌面端组件库（表格、表单、对话框、消息提示） |
| 状态管理 | Pinia | 2.x | Vue 3 官方推荐状态管理，Token/用户信息持久化 |
| 路由 | Vue Router | 4.x | SPA 路由，懒加载 + 导航守卫 |
| HTTP | Axios | 1.x | 请求/响应拦截、统一错误处理 |
| 图表 | ECharts 5 + vue-echarts | 5.5 / 7.0 | 数据统计可视化（折线图、柱状图、饼图） |
| CSS | Tailwind CSS + SCSS | 3.4 | 原子化 CSS + SCSS 预处理 |
| 进度条 | NProgress | 0.2.0 | 页面加载进度指示 |
| Cookie | js-cookie | 3.0 | Token 及用户信息本地持久化 |
| 代码规范 | ESLint + Prettier + Husky | — | 代码格式化 + Git Hooks |

### 用户小程序端

- **框架**: Uni-app（跨端开发，编译为微信小程序，Vue 2 语法）
- **运行环境**: 微信小程序原生能力（微信登录、微信支付）
- **开发工具**: HBuilderX → 微信开发者工具
- **AI 通信**: SSE 流式（wx.request enableChunked），实时逐字输出
- **目录**: `front/mp-weixin/project-rjwm-weixin-uniapp/`

---

## 核心功能详解

### 商家管理端

#### 1. 员工管理
- 员工 CRUD（新增、编辑、查询、启用/禁用）
- 登录认证：用户名 + 密码 → JWT Token 签发（有效期 2 小时）
- 状态控制：禁用员工无法登录系统
- 技术点：密码采用 MD5 加密存储；JWT 拦截器校验 Token 有效性并注入 ThreadLocal 上下文

#### 2. 分类管理
- 菜品分类 / 套餐分类的增删改查
- 分类排序控制前端展示顺序
- 类型区分（菜品分类 type=1、套餐分类 type=2）

#### 3. 菜品管理
- 菜品 CRUD + 上下架操作
- 菜品口味管理（多口味如辣度、温度，关联表 `dish_flavor`）
- 图片上传（MinIO / 阿里云 OSS 直传，前端拿到 URL 后随表单提交）
- 菜品与分类关联
- 技术点：文件上传通过 `CommonController` 统一处理，返回访问 URL

#### 4. 套餐管理
- 套餐 CRUD + 上下架操作
- 套餐与菜品多对多关联（`setmeal_dish` 中间表）
- 套餐图片上传
- 技术点：套餐数据使用 Redis 缓存，`@Cacheable` 自动缓存，`@CacheEvict` 在修改时清除

#### 5. 订单管理
- 订单全生命周期：待支付 → 待接单 → 待派送 → 派送中 → 已完成 / 已取消
- 操作能力：接单、拒单、取消、派送、完成
- 订单详情查看（订单基本信息 + 菜品明细）
- 订单搜索（按订单号、手机号、状态、时间范围筛选）
- 技术点：
  - 微信支付成功回调 (`PayNotifyController`) → 更新订单状态 → WebSocket 推送商家
  - 超时未支付订单由定时任务每分钟扫描自动取消

#### 6. 数据统计
- **营业额统计**: 按日期范围汇总已完成订单金额
- **用户统计**: 新增用户数 + 总用户数趋势
- **订单统计**: 订单总数、有效订单数、完成率
- **销量排名 Top 10**: 按菜品/套餐销量降序排列
- **数据概览**: 当日核心指标一览
- 技术点：ECharts 可视化渲染；后端使用 MyBatis 动态 SQL 按日期范围聚合查询

#### 7. Excel 报表导出
- 一键导出运营数据汇总 Excel 文件
- 技术点：后端 Apache POI 动态生成 `.xlsx` 文件 → 前端 Axios blob 接收触发下载

#### 8. 实时通知
- **来单提醒**: 用户下单后 WebSocket 实时推送订单信息到商家端
- **客户催单**: 用户点击催单后 WebSocket 推送催单通知
- 技术点：WebSocket 服务端采用 JSR 356 标准 `@ServerEndpoint`

#### 9. 🤖 AI 运营诊断
- **自然语言数据查询**: "上周营业额是多少？""销量最高的菜品是什么？"
- **一键经营诊断**: 自动分析营业额、用户增长、订单趋势、菜品排名，生成运营建议
- 技术点：LangChain4j + DeepSeek SSE 流式输出，`BusinessToolService` 提供经营数据查询工具

### 用户小程序端

#### 1. 用户登录
- 微信一键授权登录（`wx.login` 获取 code → 后端换取 openid → JWT 签发）
- 新用户首次登录自动注册

#### 2. 地址管理
- 地址 CRUD + 默认地址设置
- 省市区三级联动选择
- 技术点：百度地图地理编码将地址文本转为经纬度坐标

#### 3. 菜品浏览与购物车
- 按分类浏览菜品/套餐
- 购物车添加、修改数量、删除、清空
- 技术点：购物车数据存储在服务端（`shopping_cart` 表），与用户 ID 绑定

#### 4. 下单支付
- 购物车 → 订单确认（选择地址）→ 调用微信支付 → 支付成功回调
- 技术点：
  - 配送范围校验：百度地图路线规划 API 计算商家到用户距离，超过 5km 拒绝下单
  - 微信支付 V3：统一下单 → 生成预支付参数 → 小程序调起支付 → 回调通知验签

#### 5. 订单管理
- 历史订单查询、订单详情
- 再来一单（复制历史订单菜品到购物车）
- 用户催单（WebSocket 推送商家）

#### 6. 🤖 AI 智能点餐助手"小速"
- **SSE 流式对话**: 逐字输出，模仿主流大模型聊天体验
- **智能推荐**: 场景感知（天热/天冷/减肥/聚会），预算友好推荐
- **购物车操作**: AI 推荐菜品，用户确认后一键加购
- **时间感知**: 自动识别早/午/晚餐时段，推荐对应餐次
- 技术点：Spring AI ChatClient + 16 个 FunctionCallback + 小程序 `enableChunked` SSE

---

## 数据库设计

### 核心表结构关系

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

### 关键表设计说明

| 表名 | 关键字段 | 说明 |
|------|----------|------|
| `employee` | id, name, username, password, phone, sex, id_number, status | 管理端员工，password 存 MD5 |
| `user` | id, openid, name, phone, sex, id_number, avatar | C端用户，openid 来自微信 |
| `category` | id, name, sort, type, status | type: 1菜品分类/2套餐分类 |
| `dish` | id, name, category_id, price, image, description, status | 菜品主表 |
| `dish_flavor` | id, dish_id, name, value | 口味如"辣度/微辣"、"温度/热" |
| `setmeal` | id, name, category_id, price, image, description, status | 套餐主表 |
| `setmeal_dish` | id, setmeal_id, dish_id, name, price, copies | 套餐包含菜品及份数 |
| `address_book` | id, user_id, consignee, phone, detail, label, is_default | 收货地址 |
| `shopping_cart` | id, user_id, dish_id/setmeal_id, dish_flavor, number, amount | 购物车（服务端存储） |
| `orders` | id, number, user_id, status, amount, address_book_id, order_time | 订单主表 |
| `order_detail` | id, order_id, dish_id/setmeal_id, name, number, amount | 订单菜品明细 |

---

## 安全机制

### JWT 双通道认证

- **管理端**: Token 名为 `token`，密钥 `admin-secret-key`，拦截 `/admin/**`（排除 `/admin/employee/login`）
- **用户端**: Token 名为 `authentication`，密钥 `user-secret-key`，拦截 `/user/**`（排除 `/user/user/login`、`/user/shop/status`）
- **上下文传递**: 拦截器解析 JWT 后将用户 ID 存入 `BaseContext`（ThreadLocal），Service 层通过 `BaseContext.getCurrentId()` 获取当前操作人

### 全局异常处理

- `@RestControllerAdvice` + `@ExceptionHandler` 统一捕获异常
- **业务异常体系**: `BaseException` 为基类，派发出多种业务异常子类
- **SQL 异常**: 自动解析唯一约束冲突消息，提取字段名返回友好提示
- 统一响应体 `Result<T>` 封装：`{ code: 1, msg: "...", data: {...} }`

### 前端路由守卫

- `router.beforeEach` 检查 Cookie 中是否存在 Token
- 未认证访问非公开页面 → 重定向到 `/login`

---

## 缓存策略

### 双层缓存策略

#### 1. Spring Cache 注解方式（套餐数据）

```java
@Cacheable(cacheNames = "setmealCache", key = "#categoryId")
public List<Setmeal> list(Long categoryId) { ... }

@CacheEvict(cacheNames = "setmealCache", key = "#categoryId")
public void delete(Long id) { ... }
```

#### 2. 手动 RedisTemplate 方式（菜品数据）

```java
// 查询时读取缓存，未命中则查库并写入
String key = "dish_" + categoryId;
// 增删改时通配符批量清除
cleanCache("dish_*");
```

#### 3. 店铺营业状态缓存

- Redis Key: `SHOP_STATUS`，1=营业中 / 0=休息中

---

## 实时通信 — WebSocket

- **服务端**: JSR 356 标准注解 `@ServerEndpoint("/ws/{sid}")`
- **会话管理**: 静态 `ConcurrentHashMap<String, Session>` 存储在线客户端
- **应用场景**: 来单提醒、客户催单实时推送

---

## 支付流程

### 微信支付 V3 集成

```
用户端小程序                Suda-Take-Out                微信支付服务
    │                          │                              │
    │  1. 提交订单              │                              │
    │─────────────────────────►│                              │
    │                          │  2. 统一下单 (JSAPI)          │
    │                          │─────────────────────────────►│
    │                          │  3. 返回 prepay_id            │
    │                          │◄─────────────────────────────│
    │  4. 返回支付参数           │                              │
    │◄─────────────────────────│                              │
    │                          │                              │
    │  5. 调起微信支付           │                              │
    │─────────────────────────────────────────────────────────►│
    │                          │                              │
    │                          │  6. 支付成功回调 (POST)        │
    │                          │◄─────────────────────────────│
    │                          │  7. 验签 + 更新订单状态        │
    │                          │  8. WebSocket 推送商家         │
```

---

## AOP 公共字段自动填充

1. 自定义注解 `@AutoFill(OperationType)` 标记 Mapper 方法
2. 切面类拦截所有 `com.suda.mapper.*.*` 且带 `@AutoFill` 的方法
3. 通过反射调用实体对象的 setter 方法自动注入字段值
4. `INSERT` → 填充 createTime/createUser/updateTime/updateUser
5. `UPDATE` → 仅填充 updateTime/updateUser

---

## 定时任务

| Cron 表达式 | 任务 | 逻辑 |
|-------------|------|------|
| `0 * * * * ?` | 处理超时订单 | 每分钟扫描 `status=1 AND order_time < 当前-15分钟` → 自动取消 |
| `0 0 1 * * ?` | 处理派送中订单 | 每天凌晨 1 点扫描 `status=4 AND order_time < 当前-1小时` → 自动完成 |

---

## 🤖 AI 集成详解

### 用户端："小速"智能点餐助手

用户在小程序 **AI小速** Tab 中与 AI 对话，实现自然语言点餐。

**AI 工具集（16 个 FunctionCallback）：**

| 类别 | 工具 | 说明 |
|------|------|------|
| 时间 | `getCurrentDate` / `getCurrentDateTime` / `getTimeContext` | 时间感知，识别早午晚晚餐时段 |
| 菜品 | `searchDishes` / `getDishDetail` / `getCategories` / `getHotDishes` / `searchByPriceRange` | 菜品搜索、详情、分类、热门、价格区间 |
| 推荐 | `getRecommendations` / `getBudgetFriendlyDishes` | 场景感知推荐、预算友好推荐 |
| 购物车 | `addToCart` / `removeFromCart` / `getCart` / `clearCart` / `getCartCount` | 购物车增删查清、数量统计 |

**安全机制**: System Prompt 强制"推荐→询问→确认→操作"流程，AI 不能自主操作购物车。

**会话管理**: Redis key `ai:session:user:{userId}`，TTL 2 天，最多 50 轮对话历史。

**API 端点：**

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/user/ai/chat` | 同步 AI 对话 |
| POST | `/user/ai/chat/stream` | SSE 流式 AI 对话（120s 超时） |
| GET | `/user/ai/cart/status` | 获取购物车状态 |
| DELETE | `/user/ai/history/{sessionId}` | 清空对话历史 |

### 管理端：AI 运营诊断

管理端 AI 页面支持自然语言数据查询和一键经营诊断报告。

**API 端点：**

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/admin/ai/query/stream` | 自然语言数据查询（SSE 流式） |
| POST | `/admin/ai/diagnose` | 一键经营诊断报告（SSE 流式） |

---

## 环境搭建

### 环境要求

| 组件 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | Java 运行环境 |
| Maven | 3.6+ | 后端构建 |
| MySQL | 8.0+ | 数据库 |
| Redis | 5.0+ | 缓存（**端口 6380**，密码 123456，db 1） |
| Node.js | 18+ | 前端构建 |
| HBuilderX | 最新版 | 小程序开发 IDE |
| 微信开发者工具 | 最新版 | 小程序预览/调试 |
| MinIO / 阿里云 OSS | — | 图片存储 |
| Cpolar | 最新版 | 微信支付回调内网穿透（开发环境） |

### 快速启动

#### 第一步：导入数据库

```bash
mysql -u root -p < sql/sky_take_out.sql
```

#### 第二步：配置环境变量

```bash
# 后端配置
cp backend/suda-take-out/suda-server/src/main/resources/application-dev.yml.example \
   backend/suda-take-out/suda-server/src/main/resources/application-dev.yml

# 管理端配置
cp front/project-suda-admin-vue3/.env.example \
   front/project-suda-admin-vue3/.env
```

编辑 `application-dev.yml` 填入数据库、Redis、OSS/MinIO、微信支付、DeepSeek API 等配置。

#### 第三步：启动后端

```bash
cd backend/suda-take-out
mvn clean install -DskipTests
cd suda-server
mvn spring-boot:run
```

启动后：
- **API 服务**: http://localhost:8080
- **API 文档 (Knife4j)**: http://localhost:8080/doc.html

#### 第四步：启动商家管理端

```bash
cd front/project-suda-admin-vue3
npm install && npm run dev
```

浏览器访问 **http://localhost:8888**

#### 第五步：启动小程序端

1. 用 **HBuilderX** 打开 `front/mp-weixin/project-rjwm-weixin-uniapp`
2. 运行 → 运行到小程序模拟器 → 微信开发者工具
3. 微信开发者工具勾选 "不校验合法域名"
4. 确保后端 `localhost:8080` 已启动

#### 第六步：Cpolar 内网穿透（开发环境微信支付回调）

```bash
cpolar http 8080
```

### 启动总结

| 端 | 目录 | 启动命令 | 默认地址 |
|---|------|---------|---------|
| 后端 API | `backend/suda-take-out/` | `mvn clean install -DskipTests && cd suda-server && mvn spring-boot:run` | `http://localhost:8080` |
| 商家管理端 | `front/project-suda-admin-vue3/` | `npm install && npm run dev` | `http://localhost:8888` |
| 用户小程序端 | `front/mp-weixin/project-rjwm-weixin-uniapp/` | HBuilderX → 运行到微信开发者工具 | 工具内预览 |
| API 文档 | — | 后端启动后访问 | `http://localhost:8080/doc.html` |

---

## API 接口文档

启动后端后访问 Knife4j 在线文档：`http://localhost:8080/doc.html`

接口分为三组：
- **管理端接口** (`/admin/*`): 员工管理、分类管理、菜品管理、套餐管理、订单管理、数据统计、报表导出、**AI 运营诊断**
- **用户端接口** (`/user/*`): 用户登录、地址管理、菜品浏览、购物车、订单、**AI 智能点餐**
- **回调接口** (`/notify/*`): 微信支付回调

---

## 项目目录

```
SUDAWAIMAI/
├── backend/
│   └── suda-take-out/            # 后端 Maven 多模块工程
│       ├── suda-common/          # 公共模块
│       ├── suda-pojo/            # 实体模块
│       └── suda-server/          # 服务启动模块（含 AI 集成）
├── front/
│   ├── project-suda-admin-vue3/  # 商家管理端 (Vue 3 SPA)
│   └── mp-weixin/
│       └── project-rjwm-weixin-uniapp/  # 用户小程序端 (Uni-app)
└── sql/                          # 数据库初始化脚本
```

- 后端项目: [suda-take-out](./backend/suda-take-out/)
- 前端管理端: [project-suda-admin-vue3](./front/project-suda-admin-vue3/)
- 小程序端: [project-rjwm-weixin-uniapp](./front/mp-weixin/project-rjwm-weixin-uniapp/) — HBuilderX 导入
