# CLAUDE.md

本文件为 Claude Code 提供项目上下文。详细文档见 `README.md`。

## 常用命令

### 后端（Maven 多模块，需 JDK 17）

```bash
cd backend/suda-take-out
mvn clean install -DskipTests          # 构建全部模块
cd suda-server && mvn spring-boot:run  # 启动 → localhost:8080
```

API 文档：`http://localhost:8080/doc.html`（Knife4j）

### 前端管理端

```bash
cd front/project-suda-admin-vue-ts
npm install && npm run serve   # 开发 → localhost:8888
npm run build                  # 生产构建
npm run lint                   # ESLint
npm run test:unit              # Jest 单元测试
npm run svg                    # 从 svg 生成图标组件
```

开发代理：`/api/*` → `http://localhost:8080/admin/*`（前缀 `/api` 被去除，见 `vue.config.js`）

### 小程序端

`front/mp-weixin/` 是独立的 Uni-app 项目，用微信开发者工具导入运行。

## 项目配置（给别人拉取后如何运行）

项目采用 **example 模板** 模式管理敏感配置——模板文件提交到 Git，真实配置 git-ignore：

| 模板文件（已提交） | 真实配置（git-ignore，本地创建） | 说明 |
|---|---|---|
| `suda-server/.../application-dev.yml.example` | `application-dev.yml` | 数据库、Redis、OSS、微信支付等 |
| `front/.../.env.example` | `.env.development` 等 | 前端环境变量（API地址、WebSocket地址等） |

**后端启动前**：复制 `application-dev.yml.example` → `application-dev.yml`，填入本地数据库密码、Redis 密码、阿里云 OSS AK、微信支付配置等。

**前端启动前**：复制 `.env.example` → 对应 `.env` 文件，填入本地 API 地址。

Git 使用电脑本地全局配置，无需项目级 Git 设置。

## 模块结构

```
backend/suda-take-out/
├── suda-common/   → 工具类、常量、异常、Result/PageResult、BaseContext、JacksonObjectMapper
├── suda-pojo/     → entity、dto、vo（不依赖 suda-common）
└── suda-server/   → controller、service、mapper、config、interceptor、aspect、task、websocket
```

Controller 分包：`controller.admin/`（管理端，`/admin/**`）、`controller.user/`（用户端，`/user/**`）、`controller.notify/`（支付回调，无鉴权）

## 核心架构模式

### JWT 双通道认证

- **管理端**：Header `token`，密钥 `admin-secret-key` → `JwtTokenAdminInterceptor`
- **用户端**：Header `authentication`，密钥 `user-secret-key` → `JwtTokenUserInterceptor`

拦截器校验通过后调用 `BaseContext.setCurrentId()`（ThreadLocal），Service 层通过 `BaseContext.getCurrentId()` 获取当前用户ID。

JWT 在 Controller 登录方法中直接创建：
```java
Map<String, Object> claims = new HashMap<>();
claims.put(JwtClaimsConstant.EMP_ID, employee.getId());  // 或 USER_ID
String token = JwtUtil.createJWT(secretKey, ttl, claims);  // HS256
```

### AOP 公共字段自动填充

Mapper 方法加 `@AutoFill(OperationType.INSERT/UPDATE)` → `AutiFillAspect`（@Before 切面）反射调用实体的 `setCreateTime/createUser/updateTime/updateUser`。实体类必须有这四个 setter。

### 全局异常处理

`GlobalExceptionHandler`（`@RestControllerAdvice`）：
- `BaseException` 及其 11 个子类 → `Result.error(message)`
- `SQLIntegrityConstraintViolationException` → 解析 `Duplicate entry 'x'` 返回友好提示

**已知问题**：`exceptionHandler(SQLIntegrityConstraintViolationException)` 方法缺少 `@ExceptionHandler` 注解，该异常目前不会被捕获。

### API 响应规范

`Result<T>`：`code=1` 成功，`code=0` 失败。分页用 `PageResult`（`total` + `records`）。分页查询前调用 `PageHelper.startPage(page, pageSize)` 自动追加 LIMIT。

### 微信支付（模拟）

**微信支付未真正调用**。`OrderServiceImpl.payment()` 中真实微信 API 调用已注释掉，改为直接返回 `fake_prepay_id`、`fake_pay_sign` 等假数据。Controller 层调用 `payment()` 后立即调用 `paySuccess()`，跳过微信回调流程。因此也**不需要 Cpolar 内网穿透**。如需接入真实支付，取消注释恢复 `weChatPayUtil.pay()` 调用，并配置 Cpolar 回调地址。

### 缓存策略

1. **Spring Cache**（套餐）：`@Cacheable(cacheNames = "setmealCache", key = "#categoryId")` / `@CacheEvict`
2. **手动 RedisTemplate**（菜品）：key 模式 `dish_{categoryId}`，批量清除用 `redisTemplate.keys("dish_*")` + `delete`

增删改菜品/套餐时必须清除对应缓存。

### WebSocket 推送

`WebSocketServer`（`@ServerEndpoint("/ws/{sid}")`），会话存在静态 `Map<String, Session>`，`sendToAllClient(message)` 群发。用于来单提醒和催单通知。

### 定时任务

`OrderTask`：
- `0 * * * * ?`：每分钟取消超时未付订单（>15分钟）
- `0 0 1 * * ?`：每天凌晨1点完成派送中订单（>1小时）

### 前端认证流程

登录 → JWT 存入 cookie（`js-cookie`, key=`token`）+ Vuex（`vuex-persistedstate` 持久化）→ Axios 拦截器附加 token → 路由守卫检查 cookie，`meta.notNeedAuth: true` 的路由跳过

### 前端 SCSS

`_variables.scss` 和 `_mixins.scss` 通过 `style-resources-loader` 自动注入所有组件，无需手动 import。

## 关键依赖

- **分页**：PageHelper — `PageHelper.startPage()` 后自动改写 SQL 加 LIMIT
- **连接池**：Druid
- **JSON**：Fastjson（手动序列化），Jackson + `JacksonObjectMapper`（MVC 自动转换）
- **API 文档**：Knife4j 3.0.2，两个 Docket 分组——"管理端接口"和"用户端接口"
