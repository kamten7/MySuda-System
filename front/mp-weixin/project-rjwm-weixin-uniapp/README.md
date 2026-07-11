# 速达外卖 · 微信小程序

> **Uni-app (Vue 2) + Vuex + SSE 流式 AI 点餐助手**

速达外卖用户端微信小程序，支持微信授权登录、菜品浏览、**AI 智能点餐助手"小速"**（SSE 流式对话）、购物车、地址管理、在线微信支付、订单追踪与催单。

---

## 🛠 技术栈

| 类别 | 技术 | 说明 |
|------|------|------|
| **框架** | Uni-app (Vue 2) + Vuex | 一套代码编译为微信小程序 |
| **开发工具** | HBuilderX | IDE，编译运行到微信开发者工具 |
| **样式** | SCSS + rpx 单位 | 蓝色系设计系统，基于 750rpx 设计稿 |
| **导航** | TabBar（3 Tab：首页/AI小速/我的） | 底部固定导航 |
| **AI 通信** | SSE 流式（wx.request enableChunked） | 实时流式 AI 对话，基础库 ≥ 2.20.1 |
| **HTTP** | 封装 request.js | JWT authentication header，`authentication` 令牌 |
| **Vuex** | store/index.js | 全局状态（AI 会话、订单列表、用户 Token） |

---

## 📁 项目结构

```
project-rjwm-weixin-uniapp/
├── pages/
│   ├── api/api.js              → 所有 API 接口定义（含 AI 相关 4 个）
│   ├── index/                  → 首页：分类侧边栏 + 菜品列表 + 购物车弹窗
│   ├── ai/index.vue            → 🆕 AI 聊天页：SSE 流式对话 + 快捷提问 + 菜品推荐卡片
│   ├── my/my.vue               → 个人中心：地址管理、历史订单、最近订单
│   ├── order/                  → 确认订单页 + 下单成功页
│   ├── historyOrder/           → 历史订单（分页加载）
│   ├── address/address.vue     → 地址列表
│   ├── addOrEditAddress/       → 新增/编辑地址
│   ├── nonet/index.vue         → 无网络页
│   └── common/
│       ├── Navbar/             → 自定义导航栏组件（渐变蓝背景）
│       └── simple-address/     → 省市区三级联动选择器
│
├── components/
│   ├── app-loading/            → 🆕 全屏加载遮罩
│   ├── app-empty/              → 🆕 空状态插画 + 提示文字
│   ├── app-skeleton/           → 🆕 骨架屏（菜品列表/分类/卡片/文本）
│   ├── dish-card/              → 🆕 菜品卡片（图片+信息+加减按钮）
│   ├── chat-bubble/            → 🆕 AI 聊天气泡（用户蓝底/AI 白底灰边）
│   ├── chat-input/             → 🆕 AI 聊天输入区
│   ├── empty/                  → 旧空状态组件（保留）
│   ├── reach-bottom/           → 上拉加载更多指示器
│   ├── uni-icons/              → uni-app 图标组件
│   ├── uni-nav-bar/            → uni-app 导航栏组件
│   └── uni-status-bar/         → uni-app 状态栏组件
│
├── utils/
│   ├── env.js                  → API baseUrl 配置
│   ├── request.js              → HTTP 请求封装（JWT authentication header）
│   ├── stream.js               → 🆕 SSE 流式请求（wx.request + enableChunked）
│   ├── webscoket.js            → WebSocket 封装
│   └── stomp.js                → STOMP 协议库
│
├── store/index.js              → Vuex 状态管理（含 AI 会话状态）
├── pages.json                  → 🆕 路由 + 3 TabBar 配置
├── uni.scss                    → 🆕 蓝色系设计 Token（品牌色/间距/圆角/动画）
├── App.vue                     → 🆕 全局样式 + 动画 keyframes
└── manifest.json               → 应用配置（AppID 等）
```

---

## 🎯 TabBar 导航

| Tab | 页面 | 图标 | 说明 |
|-----|------|------|------|
| 🏠 首页 | `pages/index/index` | 房屋 | 分类浏览 + 菜品列表 + 购物车 |
| 🤖 AI小速 | `pages/ai/index` | 对话 | SSE 流式 AI 对话 + 智能点餐 |
| 👤 我的 | `pages/my/my` | 用户 | 个人中心 + 地址 + 历史订单 |

---

## 🤖 AI 智能点餐助手"小速"

在 **AI小速** Tab 中与 AI 对话，实现自然语言点餐。

### 功能特性

- **SSE 流式对话**: 逐字输出，模仿主流大模型聊天体验
- **快捷提问**: 预设推荐问题（"帮我推荐几道菜"、"有什么招牌菜？"等）
- **菜品推荐卡片**: AI 推荐的菜品以卡片形式展示，支持查看详情和加入购物车
- **时间感知**: 自动识别早/午/晚餐时段，推荐对应餐次
- **场景推荐**: 根据用户描述的场景（天热/天冷/减肥/聚会）智能推荐

### AI API 端点

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/user/ai/chat` | 同步 AI 对话 |
| POST | `/user/ai/chat/stream` | SSE 流式 AI 对话（120s 超时） |
| GET | `/user/ai/cart/status` | 获取购物车状态 |
| DELETE | `/user/ai/history/{sessionId}` | 清空对话历史 |

### 技术实现

小程序端通过 `utils/stream.js` 封装 SSE 请求：

```javascript
// 使用 wx.request 的 enableChunked 模式实现流式读取
wx.request({
  url: baseUrl + '/user/ai/chat/stream',
  method: 'POST',
  enableChunked: true,    // 开启分块传输
  data: { message: question, sessionId: '' },
  success: (res) => {
    // 处理流式数据，逐字输出
  }
})
```

要求微信基础库 **≥ 2.20.1**。

---

## 🎨 设计系统

与管理端统一的蓝色品牌色系。

### 品牌色彩

| Token | 色值 | 用途 |
|-------|------|------|
| 品牌主色 | `#1a56db` | 导航栏、按钮、选中态 |
| 品牌深色 | `#1e40af` | 按钮按下态 |
| 品牌浅蓝 | `#eff6ff` / `#dbeafe` | 卡片浅蓝底、选中背景 |
| 强调色 | `#f59e0b` | 价格颜色、标签 |
| 成功绿 | `#10b981` | 订单完成 |
| 危险红 | `#ef4444` | 删除、角标 |
| 页面背景 | `#f3f4f7` | 全局底色 |
| 卡片背景 | `#fff` | 卡片、列表项 |

### 卡片规范

| 属性 | 值 |
|------|-----|
| 圆角 | `16rpx` |
| 阴影 | `0 4rpx 24rpx rgba(0,0,0,0.06)` |
| 内边距 | `24rpx` |
| 悬浮阴影 | `0 8rpx 32rpx rgba(0,0,0,0.10)` |

### 导航栏渐变

```css
linear-gradient(135deg, #1e3a8a 0%, #1a56db 100%)
```

---

## 📱 主要功能模块

### 首页

- **分类侧边栏**: 左侧菜品分类列表，右侧对应菜品卡片
- **菜品卡片**: 图片 + 名称 + 价格 + 月售数量，点击可查看详情和口味选择
- **购物车弹窗**: 底部弹出购物车，支持增减数量、清空
- **店铺状态**: 顶部显示营业中/休息中状态

### AI小速

- **聊天界面**: 用户消息蓝色气泡靠右，AI 回复白色气泡靠左
- **打字机效果**: SSE 流式逐字输出，等待状态动画
- **快捷提问**: 底部预设推荐问题按钮
- **菜品推荐卡片**: AI 返回的菜品信息以卡片形式嵌入对话流

### 我的

- **用户信息**: 头像 + 昵称
- **地址管理**: 新增/编辑/删除收货地址，省市区三级联动
- **历史订单**: 分页加载，彩色状态标签
- **再来一单**: 复制历史订单菜品到购物车

### 订单流程

```
首页选择菜品 → 加入购物车 → 确认订单页（选择地址）
    → 微信支付 → 支付成功 → 订单追踪
```

---

## 🔧 开发指南

### 运行环境

1. 安装 **HBuilderX**（[下载地址](https://www.dcloud.io/hbuilderx.html)）
2. 安装 **微信开发者工具**（[下载地址](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)）
3. 用 HBuilderX 打开项目目录

### 运行步骤

1. HBuilderX → 文件 → 导入 → 选择 `front/mp-weixin/project-rjwm-weixin-uniapp`
2. 运行 → 运行到小程序模拟器 → 微信开发者工具
3. 微信开发者工具勾选 **"不校验合法域名"**
4. 确保后端 `localhost:8080` 已启动

### 注意事项

- **无 npm 依赖**: 本项目不使用 `package.json`，所有依赖来自 HBuilderX IDE 工具链
- **rpx 单位**: 所有尺寸使用 `rpx`，基于 750rpx 设计稿（1rpx = 屏幕宽度/750）
- **支付功能**: 个人微信账号资质无法发起微信支付，需要企业资质
- **开发环境 API**: 后端 baseUrl 配置在 `utils/env.js`，默认 `localhost:8080`
- **微信支付回调**: 开发环境使用 Cpolar 内网穿透使微信服务器能回调到本地

---

## 📄 相关文档

- [根项目 README](../../README.md) — 项目整体介绍
- [后端 README](../../backend/suda-take-out/README.md) — 后端详细说明
- [管理端 README](../../project-suda-admin-vue3/README.md) — 管理端详细说明
