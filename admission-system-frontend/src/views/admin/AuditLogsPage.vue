<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { getAuditLogs, getStatusHistories } from '../../api/auditlogs/service'

interface AuditLogRow {
  id: number
  entity_type: string
  entity_id: string
  action_name: string
  operated_by: number
  operated_at: string
  detail?: string | null
}

interface StatusHistoryRow {
  id: number
  from_status?: string | null
  to_status: string
  action_name: string
  operated_by: number
  operated_at: string
}

const loading = ref(false)
const logs = ref<AuditLogRow[]>([])
const histories = ref<StatusHistoryRow[]>([])

const filterForm = reactive({
  entityType: '',
  entityId: '',
  operatorId: undefined as number | undefined,
  limit: 100,
  applicationId: undefined as number | undefined,
})

async function loadData() {
  loading.value = true
  try {
    logs.value = (await getAuditLogs({
      entityType: filterForm.entityType || undefined,
      entityId: filterForm.entityId || undefined,
      operatorId: filterForm.operatorId,
      limit: filterForm.limit,
    })) as AuditLogRow[]

    histories.value = filterForm.applicationId
      ? ((await getStatusHistories(filterForm.applicationId)) as StatusHistoryRow[])
      : []
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <section class="page-shell">
    <header class="page-header">
      <div>
        <h1 class="page-title">日志查询</h1>
        <p class="page-subtitle">当前同时支持审计日志与申请状态历史查询，方便排查状态异常和管理操作。</p>
      </div>
    </header>

    <el-card shadow="never" class="page-card card-body">
      <div class="filters">
        <el-input v-model="filterForm.entityType" clearable placeholder="实体类型，如 APPLICATION" />
        <el-input v-model="filterForm.entityId" clearable placeholder="实体 ID" />
        <el-input-number v-model="filterForm.operatorId" :min="1" placeholder="操作人 ID" />
        <el-input-number v-model="filterForm.applicationId" :min="1" placeholder="状态历史 applicationId" />
        <el-input-number v-model="filterForm.limit" :min="1" :max="200" placeholder="数量上限" />
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>
    </el-card>

    <div class="log-grid" v-loading="loading">
      <el-card shadow="never" class="page-card card-body">
        <div class="table-toolbar">
          <div>
            <h2 class="section-title">审计日志</h2>
            <p class="section-subtitle">对应 `/api/audit-logs`。</p>
          </div>
        </div>
        <el-table :data="logs" border>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="entity_type" label="实体类型" width="140" />
          <el-table-column prop="entity_id" label="实体 ID" width="120" />
          <el-table-column prop="action_name" label="动作" min-width="180" />
          <el-table-column prop="operated_by" label="操作人" width="100" />
          <el-table-column prop="operated_at" label="时间" min-width="180" />
          <el-table-column prop="detail" label="详情" min-width="240" />
        </el-table>
      </el-card>

      <el-card shadow="never" class="page-card card-body">
        <div class="table-toolbar">
          <div>
            <h2 class="section-title">状态历史</h2>
            <p class="section-subtitle">仅当填写 `applicationId` 时才查询 `/api/audit-logs/status-histories`。</p>
          </div>
        </div>
        <el-table :data="histories" border>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="from_status" label="原状态" min-width="140" />
          <el-table-column prop="to_status" label="新状态" min-width="140" />
          <el-table-column prop="action_name" label="动作" min-width="180" />
          <el-table-column prop="operated_by" label="操作人" width="100" />
          <el-table-column prop="operated_at" label="时间" min-width="180" />
        </el-table>
      </el-card>
    </div>
  </section>
</template>

<style scoped>
.card-body {
  padding: 24px;
}

.filters {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr)) auto;
  gap: 12px;
}

.log-grid {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 20px;
}

@media (max-width: 1380px) {
  .filters,
  .log-grid {
    grid-template-columns: 1fr;
  }
}
</style>
