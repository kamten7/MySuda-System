# 速达外卖 (Suda Take-Out)

> **基于 Spring Boot + Vue.js + 微信小程序的全链路外卖平台系统**

一个完整的外卖业务全链路平台，涵盖商家后台管理端（Web SPA）、用户小程序端与后端 RESTful API 服务，支持从菜品浏览、购物车、地址管理、微信支付下单到商家接单、订单流转、数据统计与报表导出的闭环业务流程。

---

## 项目简介

速达外卖采用 **前后端分离 + 多端协同** 架构，包含三端：

| 端 | 技术栈 | 目录 | 说明 |
|---|--------|------|------|
| **后端服务** | Spring Boot + MyBatis + MySQL + Redis | `backend/suda-take-out/` | RESTful API 服务，JWT 双通道认证 |
| **商家管理端** | Vue 3 + TypeScript + Element Plus + Tailwind | `front/project-suda-admin-vue3/` | SPA 后台管理系统 |
| **用户小程序端** | Uni-app (微信小程序) | `front/mp-weixin/` | C 端用户点餐小程序 |

核心技术挑战包括：JWT 双通道认证体系设计、基于 ThreadLocal 的用户上下文隔离、AOP 自动字段填充减少样板代码、WebSocket 实时推送实现来单提醒与催单通知、Spring Cache + Redis 缓存策略优化高并发菜品查询、百度地图 API 配送范围校验、微信支付 V3 API 集成及回调处理、定时任务处理超时订单取消与派送状态流转、Apache POI 生成运营数据 Excel 报表并支持前端 Blob 下载。

---

## 项目架构

```
suda-take-out/
├── suda-common/         # 公共模块 — 工具类、常量、异常定义、统一返回结果
├── suda-pojo/           # 实体模块 — 数据库实体、DTO、VO
└── suda-server/         # 服务模块 — Controller、Service、Mapper、配置、拦截器、AOP、WebSocket
    ├── com.suda
    │   ├── annotation/      # 自定义注解 (@AutoFill)
    │   ├── aspect/          # AOP 切面 (公共字段自动填充)
    │   ├── config/          # 配置类 (WebMvc、OSS、Redis、WebSocket)
    │   ├── constant/        # 常量定义
    │   ├── context/         # ThreadLocal 用户上下文
    │   ├── controller/      # 控制器层 (admin/、user/、notify/)
    │   ├── enumeration/     # 枚举 (操作类型、订单状态)
    │   ├── exception/       # 业务异常体系
    │   ├── handler/         # 全局异常处理器
    │   ├── interceptor/     # JWT 拦截器 (管理端/用户端)
    │   ├── json/            # JSON 序列化配置
    │   ├── mapper/          # MyBatis Mapper 接口
    │   ├── properties/      # 配置属性类
    │   ├── result/          # 统一响应体 Result<T>
    │   ├── service/         # 业务服务层
    │   ├── task/            # 定时任务
    │   ├── utils/           # 工具类 (JWT、HttpClient)
    │   └── websocket/       # WebSocket 服务端
    └── resources/
        ├── mapper/          # MyBatis XML 映射文件
        └── application*.yml # 多环境配置
```

**分层架构说明：**

| 层次 | 职责 | 关键实现 |
|------|------|----------|
| Controller | 接收 HTTP 请求、参数校验 | 按 admin/user 分组；CommonController 处理 OSS 文件上传 |
| Interceptor | JWT Token 校验、用户身份注入 | 双通道设计 — admin 路由用 `admin-secret-key`，user 路由用 `user-secret-key` |
| Service | 业务逻辑编排 | 事务管理、缓存注解、第三方 API 调用 |
| Mapper | 数据持久化 | MyBatis XML 写复杂 SQL；PageHelper 自动分页 |
| AOP | 横切关注点 | `@AutoFill` 注解 + AspectJ 自动注入 createTime/updateTime/createUser/updateUser |

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
| 缓存 | Redis + Spring Cache | — | 套餐/菜品数据缓存，减少数据库压力 |
| 认证授权 | JWT (JJWT) | 0.12.6 | 无状态 Token 认证，管理端/用户端双密钥 |
| API 文档 | Knife4j | 4.5.0 | Swagger / OpenAPI 3 增强，分组展示管理端/用户端接口 |
| 实时通信 | WebSocket (JSR 356) | — | 来单提醒、客户催单实时推送 |
| AOP | AspectJ | 1.9.22 | 自定义注解 + 切面实现公共字段自动填充 |
| 文件存储 | 阿里云 OSS SDK | 3.10.2 | 菜品/套餐图片上传与回显 |
| JSON 处理 | Fastjson2 | 2.0.53 | 高性能 JSON 序列化/反序列化 |
| Excel | Apache POI | 5.4.0 | 运营数据报表导出 |
| 微信支付 | wechatpay-apiv3 | 0.4.8 | 微信支付 V3 API，Apache HttpClient 签名 |
| 地图 API | 百度地图 API | — | 地址地理编码 + 路线规划 + 配送范围校验 |
| AI 集成 | LangChain4j | 1.6.0 | AI Agent 智能问答 |
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

- **框架**: Uni-app（跨端开发，编译为微信小程序）
- **运行环境**: 微信小程序原生能力（微信登录、微信支付）
- **目录**: `front/mp-weixin/`

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
- 图片上传（阿里云 OSS 直传，前端拿到 URL 后随表单提交）
- 菜品与分类关联
- 技术点：文件上传通过 `CommonController` 统一处理，返回 OSS 访问 URL

#### 4. 套餐管理
- 套餐 CRUD + 上下架操作
- 套餐与菜品多对多关联（`setmeal_dish` 中间表）
- 套餐图片上传
- 技术点：套餐数据使用 Redis 缓存，`@Cacheable` 自动缓存，`@CacheEvict` 在修改时清除，避免缓存穿透

#### 5. 订单管理
- 订单全生命周期：待支付 → 已支付 → 商家已接单 → 派送中 → 已完成
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
- 技术点：
  - 后端：Apache POI 动态生成 `.xlsx` 文件，写入 `HttpServletResponse.outputStream`
  - 前端：Axios `responseType: blob` 接收二进制流，构造 Blob URL 触发浏览器下载

#### 8. 实时通知
- **来单提醒**: 用户下单后 WebSocket 实时推送订单信息到商家端
- **客户催单**: 用户点击催单后 WebSocket 推送催单通知
- 技术点：
  - WebSocket 服务端采用 JSR 356 标准 `@ServerEndpoint`
  - 会话管理使用 `ConcurrentHashMap`，通过 `sid` 标识客户端
  - 前端 WebSocket 连接建立后，监听服务端推送并弹窗/语音提醒

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
- 技术点：购物车数据存储在服务端（`shopping_cart` 表），与用户 ID 绑定，支持跨设备同步

#### 4. 下单支付
- 购物车 → 订单确认（选择地址）→ 调用微信支付 → 支付成功回调
- 技术点：
  - 配送范围校验：百度地图路线规划 API 计算商家到用户距离，超过 5km 拒绝下单
  - 微信支付 V3：统一下单 → 生成预支付参数 → 小程序调起支付 → 回调通知验签
  - 开发环境使用 Cpolar 内网穿透使微信服务器能回调到本地

#### 5. 订单管理
- 历史订单查询、订单详情
- 再来一单（复制历史订单菜品到购物车）
- 用户催单（WebSocket 推送商家）

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
| `orders` | id, number, user_id, status, amount, address_book_id, order_time | 订单主表，status: 1待支付/2已支付/3已接单/4派送中/5已完成/6已取消 |
| `order_detail` | id, order_id, dish_id/setmeal_id, name, number, amount | 订单菜品明细 |

---

## 安全机制

### JWT 双通道认证

```
                     ┌─────────────────────────┐
                     │      HTTP Request        │
                     │  Header: token /         │
                     │          authentication   │
                     └───────────┬─────────────┘
                                 │
                    ┌────────────┴────────────┐
                    │                         │
              /admin/**                   /user/**
                    │                         │
          ┌─────────┴─────────┐     ┌─────────┴─────────┐
          │ JwtTokenAdmin      │     │ JwtTokenUser       │
          │ Interceptor        │     │ Interceptor        │
          │ (admin-secret-key) │     │ (user-secret-key)  │
          └─────────┬─────────┘     └─────────┬─────────┘
                    │                         │
                    └────────────┬────────────┘
                                 │
                     ┌───────────┴───────────┐
                     │   BaseContext          │
                     │   ThreadLocal<Long>    │
                     │   存储当前用户 ID       │
                     └───────────────────────┘
```

- **管理端**: Token 名为 `token`，密钥 `admin-secret-key`，拦截 `/admin/**`（排除 `/admin/employee/login`）
- **用户端**: Token 名为 `authentication`，密钥 `user-secret-key`，拦截 `/user/**`（排除 `/user/user/login`、`/user/shop/status`）
- **上下文传递**: 拦截器解析 JWT 后将用户 ID 存入 `BaseContext`（ThreadLocal），Service 层通过 `BaseContext.getCurrentId()` 获取当前操作人，实现线程级隔离

### 全局异常处理

- `@RestControllerAdvice` + `@ExceptionHandler` 统一捕获异常
- **业务异常体系**: `BaseException` 为基类，派生出 11 个子类（`AccountLockedException`、`AccountNotFoundException`、`LoginFailedException`、`PasswordErrorException`、`DeletionNotAllowedException`、`SetmealEnableFailedException`、`AddressBookBusinessException`、`OrderBusinessException`、`ShoppingCartBusinessException`、`UserNotLoginException`、`PasswordEditFailedException`），覆盖各业务场景
- **SQL 异常**: `SQLIntegrityConstraintViolationException`（唯一约束冲突）→ 自动解析 `Duplicate entry 'xxx' for key 'xxx'` 消息，提取字段名返回友好提示（如"admin 已存在"）
- 统一响应体 `Result<T>` 封装：`{ code: 1, msg: "...", data: {...} }`，`code=1` 表示成功，`code=0` 表示失败

### 前端路由守卫

- `router.beforeEach` 检查 Cookie 中是否存在 Token
- 未认证访问非公开页面 → 重定向到 `/login`
- 公开页面通过在路由 `meta` 中标记 `notNeedAuth: true` 跳过校验

---

## 缓存策略

### 双层缓存策略

项目采用 **Spring Cache 注解** + **手动 RedisTemplate** 两种方式协同工作：

#### 1. Spring Cache 注解方式（套餐数据）

```java
// 查询时自动缓存 — Redis Key: setmealCache::{categoryId}
@Cacheable(cacheNames = "setmealCache", key = "#categoryId")
public List<Setmeal> list(Long categoryId) { ... }

// 修改时精确清除对应缓存分区
@CacheEvict(cacheNames = "setmealCache", key = "#categoryId")
public void delete(Long id) { ... }
```

#### 2. 手动 RedisTemplate 方式（菜品数据）

```java
// 用户端查询菜品 — 缓存 Key: dish_{categoryId}
String key = "dish_" + categoryId;
String cached = redisTemplate.opsForValue().get(key);
if (cached != null) return JSON.parseArray(cached, DishVO.class);
// 缓存未命中 → 查数据库 → 写入 Redis
redisTemplate.opsForValue().set(key, JSON.toJSONString(dishList), 60, TimeUnit.MINUTES);

// 管理员增删改菜品时，统一清除所有菜品缓存
private void cleanCache(String pattern) {
    Set<String> keys = redisTemplate.keys(pattern);
    redisTemplate.delete(keys);
}
```

#### 3. 店铺营业状态缓存

- Redis Key: `SHOP_STATUS`，存储整型值（1=营业中 / 0=休息中）
- 商家端切换状态时直接写入 Redis，无需落库
- 用户端每次查询实时读取，保证状态即时生效

- **缓存对象**: 套餐列表（按分类 ID 缓存）、菜品列表、店铺营业状态
- **缓存策略**: 查询时缓存，增删改时清除对应缓存；菜品采用通配符批量删除 `dish_*`
- **Redis 配置**: 自定义 `RedisConfiguration`，Key 使用 `StringRedisSerializer`，Value 使用 `Jackson2JsonRedisSerializer`

---

## 实时通信 — WebSocket

### 技术方案

- **服务端**: JSR 356 标准注解 `@ServerEndpoint("/ws/{sid}")`，`sid` 为客户端唯一标识
- **会话管理**: 静态 `ConcurrentHashMap<String, Session>` 存储在线客户端
- **推送方式**: 遍历所有会话调用 `session.getBasicRemote().sendText(message)` 群发（也可以扩展为按 sid 单发）

### 应用场景

| 场景 | 触发方 | 推送内容 | 接收方 |
|------|--------|----------|--------|
| 来单提醒 | 用户支付成功回调 | 订单编号、金额、下单时间 | 所有在线商家客户端 |
| 客户催单 | 用户点击催单 | 订单编号、催单信息 | 所有在线商家客户端 |

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

- **验签机制**: 回调时使用微信平台证书验证签名，防止伪造通知
- **开发模式**: 配置中可切换模拟支付，避免频繁调用微信接口

---

## AOP 公共字段自动填充

### 设计思路

数据库表中普遍存在 `create_time`、`update_time`、`create_user`、`update_user` 四个公共字段，若每个 Service 方法都手动 set，样板代码大量重复。

### 实现方案

1. 自定义注解 `@AutoFill(OperationType)` 标记 Mapper 方法
2. 切面类 `AutiFillAspect` 拦截所有 `com.suda.mapper.*.*` 且带 `@AutoFill` 的方法
3. 通过反射调用实体对象的 setter 方法自动注入字段值
4. `OperationType.INSERT` → 同时填充 `createTime/createUser/updateTime/updateUser`
5. `OperationType.UPDATE` → 仅填充 `updateTime/updateUser`

> **难点**: 反射调用需要约定实体类的 setter 方法名（如 `setCreateTime`），通过 `MethodSignature` 获取注解参数和实体对象，利用 `Method.invoke` 动态赋值。

---

## 定时任务

| Cron 表达式 | 任务 | 逻辑 |
|-------------|------|------|
| `0 * * * * ?` | 处理超时订单 | 每分钟扫描 `status=待支付 AND order_time < 当前-15分钟` → 自动取消 |
| `0 0 1 * * ?` | 处理派送中订单 | 每天凌晨 1 点扫描 `status=派送中 AND order_time < 当前-1小时` → 自动完成 |

---

## 数据统计与报表

### 统计接口 (`ReportController`)

| 接口 | 说明 | SQL 策略 |
|------|------|----------|
| `/report/turnoverStatistics` | 营业额统计 | 按日期分组 SUM 已完成订单金额 |
| `/report/userStatistics` | 用户统计 | 按日期分组 COUNT 新增用户 |
| `/report/ordersStatistics` | 订单统计 | 按日期分组 COUNT + 状态过滤 |
| `/report/top10` | 销量排名 | 按 dish_name GROUP BY 后 SUM(number) ORDER BY DESC LIMIT 10 |
| `/report/export` | Excel 导出 | Apache POI 生成工作簿 → 输出流写入 |

### 前端可视化

- 使用 ECharts 渲染折线图（趋势）、柱状图（对比）、饼图（占比）
- 组件化拆分：`turnoverStatistics.vue`、`userStatistics.vue`、`orderStatistics.vue`、`top10.vue`、`overview.vue`
- 日期选择器联动数据请求，动态刷新图表

---

## 配送范围校验

### 百度地图 API 调用链

1. **地理编码**: 用户收货地址 → 百度地图 Geocoding API → 经纬度坐标
2. **路线规划**: 商家坐标 → 用户坐标 → Driving Direction API → 驾车距离
3. **范围判断**: 距离 > 5km → 抛出 `OrderBusinessException` → 前端提示"配送距离超出范围"

---

## 环境搭建

### 环境要求

| 组件 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | Java 运行环境 |
| Maven | 3.6+ | 后端构建 |
| MySQL | 8.0+ | 数据库 |
| Redis | 5.0+ | 缓存 |
| Node.js | 14+ | 前端构建 |
| 微信开发者工具 | 最新版 | 小程序预览/调试 |
| Cpolar | 最新版 | 微信支付回调内网穿透（开发环境） |

### 快速启动

#### 第一步：导入数据库

```bash
mysql -u root -p < sql/sky_take_out.sql
```

#### 第二步：修改配置

编辑 `suda-server/src/main/resources/application-dev.yml`，根据本地环境填入以下配置：

- `suda.datasource.*` — 数据库连接（host、port、database、username、password）
- `suda.redis.*` — Redis 连接（host、port、password、database）
- `suda.alioss.*` — 阿里云 OSS（endpoint、access-key-id、access-key-secret、bucket-name）
- `suda.wechat.*` — 微信支付 V3（app-id、secret、mchid、mchSecret、apiV3Key、notifyUrl 等）

#### 第三步：启动后端

```bash
# 进入后端根目录
cd backend/suda-take-out

# 编译并安装所有模块（跳过测试）
mvn clean install -DskipTests

# 进入服务模块并启动
cd suda-server
mvn spring-boot:run
```

启动成功后会看到 `server started` 日志，后端运行在 **`http://localhost:8080`**。

#### 第四步：启动商家管理端（前端）

```bash
# 进入前端项目
cd front/project-suda-admin-vue3

# 首次运行需安装依赖
npm install

# 启动开发服务器
npm run dev
```

启动后浏览器访问 **`http://localhost:8888`**，进入商家后台管理登录页面。

#### 第五步：启动小程序端

1. 打开 **微信开发者工具**
2. 导入项目 → 选择 `front/mp-weixin` 目录
3. 填入小程序的 **AppID**（在 `application-dev.yml` 的 `suda.wechat.app-id` 中配置的对应小程序）
4. 开发者工具中即可预览和调试

#### 第六步：启动 Cpolar 内网穿透（开发环境）

微信支付回调需要外网可达的 URL，开发环境使用 Cpolar 做内网穿透：

```bash
# 将本地 8080 端口暴露到公网
cpolar http 8080
```

启动后会生成一个公网地址，例如 `https://xxxx.r30.cpolar.top`。需要同步修改以下配置：

1. 编辑 `suda-server/src/main/resources/application-dev.yml`，将 `suda.wechat.notifyUrl` 改为 `https://xxxx.r30.cpolar.top/notify/paySuccess`
2. 在**微信支付商户平台**后台将回调地址也更新为同一个 URL
3. 重启后端服务使配置生效

> **提示**：cpolar 免费版每次重启后公网 URL 会变化，正式开发建议使用付费固定域名或 Cpolar 的预留域名功能。

### 启动总结

| 端 | 目录 | 启动命令 | 默认地址 |
|---|------|---------|---------|
| 后端 API | `backend/suda-take-out/` | `mvn clean install -DskipTests && cd suda-server && mvn spring-boot:run` | `http://localhost:8080` |
| 商家管理端 | `front/project-suda-admin-vue3/` | `npm install && npm run dev` | `http://localhost:8888` |
| 用户小程序端 | `front/mp-weixin/` | 微信开发者工具导入 | 工具内预览 |
| Cpolar 穿透 | - | `cpolar http 8080` | `https://xxxx.r30.cpolar.top` |
| API 文档 | - | 后端启动后访问 | `http://localhost:8080/doc.html` |

---

## API 接口文档

启动后端后访问 Knife4j 在线文档：`http://localhost:8080/doc.html`

接口分为两组：
- **管理端接口**: 员工管理、分类管理、菜品管理、套餐管理、订单管理、数据统计、报表导出
- **用户端接口**: 用户登录、地址管理、菜品浏览、购物车、订单、店铺状态

---

## 项目目录

```
SUDAWAIMAI/
├── backend/
│   └── suda-take-out/            # 后端 Maven 多模块工程
│       ├── suda-common/          # 公共模块
│       ├── suda-pojo/            # 实体模块
│       └── suda-server/          # 服务启动模块
├── front/
│   ├── project-suda-admin-vue3/  # 商家管理端 (Vue 3 SPA)
│   └── mp-weixin/                # 用户小程序端 (Uni-app)
└── sql/                          # 数据库初始化脚本
```

- 后端项目: [suda-take-out](./)
- 前端管理端: [project-suda-admin-vue3](../front/project-suda-admin-vue3)
- 小程序端: [mp-weixin](../front/mp-weixin) — 独立 Uni-app 工程（微信开发者工具导入）
