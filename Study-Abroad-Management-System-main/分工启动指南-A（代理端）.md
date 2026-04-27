# 分工启动指南-A（代理端）

## 一、这份文档是干什么的

- 这份文档是你作为代理端开发同学的正式开工手册。
- 你必须按这份文档开发，不允许凭感觉扩写需求或接口。
- 你只负责代理端页面，不负责审核、不负责候补逻辑实现、不负责配额、不负责数据库。

## 二、你现在是否可以开始开发

- 可以。
- 当前你需要的页面、接口、固定账号、固定数据、ownership 边界都已经冻结。
- 你现在缺的不是条件，而是按边界执行。

## 三、你只认这些正式文档

- [一页式项目开工冻结页.md](D:/大二下/软件开发与设计/实践二/一页式项目开工冻结页.md:1)
- [前端页面与路由清单.md](D:/大二下/软件开发与设计/实践二/前端页面与路由清单.md:1)
- [开发 ownership 表.md](D:/大二下/软件开发与设计/实践二/开发 ownership 表.md:1)
- [模块验收清单.md](D:/大二下/软件开发与设计/实践二/模块验收清单.md:1)
- [后端接口文档.md](D:/大二下/软件开发与设计/实践二/后端接口文档.md:1)

## 四、你在团队中的唯一职责

- 你只负责代理端。
- 你只负责：
  - 新建申请
  - 我的申请列表
  - 申请详情
  - 文件上传
  - 补件
  - 提交申请
  - 撤销申请
  - 候补确认
  - 调剂接受
  - 调剂拒绝

## 五、你负责的页面

- `/applications/create`
- `/applications`
- `/applications/:id`

## 六、你负责的前端文件

- `src/views/applications/ApplicationCreatePage.vue`
- `src/views/applications/ApplicationListPage.vue`
- `src/views/applications/ApplicationDetailPage.vue`
- `src/api/applications/service.ts`
- `src/api/files/service.ts`

## 七、你绝对不能改的文件

- `src/router/index.ts`
- `src/stores/auth.ts`
- `src/api/request.ts`
- `src/components/common/*`
- `src/layouts/*`
- `src/views/login/*`
- `src/views/dashboard/*`
- `src/views/domesticreviews/*`
- `src/views/schoolreviews/*`
- `src/views/waitlists/*`
- `src/views/admin/*`

## 八、你能使用的测试账号

- 代理端联调账号：
  - `agent01 / agent123`
- 管理员账号只用于查看，不用于你自己的正常开发：
  - `admin / admin123`

## 九、你必须知道的首次登录规则

- 所有初始化账号默认 `mustChangePassword = true`
- 所以你第一次登录后：
  1. 先调 `POST /api/auth/login`
  2. 再调 `POST /api/auth/change-password`
  3. 再调 `GET /api/auth/me`
- 不做这一步，后续很多接口会被后端拦住

## 十、你只允许调用这些真实接口

- `POST /api/auth/login`
- `POST /api/auth/change-password`
- `GET /api/auth/me`
- `POST /api/files/transcripts`
- `POST /api/files/attachments`
- `POST /api/applications`
- `GET /api/applications/my`
- `GET /api/applications/{id}`
- `POST /api/applications/{id}/supplement`
- `POST /api/applications/{id}/submit`
- `POST /api/applications/{id}/cancel`
- `POST /api/applications/{id}/waitlist-confirm`
- `POST /api/applications/{id}/accept-adjustment`
- `POST /api/applications/{id}/reject-adjustment`

## 十一、你绝对不能调用这些不存在的接口

- `/api/dashboard/**`
- `/api/domesticreviews/pending`
- `/api/domesticreviews/{id}`
- `/api/domesticreviews/records`
- `/api/school-reviews/pending`
- `/api/school-reviews/{id}`
- `/api/school-reviews/records`
- `/api/users/{id}/reset-password`

## 十二、你必须用的固定联调数据

- `124`：补件场景
- `128`：候补确认场景
- `130`：调剂建议场景
- `132`：撤销终态场景
- `142`：第二组补件
- `144`：第二组调剂建议

## 十三、每个固定数据你要拿来做什么

### `124` / `142`

- 打开详情页
- 做补件上传
- 再次提交申请

### `128`

- 候补待确认详情展示
- 点“接受候补”或“拒绝候补”

### `130` / `144`

- 查看调剂建议
- 做“拒绝调剂”
- 如后端已生成调剂草稿，再做“接受调剂”

### `132`

- 查看撤销终态展示
- 验证撤销后状态和文案

## 十四、你必须遵守的页面规则

- `/applications/create` 必须真实联调
- `/applications` 必须真实联调
- `/applications/:id` 必须真实联调
- 你不能做任何占位页冒充真实页面
- 你不能因为接口还没完全熟就先写假数据版本再说“后面再接”

## 十五、你必须遵守的开发顺序

### 第一步：登录闭环

- 跑通登录
- 跑通首次改密
- 跑通 `me`

### 第二步：创建页

- 上传成绩单
- 上传附件
- 创建申请草稿

### 第三步：列表页

- 我的申请列表
- 状态筛选
- 提交入口
- 撤销入口

### 第四步：详情页

- 详情展示
- 补件
- 候补确认
- 调剂接受/拒绝

## 十六、你开发时必须知道的业务边界

- 申请状态不是你决定的，是后端返回的
- 你不能在前端写一套“如果我觉得该变成某状态就显示某状态”
- 你不能手工拼“审核通过”“候补中”“调剂成功”这类结果
- 一切状态展示只认后端返回

## 十七、你绝对不能做的事

- 不允许改数据库
- 不允许直接 update `applications.current_status`
- 不允许手工造候补或调剂数据
- 不允许改公共组件 props
- 不允许改路由守卫
- 不允许改 request 拦截器
- 不允许顺手修别人的页面

## 十八、你提交前必须自查

- 只改了自己的页面和 service 文件
- 没有改公共层
- 没有调用不存在的接口
- 所有动作都用固定账号和固定 applicationId 跑过
- 页面上的状态文案来自真实接口返回

## 十九、你的完成标准

- 以 [模块验收清单.md](D:/大二下/软件开发与设计/实践二/模块验收清单.md:1) 中 A 模块验收为准
- 最低通过线：
  - 登录改密可用
  - 创建申请可用
  - 我的申请可用
  - 详情可用
  - 补件可用
  - 提交可用
  - 撤销可用
  - 候补确认可用
  - 调剂接受/拒绝可用

## 二十、你最容易踩的坑

- 把 PRD 里的页面能力当成全部已实现
- 看到某个按钮就想自己补一个后端没有的接口
- 为了赶进度去改公共组件
- 直接拿假数据把页面糊出来
- 不先做改密，后面接口全被拦

## 二十一、你和负责人沟通时只需要说这些

- 我改了哪些页面
- 我改了哪些接口 service
- 我用了哪个固定 applicationId 验证
- 哪个接口返回和页面需求不匹配
- 我没有改公共层

## 二十二、结论

- 你现在可以正式开始。
- 你的唯一目标是把代理端三页和对应真实接口稳定联调起来，别越界。

