<script setup lang="ts">
const metrics = [
  { label: '代理申请', value: '草稿 / 提交 / 调剂' },
  { label: '国内审核', value: '认领 / 补件 / 通过' },
  { label: '学校审核', value: '候补 / 录取 / 拒绝' },
  { label: '管理端', value: '批次 / 配额 / 用户' },
]
</script>

<template>
  <section class="page-shell">
    <header class="page-header">
      <div>
        <h1 class="page-title">首页工作台</h1>
        <p class="page-subtitle">按角色拆分入口，先保证页面、路由、模块 API 和公共组件稳定。</p>
      </div>
    </header>

    <div class="metric-grid">
      <el-card v-for="item in metrics" :key="item.label" shadow="never" class="page-card metric-card">
        <div class="metric-label">{{ item.label }}</div>
        <div class="metric-value">{{ item.value }}</div>
      </el-card>
    </div>

    <el-card shadow="never" class="page-card">
      <div class="page-grid">
        <div>
          <strong>当前前端骨架已对齐</strong>
          <div class="hint-text">路由结构、模块目录、service.ts、统一 Result&lt;T&gt;、公共组件。</div>
        </div>
        <div>
          <strong>联调建议</strong>
          <div class="hint-text">先从登录、我的申请、申请详情、学校审核提交四条链路开始。</div>
        </div>
      </div>
    </el-card>
  </section>
</template>
