# SUDAWAIMAI

苏大外卖 - 校园外卖管理平台

## 项目结构

```
├── backend/                          # 后端
│   └── suda-take-out/                # Spring Boot 外卖系统
│       ├── suda-common/              # 公共模块
│       ├── suda-pojo/                # 实体类
│       ├── suda-server/              # 服务端
│       └── sky-take-out/             # 另一个后端版本
├── front/                            # 前端
│   ├── project-suda-admin-vue-ts/    # 管理后台 (Vue3 + TypeScript)
│   ├── mp-weixin/                    # 微信小程序 (uni-app)
│   └── frontEnv/                     # Nginx 配置 & 部署环境
├── test/                             # 测试/练习项目
└── .github/                          # GitHub 配置
```

## 技术栈

| 模块 | 技术 |
|------|------|
| 后端 | Java, Spring Boot, MyBatis, JWT, WebSocket |
| 管理后台 | Vue 3, TypeScript, Element Plus, ECharts |
| 微信小程序 | uni-app, Vue |
| 部署 | Nginx, Docker |

## 功能

- 菜品/套餐管理
- 订单管理（接单、配送、完成、取消）
- 员工管理
- 数据统计（营业额、用户量、订单量、Top10）
- 微信小程序端用户点餐
- WebSocket 实时订单通知
