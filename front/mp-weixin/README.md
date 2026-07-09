
<h1 align="center">
  <img src="https://img.shields.io/badge/Uni--app-2.0-2B9939?logo=vue.js&logoColor=white" alt="Uni-app">
  <img src="https://img.shields.io/badge/微信小程序-WeChat-07C160?logo=wechat&logoColor=white" alt="WeChat">
  <br><br>
  速达外卖 · 用户小程序端
</h1>

<p align="center">
  <strong>Uni-app + Vue 2 + WebSocket — 微信小程序点餐平台</strong>
</p>

---

## 📖 简介

速达外卖小程序端是面向 **C 端用户** 的微信小程序，基于 **Uni-app 跨端框架** 开发，支持微信授权登录、菜品浏览、多人协同购物车、在线支付、订单追踪与催单等完整点餐体验。

### 核心场景

- 🔍 **浏览点餐** — 按分类浏览菜品/套餐，查看详情，加入购物车
- 🛒 **多人协同** — WebSocket 实时同步购物车，多人同桌扫码点餐
- 💳 **在线支付** — 微信支付 V3 完成下单，支付成功回调更新订单状态
- 📦 **订单追踪** — 查看历史订单、订单详情、再来一单、催单提醒

---

## 🛠 技术栈

| 类别 | 技术 | 说明 |
|------|------|------|
| **跨端框架** | Uni-app (Vue 2) | 一套代码编译为微信小程序 |
| **运行环境** | 微信小程序 | 微信登录、微信支付等原生能力 |
| **实时通信** | WebSocket | 多人点餐购物车数据同步 |
| **HTTP 请求** | uni.request | Uni-app 内置网络请求 API |
| **状态管理** | Vuex | 购物车、用户信息全局状态 |
| **CSS** | uni.scss + Flex 布局 | 跨端兼容样式方案 |

---

## 📁 项目结构

```
mp-weixin/
├── project.config.json             # 微信小程序项目配置
├── project.private.config.json     # 私有配置（不提交 Git）
├── README.md                       # 本文件
└── project-rjwm-weixin-uniapp/     # Uni-app 源码
    ├── pages/                      #   页面目录
    │   ├── index/                  #     首页（菜品分类 + 菜品列表）
    │   ├── order/                  #     下单页 + 下单成功页
    │   ├── address/                #     地址管理
    │   ├── addOrEditAddress/       #     新增/编辑地址
    │   ├── my/                     #     个人中心
    │   ├── historyOrder/           #     历史订单
    │   ├── nonet/                  #     无网络提示页
    │   ├── api/                    #     API 接口封装
    │   └── common/                 #     公共组件（Navbar、省市区选择器）
    ├── components/                 #   全局组件
    │   ├── empty/                  #     空状态组件
    │   ├── reach-bottom/           #     触底提示组件
    │   ├── uni-icons/              #     图标组件
    │   ├── uni-nav-bar/            #     自定义导航栏
    │   └── uni-status-bar/         #     状态栏占位组件
    ├── store/                      #   Vuex 状态管理
    ├── utils/                      #   工具函数
    ├── static/                     #   静态资源
    ├── image/                      #   图片资源
    ├── design/                     #   设计稿（效果图 + GIF 动图）
    ├── App.vue                     #   根组件（应用生命周期）
    ├── main.js                     #   项目入口（Vue 实例初始化）
    ├── pages.json                  #   页面路由 + 全局样式配置
    ├── manifest.json               #   应用配置（AppID、权限等）
    └── uni.scss                    #   全局 SCSS 变量
```

---

## 🚀 快速开始

### 前置条件

| 工具 | 说明 |
|------|------|
| **微信开发者工具** | [下载](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html) 最新稳定版 |
| **HBuilderX** | [下载](https://www.dcloud.io/hbuilderx.html) App 开发版（可选，用于修改 Uni-app 源码后重新编译） |
| **微信小程序 AppID** | 在[微信公众平台](https://mp.weixin.qq.com/)注册获取，或使用测试号 |
| **后端服务** | 运行在 `http://localhost:8080` |

### 导入运行

#### 方式一：直接导入（推荐）

1. 打开**微信开发者工具**
2. 点击「导入项目」→ 选择 `front/mp-weixin/` 目录
3. 填入小程序 **AppID**（开发阶段可使用测试号）
4. 点击「确定」即可预览

#### 方式二：通过 HBuilderX 运行

1. 打开 **HBuilderX** → 文件 → 导入 → 从本地目录导入
2. 选择 `front/mp-weixin/project-rjwm-weixin-uniapp/` 目录
3. 点击工具栏「运行」→「运行到小程序模拟器」→「微信开发者工具」
4. HBuilderX 会自动编译并唤起微信开发者工具

> ⚠️ **首次使用 HBuilderX 运行**需要配置微信开发者工具路径：HBuilderX → 工具 → 设置 → 运行配置 → 微信开发者工具路径

---

## 📱 页面功能

| 页面 | 路径 | 功能说明 |
|------|------|---------|
| 🏠 **首页** | `pages/index/index` | 菜品分类展示、菜品/套餐列表、加入购物车 |
| 🛒 **下单** | `pages/order/index` | 确认订单（选择地址、查看菜品明细、计算金额） |
| ✅ **下单成功** | `pages/order/success` | 支付成功提示，返回首页 |
| 📍 **地址管理** | `pages/address/address` | 地址列表、新增/编辑/删除、设为默认 |
| ✏️ **编辑地址** | `pages/addOrEditAddress/addOrEditAddress` | 省市区三级联动、详细地址输入 |
| 👤 **个人中心** | `pages/my/my` | 用户信息、历史订单入口 |
| 📋 **历史订单** | `pages/historyOrder/historyOrder` | 订单列表、订单详情、再来一单、催单 |
| 🔌 **无网络** | `pages/nonet/index` | 网络断开时提示页面 |

---

## 🔐 认证与支付

### 微信登录

```
小程序端                    后端 API                      微信服务
    │                          │                              │
    │  1. wx.login() 获取 code  │                              │
    │─────────────────────────►│                              │
    │                          │  2. code → openid            │
    │                          │─────────────────────────────►│
    │                          │  3. 返回 openid              │
    │                          │◄─────────────────────────────│
    │  4. 签发 JWT Token       │                              │
    │◄─────────────────────────│                              │
    │                          │                              │
    │  5. 后续请求携带 Token    │                              │
    │  Header: authentication  │                              │
```

- 新用户首次登录自动注册
- JWT Token 通过 Header `authentication` 传递
- 密钥：`user-secret-key`

### 微信支付流程

```
1. 用户确认订单 → 2. 后端统一下单（JSAPI）
→ 3. 小程序调起微信支付 → 4. 用户完成付款
→ 5. 微信支付回调后端 → 6. 验签 + 更新订单状态
→ 7. WebSocket 推送商家端
```

> ⚠️ **注意**：微信支付功能需要**企业资质**才能发起，个人微信账号无法完成支付。开发阶段可通过后端配置切换模拟支付模式。

### 配送范围校验

- 百度地图 API 将收货地址转为经纬度坐标
- 驾车路线规划计算商家到用户的配送距离
- 超过 **5km** 拒绝下单并提示用户

---

## 🔄 WebSocket 多人协同点餐

小程序支持**多人同桌扫码点餐**，通过 WebSocket 实时同步购物车：

- 扫描桌台二维码进入 → 获取桌台 ID
- 同一桌台的所有用户通过 WebSocket 共享购物车状态
- 任一用户加减菜品 → 广播同步给同桌其他用户
- 下单时锁定桌台，防止并发冲突

---

## 🎨 设计资源

项目 `design/` 目录包含 UI 设计稿：

| 资源 | 说明 |
|------|------|
| `action.gif` | 交互流程动图 |
| `index.png` | 首页设计稿 |
| `dish.png` | 菜品展示页设计稿 |
| `detail.png` | 菜品详情页设计稿 |

---

## 📝 开发说明

### Uni-app 开发规范

- 页面文件遵循 [Vue 单文件组件 (SFC) 规范](https://vue-loader.vuejs.org/zh/spec.html)
- 组件标签遵循[小程序规范](https://uniapp.dcloud.io/component/README)
- 数据绑定及事件处理同 Vue.js 规范
- 为兼容多端运行，推荐使用 **Flex 布局**
- 微信小程序 API `wx.*` 替换为 Uni-app 通用 API `uni.*`

### 关键文件说明

| 文件 | 作用 |
|------|------|
| `pages.json` | 页面路径注册、窗口样式、导航栏配置 |
| `manifest.json` | 应用配置（AppID、权限、各平台特有设置） |
| `App.vue` | 根组件，应用级生命周期钩子 |
| `main.js` | 项目入口，Vue 实例初始化与插件注册 |
| `uni.scss` | 全局 SCSS 变量，统一控制应用风格 |

---

## 📄 相关文档

- [根项目 README](../../README.md) — 项目整体介绍
- [后端 README](../../backend/suda-take-out/README.md) — 后端 API 详细说明
- [管理端 README](../project-suda-admin-vue3/README.md) — 商家后台管理端说明
- [Uni-app 官方文档](https://uniapp.dcloud.io/) — Uni-app 框架文档
- [微信小程序文档](https://developers.weixin.qq.com/miniprogram/dev/) — 微信官方文档
