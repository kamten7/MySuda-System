# 后端 CLAUDE.md — suda-take-out

## 技术栈
- Spring Boot 3.4.3 + Java 17 + Maven
- MyBatis + PageHelper + Druid 连接池
- MySQL 8.0+ / Redis 5.0+（端口 6380，密码 123456，db 1）
- LangChain4j 1.6.0（用户端 + 管理端 AI，统一框架）
- Agnes AI / DeepSeek V4 Flash（OpenAI 兼容协议）
- MinIO / 阿里云 OSS（图片存储）
- WebSocket（JSR 356，来单/催单推送）
- 微信支付 V3 API
- Knife4j（API 文档）

## 模块结构
suda-take-out/
├── suda-common/          # 工具类、常量、异常、Result/PageResult、BaseContext
├── suda-pojo/            # entity、dto、vo
└── suda-server/          # 核心服务
    ├── AI/               # AI 集成层（LangChain4j）
    │   ├── AIAgent.java  # 管理端 + 用户端 LangChain4j 配置
    │   ├── service/      # UserAIIntentService / UserAIPromptBuilder（预执行架构）
    │   └── tools/        # UserAITools / DishTools / RecommendTools / CartTools（纯 Java 工具）
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
| `suda.langchain4j.open-ai.*` | LangChain4j 流式/同步模型配置（用户端 + 管理端共用） |

### LangChain4j API 配置要点
- 用户端 + 管理端统一使用 LangChain4j `OpenAiStreamingChatModel`
- base URL 格式：`https://apihub.agnes-ai.com/v1`（**含** `/v1`）
- 模型：`agnes-2.0-flash`（或 `deepseek-v4-flash`）
- 用户端 temperature=0.7，管理端 temperature=0.1

## AI 集成要点

### 用户端「小速」—— Java 预执行 + LangChain4j 文本生成

**v3.0 架构**：不再依赖 AI 模型调用工具，改为 Java 层确定性预执行。
1. **意图检测**（`UserAIIntentService`）→ 正则 + 关键词判断用户意图
2. **工具预执行** → Java 直接调用 DishTools/RecommendTools/CartTools
3. **构建增强 Prompt**（`UserAIPromptBuilder`）→ 注入真实菜品数据 + [ID:xxx]
4. **流式文本生成**（LangChain4j）→ AI 仅做 NLG 文本润色
5. **Java 后处理** → 会话持久化、推荐追踪、加购事件

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
2. **Redis 端口**：6380，不是默认 6379
3. **JWT 双通道**：管理端用 `token` header，用户端用 `authentication` header
4. **LangChain4j base-url**：必须带 `/v1`，直接拼接到 `/chat/completions`
5. **模型名**：用 `agnes-2.0-flash` 或 `deepseek-v4-flash`
