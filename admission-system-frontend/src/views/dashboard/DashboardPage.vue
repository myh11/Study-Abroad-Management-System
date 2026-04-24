<template>
  <section class="page-shell">
    <header class="page-header">
      <div>
        <h1 class="page-title">工作台</h1>
        <p class="page-subtitle">
          当前后端未开放 `dashboard` 汇总接口，本页只保留统一入口和信息架构，不自行编造业务数据。
        </p>
      </div>
    </header>

    <div class="metric-grid">
      <article class="metric-card">
        <div class="metric-label">当前能力</div>
        <div class="metric-value">真实联调</div>
        <div class="hint-text">登录、申请、审核、候补、配额等核心链路已接真实接口。</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">仪表盘状态</div>
        <div class="metric-value">占位中</div>
        <div class="hint-text">待后端开放汇总接口后再补统计卡片与提醒队列。</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">联调准则</div>
        <div class="metric-value">只认 v4</div>
        <div class="hint-text">页面和数据都以后端接口文档 v4 与固定测试数据为准。</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">状态约束</div>
        <div class="metric-value">统一状态机</div>
        <div class="hint-text">不允许前端直改状态，只能通过真实命令接口推进业务。</div>
      </article>
    </div>

    <div class="dashboard-grid">
      <el-card shadow="never" class="page-card panel-card">
        <h2 class="section-title">当前角色建议动作</h2>
        <p class="section-subtitle">按冻结页与 ownership 表执行，不跨模块写页面和接口。</p>
        <ul class="task-list">
          <li>代理端仅处理申请、补件、候补确认、调剂确认。</li>
          <li>国内审核只做详情、认领、提交，不自行扩列表接口。</li>
          <li>学校审核与管理端以后端真实接口为准，未实现模块只保留占位。</li>
        </ul>
      </el-card>

      <el-card shadow="never" class="page-card panel-card">
        <h2 class="section-title">当前未开放接口</h2>
        <p class="section-subtitle">这些能力不在当前迭代里强行 mock。</p>
        <div class="api-list">
          <span class="status-pill warning">/api/dashboard/**</span>
          <span class="status-pill warning">/api/school-reviews/pending</span>
          <span class="status-pill warning">/api/school-reviews/{id}</span>
        </div>
      </el-card>
    </div>
  </section>
</template>

<style scoped>
.dashboard-grid {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 20px;
}

.panel-card {
  padding: 26px;
}

.task-list {
  display: grid;
  gap: 12px;
  padding-left: 20px;
  margin: 22px 0 0;
  color: #465a79;
  line-height: 1.7;
}

.api-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 22px;
}

@media (max-width: 1200px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}
</style>
