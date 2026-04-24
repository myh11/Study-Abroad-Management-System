<template>
  <section class="page-shell">
    <header class="page-header">
      <div>
        <h1 class="page-title">学校审核队列</h1>
        <p class="page-subtitle">
          当前后端还未开放学校审核列表接口，本页保留原型视觉结构与操作说明，不自行伪造队列数据。
        </p>
      </div>
    </header>

    <div class="metric-grid">
      <article class="metric-card">
        <div class="metric-label">真实可联调页</div>
        <div class="metric-value">详情页</div>
        <div class="hint-text">使用 `/applications/{id}` 拉申请详情，再调用 `/school-reviews/{id}/submit` 提交决策。</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">当前未开放</div>
        <div class="metric-value">列表接口</div>
        <div class="hint-text">`/api/school-reviews/pending` 与详情查询接口仍未实现。</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">建议联调数据</div>
        <div class="metric-value">SCHOOL_REVIEWING</div>
        <div class="hint-text">从固定测试数据里挑处于学校审核中的申请直接进入详情页联调。</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">决策结果</div>
        <div class="metric-value">4 类</div>
        <div class="hint-text">录取、候补、建议调剂、拒绝都走同一个提交接口。</div>
      </article>
    </div>

    <el-card shadow="never" class="page-card list-card">
      <div class="table-toolbar">
        <div>
          <h2 class="section-title">占位队列说明</h2>
          <p class="section-subtitle">学校审核列表页只保留结构，不承诺数据展示。</p>
        </div>
        <router-link to="/schoolreviews/128">
          <el-button type="primary">直接进入示例详情</el-button>
        </router-link>
      </div>

      <el-alert type="warning" :closable="false" show-icon>
        <template #title>
          当前仅学校审核详情页做真实联调。本页用于保留路由、菜单入口与原型布局，不对外展示伪队列。
        </template>
      </el-alert>
    </el-card>
  </section>
</template>

<style scoped>
.list-card {
  padding: 28px;
}
</style>
