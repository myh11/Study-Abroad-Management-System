<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AppTable from '../../components/common/AppTable.vue'
import AppLoading from '../../components/common/AppLoading.vue'
import { getMyApplications } from '../../api/applications/service'
import type { ApplicationListItem } from '../../types/application'

const loading = ref(false)
const rows = ref<ApplicationListItem[]>([])

onMounted(async () => {
  loading.value = true
  try {
    rows.value = await getMyApplications()
  } finally {
    loading.value = false
  }
})

const columns = [
  { prop: 'id', label: '申请ID', minWidth: 110 },
  { prop: 'current_status', label: '状态', minWidth: 160 },
  { prop: 'school_code', label: '学校', minWidth: 120 },
  { prop: 'major_code', label: '专业', minWidth: 140 },
  { prop: 'submitted_at', label: '提交时间', minWidth: 180 },
]
</script>

<template>
  <section class="page-shell">
    <header class="page-header">
      <div>
        <h1 class="page-title">代理申请列表</h1>
        <p class="page-subtitle">已接 `/api/applications/my`，列表字段按当前后端返回结构组织。</p>
      </div>
    </header>

    <el-card shadow="never" class="page-card">
      <AppLoading :loading="loading">
        <AppTable :columns="columns" :data="rows" />
      </AppLoading>
    </el-card>
  </section>
</template>
