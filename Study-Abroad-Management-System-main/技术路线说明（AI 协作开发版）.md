留学申请与审核系统

文档优先级说明：
1. 业务规则与产品边界以 PRD 为准
2. 接口设计与状态流转以后端接口文档为准
3. 数据落库结构以数据库设计文档为准
4. 若出现冲突，按以上顺序处理；个人理解和群聊口头描述不作为最终规则

### 技术路线说明（AI 协作开发版）

版本：V1.0
适用对象：你 / A / B
目标：三人并行开发、AI 辅助生成代码、实现快、效果稳、低冲突
本技术路线基于当前已定的业务 PRD、分工方案、后端接口文档 v4、数据库设计文档制定。PRD 明确了系统核心是“申请 → 国内审核 → 学校审核 → 候补/调剂/占位 → 归档日志”的业务链路，并要求权限控制、状态流转、配额双控、候补时效、审计追踪等能力。

分工和接口文档已经确定：你负责核心状态、学校审核、候补/调剂/配额与日志，A 负责申请端，B 负责国内审核，这种边界天然适合单体后端 + 管理后台前端的技术路线。
￼

## 一、最终技术选型

1. ### 后端

  • 语言：Java 17
  • 框架：Spring Boot 3.x
  • Web：Spring Web
  • 参数校验：Spring Validation
  • 安全认证：JWT + Spring Security（轻量使用）
  • ORM / 数据访问：MyBatis-Plus
  • 构建工具：Maven
  • 常用辅助：Lombok
  Spring Boot 官方文档强调其目标是帮助你快速创建独立、可运行、少配置的 Spring 应用；Spring Boot 3.x 线路要求至少 Java 17。
  Home
  +1
  MyBatis-Plus 官方文档说明它是在 MyBatis 之上做增强，“只增强不改变”，目标是简化开发、提高效率，并提供开箱即用的 CRUD、分页等能力。
  MyBatis-Plus
  +2
  MyBatis-Plus
  +2
2. ### 前端

  • 框架：Vue 3
  • 构建工具：Vite
  • 路由：Vue Router
  • 状态管理：Pinia
  • UI 组件库：Element Plus
  • HTTP：Axios
  • 语言：TypeScript
  Vue 官方文档当前主线是 Vue 3；Vue 3 官方迁移建议明确推荐使用 Vite 作为构建工具、Pinia 作为状态管理。
  Vue.js
  +1
  Vite 官方将自己定位为更快、更轻的现代前端构建工具，开发期提供很快的 HMR。
  vitejs
  Element Plus 官方明确其是一个基于 Vue 3 的组件库，适合做表单、表格、审核页、弹窗、分页等后台场景。
  Element Plus
  +1
  Pinia 官方将自己定位为轻量、灵活的 Vue 状态管理方案。
  Pinia
3. ### 数据库

  • 数据库：MySQL 8.4
  • 字符集：utf8mb4
  • 排序规则：utf8mb4_0900_ai_ci
  MySQL 官方文档说明 8.4 是 LTS 线，适合在 LTS 系列内稳定使用和升级。
  MySQL Developer ZoneMySQL Develop…
  +1
4. ### 版本管理与协作

  • 仓库：GitHub
  • 分支：main / dev / feature/*
  • 合并方式：PR
  • 合并负责人：你



## 二、为什么选这套技术路线

### 1. 对 AI 最友好，最好生成

你们现在是 AI 辅助开发，最重要的是：

+ 样板多
+ 资料多
+ 提示词稳定
+ 生成出来的代码结构统一

**Spring Boot + Vue 3** 是当前最主流、最容易被 AI 正确生成的一类“后台管理系统”组合。
 你们这个系统本质上是：

+ 登录权限
+ 表单录入
+ 列表查询
+ 审核动作
+ 状态流转
+ 配额与候补
+ 日志追踪

它不是高实时系统，也不是复杂微服务平台，所以没必要上很重的架构。PRD 本身也集中在标准业务流、状态机、配额、候补和调剂上。

### 2. 最适合三人低冲突开发

你们已经把分工切成：

+ 你：核心状态 / 学校审核 / 候补 / 调剂 / 配额 / 日志
+ A：申请端
+ B：国内审核

这种分法最适合：

+ 后端按模块拆
+ 前端按页面和接口拆
+ 数据库按表归属拆 

Spring Boot 单体项目很适合按模块建目录，MyBatis-Plus 很适合这种表结构明确、CRUD 多、但关键动作仍需要你手写 SQL 和事务的场景。

### 3. 实现成本低、效果也足够好

Vue 3 + Element Plus 能很快做出：

+ 登录页
+ 表单页
+ 列表页
+ 审核详情页
+ 候补管理页
+ 配额配置页

效果不会差，而且开发速度快。Element Plus 的组件覆盖了表单、表格、分页、上传、对话框、步骤等后台常见能力。

### 4. 和你现有文档天然匹配

你现在已经有：

+ PRD
+ 分工文档
+ 后端接口文档 v4
+ 数据库设计文档

这些文档的结构，天然就是标准 Java 后端 + 管理台前端的风格。
 如果这时候换成 Node/Nest、MongoDB、微服务，反而会让接口、DTO、数据库事务和目录规划全部重来。

------

## 三、不选别的方案的原因

### 1. 不选 Node / Express / Nest 作为主后端

不是不能做，而是**不如 Java 稳**。
 你已经明确后端语言规定 Java，那最稳的就是 Spring Boot。Java 17 也是 Spring Boot 3 的官方基线。

### 2. 不选 JPA 作为主要 ORM

你们系统里有很多对事务和 SQL 过程要求很明确的动作：

+ 占位扣减
+ 候补补位
+ 配额释放
+ 调剂派生
+ 状态历史
+ 审计日志

JPA 不是不能做，但对三人协作来说，级联、关联映射、懒加载更容易引出隐藏复杂度。
 MyBatis-Plus 更适合“普通 CRUD 快、关键 SQL 自己控”。

### 3. 不选微服务

你们只有三个人，PRD 也是一个完整闭环系统，没有跨团队服务边界。
 微服务只会增加：

+ 部署复杂度
+ 接口联调复杂度
+ 权限传递复杂度
+ AI 提示词复杂度

### 4. 不选 MongoDB

你们核心是强关系业务：

+ 申请表
+ 审核表
+ 候补记录
+ 配额表
+ 状态历史
+ 审计日志 

这就是典型关系库场景，MySQL 更合适。

### 5. 前端统一使用 TypeScript

当前实际前端项目已经采用 **Vue 3 + TypeScript**，因此这里统一按 TypeScript 执行。
因为你们现在第一目标是：

+ 跑通流程
+ 降低冲突
+ 让 AI 更稳定地产出可用代码

不是先把类型系统做到最严。



## 四、项目总体架构

### 1. 架构方式

采用：**前后端分离 + 单体后端**

结构如下：

```
前端（Vue 3 + Element Plus）
        ↓ HTTP/JSON
后端（Spring Boot 3）
        ↓
数据库（MySQL 8.4）
```

### 2. 后端不拆服务

所有模块在一个 Spring Boot 项目里，按业务模块分包：

+ auth
+ users
+ applications
+ domestic-reviews
+ school-reviews
+ waitlists
+ quotas
+ batches
+ schools
+ majors
+ audit-logs
+ dashboard（前端工作台可保留占位页，后端接口当前未实现，不纳入本期并行开发范围）

理由：

+ 部署简单
+ 调试简单
+ 权限和事务集中
+ 更适合你统一把控核心状态与配额逻辑

------

## 五、三人开发前必须冻结的技术路线规则

### 1. 后端框架冻结

只允许：

+ Java 17
+ Spring Boot 3.x
+ Spring Web
+ Spring Validation
+ JWT + Spring Security
+ MyBatis-Plus
+ Maven

**禁止**：

+ 个人换 ORM
+ 个人换框架
+ 个人加另一套权限方案
+ 个人另写一套接口风格

### 2. 前端框架冻结

只允许：

+ Vue 3
+ Vite
+ Vue Router
+ Pinia
+ Element Plus
+ Axios
+ TypeScript

**禁止**：

+ 个人换 React
+ 个人自己封另一套 UI 规范
+ 个人把公共页面重写成另一风格

### 3. 接口协议冻结

统一走：

+ RESTful JSON
+ `code / message / data`
+ 分页统一 `list / page / pageSize / total`

接口文档 v4 已经冻结为当前真实后端实现。

### 4. 状态流转冻结

application 状态只能通过既定业务接口变更。
 A/B 不允许直接改 `application.status`。
 学校审核只允许一个 submit 接口。
 候补补位先进入 `WAITLIST_PENDING_CONFIRM`。
 接受调剂后原申请立即关闭。

### 5. 数据库规则冻结

数据库结构以数据库设计文档为准，表关系和接口读写表映射也已明确。

**禁止**：

+ 直接改核心表字段含义
+ 直接更新 `applications.current_status`
+ 个人新增表不通知你
+ 个人修改 quota 表逻辑

### 6. 公共目录冻结

后端目录：

```
src/
  main/
    java/
      .../
        common/
        config/
        modules/
        auth/
        users/
        batches/
         schools/
         majors/
         quotas/
         files/
         applications/
         domesticreviews/
         schoolreviews/
         waitlists/
         adjustments/
         auditlogs/
         dashboard/
```

说明：代码目录统一使用不带短横线的 Java 包风格；
文档描述中如出现 domestic-reviews / school-reviews / audit-logs，仅表示业务模块名称，不表示实际包名。

前端目录：

```
src/
  api/
  router/
  stores/
  views/
    login/
    agent/
    domestic-review/
    school-review/
    admin/
  components/
  utils/
```

### 7. 公共命名冻结

+ 表名：snake_case
+ 字段名：snake_case
+ Java 字段：camelCase
+ 状态值：全大写下划线
+ DTO：`CreateXxxRequest / UpdateXxxRequest`
+ VO：`XxxVO`
+ Service：`XxxService`

### 8. 事务规则冻结

以下操作必须事务：

+ 学校审核占位
+ 候补补位
+ 候补确认
+ 接受调剂
+ 批次结束候补失效处理

PRD 已明确占位要锁申请、锁学校配额、锁专业配额，并整体回滚。

### 9. 权限规则冻结

+ 代理：只能看自己创建的申请
+ 国内审查员：只能看国内审核所需范围
+ 学校专员：只能访问本校数据
+ 管理员：全量

权限矩阵和字段可见范围已经在 PRD 与接口文档中明确。

### 10. Git 流程冻结

+ `main`：稳定
+ `dev`：开发主分支
+ `feature/*`：个人功能分支
+ 所有人提 PR 到 `dev`
+ 只有你 merge

------

## 六、后端技术实现规范

### 1. 分层规则

+ `controller`：只接参数、调 service、返回结果
+ `service`：写业务逻辑
+ `mapper`：只做数据库访问
+ `entity`：表实体
+ `dto`：请求对象
+ `vo`：返回对象

### 2. 状态机实现规则

所有状态流转只允许放在：

+ `ApplicationService`
+ `SchoolReviewService`
+ `WaitlistService`

A/B 不允许在 controller、mapper 或前端里自定义状态流转。

### 3. 日志规则

以下动作必须写：

+ `application_status_histories`
+ `audit_logs`

范围按接口文档 v4 执行。

### 4. 文件上传规则

开发阶段采用：

+ 本地文件存储
+ 数据库存 `fileId / filePath / fileMeta`
+ 文件访问必须鉴权

PRD 已要求文件不可直接暴露存储路径。

------

## 七、前端技术实现规范

### 1. 页面组件规则

使用 Element Plus 实现：

+ 表单
+ 表格
+ 上传
+ 抽屉/弹窗
+ 标签页
+ 分页
+ 步骤/状态展示

### 2. 前端状态管理规则

只用 Pinia 保存：

+ 当前用户信息
+ token
+ 少量全局状态

**禁止**把业务主状态流转放前端决定。
 申请状态、审核状态、候补状态全部以后端返回为准。

### 3. API 调用规则

统一走：

+ `src/api/*`
+ Axios 实例
+ 统一请求头注入 token
+ 统一错误拦截

------

## 八、三人具体技术边界

### 你

负责：

+ Spring Boot 项目骨架
+ JWT / Security
+ 公共响应结构
+ application 核心状态机
+ school-reviews
+ waitlists
+ quotas
+ audit-logs
+ batches / schools / majors / users

### A

负责：

+ Vue 申请端页面
+ 文件上传前端
+ 申请草稿、详情、补件页面
+ 对接 `/api/applications` 草稿相关接口
+ 不碰核心状态接口

### B

负责：

+ Vue 国内审核页面
+ 对接 `/api/domesticreviews/**`
+ 不碰学校审核
+ 不碰 quota / waitlist / 调剂

这和你们已经定好的接口边界、表归属边界一致。

------

## 九、开工前你必须先完成什么

在 A 和 B 开工前，你先完成：

1. 项目骨架初始化
2. 数据库表建好并初始化基础数据
3. 登录 / 获取当前用户跑通
4. 通用响应结构写好
5. JWT 鉴权写好
6. 状态枚举和公共常量写好
7. 申请核心接口骨架写好
8. 学校审核 submit 接口骨架写好
9. 文件上传骨架写好
10. 测试账号准备好

这些是为了保证他们一接手就能开始，而不是被“环境还没好”卡住。现有文档里已经把必须先定的数据结构、接口和项目骨架列出来了。

------

## 十、这套技术路线的最终理由

我最后帮你压缩成最核心的理由：

### 1. **最好实现**

因为你们做的是标准业务系统，不需要复杂架构。Spring Boot + Vue 3 + MySQL 正好对口。

### 2. **AI 最好写**

这套栈资料最多、模板最多、提示最稳定，AI 更容易生成可运行代码。

### 3. **三人最不容易冲突**

你负责核心，A 和 B 走页面和单一审核模块，单体架构最容易集中管控。

### 4. **效果足够好**

Vue 3 + Element Plus 做管理系统页面很快，成品效果不会差。

### 5. **和现有文档完全对齐**

不用推翻你已经写好的 PRD、接口文档、数据库设计和分工方案。

------

## 最终定版

**技术路线最终定版：**
 前端：**Vue 3 + Vite + Vue Router + Pinia + Element Plus + Axios + TypeScript**
 后端：**Java 17 + Spring Boot 3.x + Spring Web + Spring Validation + JWT + Spring Security + MyBatis-Plus + Lombok + Maven**
 数据库：**MySQL 8.4**

