<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppButton from '../../components/common/AppButton.vue'
import { getDashboardSummary, type DashboardMetric } from '../../api/dashboard/service'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const metrics = ref<DashboardMetric[]>([])

const roleLabelMap: Record<string, string> = {
  ADMIN: '管理员',
  AGENT: '代理专员',
  DOMESTIC_REVIEWER: '国内审核员',
  SCHOOL_REVIEWER: '学校专员',
}

const quickActions = computed(() => {
  const role = authStore.currentUser?.role

  if (role === 'AGENT') {
    return [
      { label: '新建申请', hint: '进入代理申请创建页', path: '/applications/create' },
      { label: '我的申请', hint: '查看当前账号名下申请', path: '/applications' },
      { label: '候补管理', hint: '查看候补确认链路', path: '/waitlists' },
    ]
  }

  if (role === 'DOMESTIC_REVIEWER') {
    return [
      { label: '国内审核任务池', hint: '查看正式审核列表', path: '/domesticreviews' },
      { label: '审核详情示例', hint: '进入当前真实列表中的申请详情', path: '/domesticreviews' },
    ]
  }

  if (role === 'SCHOOL_REVIEWER') {
    return [
      { label: '学校审核队列', hint: '查看当前学校的正式任务列表', path: '/schoolreviews' },
      { label: '候补管理', hint: '查看候补详情与推进', path: '/waitlists' },
    ]
  }

  return [
    { label: '批次管理', hint: '查看和维护批次', path: '/admin/batches' },
    { label: '学校专业配置', hint: '查看学校与专业主数据', path: '/admin/schools-majors' },
    { label: '用户管理', hint: '管理账号与首次改密标记', path: '/admin/users' },
    { label: '日志查询', hint: '排查状态流转与审计记录', path: '/admin/audit-logs' },
  ]
})

const currentRoleLabel = computed(() => roleLabelMap[authStore.currentUser?.role || ''] || '当前角色')

async function loadSummary() {
  loading.value = true
  try {
    const result = await getDashboardSummary()
    metrics.value = result.metrics || []
  } finally {
    loading.value = false
  }
}

function go(path: string) {
  router.push(path)
}

onMounted(loadSummary)
</script>

<template>
  <section class="page-shell">
    <header class="page-header">
      <div>
        <h1 class="page-title">工作台</h1>
        <p class="page-subtitle">首页已接入后端 `dashboard` 汇总接口，按当前角色展示真实统计与主流程入口。</p>
      </div>
      <AppButton type="primary" :loading="loading" @click="loadSummary">刷新统计</AppButton>
    </header>

    <div class="metric-grid">
      <article class="metric-card">
        <div class="metric-label">当前角色</div>
        <div class="metric-value">{{ currentRoleLabel }}</div>
        <div class="hint-text">首页统计会随当前账号角色切换。</div>
      </article>
      <article v-for="metric in metrics" :key="metric.key" class="metric-card">
        <div class="metric-label">{{ metric.label }}</div>
        <div class="metric-value">{{ metric.value }}</div>
        <div class="hint-text">来自后端实时统计。</div>
      </article>
    </div>

    <div class="dashboard-grid">
      <el-card shadow="never" class="page-card panel-card">
        <h2 class="section-title">当前角色快捷入口</h2>
        <p class="section-subtitle">登录后先从这里进核心流程，比在侧边栏里挨个试更稳。</p>
        <div class="quick-grid">
          <button v-for="item in quickActions" :key="item.label" class="quick-card" @click="go(item.path)">
            <strong>{{ item.label }}</strong>
            <span>{{ item.hint }}</span>
          </button>
        </div>
      </el-card>

      <el-card shadow="never" class="page-card panel-card">
        <h2 class="section-title">当前系统说明</h2>
        <p class="section-subtitle">这几个页面现在都已接真实后端接口，不再依赖纯说明占位。</p>
        <div class="api-list">
          <span class="status-pill warning">/api/dashboard/summary</span>
          <span class="status-pill warning">/api/domesticreviews</span>
          <span class="status-pill warning">/api/school-reviews</span>
        </div>
        <div class="helper-action">
          <AppButton type="primary" @click="go(quickActions[0]?.path || '/dashboard')">进入当前主流程</AppButton>
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

.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin-top: 20px;
}

.quick-card {
  display: grid;
  gap: 8px;
  padding: 18px;
  border: 1px solid #dbe4f1;
  border-radius: 12px;
  background: #f8fbff;
  color: #1f3252;
  text-align: left;
  cursor: pointer;
}

.quick-card span {
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

.api-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 22px;
}

.helper-action {
  margin-top: 24px;
}

@media (max-width: 1200px) {
  .dashboard-grid,
  .quick-grid {
    grid-template-columns: 1fr;
  }
}
</style>
