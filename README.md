# 个人记账看板

基于 SSM（Spring + SpringMVC + MyBatis）+ Vue 3 + ECharts 的个人记账系统，支持账单管理、分类管理、统计分析、预算提醒和数据可视化。

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Vue 3](https://img.shields.io/badge/Vue-3.x-green.svg)](https://vuejs.org/)
[![Spring MVC](https://img.shields.io/badge/Spring-MVC-brightgreen.svg)](https://spring.io/)
[![ECharts](https://img.shields.io/badge/ECharts-5.x-blue.svg)](https://echarts.apache.org/)

## 📋 目录

- [项目概述](#项目概述)
- [功能特性](#功能特性)
- [适用用户](#适用用户)
- [技术架构](#技术架构)
- [涉及原理](#涉及原理)
- [运行环境](#运行环境)
- [快速开始](#快速开始)
- [项目结构](#项目结构)
- [API 接口](#api-接口)
- [部署指南](#部署指南)
- [常见问题](#常见问题)

---

## 项目概述

个人记账看板是一款面向校园学生和个人用户的轻量级财务管理工具。通过简洁直观的界面设计，帮助用户轻松记录日常收支、分析消费习惯、制定预算计划，实现对个人财务的精细化管理。

**核心价值**：让每一笔收支都有迹可循，通过数据可视化洞察消费趋势，培养健康的理财习惯。

---

## 功能特性

###  用户认证模块

- **登录功能**：支持用户名密码登录，提供测试账号快捷入口
- **注册功能**：支持新用户注册，包含表单验证和密码强度检测
- **会话管理**：基于 Session 的身份认证，自动保持登录状态
- **权限控制**：拦截器实现接口级别的访问控制

###  仪表板模块

- **预算进度**：实时显示月度预算使用情况
- **快捷记账**：一键快速录入收支记录
- **分类占比**：饼图展示各类别支出占比
- **月度趋势**：柱状图展示月度收支变化

###  账单管理模块

- **账单列表**：分页展示所有账单记录
- **筛选功能**：支持按时间、类型、分类筛选
- **添加账单**：完整的记账表单，支持金额、分类、日期、备注
- **编辑删除**：支持账单的修改和删除操作
- **金额合计**：实时计算当前筛选结果的收支总额

###  统计分析模块

- **分类占比图**：环形图展示各分类消费占比
- **分类排行榜**：按金额排序展示各类别消费情况
- **月度趋势图**：折线图展示月度收支趋势
- **消费洞察**：自动生成消费分析报告和建议

###  系统设置模块

- **个人资料**：查看和修改用户信息
- **预算设置**：设置各类别月度预算
- **分类管理**：自定义收支分类
- **数据导出**：支持 Excel 格式导出账单数据

---

## 适用用户

###  校园学生

- 生活费有限，需要合理规划支出
- 希望了解消费习惯，控制不必要开支
- 需要记录日常消费，培养理财意识

###  初入职场者

- 开始独立管理财务，需要记账工具
- 希望通过数据分析优化消费结构
- 需要简单易用的个人财务管理方案

###  课程学习者

- 学习 SSM 框架和前后端分离开发
- 了解企业级项目架构设计
- 需要完整的项目案例进行实践

###  开发者

- 学习 Vue 3 + Vite 现代前端技术栈
- 了解 Spring MVC + MyBatis 后端开发
- 参考前后端联调、API 设计、权限控制等实践

---

## 技术架构

### 架构设计

采用经典的 **前后端分离** 架构模式：

```
┌─────────────────────────────────────────────────────────────┐
│                      前端应用层                              │
│  Vue 3 + Vite + Element Plus + ECharts + Vue Router + Pinia │
└───────────────────────────┬─────────────────────────────────┘
                            │ HTTP/JSON
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                      后端服务层                              │
│  Spring MVC + MyBatis + MySQL + Tomcat                      │
├──────────────┬──────────────┬──────────────┬───────────────┤
│  Controller  │   Service    │   Mapper     │   Entity      │
│  (控制层)    │   (业务层)    │   (数据层)    │   (实体层)    │
└──────────────┴──────────────┴──────────────┴───────────────┘
                            │ JDBC
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                      数据存储层                              │
│                    MySQL 8.x 数据库                          │
└─────────────────────────────────────────────────────────────┘
```

### 前端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.4+ | 前端框架，组件化开发 |
| Vite | 5.0+ | 构建工具，快速开发和构建 |
| Vue Router | 4.2+ | 路由管理，页面导航 |
| Pinia | 2.1+ | 状态管理，全局数据共享 |
| Element Plus | 2.4+ | UI 组件库 |
| ECharts | 5.4+ | 数据可视化图表 |
| Axios | 1.6+ | HTTP 请求库 |
| Day.js | 1.11+ | 日期处理工具 |

### 后端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Framework | 6.1+ | 核心框架 |
| Spring MVC | 6.1+ | Web 框架，请求处理 |
| MyBatis | 3.5+ | ORM 框架，数据库操作 |
| MySQL | 8.0+ | 关系型数据库 |
| Tomcat | 11.0+ | Web 服务器 |
| Maven | 3.9+ | 依赖管理和构建工具 |
| Jackson | 2.15+ | JSON 序列化/反序列化 |

---

## 涉及原理

### 1. 前后端分离原理

- **解耦设计**：前端和后端独立开发、部署、维护
- **接口契约**：通过 RESTful API 定义接口规范
- **跨域处理**：配置 CORS 允许前端跨域请求
- **数据交互**：使用 JSON 格式进行数据传输

### 2. 身份认证原理

- **Session 机制**：服务端维护用户登录状态
- **Cookie 传递**：通过 HttpOnly Cookie 传递 Session ID
- **拦截器模式**：Spring MVC Interceptor 实现权限校验
- **状态管理**：前端 Pinia Store 管理用户信息

### 3. 路由原理

- **前端路由**：Vue Router 实现单页应用路由
- **History Mode**：使用 hash 模式适配静态部署
- **导航守卫**：全局前置守卫实现权限控制
- **懒加载**：组件按需加载优化首屏性能

### 4. 数据可视化原理

- **ECharts 渲染**：基于 Canvas/SVG 的图表绑定
- **数据聚合**：后端 SQL 统计和前端数据处理
- **响应式更新**：Vue 响应式数据驱动图表更新
- **交互事件**：图表点击、悬浮等交互效果

### 5. 构建优化原理

- **代码分割**：Vite Rollup 配置按需加载
- **依赖分包**：第三方库单独打包（echarts、element-plus、vue）
- **压缩优化**：生产环境自动压缩 CSS/JS
- **资源哈希**：构建产物添加哈希值实现缓存策略

---

## 运行环境

- **JDK**：17+
- **Maven**：3.9+
- **Node.js**：18+
- **MySQL**：8.x
- **Tomcat**：11.0+

---

## 快速开始

### 数据库准备

```sql
CREATE DATABASE IF NOT EXISTS account_dashboard
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

SOURCE src/main/resources/sql/schema.sql;
```

### 后端启动

```bash
# 使用 Maven 构建
mvn clean package -DskipTests

# 部署到 Tomcat 或使用 IDE 直接运行
```

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

### 访问地址

- 前端：`http://localhost:3000`
- 后端：`http://localhost:8080/account-dashboard`

### 测试账号

- 用户名：`testuser`
- 密码：`123456`

---

## 项目结构

```
ssmown/
├── frontend/                    # 前端项目
│   ├── src/
│   │   ├── views/              # 页面组件
│   │   ├── layouts/            # 布局组件
│   │   ├── router/             # 路由配置
│   │   ├── stores/             # 状态管理
│   │   ├── utils/              # 工具函数
│   │   ├── App.vue             # 根组件
│   │   └── main.js             # 入口文件
│   ├── index.html              # HTML 模板
│   ├── package.json            # 依赖配置
│   └── vite.config.js          # Vite 配置
├── src/main/                   # 后端项目
│   ├── java/com/zhangxin/
│   │   ├── controller/         # 控制器层
│   │   ├── service/            # 业务逻辑层
│   │   ├── mapper/             # 数据访问层
│   │   ├── entity/             # 实体类
│   │   ├── dto/                # 数据传输对象
│   │   └── interceptor/        # 拦截器
│   └── resources/
│       ├── mapper/             # MyBatis 映射文件
│       ├── sql/                # SQL 脚本
│       ├── applicationContext.xml  # Spring 配置
│       ├── spring-mvc.xml      # Spring MVC 配置
│       └── database.properties # 数据库配置
├── docs/                       # 项目文档
├── miniapp-prototype/          # 微信小程序原型
└── pom.xml                     # Maven 配置
```

---

## API 接口

### 认证接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/auth/login` | POST | 用户登录 |
| `/api/auth/logout` | POST | 用户登出 |
| `/api/auth/check` | GET | 检查登录状态 |

### 用户接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/users` | GET | 获取当前用户信息 |
| `/api/users` | PUT | 更新用户信息 |
| `/api/users/password` | PUT | 修改密码 |

### 账单接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/bills` | GET | 获取账单列表 |
| `/api/bills` | POST | 添加账单 |
| `/api/bills/{id}` | PUT | 更新账单 |
| `/api/bills/{id}` | DELETE | 删除账单 |

### 分类接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/categories` | GET | 获取分类列表 |
| `/api/categories` | POST | 添加分类 |
| `/api/categories/{id}` | PUT | 更新分类 |
| `/api/categories/{id}` | DELETE | 删除分类 |

### 统计接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/statistics/monthly` | GET | 月度统计 |
| `/api/statistics/category` | GET | 分类统计 |
| `/api/statistics/trend` | GET | 趋势统计 |




## 常见问题

### Q1: 前端页面无法访问

确保前端开发服务器已启动：
```bash
cd frontend
npm run dev
```

### Q2: 后端接口连接失败

检查 Tomcat 是否启动，访问：
```
http://localhost:8080/account-dashboard/api/auth/check
```

### Q3: 数据库连接失败

检查 MySQL 服务状态和连接配置：
```properties
jdbc.url=jdbc:mysql://localhost:3306/account_dashboard
jdbc.username=root
jdbc.password=123456
```

### Q4: 页面样式异常

清除浏览器缓存或重新构建：
```bash
cd frontend
npm run build
```

---

## 📄 项目文档

- [交付说明](docs/PROJECT_DELIVERY.md)：功能完成度、验收清单和演示建议
- [项目逻辑说明](docs/PROJECT_LOGIC.md)：后端分层、前端页面、数据流说明
- [代码导读](docs/CODE_WALKTHROUGH.md)：关键文件职责、阅读顺序
- [接口对照说明](docs/API_REFERENCE.md)：REST API 参数、返回说明
- [数据库设计说明](docs/DATABASE_DESIGN.md)：核心表、外键关系
- [答辩演示指南](docs/DEFENSE_GUIDE.md)：演示顺序、讲解话术
- [微信小程序化改造方案](docs/MINI_PROGRAM_UPGRADE.md)：小程序改造方案

---

## 📝 License

This project is licensed under the MIT License.
