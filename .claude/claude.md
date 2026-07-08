# CLAUDE.md — 速达外卖项目上下文

本文件为 Claude Code 提供项目上下文。详细文档见 `README.md`。

## 子项目索引

| 子项目 | 目录 | 说明 |
|--------|------|------|
| **后端服务** | `backend/suda-take-out/` | Spring Boot 2.7 + MyBatis + MySQL + Redis |
| **管理端** | `front/project-suda-admin-vue3/` | Vue 3 + Vite + Element Plus + Tailwind + Pinia |
| **小程序端** | `front/mp-weixin/` | Uni-app 微信小程序 |

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

开发代理：`/api/*` → `http://localhost:8080/admin/*`（去除 `/api` 前缀，见 `vite.config.ts`）

### 小程序端

`front/mp-weixin/` 是 Uni-app 项目，用微信开发者工具导入运行。

## 项目配置 — example 模板模式

敏感配置通过模板文件管理：

| 模板（已提交） | 真实配置（git-ignore） |
|---|---|
| `suda-server/.../application-dev.yml.example` | `application-dev.yml` |
| `front/project-suda-admin-vue3/.env.example` | `.env` |

## 后端模块

```
backend/suda-take-out/
├── suda-common/   → 工具类、常量、异常、Result/PageResult、BaseContext
├── suda-pojo/     → entity、dto、vo
└── suda-server/   → controller、service、mapper、config、interceptor、aspect、task、websocket
```

Controller 分包：`controller.admin/`（管理端 `/admin/**`）、`controller.user/`（用户端 `/user/**`）、`controller.notify/`（支付回调）

## 管理端前端 (Vue 3) 架构

### 目录 (`front/project-suda-admin-vue3/src/`)

```
src/
├── api/          → Axios 实例 + modules/（按业务拆分）+ types/（ApiResponse、PageResult）
├── components/   → layout/（Sidebar/Navbar/Breadcrumb）+ StatCard + EmptyState + ImageUpload
├── composables/  → usePagination、useChart、useTheme
├── layouts/      → DefaultLayout.vue
├── router/       → routes.ts + guards.ts（NProgress + token 守卫）
├── stores/       → Pinia：app.ts（主题/侧边栏/isFirstEnter）+ user.ts（token/登录）
├── styles/       → tailwind.css + index.scss + element-override.scss（含 html.dark 全覆盖）
├── utils/        → cookies.ts、auth.ts、date.ts（dayjs）
└── views/        → login/ dashboard/ order/ dish/ setmeal/ category/ employee/ statistics/ message/ error/ client-login/
```

### 技术栈

Vue 3.5 + TypeScript 5.6 + Vite 6 + Element Plus 2.9 + Tailwind CSS 3.4 + Pinia 2 + Vue Router 4 + Axios 1.x + ECharts 5

### 认证

- 登录：POST `/employee/login` → `{ code, data: { token, id, userName, name } }`
- Token 存储在 cookie `token`，请求头 `token: <jwt>`
- 路由守卫：无 token → 重定向 `/login`

### API 响应规范

- `Result<T>`：`code=1` 成功，`code=0` 失败
- `PageResult`：`{ total, records }`

### 主题

- 品牌色：深蓝 `#1a56db`，强调色 `#d32f2f`（登录按钮），辅助色 `#f59e0b`
- 深色模式：`html.dark` CSS 全覆盖，Element Plus 组件 + Tailwind 工具类 + 页面级 scoped
- 登录页 Canvas 粒子：卡片为波源，250 粒子涟漪扩散，浅蓝→深蓝渐变

### 设计原则

- `<script setup lang="ts">` 风格
- 暗色模式 `html.dark` 下零白色残留
- 登录成功 → 侧边栏菜单逐项飞入动画（`@keyframes flyInFromCenter`）
- 删除权限由 `VITE_DELETE_PERMISSIONS` 环境变量控制

## 核心架构模式（后端）

### JWT 双通道认证

- **管理端**：Header `token`，密钥 `admin-secret-key` → `JwtTokenAdminInterceptor`
- **用户端**：Header `authentication`，密钥 `user-secret-key` → `JwtTokenUserInterceptor`

拦截器校验通过 → `BaseContext.setCurrentId()`（ThreadLocal）→ Service 通过 `BaseContext.getCurrentId()` 获取。

### AOP 自动填充

Mapper 方法加 `@AutoFill(OperationType.INSERT/UPDATE)` → `AutiFillAspect` 反射调用实体的 `setCreateTime/createUser/updateTime/updateUser`。

### 全局异常

`GlobalExceptionHandler`：`BaseException` + 11 子类 → `Result.error(msg)`；`SQLIntegrityConstraintViolationException` → 解析 `Duplicate entry` 返回友好提示。

### 缓存

1. Spring Cache（套餐）`@Cacheable(key="#categoryId")` / `@CacheEvict`
2. RedisTemplate（菜品）key `dish_{categoryId}`，批量清除 `redisTemplate.keys("dish_*")` + `delete`

### WebSocket

`@ServerEndpoint("/ws/{sid}")`，静态 `Map<String, Session>`，`sendToAllClient` 群发来单/催单通知。

### 定时任务

- `0 * * * * ?`：每分钟取消超时未付订单（>15分钟）
- `0 0 1 * * ?`：每天凌晨 1 点完成派送中订单（>1小时）
