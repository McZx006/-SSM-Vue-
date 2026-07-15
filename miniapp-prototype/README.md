# 微信小程序原型

该目录是个人记账看板的小程序化原型，使用原生微信小程序写法组织页面。它不是单独的新后端，而是复用当前 SSM 项目的 RESTful 接口，用于展示后续迁移为微信小程序或 uni-app 的可行路径。

## 页面结构

- `pages/login/index`：登录页，支持体验账号填充和后端登录接口。
- `pages/register/index`：注册页，支持用户名、邮箱、密码确认和密码强度提示。
- `pages/home/index`：首页，调用仪表板统计接口展示本月收入、支出、结余和预算状态。
- `pages/bill-form/index`：快速记账页，支持早餐、通勤、教材、兼职模板，并从后端加载分类。
- `pages/bills/index`：账单列表页，调用账单分页接口，支持本月、支出、收入、全部筛选。
- `pages/statistics/index`：统计分析页，调用分类占比和年度月度趋势接口。
- `pages/profile/index`：我的页面，展示用户信息、预算入口、分类入口、导出入口和退出登录。

## 后端接口

默认复用当前 SSM 后端接口，基础地址在 `utils/request.js` 中配置：

```js
const BASE_URL = 'http://localhost:8080/account-dashboard/api'
```

当前请求工具已经处理基础 Cookie 登录态：登录接口返回 `Set-Cookie` 后会保存到本地，后续请求会带上 `Cookie` 请求头。正式上线微信小程序时，更推荐把 Session 登录升级为 Token 登录。

首页、账单和统计等 Tab 页面会先调用 `ensureLogin()` 检查本地登录态；如果未登录或接口返回 401，会清理本地状态并跳转回登录页。

## 导入方式

1. 打开微信开发者工具。
2. 选择“导入项目”。
3. 项目目录选择 `miniapp-prototype`。
4. AppID 可先选择测试号。
5. 根据实际后端地址修改 `utils/request.js` 中的 `BASE_URL`。
6. 本地联调时，先启动后端 Tomcat，再打开小程序页面。

## 已有交互

- 登录页调用 `POST /api/auth/login`。
- 注册页调用 `POST /api/auth/register`。
- 首页调用 `GET /api/statistics/dashboard`。
- 记账页调用 `GET /api/categories` 和 `POST /api/bills`。
- 账单页调用 `GET /api/bills`。
- 统计页调用 `GET /api/statistics/category` 和 `GET /api/statistics/monthly`。
- 我的页面支持清理本地登录态并返回登录页。

## 后续完善方向

- 接入 `echarts-for-weixin`，把统计页的占比和趋势从简化条形展示升级为真实图表。
- 将 Session 登录改成 Token 登录，减少小程序 Cookie 兼容问题。
- 增加预算设置、分类维护、Excel 导出等页面的真实交互。
- 部署后端 HTTPS 域名，并在微信公众平台配置合法请求域名。
