# 速达外卖 (Suda Take-Out)

> **Spring Boot + Vue 3 + 微信小程序 — 全链路外卖平台**

前后端分离 + 多端协同架构，覆盖商家后台管理、用户点餐、订单配送全流程。

---

## 子项目

| 端 | 技术栈 | 目录 | 文档 |
|---|--------|------|------|
| **后端 API** | Spring Boot 2.7 + MyBatis + MySQL + Redis + JWT | `backend/suda-take-out/` | [README](backend/suda-take-out/README.md) |
| **管理端** | Vue 3 + Vite + Element Plus + Tailwind + Pinia | `front/project-suda-admin-vue3/` | [README](front/project-suda-admin-vue3/README.md) |
| **小程序端** | Uni-app (微信小程序) | `front/mp-weixin/` | [README](front/mp-weixin/project-rjwm-weixin-uniapp/README.md) |

---

## 快速启动

### 1. 后端

```bash
cd backend/suda-take-out
# 复制环境配置模板 → 填入数据库/Redis/OSS 等配置
cp suda-server/src/main/resources/application-dev.yml.example suda-server/src/main/resources/application-dev.yml
mvn clean install -DskipTests
cd suda-server && mvn spring-boot:run   # → http://localhost:8080
```

### 2. 管理端前端

```bash
cd front/project-suda-admin-vue3
cp .env.example .env
npm install && npm run dev              # → http://localhost:8888
```

### 3. 小程序端

用**微信开发者工具**导入 `front/mp-weixin/` 目录即可预览。

---

## 目录结构

```
SUDAWAIMAI/
├── backend/suda-take-out/            # 后端 Maven 多模块
│   ├── suda-common/                  # 工具类、常量、Result
│   ├── suda-pojo/                    # Entity、DTO、VO
│   └── suda-server/                  # Controller、Service、Mapper
├── front/
│   ├── project-suda-admin-vue3/      # Vue 3 管理端
│   └── mp-weixin/                    # Uni-app 小程序
└── .claude/                          # Claude Code 配置
    └── CLAUDE.md                     # AI 上下文文件
```

---

## 关键架构决策

- **JWT 双通道认证**：管理端 `token` header + 用户端 `authentication` header，独立密钥
- **ThreadLocal 上下文**：`BaseContext` 存储当前用户 ID，Service 层无感获取
- **AOP 自动填充**：`@AutoFill` 注解 + AspectJ 切面自动注入 createTime/updateTime
- **双层缓存**：Spring Cache（套餐） + RedisTemplate（菜品），增删改时自动清除
- **WebSocket 推送**：来单提醒 + 催单通知实时推送到商家端
- **统一响应**：`Result<T>` { code: 1 成功 / 0 失败, msg, data } + `PageResult` { total, records }
