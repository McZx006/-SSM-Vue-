# REST API 对照说明

## 1. 通用约定

后端基础路径：

```text
http://localhost:8080/account-dashboard/api
```

Web 前端通过 Vite 代理访问：

```text
/api
```

统一 JSON 响应结构：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

当前鉴权方式：

- 登录成功后，后端把用户对象写入 `HttpSession`。
- 除 `/api/auth/**` 和 `/api/registry` 外，其它 `/api/**` 接口都需要登录。
- 未登录会返回 `401` 和中文错误信息。

## 2. 认证接口

| 功能 | 方法 | 路径 | 参数 | 前端调用 |
| --- | --- | --- | --- | --- |
| 登录 | `POST` | `/auth/login` | `username`、`password` | `frontend/src/stores/user.js`、`miniapp-prototype/pages/login/index.js` |
| 注册 | `POST` | `/auth/register` | `username`、`password`、`email`、`phone` | `frontend/src/stores/user.js`、`miniapp-prototype/pages/register/index.js` |
| 退出 | `POST` | `/auth/logout` | 无 | `frontend/src/stores/user.js` |
| 检查登录态 | `GET` | `/auth/check` | 无 | `frontend/src/stores/user.js` |

登录成功返回：

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "user": {},
    "token": "sessionId"
  }
}
```

说明：这里的 `token` 实际是 Session ID，用于前端保存本地状态；后端鉴权仍以 Session Cookie 为准。

## 3. 账单接口

| 功能 | 方法 | 路径 | 参数 | 前端调用 |
| --- | --- | --- | --- | --- |
| 查询账单列表 | `GET` | `/bills` | `type`、`categoryId`、`startDate`、`endDate`、`keyword`、`pageNum`、`pageSize` | `Bills.vue`、`miniapp-prototype/pages/bills/index.js` |
| 查询单个账单 | `GET` | `/bills/{id}` | 路径参数 `id` | 预留 |
| 添加账单 | `POST` | `/bills` | `amount`、`type`、`categoryId`、`date`、`remark` | `MainLayout.vue`、`Bills.vue`、`miniapp-prototype/pages/bill-form/index.js` |
| 更新账单 | `PUT` | `/bills/{id}` | 路径参数 `id` 和账单 JSON | `Bills.vue` |
| 删除账单 | `DELETE` | `/bills/{id}` | 路径参数 `id` | `Bills.vue` |
| 最近账单 | `GET` | `/bills/recent` | `limit` | `Dashboard.vue` |

账单类型：

```text
income  收入
expense 支出
```

列表返回的 `data` 包含：

```json
{
  "list": [],
  "total": 0,
  "pageNum": 1,
  "pageSize": 10
}
```

## 4. 分类接口

| 功能 | 方法 | 路径 | 参数 | 前端调用 |
| --- | --- | --- | --- | --- |
| 查询分类 | `GET` | `/categories` | 无 | `MainLayout.vue`、`Bills.vue`、`Settings.vue`、小程序记账页 |
| 按类型查询分类 | `GET` | `/categories/type/{type}` | `income` 或 `expense` | 预留 |
| 查询单个分类 | `GET` | `/categories/{id}` | 路径参数 `id` | 预留 |
| 新增分类 | `POST` | `/categories` | `name`、`type`、`color` | `Settings.vue` |
| 修改分类 | `PUT` | `/categories/{id}` | 路径参数 `id` 和分类 JSON | `Settings.vue` |
| 删除分类 | `DELETE` | `/categories/{id}` | 路径参数 `id` | `Settings.vue` |

分类逻辑：

- 系统分类 `userId` 为空。
- 用户自定义分类 `userId` 为当前登录用户 ID。
- 前端会把英文旧分类通过 `formatCategoryName` 显示成中文。

## 5. 统计接口

| 功能 | 方法 | 路径 | 参数 | 前端调用 |
| --- | --- | --- | --- | --- |
| 月度总额 | `GET` | `/statistics/total` | `month`、可选 `type` | 预留 |
| 分类占比 | `GET` | `/statistics/category` | `month`、`type` | `Statistics.vue` |
| 月度趋势 | `GET` | `/statistics/monthly` | `year` | `Statistics.vue`、`Dashboard.vue` |
| 仪表板数据 | `GET` | `/statistics/dashboard` | `month`、`year` | `Dashboard.vue` |
| Excel 导出 | `GET` | `/statistics/export` | `startDate`、`endDate` | `Settings.vue` |

`/statistics/dashboard` 返回重点字段：

```json
{
  "monthIncome": 0,
  "monthExpense": 0,
  "monthBalance": 0,
  "yearIncome": 0,
  "yearExpense": 0,
  "yearBalance": 0,
  "categoryStatistics": [],
  "monthlyStatistics": [],
  "budgetUsage": 0,
  "budgetExceeded": false
}
```

Excel 导出返回 `.xlsx` 文件流，不是普通 JSON。

## 6. 用户接口

| 功能 | 方法 | 路径 | 参数 | 前端调用 |
| --- | --- | --- | --- | --- |
| 查询用户列表 | `GET` | `/users` | 无 | 预留/管理功能 |
| 查询单个用户 | `GET` | `/users/{id}` | 路径参数 `id` | 预留 |
| 当前用户 | `GET` | `/users/current` | 无 | 预留 |
| 修改用户 | `PUT` | `/users/{id}` | `email`、`phone`、`budget` 等 | `Settings.vue` |
| 修改密码 | `POST` | `/users/{id}/change-password` | `oldPassword`、`newPassword` | `Settings.vue` |
| 删除用户 | `DELETE` | `/users/{id}` | 路径参数 `id` | 预留/管理功能 |

## 7. 兼容注册接口

项目要求中包含：

```text
POST /api/registry
```

当前通过 `RegistryController` 保留该接口，逻辑与 `/api/auth/register` 一致。

## 8. 前端页面和接口关系

| 页面 | 主要接口 |
| --- | --- |
| 登录页 | `/auth/login` |
| 注册页 | `/auth/register` |
| 主布局快捷记账 | `/categories`、`/bills` |
| 仪表板 | `/statistics/dashboard`、`/bills/recent` |
| 账单管理 | `/bills`、`/categories` |
| 统计分析 | `/statistics/category`、`/statistics/monthly` |
| 系统设置 | `/users/{id}`、`/users/{id}/change-password`、`/categories`、`/statistics/export` |
| 小程序登录 | `/auth/login` |
| 小程序注册 | `/auth/register` |
| 小程序记一笔 | `/categories`、`/bills` |
| 小程序账单 | `/bills` |

## 9. 常见排查顺序

### 401 未登录

1. 先确认是否已经登录。
2. Web 端确认 Axios 是否 `withCredentials: true`。
3. 后端确认 Session 中是否有 `user`。
4. 小程序端如果 Cookie 不稳定，后续建议改 token 鉴权。

### 500 服务器错误

1. 看 Tomcat 控制台异常。
2. 检查请求参数是否为空或格式错误。
3. 检查 MyBatis XML 中 SQL 字段是否和数据库一致。
4. 检查日期格式是否为 `YYYY-MM-DD` 或 `YYYY-MM`。

### 图表为空

1. 确认对应月份/年份有账单数据。
2. 确认分类类型是 `expense` 或 `income`。
3. 确认接口返回 `categoryStatistics` 或 `monthlyStatistics`。
4. 确认 ECharts 容器高度不为 0。
