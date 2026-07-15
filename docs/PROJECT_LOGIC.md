# 项目逻辑说明

## 1. 总体架构

项目采用“后端 SSM + 前端 Vue + 小程序原型”的结构：

- 后端：Spring、SpringMVC、MyBatis，负责登录注册、账单、分类、统计、导出等 RESTful 接口。
- Web 前端：Vue 3、Pinia、Axios、Element Plus、ECharts，负责页面展示、表单交互、图表渲染。
- 小程序原型：`miniapp-prototype`，用于展示微信小程序端页面结构和接口复用方式。
- 数据库：MySQL，核心表为 `user`、`category`、`bill`。

数据库字段、外键关系和统计 SQL 的详细说明见 [DATABASE_DESIGN.md](D:/桌面文件/我的文件/课程代码/软件开发框架/ssmown/docs/DATABASE_DESIGN.md)。

核心数据流：

```text
用户操作页面
  -> Axios / wx.request
  -> SpringMVC Controller
  -> Service 业务逻辑
  -> MyBatis Mapper
  -> MySQL
  -> Result JSON 返回前端
  -> Vue / 小程序页面刷新数据和图表
```

## 2. 后端分层逻辑

### Controller 层

Controller 负责接收 HTTP 请求、读取参数、取当前登录用户、调用 Service，并返回统一 `Result`。

- `AuthController`：登录、注册、退出、检查登录态。
- `BillController`：账单列表、新增、修改、删除、最近账单。
- `CategoryController`：分类列表、新增、修改、删除。
- `StatisticsController`：总额、分类占比、月度趋势、仪表板数据、Excel 导出。
- `UserController`：用户查询、资料修改、密码修改。
- `RegistryController`：兼容项目要求中的 `/api/registry` 注册接口。

### Service 层

Service 负责业务规则和数据加工：

- `UserService`：密码 MD5 加密校验、注册查重、修改密码。
- `BillService`：账单 CRUD、预算使用率、统计数据、Excel 文件生成。
- `CategoryService`：系统分类和用户分类合并展示，限制系统分类不能被普通删除。

### Mapper 层

Mapper 负责 SQL：

- `BillMapper.xml`：账单查询、按月求和、按分类分组、按月统计收支。
- `CategoryMapper.xml`：分类 CRUD 和类型筛选。
- `UserMapper.xml`：用户 CRUD、按用户名查询。

## 3. 登录和权限逻辑

当前后端鉴权以 `HttpSession` 为准：

1. 用户调用 `POST /api/auth/login`。
2. 后端校验用户名和密码。
3. 登录成功后，把 `user` 放入 Session。
4. 后续访问 `/api/**` 时，`AuthInterceptor` 检查 Session 中是否有 `user`。
5. 如果没有登录，返回 401 JSON：`未登录或登录已过期`。

前端 `user.js` 里也保存了 `token` 和 `user` 到 `localStorage`，其中 `user` 用于前端路由守卫和页面展示；当前后端实际不是用 token 鉴权，而是依赖 Cookie + Session。也就是说：

- Web 端请求要保持 `withCredentials: true`。
- Vite 代理要转发到 `/account-dashboard`。
- 小程序正式发布前建议把 Session 方案升级为 token 方案，因为小程序跨域和 Cookie 管理比浏览器更复杂。

## 4. Web 前端页面逻辑

### 路由

`frontend/src/router/index.js` 定义页面：

- `/login`：登录页。
- `/register`：注册页。
- `/`：主布局，包含仪表板。
- `/bills`：账单管理。
- `/statistics`：统计分析。
- `/settings`：系统设置。

路由守卫逻辑：

```text
访问需要登录的页面
  -> Pinia 中没有 user
  -> 从 localStorage 恢复 user
  -> 仍未登录则跳转 /login
```

### 请求封装

`frontend/src/utils/api.js` 使用 Axios：

- `baseURL: /api`
- `withCredentials: true`
- 401 自动提示登录过期并跳转登录页。
- 500、404、403 等状态统一显示中文错误。

### 页面职责

- `Login.vue`：登录、体验账号、记住账号。
- `Register.vue`：注册、密码强度、表单校验。
- `MainLayout.vue`：顶部用户区、侧边导航、移动端底部 Tab、全局快捷记账弹窗。
- `Dashboard.vue`：仪表板统计卡片、预算进度、饼图、柱状图、最近账单、智能建议。
- `Bills.vue`：账单筛选、分页、增删改查、当前页合计。
- `Statistics.vue`：分类占比、分类排行、月度趋势、智能分析建议。
- `Settings.vue`：个人资料、密码、分类管理、Excel 导出、紧凑模式、深色模式。

## 5. 快捷记账逻辑

快捷记账入口在 `MainLayout.vue` 中，全局可用。

流程：

```text
点击右下角“记一笔”
  -> 打开快捷记账弹窗
  -> 可点击早餐/通勤/教材/兼职模板
  -> 自动填充类型、金额、分类、备注
  -> 保存时调用 POST /api/bills
  -> 保存成功后触发 bill-created 全局事件
  -> Dashboard / Bills / Statistics 监听事件并重新加载数据
```

这样用户在任意页面都能快速记账，符合小程序高频操作习惯。

## 6. 统计图表逻辑

后端统计接口：

- `GET /api/statistics/category?month=YYYY-MM&type=expense`
- `GET /api/statistics/monthly?year=YYYY`
- `GET /api/statistics/dashboard?month=YYYY-MM&year=YYYY`

前端使用 ECharts：

- 仪表板饼图：显示支出分类占比。
- 仪表板柱状图：显示年度月度收入和支出。
- 统计页饼图：显示分类占比。
- 统计页横向柱状图：显示分类排行。
- 统计页折线图：显示收入、支出、结余趋势。

前端会把缺失月份补成 0，保证折线图和柱状图始终显示完整 12 个月。

## 7. 智能建议逻辑

`frontend/src/utils/advice.js` 是本地规则版智能分析，不依赖外部 AI 服务。

首页建议依据：

- 预算使用率。
- 本月收入、支出、结余。
- 支出最高的分类。

统计页建议依据：

- 分类排行最高项。
- 年度支出峰值月份。
- 年度累计结余。

它的作用是作为“AI 分析账单及建议”的课程原型，后续可以替换为真正的大模型接口。

## 8. Excel 导出逻辑

入口在 `Settings.vue` 的“数据导出”区域。

流程：

```text
选择开始日期和结束日期
  -> GET /api/statistics/export
  -> 后端 BillService 使用 Apache POI 生成 xlsx
  -> 前端接收 blob
  -> 创建 a 标签下载文件
```

导出文件类型为：

```text
application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
```

## 9. 小程序原型逻辑

`miniapp-prototype` 是原生微信小程序骨架，复用当前后端接口。

页面：

- `pages/login/index`：登录，调用 `/auth/login`。
- `pages/register/index`：注册，调用 `/auth/register`。
- `pages/home/index`：首页总览和快捷模板。
- `pages/bill-form/index`：记一笔，调用 `/categories` 和 `/bills`。
- `pages/bills/index`：账单列表，调用 `/bills` 并支持筛选。
- `pages/statistics/index`：统计分析页，调用分类统计和月度趋势接口，展示分类占比、趋势和智能建议。
- `pages/profile/index`：我的页面。

请求封装：

```text
miniapp-prototype/utils/request.js
BASE_URL = http://localhost:8080/account-dashboard/api
```

注意：微信小程序正式上线需要 HTTPS 域名，并在微信公众平台配置 request 合法域名。

## 10. 代码维护重点

- 如果后端改 context path，必须同步修改 Vite 代理和小程序 `BASE_URL`。
- 如果鉴权从 Session 改成 token，需要同步修改 `AuthInterceptor`、`api.js`、小程序 `request.js`。
- 如果新增账单字段，需要同步修改 `Bill.java`、`BillMapper.xml`、表单页面和导出逻辑。
- 如果新增统计图，需要优先在 MyBatis 写聚合 SQL，再由前端图表消费接口结果。
- 如果出现 401，优先检查是否登录、Cookie 是否携带、Session 是否存在。
- 如果出现 500，优先看 Tomcat 控制台异常和 MyBatis SQL 参数。
