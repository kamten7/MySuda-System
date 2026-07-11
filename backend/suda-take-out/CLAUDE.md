# 后端 CLAUDE.md — suda-take-out

## 技术栈
- Spring Boot 3.4.3 + Java 17 + Maven
- MyBatis + PageHelper + Druid 连接池
- MySQL 8.0+ / Redis 5.0+（端口 6380，密码 123456，db 1）
- Spring AI 1.0.0-M5（用户端 AI）+ LangChain4j 1.6.0（管理端 AI）
- DeepSeek V4 Flash（官方 API，OpenAI 兼容协议）
- MinIO / 阿里云 OSS（图片存储）
- WebSocket（JSR 356，来单/催单推送）
- 微信支付 V3 API
- Knife4j（API 文档）

## 模块结构
suda-take-out/
├── suda-common/          # 工具类、常量、异常、Result/PageResult、BaseContext
├── suda-pojo/            # entity、dto、vo
└── suda-server/          # 核心服务
    ├── AI/               # Spring AI 集成层
    │   ├── config/       # SpringAIConfig（ChatClient + 16 个 FunctionCallback）
    │   ├── AIAgent.java  # LangChain4j 管理端 AI Agent（流式查询 + 诊断）
    │   └── tools/        # UserAITools / DishTools / RecommendTools / CartTools
    ├── annotation/       # 自定义注解 (@AutoFill)
    ├── aspect/           # AOP 切面（公共字段自动填充）
    ├── config/           # 配置类（MVC、OSS/MinIO、Redis、WebSocket）
    ├── context/          # ThreadLocal 用户上下文
    ├── controller/
    │   ├── admin/        # 管理端（含 AdminAIController）
    │   ├── user/         # 用户端（含 UserAIController）
    │   └── notify/       # 支付回调
    ├── interceptor/      # JWT 拦截器（管理端 + 用户端）
    ├── mapper/           # MyBatis Mapper 接口
    ├── service/          # 业务服务层（含 AIService / UserAIService）
    ├── task/             # 定时任务（Spring Task，超时取消/自动完成）
    └── websocket/        # WebSocket 服务端（来单/催单推送）

## 数据库初始化
SQL 脚本位置：`backend/suda-take-out/sql/sky_take_out.sql`
执行命令：`mysql -u root -p sky_take_out < sql/sky_take_out.sql`

## 环境配置
配置文件：`suda-server/src/main/resources/application-dev.yml`（从 `.example` 复制）
必须配置项：
| 配置项 | 说明 |
|--------|------|
| `suda.datasource.*` | MySQL 连接信息 |
| `suda.redis.*` | Redis 连接（端口 6380） |
| `suda.minio.*` | MinIO 对象存储 |
| `suda.wechat.*` | 微信支付相关 |
| `spring.ai.openai.*` | Spring AI 配置（DeepSeek 官方 API，base-url + API key） |
| `suda.ai.deepseek.*` | LangChain4j 管理端 AI 配置（DeepSeek 官方 API） |
| `suda.langchain4j.open-ai.*` | LangChain4j 流式/同步模型配置 |

### DeepSeek 官方 API 配置要点
- **注册地址**: [platform.deepseek.com](https://platform.deepseek.com)，新用户赠送 500 万 token
- **User-agent AI（Spring AI）**: base URL `https://api.deepseek.com`（⚠️ **不含** `/v1`，`OpenAiApi` 内部自动拼接 `/v1/chat/completions`）
- **Admin AI（LangChain4j）**: base URL `https://api.deepseek.com/v1`（**含** `/v1`）
- **API Key**: DeepSeek 官方 `sk-...` 格式
- **模型**: `deepseek-v4-flash`（$0.14 / 百万 token 输入，$0.28 / 百万 token 输出）
- **已排除自动配置**: Spring AI `OpenAiAutoConfiguration` + `SpringAiRetryAutoConfiguration`，由 `SpringAIConfig` 手动创建 `OpenAiChatModel`

## AI 集成要点

### 用户端「小速」—— 16 个 FunctionCallback
| 类别 | 工具 | 说明 |
|------|------|------|
| 时间 | getCurrentDate / getCurrentDateTime / getTimeContext | 时间感知 |
| 菜品 | searchDishes / getDishDetail / getCategories / getHotDishes / searchByPriceRange | 菜品查询 |
| 推荐 | getRecommendations / getBudgetFriendlyDishes | 智能推荐 |
| 购物车 | addToCart / removeFromCart / getCart / clearCart / getCartCount | 购物车操作 |

**安全机制**：System Prompt 强制「推荐→询问→确认→操作」流程，AI 不能自主操作购物车。

### 管理端 AI 运营诊断
- 自然语言数据查询（`/admin/ai/query/stream`，SSE 流式）
- 一键经营诊断报告（`/admin/ai/diagnose`，SSE 流式）

### AI 会话管理
Redis key：`ai:session:user:{userId}`，TTL 2 天，最多 50 轮对话。

## API 端点速查
| 端 | 方法 | 路径 | 说明 |
|----|------|------|------|
| 用户 | POST | `/user/ai/chat` | 同步对话 |
| 用户 | POST | `/user/ai/chat/stream` | SSE 流式（120s 超时） |
| 用户 | GET | `/user/ai/cart/status` | 购物车状态 |
| 用户 | DELETE | `/user/ai/history/{sessionId}` | 清空历史 |
| 管理 | POST | `/admin/ai/query/stream` | 自然语言查询（SSE） |
| 管理 | POST | `/admin/ai/diagnose` | 诊断报告（SSE） |

## WebSocket 端点
| 端点 | 说明 | 使用场景 |
|------|------|----------|
| `/ws/{clientId}` | 来单提醒 | 管理端浏览器连接，接收新订单通知 |
| `/ws/admin` | 催单提醒 | 管理端接收用户催单 |

## API 文档
Knife4j 地址：`http://localhost:8080/doc.html`
- 管理端接口分组：`/admin/**`
- 用户端接口分组：`/user/**`

## 测试命令
```bash
mvn test                              # 运行所有测试
mvn test -pl suda-server              # 只测试 server 模块
mvn clean install -DskipTests         # 跳过测试构建
```

## 常见陷阱
1. **中文双引号**：`.description("...不知道'今天'是哪一天...")` 用单引号
2. **FunctionCallback 注入**：用 `List<FunctionCallback>` 自动收集，不用参数名匹配
3. **Redis 端口**：6380，不是默认 6379
4. **JWT 双通道**：管理端用 `token` header，用户端用 `authentication` header
5. **Spring AI base-url**：不能带 `/v1`，`OpenAiApi` 内部拼接为 `{baseUrl}/v1/chat/completions`
6. **LangChain4j base-url**：必须带 `/v1`，直接拼接到 `/chat/completions`
7. **模型名**：用 `deepseek-v4-flash`，不要用 `deepseek-reasoner`（付费 R1）
8. **Spring AI 自动配置**：已在 `SudaApplication` 排除 `OpenAiAutoConfiguration`，手动创建 `OpenAiChatModel`
