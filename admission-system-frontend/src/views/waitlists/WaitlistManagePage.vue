<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { AlarmClock, InfoFilled, Lightning, RefreshRight } from '@element-plus/icons-vue'
import {
  getWaitlistDetail,
  invalidateWaitlist,
  promoteWaitlist,
  type WaitlistDetail,
} from '../../api/waitlists/service'
import { toast } from '../../components/common/toast'
import { useAuthStore } from '../../stores/auth'

const authStore = useAuthStore()
const loading = ref(false)
const actionLoading = ref(false)
const waitlist = ref<WaitlistDetail | null>(null)
const queryForm = reactive({
  applicationId: 128,
})

const canManage = computed(() =>
  ['SCHOOL_REVIEWER', 'ADMIN'].includes(authStore.currentUser?.role || ''),
)

const metricItems = computed(() => {
  if (!waitlist.value) return []
  return [
    { label: '当前排名', value: `#${waitlist.value.current_rank}`, tone: 'info' },
    { label: '候补状态', value: waitlist.value.waitlist_status, tone: 'warning' },
    { label: '综合评分', value: waitlist.value.total_score, tone: 'success' },
    { label: '关键科目', value: waitlist.value.key_subject_score, tone: 'info' },
  ]
})

async function loadWaitlist() {
  loading.value = true
  try {
    waitlist.value = await getWaitlistDetail(queryForm.applicationId)
  } finally {
    loading.value = false
  }
}

function useSample(applicationId: number) {
  queryForm.applicationId = applicationId
  void loadWaitlist()
}

async function handlePromote() {
  if (!waitlist.value) return
  actionLoading.value = true
  try {
    await promoteWaitlist(waitlist.value.application_id)
    toast.success('已触发候补补位')
    await loadWaitlist()
  } finally {
    actionLoading.value = false
  }
}

async function handleInvalidate() {
  if (!waitlist.value) return
  actionLoading.value = true
  try {
    await invalidateWaitlist(waitlist.value.application_id)
    toast.success('候补记录已失效')
    await loadWaitlist()
  } finally {
    actionLoading.value = false
  }
}

onMounted(loadWaitlist)
</script>

<template>
  <section class="page-shell" v-loading="loading">
    <header class="page-header">
      <div>
        <h1 class="page-title">候补管理</h1>
        <p class="page-subtitle">
          当前后端只开放单条候补详情、触发补位、手动失效三个接口。本页严格按固定
          `applicationId` 联调，不伪造候补列表。
        </p>
      </div>
    </header>

    <el-card shadow="never" class="page-card filter-card">
      <div class="table-toolbar">
        <div>
          <h2 class="section-title">按申请编号查询候补</h2>
          <p class="section-subtitle">建议优先使用固定候补联调数据，例如 `128`。</p>
        </div>
        <div class="filter-row">
          <el-input-number v-model="queryForm.applicationId" :min="1" />
          <el-button type="primary" @click="loadWaitlist">查询</el-button>
        </div>
      </div>
      <div class="quick-links">
        <el-tag
          v-for="id in [127, 128, 137, 141]"
          :key="id"
          class="quick-tag"
          effect="plain"
          @click="useSample(id)"
        >
          固定样本 {{ id }}
        </el-tag>
      </div>
    </el-card>

    <el-alert type="info" :closable="false" show-icon>
      <template #title>
        候补排序规则以当前后端实现为准：综合评分优先，其次国内审核时间、关键科目分数、申请提交时间。
      </template>
    </el-alert>

    <div v-if="waitlist" class="metric-grid">
      <article v-for="item in metricItems" :key="item.label" class="metric-card">
        <div class="metric-label">{{ item.label }}</div>
        <div class="metric-value">{{ item.value }}</div>
      </article>
    </div>

    <div v-if="waitlist" class="waitlist-grid">
      <el-card shadow="never" class="page-card detail-card">
        <div class="section-head">
          <div class="section-badge">
            <el-icon><InfoFilled /></el-icon>
            候补详情
          </div>
          <span class="status-pill warning">{{ waitlist.waitlist_status }}</span>
        </div>
        <div class="detail-list">
          <div class="detail-row">
            <span>申请编号</span>
            <strong>{{ waitlist.application_id }}</strong>
          </div>
          <div class="detail-row">
            <span>申请状态</span>
            <strong>{{ waitlist.application_status }}</strong>
          </div>
          <div class="detail-row">
            <span>批次 / 学校 / 专业</span>
            <strong>{{ waitlist.batch_id }} / {{ waitlist.school_code }} / {{ waitlist.major_code }}</strong>
          </div>
          <div class="detail-row">
            <span>排名原因</span>
            <strong>{{ waitlist.rank_reason || '后端未返回' }}</strong>
          </div>
          <div class="detail-row">
            <span>申请提交时间</span>
            <strong>{{ waitlist.application_submitted_at || '暂无' }}</strong>
          </div>
          <div class="detail-row">
            <span>补位触发时间</span>
            <strong>{{ waitlist.promoted_at || '尚未触发' }}</strong>
          </div>
          <div class="detail-row">
            <span>确认截止时间</span>
            <strong>{{ waitlist.confirm_deadline || waitlist.waitlist_confirm_deadline || '暂无' }}</strong>
          </div>
          <div class="detail-row">
            <span>失效时间</span>
            <strong>{{ waitlist.expired_at || '未失效' }}</strong>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="page-card action-card">
        <div class="section-head">
          <div class="section-badge">
            <el-icon><AlarmClock /></el-icon>
            候补动作
          </div>
        </div>
        <p class="action-copy">
          `promote` 会将申请从 `WAITLISTED` 推进到 `WAITLIST_PENDING_CONFIRM` 并预占配额；
          `invalidate` 会关闭当前候补记录并释放配额。
        </p>
        <div class="action-stack">
          <el-button
            type="primary"
            :icon="Lightning"
            :disabled="!canManage"
            :loading="actionLoading"
            @click="handlePromote"
          >
            触发候补补位
          </el-button>
          <el-button
            plain
            :icon="RefreshRight"
            :disabled="!canManage"
            :loading="actionLoading"
            @click="handleInvalidate"
          >
            手动失效
          </el-button>
        </div>
        <el-alert
          v-if="!canManage"
          type="warning"
          :closable="false"
          show-icon
          title="当前账号没有候补管理权限，只允许查看候补详情。"
        />
      </el-card>
    </div>
  </section>
</template>

<style scoped>
.filter-card,
.detail-card,
.action-card {
  padding: 24px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.quick-links {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.quick-tag {
  cursor: pointer;
}

.waitlist-grid {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 20px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.section-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #16396f;
  font-weight: 700;
}

.detail-list {
  display: grid;
  gap: 12px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 16px;
  border-radius: 16px;
  background: #f6f8fc;
}

.detail-row span {
  color: #6d7e98;
  font-size: 13px;
}

.action-copy {
  margin: 0;
  color: #667894;
  line-height: 1.8;
}

.action-stack {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin: 22px 0;
}

@media (max-width: 1180px) {
  .waitlist-grid {
    grid-template-columns: 1fr;
  }

  .filter-row {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
