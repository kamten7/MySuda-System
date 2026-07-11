# CLAUDE.md — 速达外卖项目

## 项目身份
全栈外卖平台：Spring Boot 3 + Vue 3 + Uni-app 小程序。详细文档见各子项目 README。

## 开发环境要求
| 环境 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | 后端必需 |
| Node.js | 18+ | 管理端必需 |
| MySQL | 8.0+ | 数据库 |
| Redis | 5.0+ | 端口 6380，密码 123456，database 1 |
| HBuilderX | 最新 | 小程序开发必需 |
| 微信开发者工具 | 最新 | 小程序预览 |

## 核心约束（全局生效）
1. **Java 字符串**：中文引号用单引号 `'今天'`，否则编译失败
2. **Vue 2 小程序**：`mapState` 必须在 `computed` 里，不在 `methods` 里
3. **Spring AI**：多个 FunctionCallback 用 `List<FunctionCallback>` 注入，不用参数名匹配
4. **Redis**：端口 6380，密码 123456，database 1
5. **小程序单位**：使用 rpx（750rpx 设计稿），px 值 × 2 = rpx
6. **JWT 双通道**：管理端 Header `token`（密钥 kamten），用户端 Header `authentication`（密钥 itheima）

## 环境配置步骤
1. **后端配置**：
    - 复制 `backend/suda-take-out/suda-server/src/main/resources/application-dev.yml.example` → `application-dev.yml`
    - 填入真实的 MySQL、Redis、微信支付、DeepSeek API Key 等配置
2. **管理端配置**：
    - 复制 `front/project-suda-admin-vue3/.env.example` → `.env`
    - 确认 `VITE_SERVER_URL=http://localhost:8080/admin`
3. **小程序配置**：
    - 修改 `utils/env.js` 中的 `baseUrl`（默认 `http://localhost:8080` ）

## 启动顺序
1. **基础设施**：启动 MySQL + Redis 服务
2. **后端**：`cd backend/suda-take-out && mvn spring-boot:run -pl suda-server` → localhost:8080
3. **管理端**：`cd front/project-suda-admin-vue3 && npm run dev` → localhost:8888
4. **小程序**：HBuilderX 打开 `front/mp-weixin/project-rjwm-weixin-uniapp` → 运行到微信开发者工具

## 子项目索引
| 模块 | 路径 | 技术栈 | 详细文档 |
|------|------|--------|----------|
| 后端 | `backend/suda-take-out/` | Spring Boot 3 + MyBatis + Redis + Spring AI + LangChain4j | `backend/suda-take-out/CLAUDE.md` |
| 管理端 | `front/project-suda-admin-vue3/` | Vue 3 + Element Plus + Pinia + ECharts | `front/project-suda-admin-vue3/CLAUDE.md` |
| 小程序 | `front/mp-weixin/project-rjwm-weixin-uniapp/` | Uni-app (Vue 2) + Vuex | `front/mp-weixin/project-rjwm-weixin-uniapp/CLAUDE.md` |

## WebSocket 地址
- 管理端来单提醒：`ws://localhost:8080/ws/{clientId}`
- 管理端催单提醒：同上，通过 WebSocket 接收用户催单通知

## API 文档地址
- Knife4j：`http://localhost:8080/doc.html`
- 管理端接口分组：`/admin/**`
- 用户端接口分组：`/user/**`

## 常用命令速查
```bash
# 后端
cd backend/suda-take-out && mvn clean install -DskipTests
cd suda-server && mvn spring-boot:run  # → localhost:8080

# 管理端
cd front/project-suda-admin-vue3 && npm install && npm run dev  # → localhost:8888

# 小程序：HBuilderX 打开 front/mp-weixin/project-rjwm-weixin-uniapp → 运行到微信开发者工具
```