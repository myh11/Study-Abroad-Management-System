<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { createBatch, getBatches, updateBatch, updateBatchStatus, type BatchPayload } from '../../api/batches/service'
import { toast } from '../../components/common/toast'

interface BatchRow {
  id: number
  batch_name: string
  start_time: string
  end_time: string
  batch_status: 'IN_PROGRESS' | 'FINISHED'
  created_by?: number
  created_at?: string
  updated_at?: string
}

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const rows = ref<BatchRow[]>([])

const formModel = reactive<BatchPayload>({
  batchName: '',
  startTime: '',
  endTime: '',
  batchStatus: 'IN_PROGRESS',
})

const statusOptions = [
  { label: '进行中', value: 'IN_PROGRESS' },
  { label: '已结束', value: 'FINISHED' },
]

const dialogTitle = computed(() => (editingId.value ? '编辑批次' : '新增批次'))

function resetForm() {
  editingId.value = null
  formModel.batchName = ''
  formModel.startTime = ''
  formModel.endTime = ''
  formModel.batchStatus = 'IN_PROGRESS'
}

async function loadRows() {
  loading.value = true
  try {
    rows.value = (await getBatches()) as BatchRow[]
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(row: BatchRow) {
  editingId.value = row.id
  formModel.batchName = row.batch_name
  formModel.startTime = row.start_time?.replace('T', ' ').slice(0, 19) || ''
  formModel.endTime = row.end_time?.replace('T', ' ').slice(0, 19) || ''
  formModel.batchStatus = row.batch_status
  dialogVisible.value = true
}

async function handleSubmit() {
  submitting.value = true
  try {
    if (editingId.value) {
      await updateBatch(editingId.value, { ...formModel })
      toast.success('批次已更新')
    } else {
      await createBatch({ ...formModel })
      toast.success('批次已创建')
    }
    dialogVisible.value = false
    await loadRows()
  } finally {
    submitting.value = false
  }
}

async function handleStatusChange(row: BatchRow) {
  const next = row.batch_status === 'IN_PROGRESS' ? 'FINISHED' : 'IN_PROGRESS'
  await updateBatchStatus(row.id, next)
  toast.success('批次状态已更新')
  await loadRows()
}

onMounted(loadRows)
</script>

<template>
  <section class="page-shell">
    <header class="page-header">
      <div>
        <h1 class="page-title">批次管理</h1>
        <p class="page-subtitle">管理招生批次、起止时间和发布状态，当前全部对接真实批次接口。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreateDialog">新增批次</el-button>
    </header>

    <el-card shadow="never" class="page-card card-body" v-loading="loading">
      <el-table :data="rows" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="batch_name" label="批次名称" min-width="180" />
        <el-table-column prop="start_time" label="开始时间" min-width="180" />
        <el-table-column prop="end_time" label="结束时间" min-width="180" />
        <el-table-column label="状态" width="130">
          <template #default="{ row }">
            <span class="status-pill" :class="row.batch_status === 'IN_PROGRESS' ? 'info' : 'warning'">
              {{ row.batch_status }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
              <el-button link @click="handleStatusChange(row)">
                {{ row.batch_status === 'IN_PROGRESS' ? '结束批次' : '恢复进行中' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px">
      <el-form label-position="top">
        <el-form-item label="批次名称">
          <el-input v-model="formModel.batchName" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-input v-model="formModel.startTime" placeholder="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-input v-model="formModel.endTime" placeholder="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formModel.batchStatus">
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.card-body {
  padding: 24px;
}

.row-actions {
  display: flex;
  gap: 10px;
}
</style>
