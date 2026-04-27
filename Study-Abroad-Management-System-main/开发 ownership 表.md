# 开发 ownership 表

文档优先级：`PRD > 后端接口文档 v4 > 技术路线说明 > 分工文档 > 个人理解`

适用范围：`D:\大二下\软件开发与设计\实践二\admission-system-frontend` 与 `D:\大二下\软件开发与设计\实践二\admission-system-backend`

## 一、前端页面 ownership（按角色）

| 页面 | 路由 | 负责人 | 是否允许他人修改 | 说明 |
| --- | --- | --- | --- | --- |
| 登录页 | `/login` | 你 | 否 | 对应 PRD 17.1；涉及登录、改密入口、鉴权跳转 |
| 首页/工作台 | `/dashboard` | 你 | 否 | 对应 PRD 17.2；当前前端为占位页，后端接口未实现 |
| 代理申请创建页 | `/applications/create` | A | 否 | 对应 PRD 17.3；只对接已实现申请草稿创建接口 |
| 代理申请列表页 | `/applications` | A | 否 | 对应 PRD 17.4；只展示本人申请与固定筛选项 |
| 申请详情页 | `/applications/:id` | A | 否 | 对应 PRD 17.5；代理侧详情由 A 负责，其他角色只可复用后端详情接口，不可改该页面结构 |
| 国内审核列表页 | `/domesticreviews` | B | 否 | 对应 PRD 17.6；当前后端列表接口未实现，前端如保留仅可做占位或固定提示 |
| 国内审核详情页 | `/domesticreviews/:id` | B | 否 | 对应 PRD 17.7；当前后端详情接口未实现，不得自行发明接口 |
| 学校审核列表页 | `/schoolreviews` | 你 | 否 | 对应 PRD 17.8；当前后端列表接口未实现，页面归核心负责人控制 |
| 学校审核详情页 | `/schoolreviews/:id` | 你 | 否 | 对应 PRD 17.9；对接唯一学校审核 `submit` 核心接口 |
| 候补管理页 | `/waitlists` | 你 | 否 | 对应 PRD 17.10；涉及候补推进、失效、归属学校校验 |
| 学校与专业配置页 | `/admin/schools-majors` | 你 | 否 | 对应 PRD 17.11；管理端核心配置页 |
| 批次管理页 | `/admin/batches` | 你 | 否 | 对应 PRD 17.12；管理端核心配置页 |
| 配额管理页 | `/admin/quotas` | 你 | 否 | 对应 PRD 17.13；涉及学校总名额/专业名额一致性约束 |
| 用户管理页 | `/admin/users` | 你 | 否 | 对应 PRD 17.14；当前无 reset-password 接口，不得自行补前端按钮逻辑 |
| 日志查询页 | `/admin/audit-logs` | 你 | 否 | 对应 PRD 17.15；只对接已实现日志查询接口 |

## 二、接口 ownership（按模块）

| 接口模块 | 接口路径 | 负责人 | 是否允许改返回结构 | 说明 |
| --- | --- | --- | --- | --- |
| 认证模块 | `/api/auth/login` `/api/auth/change-password` `/api/auth/me` | 你 | 否 | 核心接口；鉴权、改密、锁定逻辑仅你可改 |
| 文件模块 | `/api/files/transcripts` `/api/files/attachments` `/api/files/{fileId}/preview` `/api/files/{fileId}/download` | A | 否 | A 负责联调接入；返回结构冻结，后端 contract 不允许 A 改 |
| 代理申请模块 | `/api/applications` `/api/applications/{id}` `/api/applications/my` `/api/applications/{id}/supplement` | A | 否 | A 负责页面对接；状态相关字段由后端统一返回，不允许改结构 |
| 申请命令模块 | `/api/applications/{id}/submit` `/api/applications/{id}/cancel` `/api/applications/{id}/waitlist-confirm` `/api/applications/{id}/accept-adjustment` `/api/applications/{id}/reject-adjustment` `/api/applications/{id}/close` | 你 | 否 | 核心接口；状态机、约束、历史、审计仅你可改 |
| 国内审核模块 | `/api/domesticreviews/{id}/claim` `/api/domesticreviews/{id}/submit` | B | 否 | B 负责联调和页面接入；返回结构冻结，不得新增自定义结果字段 |
| 学校审核模块 | `/api/school-reviews/{id}/submit` | 你 | 否 | 核心接口；占位、候补、调剂、学校归属校验仅你可改 |
| 候补模块 | `/api/waitlists/{applicationId}` `/api/waitlists/{applicationId}/promote` `/api/waitlists/{applicationId}/invalidate` | 你 | 否 | 核心接口；候补时效、批次结束失效、预占配额仅你可改 |
| 批次管理模块 | `/api/batches` `/api/batches/{id}` `/api/batches/{id}/status` | 你 | 否 | 核心接口；发布与配置校验仅你可改 |
| 学校管理模块 | `/api/schools` `/api/schools/{schoolCode}` `/api/schools/{schoolCode}/status` | 你 | 否 | 管理端核心接口 |
| 专业管理模块 | `/api/majors` `/api/majors/{majorCode}` `/api/majors/{majorCode}/status` | 你 | 否 | 管理端核心接口 |
| 配额管理模块 | `/api/quotas` `/api/quotas/adjustments` `/api/quotas/schools/{id}/adjust` `/api/quotas/majors/{id}/adjust` | 你 | 否 | 核心接口；名额调整、并发控制、发布一致性约束仅你可改 |
| 用户管理模块 | `/api/users` `/api/users/{id}` `/api/users/{id}/status` | 你 | 否 | 核心接口；用户启停、角色和学校绑定只由你维护 |
| 日志查询模块 | `/api/audit-logs` `/api/audit-logs/status-histories` | 你 | 否 | 核心接口；查询结构冻结 |

强制说明：
- 上表中负责人为 A 或 B 的接口，只表示“联调接入负责人”，不表示可以改后端 contract。
- 所有核心接口的请求字段、返回结构、状态语义，只能由你修改。

## 三、前端目录 ownership（重点）

| 文件/目录 | 负责人 | 是否允许他人修改 | 说明 |
| --- | --- | --- | --- |
| `src/router/index.ts` | 你 | 否 | 全局路由、角色守卫、菜单可见性统一入口 |
| `src/stores/auth.ts` | 你 | 否 | 登录态、当前用户、改密后状态刷新统一入口 |
| `src/api/request.ts` | 你 | 否 | Axios 实例、token 注入、统一错误处理 |
| `src/api/auth/service.ts` | 你 | 否 | 认证 contract 只由你维护 |
| `src/api/applications/service.ts` | A | 否 | A 负责代理申请侧接口接入 |
| `src/api/files/service.ts` | A | 否 | A 负责上传与附件接入 |
| `src/api/domesticreviews/service.ts` | B | 否 | B 负责国内审核侧接口接入 |
| `src/api/schoolreviews/service.ts` | 你 | 否 | 学校审核核心接口 |
| `src/api/batches/service.ts` | 你 | 否 | 管理端核心接口 |
| `src/api/schools/service.ts` | 你 | 否 | 管理端核心接口 |
| `src/api/majors/service.ts` | 你 | 否 | 管理端核心接口 |
| `src/api/quotas/service.ts` | 你 | 否 | 管理端核心接口 |
| `src/api/users/service.ts` | 你 | 否 | 管理端核心接口 |
| `src/api/auditlogs/service.ts` | 你 | 否 | 管理端核心接口 |
| `src/components/common/*` | 你 | 否 | 公共组件层，任何样式和 props 变更都只能由你改 |
| `src/layouts/*` | 你 | 否 | 全局布局与菜单结构统一入口 |
| `src/views/login/LoginPage.vue` | 你 | 否 | 认证入口页面 |
| `src/views/dashboard/DashboardPage.vue` | 你 | 否 | 当前为占位页，避免他人提前接假接口 |
| `src/views/applications/*` | A | 否 | 代理申请侧页面 |
| `src/views/domesticreviews/*` | B | 否 | 国内审核侧页面 |
| `src/views/schoolreviews/*` | 你 | 否 | 学校审核侧页面 |
| `src/views/waitlists/*` | 你 | 否 | 候补核心页面 |
| `src/views/admin/*` | 你 | 否 | 所有管理端页面归你 |
| `src/views/shared/PagePlaceholder.vue` | 你 | 否 | 占位页统一口径，避免各自写不同提示 |

## 四、后端模块 ownership

| 模块 | 负责人 | 可修改范围 | 禁止修改 |
| --- | --- | --- | --- |
| `auth` | 你 | 登录、改密、锁定、JWT、RBAC、密码策略 | A/B 不得改认证流程、token 结构、角色校验 |
| `config` | 你 | `SecurityConfig`、过滤器、全局配置 | A/B 不得修改安全链、跨域、鉴权规则 |
| `common` | 你 | 统一响应、异常、公共工具 | A/B 不得改通用返回结构 |
| `applications` | 你 | 草稿、详情、命令、状态机、约束、历史、审计 | A/B 不得改状态流转、约束逻辑、返回 contract |
| `files` | 你 | 文件上传、预览、下载、权限校验 | A/B 不得改存储路径和鉴权规则 |
| `domesticreviews` | B | `claim/submit` 相关 DTO、页面所需 VO、当前模块 service/controller 内的非核心展示逻辑 | 不得改 application 状态机、配额、候补、学校审核逻辑 |
| `schoolreviews` | 你 | 学校审核评分、结果提交、调剂派生 | B/A 不得修改 |
| `waitlists` | 你 | 候补查询、推进、失效、定时任务 | A/B 不得修改 |
| `quotas` | 你 | 配额读取、调整、并发控制、释放占用 | A/B 不得修改 |
| `admin` | 你 | 批次、学校、专业、用户、日志管理接口 | A/B 不得修改 |
| `auditlogs` | 你 | 日志查询接口与写入规则 | A/B 不得修改 |
| `batches` | 你 | 批次配置与状态更新 | A/B 不得修改 |
| `schools` | 你 | 学校配置 | A/B 不得修改 |
| `majors` | 你 | 专业配置 | A/B 不得修改 |
| `users` | 你 | 用户管理与学校归属 | A/B 不得修改 |
| `dashboard` | 你 | 是否开放、返回结构、占位逻辑 | A/B 不得提前实现假接口 |

## 五、数据库表 ownership

| 表名 | 负责人 | 是否允许直接操作 | 说明 |
| --- | --- | --- | --- |
| `applications` | 你 | 否 | 核心表；A/B 只能通过接口 |
| `application_status_histories` | 你 | 否 | 状态历史只能由状态机写入 |
| `audit_logs` | 你 | 否 | 审计日志只能由后端 service 写入 |
| `rule_snapshots` | 你 | 否 | 提交申请时自动生成，不允许直改 |
| `waitlist_records` | 你 | 否 | 候补记录只能通过候补 service 修改 |
| `school_quotas` | 你 | 否 | 学校总名额只能通过配额接口修改 |
| `major_quotas` | 你 | 否 | 专业名额只能通过配额接口修改 |
| `school_reviews` | 你 | 否 | 学校审核结果只能通过学校审核接口写入 |
| `domestic_reviews` | B | 否 | B 负责业务联调，但只能通过国内审核接口写入 |
| `students` | A | 否 | A 负责申请侧联调，但只能通过申请接口写入 |
| `transcripts` | A | 否 | 只能通过文件上传与补件接口写入 |
| `personal_statements` | A | 否 | 只能通过申请接口写入 |
| `files` | 你 | 否 | 文件元数据只允许后端上传逻辑写入 |
| `users` | 你 | 否 | 账号、角色、学校归属只能通过用户管理接口修改 |
| `admission_batches` | 你 | 否 | 只能通过批次接口修改 |
| `schools` | 你 | 否 | 只能通过学校接口修改 |
| `majors` | 你 | 否 | 只能通过专业接口修改 |

强制说明：
- `applications / school_quotas / major_quotas / waitlist_records` 一律归你。
- A/B 任何场景都不允许直接执行 SQL 修改业务表，只允许调用现有接口。

## 六、强制规则

- 公共文件只有负责人可改；公共文件包括 `src/router/index.ts`、`src/stores/*`、`src/api/request.ts`、`src/components/common/*`、`src/layouts/*`、后端 `auth/config/common/applications/schoolreviews/waitlists/quotas/admin`。
- 跨模块修改必须先确认负责人；未确认前，不得提交跨模块代码。
- 状态相关逻辑只能由核心负责人实现；任何人不得在前端、controller、SQL 脚本中绕过状态机直接改状态。
- A 只负责代理申请侧页面与对应前端接口接入；不得改审核、候补、配额、权限。
- B 只负责国内审核侧页面与对应前端接口接入，以及 `domesticreviews` 模块内非核心展示逻辑；不得改学校审核、候补、配额、状态机。
- 你负责所有核心接口、公共层、管理端、学校审核、候补、配额、状态机、数据库约束与最终合并。
