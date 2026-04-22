<script setup lang="ts">
import AppButton from '../../components/common/AppButton.vue'
import AppTable from '../../components/common/AppTable.vue'
import AppPagination from '../../components/common/AppPagination.vue'

defineProps<{
  title: string
  subtitle: string
  apiHint: string
  columns?: { prop: string; label: string; minWidth?: number }[]
  rows?: Record<string, unknown>[]
}>()
</script>

<template>
  <section class="page-shell">
    <header class="page-header">
      <div>
        <h1 class="page-title">{{ title }}</h1>
        <p class="page-subtitle">{{ subtitle }}</p>
      </div>
      <AppButton type="primary">预留操作入口</AppButton>
    </header>

    <el-card shadow="never" class="page-card">
      <el-alert type="info" :closable="false" show-icon>
        <template #title>{{ apiHint }}</template>
      </el-alert>
    </el-card>

    <el-card shadow="never" class="page-card">
      <AppTable
        :columns="
          columns || [
            { prop: 'field', label: '字段' },
            { prop: 'value', label: '说明', minWidth: 280 },
          ]
        "
        :data="
          rows || [
            { field: 'status', value: '页面骨架已接公共组件，后续直接填业务字段。' },
            { field: 'api', value: '模块 service.ts 已就位，按后端接口继续补联调。' },
          ]
        "
      />
      <div style="margin-top: 16px; display: flex; justify-content: flex-end">
        <AppPagination :total="2" :page-num="1" :page-size="10" @change="() => undefined" />
      </div>
    </el-card>
  </section>
</template>
