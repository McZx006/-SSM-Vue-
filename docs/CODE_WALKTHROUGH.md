# 代码导读与阅读顺序

## 1. 推荐阅读顺序

如果是第一次看这个项目，建议按下面顺序阅读：

1. `README.md`：先知道项目怎么运行。
2. `docs/PROJECT_DELIVERY.md`：知道项目完成了哪些功能。
3. `docs/PROJECT_LOGIC.md`：理解整体数据流和业务流程。
4. `docs/API_REFERENCE.md`：理解接口和页面之间的对应关系。
5. `docs/DATABASE_DESIGN.md`：理解表结构和统计 SQL。
6. 后端 Controller、Service、Mapper。
7. Web 前端路由、请求封装、页面组件。
8. `miniapp-prototype` 小程序原型。

## 2. 后端代码导读

后端主包：

```text
src/main/java/com/zhangxin
```

### Controller

Controller 是接口入口，负责接收请求和返回 `Result`。

| 文件 | 作用 |
| --- | --- |
| `AuthController.java` | 登录、注册、退出、检查登录态 |
| `BillController.java` | 账单查询、新增、修改、删除、最近账单 |
| `CategoryController.java` | 分类查询、新增、修改、删除 |
| `StatisticsController.java` | 仪表板、分类统计、月度趋势、Excel 导出 |
| `UserController.java` | 用户资料、密码修改、用户管理 |
| `RegistryController.java` | 兼容 `/api/registry` 注册接口 |

阅读建议：

- 想看登录逻辑，从 `AuthController.login` 开始。
- 想看账单增删改查，从 `BillController` 开始。
- 想看图表数据，从 `StatisticsController.getDashboard`、`getCategoryStatistics`、`getMonthlyStatistics` 开始。

### Service

Service 是业务逻辑层。

| 文件 | 作用 |
| --- | --- |
| `UserService.java` | 登录校验、注册、修改密码 |
| `BillService.java` | 账单业务、预算、统计、Excel 导出 |
| `CategoryService.java` | 分类业务、系统分类和用户分类管理 |

阅读建议：

- 统计图表为空时，看 `BillService.getCategoryStatistics` 和 `BillService.getMonthlyStatistics`。
- 预算提醒异常时，看 `BillService.getBudgetUsage` 和 `BillService.checkBudgetExceeded`。
- Excel 导出格式问题，看 `BillService.exportBillsExcel`。

### Mapper

Mapper 接口在：

```text
src/main/java/com/zhangxin/mapper
```

SQL 文件在：

```text
src/main/resources/mapper
```

| 文件 | 作用 |
| --- | --- |
| `BillMapper.xml` | 账单查询、分页、按月求和、分类分组、月度趋势 |
| `CategoryMapper.xml` | 分类查询、新增、修改、删除 |
| `UserMapper.xml` | 用户查询、新增、修改、删除 |

阅读建议：

- 想理解 ECharts 数据怎么来的，重点看 `BillMapper.xml`。
- 想理解分类列表为什么包含系统分类和用户分类，看 `CategoryMapper.xml` 的 `findSystemAndUserCategories`。

### DTO 和 Entity

| 文件 | 作用 |
| --- | --- |
| `Result.java` | 统一响应结构，包含 `code`、`message`、`data` |
| `BillQuery.java` | 账单筛选条件 |
| `CategoryStatistics.java` | 分类统计结果 |
| `MonthlyStatistics.java` | 月度统计结果 |
| `User.java` | 用户实体 |
| `Category.java` | 分类实体 |
| `Bill.java` | 账单实体 |

### 拦截器

`AuthInterceptor.java` 负责保护 `/api/**` 接口。

逻辑：

```text
请求进入 /api/**
  -> 登录/注册接口放行
  -> OPTIONS 预检放行
  -> 检查 Session 中是否有 user
  -> 没有 user 返回 401 JSON
```

## 3. Web 前端代码导读

Web 前端目录：

```text
frontend/src
```

### 入口文件

| 文件 | 作用 |
| --- | --- |
| `main.js` | 创建 Vue 应用、注册 Pinia、Router、Element Plus |
| `App.vue` | 全局主题、深色模式、根路由出口 |
| `router/index.js` | 路由配置和登录守卫 |
| `stores/user.js` | 登录、注册、退出、恢复用户状态 |
| `utils/api.js` | Axios 封装、错误处理、401 跳转 |

阅读建议：

- 页面跳转问题看 `router/index.js`。
- 接口 401 或 500 提示看 `utils/api.js`。
- 登录状态丢失看 `stores/user.js`。
- 深色模式看 `App.vue` 和 `Settings.vue`。

### 页面文件

| 页面 | 文件 | 主要职责 |
| --- | --- | --- |
| 登录 | `views/Login.vue` | 登录表单、体验账号、记住账号 |
| 注册 | `views/Register.vue` | 注册表单、密码强度、校验 |
| 主布局 | `layouts/MainLayout.vue` | 顶部栏、侧边栏、移动端 Tab、全局快捷记账 |
| 仪表板 | `views/Dashboard.vue` | 统计卡片、预算、图表、最近账单、智能建议 |
| 账单管理 | `views/Bills.vue` | 查询、筛选、新增、编辑、删除、分页 |
| 统计分析 | `views/Statistics.vue` | 饼图、柱状图、折线图、趋势建议 |
| 系统设置 | `views/Settings.vue` | 个人资料、密码、分类、导出、偏好 |

### 工具文件

| 文件 | 作用 |
| --- | --- |
| `utils/category.js` | 把旧英文分类显示成中文 |
| `utils/advice.js` | 本地规则版智能消费建议 |
| `utils/api.js` | 统一接口请求和错误处理 |

## 4. 关键业务流程对应文件

### 登录流程

```text
Login.vue
  -> stores/user.js login()
  -> utils/api.js
  -> AuthController.login
  -> UserService.login
  -> UserMapper.findByUsername
  -> Session 保存 user
```

### 添加账单流程

```text
MainLayout.vue 或 Bills.vue
  -> POST /api/bills
  -> BillController.add
  -> BillService.add
  -> BillMapper.insert
  -> 页面触发 bill-created 刷新图表和列表
```

### 仪表板流程

```text
Dashboard.vue
  -> GET /api/statistics/dashboard
  -> StatisticsController.getDashboard
  -> BillService 获取月收入、月支出、分类统计、月度趋势、预算
  -> Dashboard.vue 渲染卡片、饼图、柱状图、智能建议
```

### 统计分析流程

```text
Statistics.vue
  -> GET /api/statistics/category
  -> GET /api/statistics/monthly
  -> ECharts 渲染饼图、排行柱状图、折线图
  -> utils/advice.js 生成趋势建议
```

### Excel 导出流程

```text
Settings.vue
  -> GET /api/statistics/export
  -> StatisticsController.exportBills
  -> BillService.exportBillsExcel
  -> Apache POI 生成 xlsx
  -> 前端 blob 下载
```

## 5. 小程序原型代码导读

目录：

```text
miniapp-prototype
```

| 文件或目录 | 作用 |
| --- | --- |
| `app.json` | 页面列表、窗口、TabBar 配置 |
| `app.wxss` | 小程序全局样式 |
| `utils/request.js` | `wx.request` 封装和后端地址 |
| `pages/login` | 登录页 |
| `pages/register` | 注册页 |
| `pages/home` | 首页总览和快捷模板 |
| `pages/bill-form` | 小程序记一笔 |
| `pages/bills` | 小程序账单列表和筛选 |
| `pages/statistics` | 小程序统计页原型 |
| `pages/profile` | 小程序我的页原型 |

小程序端目前是原型级别：

- 登录、注册、记账、账单列表已经写了接口调用。
- 统计页已接入分类统计和月度趋势接口，目前用轻量条形展示表达数据结构；后续可接入 `echarts-for-weixin` 升级为真实图表。
- 正式上线前建议后端从 Session 鉴权升级为 token 鉴权。

## 6. 常见修改位置

| 想改的内容 | 优先修改文件 |
| --- | --- |
| 新增一个后端接口 | Controller + Service + Mapper |
| 新增一个账单字段 | `bill` 表、`Bill.java`、`BillMapper.xml`、前端表单 |
| 改登录逻辑 | `AuthController.java`、`UserService.java`、`stores/user.js` |
| 改图表数据 | `BillMapper.xml`、`BillService.java`、`Dashboard.vue` 或 `Statistics.vue` |
| 改导出字段 | `BillService.exportBillsExcel` |
| 改分类显示 | `utils/category.js`、`CategoryMapper.xml` |
| 改深色模式 | `App.vue`、`Settings.vue` |
| 改小程序后端地址 | `miniapp-prototype/utils/request.js` |

## 7. 答辩讲解建议

讲项目时可以按这条主线：

1. 先讲需求：大学生日常收支记录，希望用图表理解消费。
2. 再讲架构：SSM 后端、Vue 前端、MySQL 数据库、小程序原型。
3. 讲数据库：用户、分类、账单三张核心表。
4. 讲接口：账单 CRUD、分类 CRUD、统计接口、导出接口。
5. 讲前端：登录注册、仪表板、账单管理、统计分析、系统设置。
6. 讲特色：快捷记账、深色模式、智能建议、Excel 导出、小程序原型。
7. 最后讲扩展：OCR、语音记账、真正 AI 分析、token 化小程序登录。
