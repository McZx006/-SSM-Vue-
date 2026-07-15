# 需求追踪与验收矩阵

## 使用方式

本文档把课程要求、当前代码位置、验证方式放在同一张表中，方便开发、验收和答辩时快速说明“功能在哪里实现、如何证明它已经完成”。

## 核心功能追踪

| 需求 | 当前实现 | 关键文件 | 验证方式 |
| --- | --- | --- | --- |
| 用户登录 | 支持用户名密码登录，登录成功后保存 Session，前端保存用户信息 | `src/main/java/com/zhangxin/controller/AuthController.java`、`frontend/src/stores/user.js`、`frontend/src/views/Login.vue` | 使用 `testuser / 123456` 登录，进入仪表板 |
| 用户注册 | 支持注册账号，校验用户名和密码长度 | `src/main/java/com/zhangxin/controller/AuthController.java`、`src/main/java/com/zhangxin/controller/RegistryController.java`、`frontend/src/views/Register.vue` | 打开注册页，新建账号后返回登录 |
| 登录状态校验 | 需要登录的接口由拦截器保护，未登录返回 401 JSON | `src/main/java/com/zhangxin/interceptor/AuthInterceptor.java`、`frontend/src/utils/api.js` | 未登录访问 `/api/bills` 返回 401，前端跳转登录 |
| 账单查询 | 支持分页、类型、日期、关键词等条件查询 | `src/main/java/com/zhangxin/controller/BillController.java`、`src/main/resources/mapper/BillMapper.xml`、`frontend/src/views/Bills.vue` | 进入账单管理页，切换本月、收入、支出筛选 |
| 账单新增 | 支持金额、类型、分类、日期、备注保存 | `src/main/java/com/zhangxin/service/BillService.java`、`frontend/src/views/Bills.vue`、`frontend/src/layouts/MainLayout.vue` | 点击“记一笔”或账单页新增，保存后列表刷新 |
| 账单修改 | 支持编辑已有账单 | `src/main/java/com/zhangxin/controller/BillController.java`、`frontend/src/views/Bills.vue` | 在账单列表点击编辑并保存 |
| 账单删除 | 支持删除已有账单 | `src/main/java/com/zhangxin/controller/BillController.java`、`frontend/src/views/Bills.vue` | 在账单列表点击删除并确认 |
| 分类管理 | 支持系统分类展示和用户自定义分类维护 | `src/main/java/com/zhangxin/controller/CategoryController.java`、`src/main/java/com/zhangxin/service/CategoryService.java`、`frontend/src/views/Settings.vue` | 系统设置页新增、编辑、删除自定义分类 |
| 支出分类占比饼图 | 使用 ECharts 展示分类占比 | `frontend/src/views/Dashboard.vue`、`frontend/src/views/Statistics.vue`、`src/main/resources/mapper/BillMapper.xml` | 登录后仪表板和统计页出现分类占比图 |
| 月度支出趋势折线图 | 使用 ECharts 展示年度月份趋势 | `frontend/src/views/Statistics.vue`、`src/main/java/com/zhangxin/controller/StatisticsController.java` | 统计分析页选择年份，查看月度趋势 |
| 月度收支对比柱状图 | 使用 ECharts 展示收入和支出对比 | `frontend/src/views/Dashboard.vue`、`frontend/src/views/Statistics.vue` | 仪表板和统计页查看收支对比图 |
| 按时间统计查询 | 支持月份、年份、日期范围筛选 | `src/main/java/com/zhangxin/controller/StatisticsController.java`、`frontend/src/views/Dashboard.vue`、`frontend/src/views/Statistics.vue` | 切换月份或年份后图表刷新 |
| Excel 导出 | 支持按日期范围导出 `.xlsx` | `src/main/java/com/zhangxin/controller/StatisticsController.java`、`src/main/java/com/zhangxin/service/BillService.java`、`frontend/src/views/Settings.vue` | 系统设置页选择日期范围并导出 Excel |
| 预算提醒 | 展示预算使用率和超支状态 | `src/main/java/com/zhangxin/service/BillService.java`、`frontend/src/views/Dashboard.vue`、`frontend/src/views/Settings.vue` | 首页查看预算进度，设置页修改预算 |
| 智能建议 | 根据预算、分类占比、年度趋势生成本地规则建议 | `frontend/src/utils/advice.js`、`frontend/src/views/Dashboard.vue`、`frontend/src/views/Statistics.vue` | 首页和统计页查看消费建议 |
| 深色模式 | 支持前端主题切换和本地保存 | `frontend/src/App.vue`、`frontend/src/views/Settings.vue` | 系统设置页开启深色模式 |
| 移动端体验 | 小屏使用底部 Tab，保留浮动记账入口 | `frontend/src/layouts/MainLayout.vue` | 缩小浏览器宽度，侧栏切换为底部导航 |
| 微信小程序原型 | 提供原生小程序页面，复用后端接口 | `miniapp-prototype` | 微信开发者工具导入 `miniapp-prototype` |

## 后端接口追踪

| 接口 | 作用 | 关键实现 |
| --- | --- | --- |
| `POST /api/auth/login` | 用户登录 | `AuthController.login` |
| `POST /api/auth/register` | 用户注册 | `AuthController.register` |
| `POST /api/registry` | 兼容课程要求的注册接口 | `RegistryController.registry` |
| `GET /api/auth/check` | 检查登录状态 | `AuthController.checkLogin` |
| `GET /api/users` | 查询用户列表 | `UserController.getAllUsers` |
| `GET /api/users/{id}` | 查询单个用户 | `UserController.getUserById` |
| `PUT /api/users/{id}` | 修改用户信息 | `UserController.updateUser` |
| `GET /api/bills` | 查询账单列表 | `BillController.getBills` |
| `GET /api/bills/recent` | 查询最近账单 | `BillController.getRecentBills` |
| `POST /api/bills` | 添加账单 | `BillController.addBill` |
| `PUT /api/bills/{id}` | 更新账单 | `BillController.updateBill` |
| `DELETE /api/bills/{id}` | 删除账单 | `BillController.deleteBill` |
| `GET /api/categories` | 查询分类 | `CategoryController.getUserCategories` |
| `POST /api/categories` | 添加分类 | `CategoryController.addCategory` |
| `PUT /api/categories/{id}` | 更新分类 | `CategoryController.updateCategory` |
| `DELETE /api/categories/{id}` | 删除分类 | `CategoryController.deleteCategory` |
| `GET /api/statistics/total` | 月度总额统计 | `StatisticsController.getTotalByMonth` |
| `GET /api/statistics/category` | 分类占比统计 | `StatisticsController.getCategoryStatistics` |
| `GET /api/statistics/monthly` | 年度月度趋势统计 | `StatisticsController.getMonthlyStatistics` |
| `GET /api/statistics/dashboard` | 仪表板综合数据 | `StatisticsController.getDashboard` |
| `GET /api/statistics/export` | Excel 导出 | `StatisticsController.exportBills` |

## 数据库追踪

| 表 | 作用 | 说明 |
| --- | --- | --- |
| `user` | 用户表 | 保存用户名、密码、邮箱、预算等信息 |
| `category` | 分类表 | 保存收入/支出分类，区分系统分类和用户分类 |
| `bill` | 账单表 | 保存金额、类型、分类、日期、备注、用户 id |

完整建表与演示数据见 `src/main/resources/sql/schema.sql`。旧数据库中文化补丁见 `src/main/resources/sql/localize_demo_data.sql`。

## 小程序原型追踪

| 页面 | 作用 | 接口 |
| --- | --- | --- |
| `pages/login/index` | 登录 | `POST /api/auth/login` |
| `pages/register/index` | 注册 | `POST /api/auth/register` |
| `pages/home/index` | 首页摘要 | `GET /api/statistics/dashboard` |
| `pages/bill-form/index` | 快速记账 | `GET /api/categories`、`POST /api/bills` |
| `pages/bills/index` | 账单列表 | `GET /api/bills` |
| `pages/statistics/index` | 统计分析 | `GET /api/statistics/category`、`GET /api/statistics/monthly` |
| `pages/profile/index` | 我的页面 | 本地登录态、后续可扩展用户设置接口 |

## 当前验证命令

推荐直接运行项目内置验收脚本：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/verify-project.ps1
```

脚本会依次检查小程序 JavaScript 语法、前端生产构建、后端 Maven 打包和本地入口可达性。

后端 Tomcat 启动并部署完成后，可以继续运行接口冒烟测试：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/api-smoke-test.ps1
```

该脚本会使用 `testuser / 123456` 登录，并依次验证登录状态、分类、账单、仪表板统计、月度统计和分类统计接口。

也可以分开执行以下命令：

```powershell
cd frontend
cmd /c npm run build
```

```powershell
& "D:\IDEA\IntelliJ IDEA 2024.3.5\plugins\maven\lib\maven3\bin\mvn.cmd" clean package -DskipTests
```

```powershell
Get-ChildItem -Path miniapp-prototype -Recurse -Filter *.js | ForEach-Object { node --check $_.FullName }
```

## 验收结论

当前项目已经覆盖课程要求中的收支管理、分类管理、用户注册登录、统计接口、ECharts 图表、MySQL 数据库、RESTful 接口、Excel 导出和运行文档，并额外提供移动端体验优化与微信小程序原型。
