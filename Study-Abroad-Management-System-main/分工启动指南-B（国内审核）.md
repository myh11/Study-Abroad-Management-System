# 分工启动指南-B（国内审核）

## 一、这份文档是干什么的

- 这份文档是你作为国内审核开发同学的正式开工手册。
- 你必须按它来，不允许自己扩接口、不允许自己补数据库逻辑、不允许越界改学校审核和公共层。

## 二、你现在是否可以开始开发

- 可以。
- 你的边界已经清楚：
  - 页面是谁的
  - 文件能改哪些
  - 接口能用哪些
  - 哪些页现在只能占位
  - 验收标准是什么

## 三、你只认这些正式文档

- [一页式项目开工冻结页.md](D:/大二下/软件开发与设计/实践二/一页式项目开工冻结页.md:1)
- [前端页面与路由清单.md](D:/大二下/软件开发与设计/实践二/前端页面与路由清单.md:1)
- [开发 ownership 表.md](D:/大二下/软件开发与设计/实践二/开发 ownership 表.md:1)
- [模块验收清单.md](D:/大二下/软件开发与设计/实践二/模块验收清单.md:1)
- [后端接口文档.md](D:/大二下/软件开发与设计/实践二/后端接口文档.md:1)

## 四、你在团队中的唯一职责

- 你只做国内审核这条链。
- 你只负责：
  - 国内审核详情展示
  - 认领申请
  - 提交国内审核结果
  - 国内审核列表占位页

## 五、你负责的页面

- `/domesticreviews`
- `/domesticreviews/:id`

## 六、你负责的文件

- `src/views/domesticreviews/DomesticReviewListPage.vue`
- `src/views/domesticreviews/DomesticReviewDetailPage.vue`
- `src/api/domesticreviews/service.ts`

## 七、你绝对不能改的文件

- `src/router/index.ts`
- `src/stores/auth.ts`
- `src/api/request.ts`
- `src/components/common/*`
- `src/layouts/*`
- `src/views/login/*`
- `src/views/dashboard/*`
- `src/views/applications/*`
- `src/views/schoolreviews/*`
- `src/views/waitlists/*`
- `src/views/admin/*`

## 八、你能使用的测试账号

- 国内审核账号：
  - `domestic01 / domestic123`
- 管理员账号只用于辅助查看，不用于你自己的常规联调：
  - `admin / admin123`

## 九、你必须知道的首次登录规则

- 所有初始化账号默认 `mustChangePassword = true`
- 所以你第一次联调顺序必须是：
  1. `POST /api/auth/login`
  2. `POST /api/auth/change-password`
  3. `GET /api/auth/me`

## 十、你只允许调用这些真实接口

- `POST /api/auth/login`
- `POST /api/auth/change-password`
- `GET /api/auth/me`
- `GET /api/applications/{id}`
- `POST /api/domesticreviews/{id}/claim`
- `POST /api/domesticreviews/{id}/submit`

## 十一、你必须知道的“当前未实现”

- `/api/domesticreviews/pending` 未实现
- `/api/domesticreviews/{id}` 详情查询未实现
- `/api/domesticreviews/records` 未实现
- `/api/domesticreviews/records/{reviewId}` 未实现

## 十二、这意味着你前端必须怎么做

- `/domesticreviews` 只能做占位页
- 占位页必须明确写“当前后端未实现列表接口”
- 不能自己伪造一个待审核列表接口
- 真正要联调的是 `/domesticreviews/:id`
- 详情页所需申请信息，当前用 `GET /api/applications/{id}` 拿

## 十三、你需要验证的业务动作

### 1. 认领

- 从 `SUBMITTED` 状态申请发起 `claim`
- 认领后进入 `DOMESTIC_REVIEWING`

### 2. 审核通过

- `result = PASS`
- 状态进入 `SCHOOL_REVIEWING`

### 3. 要求补件

- `result = SUPPLEMENT_REQUIRED`
- 状态进入 `DOMESTIC_SUPPLEMENT`

### 4. 审核拒绝

- `result = REJECT`
- 状态进入 `DOMESTIC_REJECTED`

## 十四、你开发时必须知道的业务边界

- 你是审核人，不是状态机实现者
- 你只能调用国内审核接口触发状态变化
- 你不能自己在前端决定“现在该进入哪个状态”
- 你不能跳过 `claim` 直接假设申请已经被你认领

## 十五、你绝对不能做的事

- 不允许自己发明 `/api/domesticreviews/pending`
- 不允许手工改数据库造认领结果
- 不允许直接改 `applications.current_status`
- 不允许改学校审核逻辑
- 不允许改候补逻辑
- 不允许改配额逻辑
- 不允许改公共文件

## 十六、你必须用到的验证方式

- 页面验证
- 接口返回验证
- 数据库结果验证

你至少要让负责人能查到：
- `domestic_reviews`
- `applications.current_status`
- `application_status_histories`
- `audit_logs`

## 十七、你的开发顺序

### 第一步：登录闭环

- 登录
- 改密
- 获取当前用户

### 第二步：详情页

- 用 `GET /api/applications/{id}` 展示申请内容
- 做认领按钮
- 做提交审核表单

### 第三步：列表页

- 只做占位
- 明确写“当前后端未实现”

## 十八、你提交前必须自查

- 只改了 `domesticreviews` 相关文件
- 没有改公共层
- 没有接不存在的接口
- `claim` 和 `submit` 都真实跑过
- `PASS / SUPPLEMENT_REQUIRED / REJECT` 三种结果都测过

## 十九、你的完成标准

- 以 [模块验收清单.md](D:/大二下/软件开发与设计/实践二/模块验收清单.md:1) 中 B 模块验收为准
- 最低通过线：
  - 国内审核详情页真实联调完成
  - 认领成功
  - 三种审核结果提交成功
  - 列表页只做占位且不乱接接口

## 二十、你最容易踩的坑

- 觉得列表页在 PRD 有，就自己补接口
- 不走 `claim` 就直接提交审核
- 只看页面提示，不查真实状态变化
- 为了调试直接改数据库
- 顺手改公共组件或公共路由

## 二十一、你和负责人沟通时只需要说这些

- 我改了哪些文件
- 我调了哪些接口
- 我验证了哪三种审核结果
- 哪个字段展示还缺后端支持
- 我没有改公共层

## 二十二、结论

- 你现在可以正式开始。
- 你的唯一目标是把国内审核详情页和 `claim/submit` 两个真实接口稳定接起来，别越界。
