<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import AppLoading from '../../components/common/AppLoading.vue'
import AppTable from '../../components/common/AppTable.vue'
import { getApplicationDetail } from '../../api/applications/service'
import type { ApplicationDetail } from '../../types/application'

const route = useRoute()
const loading = ref(false)
const detail = ref<ApplicationDetail | null>(null)

const historyRows = computed(() => detail.value?.status_histories || [])
const auditRows = computed(() => detail.value?.audit_logs || [])

onMounted(async () => {
  loading.value = true
  try {
    detail.value = await getApplicationDetail(Number(route.params.id))
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <section class="page-shell">
    <header class="page-header">
      <div>
        <h1 class="page-title">申请详情</h1>
        <p class="page-subtitle">详情页已对齐当前后端详情聚合接口，包含学生、成绩、材料、规则快照、状态历史、审计日志。</p>
      </div>
    </header>

    <AppLoading :loading="loading">
      <el-card shadow="never" class="page-card">
        <div class="page-grid">
          <div><strong>申请ID：</strong>{{ detail?.id }}</div>
          <div><strong>状态：</strong>{{ detail?.current_status }}</div>
          <div><strong>学校：</strong>{{ detail?.school_code }}</div>
          <div><strong>专业：</strong>{{ detail?.major_code }}</div>
          <div><strong>学生：</strong>{{ detail?.student.full_name }}</div>
          <div><strong>平均分：</strong>{{ detail?.transcript.average_score }}</div>
        </div>
      </el-card>

      <el-card shadow="never" class="page-card">
        <template #header>状态历史</template>
        <AppTable
          :columns="[
            { prop: 'action_name', label: '动作' },
            { prop: 'from_status', label: '原状态' },
            { prop: 'to_status', label: '新状态' },
            { prop: 'operated_at', label: '操作时间', minWidth: 180 },
          ]"
          :data="historyRows"
        />
      </el-card>

      <el-card shadow="never" class="page-card">
        <template #header>审计日志</template>
        <AppTable
          :columns="[
            { prop: 'action_name', label: '动作' },
            { prop: 'entity_type', label: '实体类型' },
            { prop: 'entity_id', label: '实体ID' },
            { prop: 'operated_at', label: '操作时间', minWidth: 180 },
          ]"
          :data="auditRows"
        />
      </el-card>
    </AppLoading>
  </section>
</template>
