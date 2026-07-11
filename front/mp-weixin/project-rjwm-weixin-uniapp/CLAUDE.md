# 小程序端 CLAUDE.md — project-rjwm-weixin-uniapp

## 技术栈
- Uni-app (Vue 2) + Vuex，HBuilderX 管理
- 编译为微信小程序，无 `package.json`
- SCSS + rpx 单位（750rpx 设计稿）
- 基础库 ≥ 2.20.1（支持 `wx.request` 的 `enableChunked`）

## 目录结构
project-rjwm-weixin-uniapp/
├── pages/
│   ├── api/api.js          # 所有 API 接口（含 AI 相关）
│   ├── index/              # 首页：店铺卡片 + 分类侧边栏 + 菜品卡片 + 购物车
│   ├── ai/index.vue        # AI 聊天页：SSE 流式对话 + 智能点餐（全屏，无自定义导航栏）
│   ├── my/my.vue           # 个人中心：用户信息卡 + 地址/订单入口 + 最近订单
│   ├── order/              # 确认订单 / 下单成功
│   ├── historyOrder/       # 历史订单（分页滚动加载）
│   ├── address/            # 地址列表（原生导航栏，自动返回按钮）
│   ├── addOrEditAddress/   # 新增/编辑地址
│   ├── nonet/              # 无网络提示
│   └── common/
│       └── simple-address/ # 三级地址选择器
├── components/
│   ├── app-loading/        # 全屏加载遮罩
│   ├── app-empty/          # 空状态插画
│   ├── app-skeleton/       # 骨架屏
│   └── dish-card/          # 菜品卡片
├── utils/
│   ├── env.js              # API baseUrl（localhost:8080）
│   ├── request.js          # HTTP 请求封装（JWT header）
│   └── stream.js           # SSE 流式请求（wx.request + enableChunked）
├── store/index.js          # Vuex（含 AI 会话状态）
├── pages.json              # 路由 + 3 TabBar 配置（原生导航栏，透明背景，空标题）
├── uni.scss                # 设计 Token（品牌色 #1a56db）
└── project.config.json     # 微信小程序配置

## 导航策略
所有页面使用**原生微信导航栏**，配置统一：
- `navigationBarTitleText`: `""`（空标题）
- `navigationBarBackgroundColor`: `#f3f4f7`（与页面背景一致，视觉上融为一体）
- `navigationBarTextStyle`: `black`（深色文字适配浅色背景）
- 子页面自动显示原生返回按钮，无需手动实现

## TabBar 导航（3 Tab）
| Tab | 页面 | 说明 |
|-----|------|------|
| 🏠 首页 | `pages/index/index` | 店铺卡片 + 分类 + 菜品 + 购物车 |
| 🤖 AI小速 | `pages/ai/index` | SSE 流式 AI 对话 + 智能点餐 |
| 👤 我的 | `pages/my/my` | 用户信息卡片 + 地址 + 历史订单 |

## API 接口文件结构
`pages/api/api.js` 包含以下接口：
| 分类 | 接口 | 说明 |
|------|------|------|
| 用户 | `userLogin` | 微信登录 |
| 分类 | `getCategoryList` | 获取分类列表（type: 1=菜品 2=套餐） |
| 菜品 | `dishListByCategoryId` | 按分类查询菜品 |
| 套餐 | `querySetmeaList` / `querySetmealDishById` | 套餐列表/详情 |
| 购物车 | `newAddShoppingCartAdd` / `newShoppingCartSub` / `getShoppingCartList` / `delShoppingCart` | 增/减/查/清空 |
| 地址 | `queryAddressBookList` / `addAddressBook` / `editAddressBook` / `deleteAddressBook` / `getDefaultAddress` / `setDefaultAddress` | 地址管理 |
| 订单 | `addOrder` / `queryOrderDetail` / `queryHistoryOrders` / `cancelOrder` / `queryOrderInProgress` | 订单管理 |
| AI | `aiChat` / `aiChatStream` / `getCartStatus` / `aiClearHistory` | AI 对话相关 |

## Vuex Store 结构
`store/index.js` 状态：
| 状态 | 类型 | 说明 |
|------|------|------|
| `storeInfo` | Object | 店铺请求的 id 信息 |
| `shopInfo` | String | 店铺详细信息 |
| `orderListData` | Array | 购物车列表信息 |
| `baseUserInfo` | Object | 用户微信信息（用户名、头像） |
| `token` | String | JWT token |
| `sessionId` | String | AI 会话 ID |
| `cartCount` | Number | 购物车实时数量（TabBar 角标） |
| `aiMessages` | Array | AI 聊天消息缓存 |
| `aiStreamingTask` | Object | AI 流式请求 task（用于中断） |

## 设计 Token（rpx）
| Token | 值 | 用途 |
|-------|-----|------|
| 品牌主色 | `#1a56db` | 导航栏、按钮、选中态 |
| 品牌深色 | `#1e40af` | 按钮按下态 |
| 品牌浅蓝 | `#eff6ff` / `#dbeafe` | 卡片选中背景、用户信息卡 |
| 强调色 | `#f59e0b` | 价格、标签 |
| 页面背景 | `#f3f4f7` | 全局底色、导航栏背景 |
| 卡片圆角 | `16rpx` | |
| 卡片阴影 | `0 4rpx 24rpx rgba(0,0,0,0.06)` | |

## SSE 流式请求（关键）
微信小程序通过 `wx.request({ enableChunked: true })` 实现 SSE，封装在 `utils/stream.js`。

```javascript
// 使用示例
import { streamRequest } from '@/utils/stream.js'
streamRequest('/user/ai/chat/stream', { message: '推荐几个菜' }, (chunk) => {
  // 逐字追加到对话气泡
})
```

## 微信开发者工具配置
文件：`project.config.json`
- AppID：需要配置真实的微信小程序 AppID
- 编译模式：默认编译到 `pages/index/index`
- 开发阶段：需开启「不校验合法域名」选项

## 常见陷阱
1. `mapState` 必须在 `computed` 里，不在 `methods` 里
2. 所有尺寸用 rpx，px 值 × 2 = rpx（如 16px → 32rpx）
3. HBuilderX 运行：必须用 HBuilderX 打开，不能直接用 npm
4. 微信开发者工具：开发阶段需开启「不校验合法域名」选项
5. JWT Header：用户端使用 `authentication` header，不是 `token`
6. 不支持 CSS `gap` 属性在部分旧版本微信上，可用 `margin` 替代
7. 页面顶部不使用自定义导航栏，由 `pages.json` 中的原生导航栏统一管理
