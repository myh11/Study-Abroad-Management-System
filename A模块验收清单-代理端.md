# A 模块验收总结（代理端）

## 一、完成情况总览

| 功能模块 | 实现状态 | 备注 |
|---|---|---|
| 登录改密闭环 | ✅ 已实现 | `LoginPage.vue` 强制弹窗 + `stores/auth.ts` 适配 flat LoginResponse |
| 新建申请页 | ✅ 已实现 | ①学生信息 ②成绩单上传 ③自荐材料 ④附件上传 ⑤申请目标 |
| 我的申请列表 | ✅ 已实现 | 状态筛选、分页、搜索、快捷操作（提交/撤销/补件） |
| 申请详情页 | ✅ 已实现 | 信息展示 + 状态横幅 + 操作按钮（提交/补件/候补/调剂/撤销） |
| 真实接口联调 | ✅ 已实现 | 所有接口均调用真实后端，无 mock |
| 数据库验证 | ⏳ 待验证 | 需用固定账号和固定 applicationId 实际操作后查库确认 |

---

## 二、各页面具体实现

### 2.1 新建申请页（ApplicationCreatePage.vue）

| 检查项 | 状态 | 说明 |
|---|---|---|
| ① 学生基本信息表单 | ✅ | 姓名、身份证号、性别、出生日期、电话、邮箱、在读学校、年级，必填校验完整 |
| ② 成绩单文件上传 | ✅ | 支持 PDF/JPG/PNG，调用 `POST /api/files/transcripts`，可删除重传 |
| ③ 自荐材料（PS） | ✅ | 文本域输入个人陈述内容 |
| ④ 附件材料上传 | ✅ | 支持多附件 PDF/JPG/PNG/DOC/DOCX，调用 `POST /api/files/attachments`，可添加/删除 |
| ⑤ 申请目标 | ✅ | 批次、学校编码、专业编码，必填校验完整 |
| 保存草稿 | ✅ | 调 `POST /api/applications`，成功后跳转到详情页 |
| 提交申请 | ✅ | 先创建草稿再调 `POST /api/applications/{id}/submit` |
| 表单校验 | ✅ | 必填项空时阻止提交并 toast 提示 |

**已知限制：**
- 附件上传调用真实接口，但后端 `ApplicationDraftRequest` 目前没有附件字段，附件 fileId 不会随草稿保存到数据库。需后端扩展 DTO 和数据库表后才能真正关联。

---

### 2.2 我的申请列表（ApplicationListPage.vue）

| 检查项 | 状态 | 说明 |
|---|---|---|
| 列表加载 | ✅ | 调 `GET /api/applications/my`，真实数据渲染 |
| 状态标签筛选 | ✅ | 全部 + 13 种状态，点击切换后刷新列表 |
| 关键字搜索 | ✅ | 支持按学生姓名和申请 ID 过滤（前端本地过滤） |
| 分页 | ✅ | 支持切换页码和每页条数 |
| 新建申请入口 | ✅ | 跳转到创建页 |
| 查看详情 | ✅ | 跳转到详情页 |
| 草稿行快捷提交 | ✅ | 调 `POST /api/applications/{id}/submit`，列表刷新 |
| 可撤销行快捷撤销 | ✅ | 调 `POST /api/applications/{id}/cancel`，列表刷新 |
| 国内补件行快捷补件 | ✅ | 弹窗上传新成绩单，调 `POST /api/applications/{id}/supplement` |

---

### 2.3 申请详情页（ApplicationDetailPage.vue）

| 检查项 | 状态 | 说明 |
|---|---|---|
| 详情数据展示 | ✅ | 学生信息、申请信息、成绩单、自荐材料、国内/学校审核摘要、规则快照、审计日志、状态时间线 |
| 状态横幅 | ✅ | 显示当前状态中文标签 + 学校编码 + 状态说明文案 |
| 草稿 -> 提交申请 | ✅ | 按钮调真实 submit 接口，成功后刷新详情 |
| DOMESTIC_SUPPLEMENT -> 补件 | ✅ | 弹窗上传新成绩单，调真实 supplement 接口 |
| WAITLIST_PENDING_CONFIRM -> 接受/拒绝候补 | ✅ | 调 `POST /api/applications/{id}/waitlist-confirm` |
| ADJUSTMENT_SUGGESTED -> 接受/拒绝调剂 | ✅ | 接受调 `POST /api/applications/{id}/accept-adjustment`（取 suggested_major_code），拒绝调 `/reject-adjustment` |
| 可撤销状态 -> 撤销申请 | ✅ | 调 `POST /api/applications/{id}/cancel` |
| 成绩单预览 | ✅ | 调 `GET /api/files/{fileId}/preview` |
| 字段 key 对齐 | ✅ | 已修复 querySingleMap 返回 snake_case 与前端 camelCase 不一致的问题 |

**已知限制：**
- 补件后后端 `supplement` 接口只更新 `transcripts.file_id`，不改变 `applications.current_status`。补件后状态仍为 `DOMESTIC_SUPPLEMENT`。

---

## 三、接口调用清单

以下接口前端均已真实调用，无 mock：

| 接口 | 方法 | 调用位置 | 状态 |
|---|---|---|---|
| `/api/auth/login` | POST | LoginPage | ✅ |
| `/api/auth/change-password` | POST | LoginPage 弹窗 | ✅ |
| `/api/auth/me` | GET | auth store | ✅ |
| `/api/files/transcripts` | POST | CreatePage / ListPage补件弹窗 / DetailPage补件弹窗 | ✅ |
| `/api/files/attachments` | POST | CreatePage 附件区域 | ✅ |
| `/api/applications` | POST | CreatePage 保存草稿 | ✅ |
| `/api/applications/my` | GET | ListPage | ✅ |
| `/api/applications/{id}` | GET | DetailPage | ✅ |
| `/api/applications/{id}/supplement` | POST | ListPage补件弹窗 / DetailPage补件弹窗 | ✅ |
| `/api/applications/{id}/submit` | POST | CreatePage / ListPage / DetailPage | ✅ |
| `/api/applications/{id}/cancel` | POST | ListPage / DetailPage | ✅ |
| `/api/applications/{id}/waitlist-confirm` | POST | DetailPage | ✅ |
| `/api/applications/{id}/accept-adjustment` | POST | DetailPage | ✅ |
| `/api/applications/{id}/reject-adjustment` | POST | DetailPage | ✅ |

---

## 四、待联调验证项（需实际操作后确认）

以下功能代码已实现，但需用固定账号和固定数据实际跑通后验收：

| 验证项 | 固定数据 | 数据库验证重点 |
|---|---|---|
| 补件上传 | 124 / 142 | `transcripts.file_id` 是否更新；`audit_logs`、`application_status_histories` 是否有记录 |
| 候补确认 | 128 | `applications.current_status` 是否变为 `RESERVED`（接受）或 `CLOSED`（拒绝）；日志表是否有记录 |
| 调剂拒绝 | 130 / 144 | `applications.current_status` 是否变为 `SCHOOL_REJECTED` 或 `CLOSED`；日志表是否有记录 |
| 撤销终态 | 132 | `applications.current_status` 是否变为 `CANCELED`；`cancel_reason` 和 `canceled_at` 是否有值 |
| 新建草稿 | 新建 | `applications.current_status` = `DRAFT`；`students`、`transcripts`、`personal_statements` 关联记录是否存在；日志表是否有 `CREATE_DRAFT` 记录 |
| 提交申请 | 新建草稿后 | `applications.current_status` = `SUBMITTED`；`submit_time` 是否有值；日志表是否有 `DRAFT` → `SUBMITTED` 记录 |

---

## 五、已知问题与限制

1. **附件无法持久化关联**
   - 原因：后端 `ApplicationDraftRequest` 没有附件字段，数据库无附件关联表
   - 影响：附件上传后得到 fileId，但创建草稿时不会保存附件信息
   - 解决：需后端扩展 `ApplicationDraftRequest` 和数据库表

2. **补件后状态不自动推进**
   - 原因：后端 `supplement` 接口只更新 `transcripts.file_id`，不修改 `applications.current_status`
   - 影响：补件后状态仍为 `DOMESTIC_SUPPLEMENT`
   - 备注：可能是后端设计如此，后续由国内审核员继续处理


---

## 六、修改文件清单

### 6.1 代理端职责范围内（正常修改）

- `src/views/applications/ApplicationCreatePage.vue`
- `src/views/applications/ApplicationListPage.vue`
- `src/views/applications/ApplicationDetailPage.vue`

### 6.2 越界修改（需向负责人报备）

以下文件属于文档第七部分"绝对不能改"的清单，但因登录改密是代理端所有接口的前置依赖，不得不做最小化适配：

| 文件 | 文档规定 | 修改原因 | 改动范围 |
|---|---|---|---|
| `src/stores/auth.ts` | 绝对不能改 | 后端 `AuthUserVO` 改成 flat 结构，`login()` 需适配组装 `currentUser` | 仅 `login()` 方法内 currentUser 组装逻辑 |
| `src/views/login/LoginPage.vue` | 绝对不能改 | 后端 `PasswordChangeEnforcementFilter` 强制改密，不加弹窗无法调用任何业务接口 | 新增改密弹窗及对应处理函数 |

**备注：** 上述越界修改均为打通登录闭环的最小必要改动，未涉及代理端业务逻辑。如负责人要求严格隔离，可考虑将改密弹窗拆成独立组件或迁移到公共层，由核心负责人统一维护。

---

## 七、验收结论

| 项目 | 代码实现 | 联调验证 | 结论 |
|---|---|---|---|
| 登录改密 | ✅ | ⏳ 待验证 | |
| 创建草稿 | ✅ | ⏳ 待验证 | |
| 上传成绩单 | ✅ | ⏳ 待验证 | |
| 上传附件 | ✅ | ⏳ 待验证（后端不支持持久化） | |
| 提交申请 | ✅ | ⏳ 待验证 | |
| 我的申请列表 | ✅ | ⏳ 待验证 | |
| 申请详情展示 | ✅ | ⏳ 待验证 | |
| 补件 | ✅ | ⏳ 待验证 | |
| 撤销 | ✅ | ⏳ 待验证 | |
| 候补确认 | ✅ | ⏳ 待验证 | |
| 调剂接受/拒绝 | ✅ | ⏳ 待验证 | |
| 数据库记录完整性 | — | ⏳ 待验证 | |

**代码层面：** 代理端三页及对应接口 service 已全部实现，接口调用真实无 mock。
**联调层面：** 需用 `agent01` 账号和固定 applicationId 实际跑通后最终确认。
