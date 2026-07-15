# 微信小程序化改造方案

## 改造目标

当前项目已经具备 SSM 后端、RESTful 接口、登录态、账单管理、分类管理、统计分析和 Excel 导出能力。小程序化改造的重点不是重写业务，而是把 Vue Web 前端替换或扩展为微信小程序端，让用户可以在手机上完成快速记账、看图表和导出数据。

## 推荐技术路线

### 路线 A：uni-app

适合想快速复用 Vue 语法的场景。

- 页面开发仍使用 Vue 风格。
- 可以编译为微信小程序。
- 请求层封装 `uni.request`，对接现有 `/api` 接口。
- 图表可以使用 ECharts 小程序适配版。

### 路线 B：微信原生小程序

适合想完整贴近微信生态的场景。

- 使用 WXML、WXSS、JavaScript。
- 使用 `wx.request` 调用后端。
- 登录态通过 Cookie 或 token 方案管理。
- 图表使用 `echarts-for-weixin`。

## 页面映射

| 当前 Vue 页面 | 小程序页面 | 主要功能 |
| --- | --- | --- |
| `Login.vue` | `pages/login/index` | 登录、注册入口、体验账号 |
| `Register.vue` | `pages/register/index` | 注册、表单校验、密码强度 |
| `Dashboard.vue` | `pages/home/index` | 首页总览、预算提醒、快捷记账、图表 |
| `Bills.vue` | `pages/bills/index` | 账单列表、筛选、新增、编辑、删除 |
| `Statistics.vue` | `pages/statistics/index` | 饼图、柱状图、折线图、消费洞察 |
| `Settings.vue` | `pages/profile/index` | 个人信息、预算、分类、导出 |

## 小程序 TabBar 建议

- 首页：总览、预算、快捷记账。
- 账单：账单列表、筛选、编辑。
- 分析：图表、排行、趋势。
- 我的：个人资料、分类、导出、偏好。

## 后端接口复用

小程序端可以直接复用当前接口：

- `POST /api/auth/login`
- `POST /api/auth/register`
- `POST /api/auth/logout`
- `GET /api/auth/check`
- `GET /api/bills`
- `POST /api/bills`
- `PUT /api/bills/{id}`
- `DELETE /api/bills/{id}`
- `GET /api/bills/recent`
- `GET /api/categories`
- `POST /api/categories`
- `PUT /api/categories/{id}`
- `DELETE /api/categories/{id}`
- `GET /api/statistics/dashboard`
- `GET /api/statistics/category`
- `GET /api/statistics/monthly`
- `GET /api/statistics/export`

## 登录态建议

如果继续使用浏览器 Cookie：

- 后端保持 Session 登录态。
- 小程序请求需要带上 Cookie。
- 开发时要处理跨域、域名白名单和 HTTPS。

如果改成 token：

- 登录接口返回 token。
- 小程序保存 token 到 `wx.setStorageSync`。
- 每次请求通过 `Authorization` 请求头携带 token。
- 后端拦截器从请求头解析用户身份。

课程设计阶段建议继续使用现有 Session 方案；如果要正式发布微信小程序，建议改成 token。

## 小程序交互优化

- 首页提供醒目的“记一笔”浮动按钮。
- 金额输入使用数字键盘。
- 分类选择使用九宫格或横向图标列表。
- 日期默认当天，允许快速选择昨天、本周、本月。
- 预算超支时用轻提醒，不阻塞用户操作。
- 图表加载失败时展示空状态和重试按钮。

## 分阶段实施

1. 保留当前 SSM 后端，稳定接口和数据库。
2. 新建 `miniapp` 或 `uniapp` 前端目录。
3. 先实现登录、首页、账单新增三个最小闭环页面。
4. 接入分类管理和统计图表。
5. 最后补导出、预算提醒、AI 分析等增强功能。

## 当前原型

项目已新增 `miniapp-prototype` 目录，包含微信小程序原生页面骨架：

- 首页 `pages/home/index`
- 登录 `pages/login/index`
- 注册 `pages/register/index`
- 记一笔 `pages/bill-form/index`
- 账单 `pages/bills/index`
- 分析 `pages/statistics/index`
- 我的 `pages/profile/index`

该原型目前用于展示小程序化界面结构，其中登录、注册、首页摘要、快捷记账、账单列表和统计分析页已经包含基础表单校验与接口调用，后续可以在微信开发者工具中继续接入 ECharts 小程序组件和更多设置类交互。
