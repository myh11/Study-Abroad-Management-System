<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  adjustMajorQuota,
  adjustSchoolQuota,
  getQuotaAdjustments,
  getQuotas,
  type MajorQuotaAdjustPayload,
  type QuotaListResult,
  type SchoolQuotaAdjustPayload,
} from '../../api/quotas/service'
import { getBatches } from '../../api/batches/service'
import { getSchools } from '../../api/schools/service'
import { toast } from '../../components/common/toast'

interface SchoolQuotaRow {
  id: number
  batch_id: number
  school_code: string
  total_quota: number
  used_quota: number
  remaining_quota: number
  school_min_score?: number | null
}

interface MajorQuotaRow {
  id: number
  batch_id: number
  school_code: string
  major_code: string
  total_quota: number
  used_quota: number
  remaining_quota: number
  reserve_line?: number | null
  waitlist_line?: number | null
  allow_adjustment_in?: number | boolean
}

interface QuotaLogRow {
  id: number
  operator_id: number
  operator_role: string
  entity_type: string
  entity_id: string
  operation_type: string
  remark?: string | null
  created_at: string
}

const loading = ref(false)
const dialogVisible = ref(false)
const activeTab = ref<'schools' | 'majors' | 'logs'>('schools')
const quotaType = ref<'school' | 'major'>('school')
const currentQuotaId = ref<number | null>(null)

const schoolRows = ref<SchoolQuotaRow[]>([])
const majorRows = ref<MajorQuotaRow[]>([])
const logRows = ref<QuotaLogRow[]>([])
const batchOptions = ref<{ id: number; batch_name: string }[]>([])
const schoolOptions = ref<{ school_code: string; school_name: string }[]>([])

const filterForm = reactive({
  batchId: undefined as number | undefined,
  schoolCode: '',
  majorCode: '',
})

const schoolAdjustForm = reactive<SchoolQuotaAdjustPayload>({
  totalQuota: undefined,
  schoolMinScore: undefined,
  schoolMinMath: undefined,
  schoolMinEnglish: undefined,
  remark: '',
})

const majorAdjustForm = reactive<MajorQuotaAdjustPayload>({
  totalQuota: undefined,
  minAverageScore: undefined,
  minMathScore: undefined,
  minEnglishScore: undefined,
  minPhysicsScore: undefined,
  minLiberalArtsScore: undefined,
  reserveLine: undefined,
  waitlistLine: undefined,
  allowAdjustmentIn: false,
  remark: '',
})

const summary = computed(() => {
  const schoolTotal = schoolRows.value.reduce((sum, item) => sum + Number(item.total_quota || 0), 0)
  const schoolUsed = schoolRows.value.reduce((sum, item) => sum + Number(item.used_quota || 0), 0)
  const schoolRemaining = schoolRows.value.reduce((sum, item) => sum + Number(item.remaining_quota || 0), 0)
  return [
    { label: '学校总配额', value: schoolTotal, tone: 'info' },
    { label: '已占位', value: schoolUsed, tone: 'success' },
    { label: '剩余名额', value: schoolRemaining, tone: 'warning' },
    { label: '调整日志', value: logRows.value.length, tone: 'info' },
  ]
})

async function loadData() {
  loading.value = true
  try {
    const [quotaData, logs, batches, schools] = await Promise.all([
      getQuotas({
        batchId: filterForm.batchId,
        schoolCode: filterForm.schoolCode || undefined,
      }),
      getQuotaAdjustments({
        batchId: filterForm.batchId,
        schoolCode: filterForm.schoolCode || undefined,
        majorCode: filterForm.majorCode || undefined,
      }),
      getBatches(),
      getSchools(),
    ])
    const typedQuota = quotaData as QuotaListResult
    schoolRows.value = (typedQuota.schoolQuotas || []) as SchoolQuotaRow[]
    majorRows.value = (typedQuota.majorQuotas || []) as MajorQuotaRow[]
    logRows.value = (logs || []) as QuotaLogRow[]
    batchOptions.value = (batches as { id: number; batch_name: string }[]) || []
    schoolOptions.value = (schools as { school_code: string; school_name: string }[]) || []
  } finally {
    loading.value = false
  }
}

function openSchoolDialog(row: SchoolQuotaRow) {
  quotaType.value = 'school'
  currentQuotaId.value = row.id
  schoolAdjustForm.totalQuota = row.total_quota
  schoolAdjustForm.schoolMinScore = row.school_min_score ?? undefined
  schoolAdjustForm.schoolMinMath = undefined
  schoolAdjustForm.schoolMinEnglish = undefined
  schoolAdjustForm.remark = ''
  dialogVisible.value = true
}

function openMajorDialog(row: MajorQuotaRow) {
  quotaType.value = 'major'
  currentQuotaId.value = row.id
  majorAdjustForm.totalQuota = row.total_quota
  majorAdjustForm.reserveLine = row.reserve_line ?? undefined
  majorAdjustForm.waitlistLine = row.waitlist_line ?? undefined
  majorAdjustForm.allowAdjustmentIn = Boolean(row.allow_adjustment_in)
  majorAdjustForm.remark = ''
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!currentQuotaId.value) return
  if (quotaType.value === 'school') {
    await adjustSchoolQuota(currentQuotaId.value, { ...schoolAdjustForm })
  } else {
    await adjustMajorQuota(currentQuotaId.value, { ...majorAdjustForm })
  }
  toast.success('配额已调整')
  dialogVisible.value = false
  await loadData()
}

onMounted(loadData)
</script>

<template>
  <section class="page-shell">
    <header class="page-header">
      <div>
        <h1 class="page-title">配额管理</h1>
        <p class="page-subtitle">学校配额、专业配额和调整日志都对接真实管理端接口，不做本地假计算。</p>
      </div>
    </header>

    <div class="metric-grid">
      <article v-for="item in summary" :key="item.label" class="metric-card">
        <div class="metric-label">{{ item.label }}</div>
        <div class="metric-value">{{ item.value }}</div>
      </article>
    </div>

    <el-card shadow="never" class="page-card card-body" v-loading="loading">
      <div class="filters">
        <el-select v-model="filterForm.batchId" clearable placeholder="按批次筛选">
          <el-option v-for="item in batchOptions" :key="item.id" :label="item.batch_name" :value="item.id" />
        </el-select>
        <el-select v-model="filterForm.schoolCode" clearable placeholder="按学校筛选">
          <el-option
            v-for="item in schoolOptions"
            :key="item.school_code"
            :label="`${item.school_code} - ${item.school_name}`"
            :value="item.school_code"
          />
        </el-select>
        <el-input v-model="filterForm.majorCode" clearable placeholder="按专业代码筛选日志" />
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="学校配额" name="schools">
          <el-table :data="schoolRows" border>
            <el-table-column prop="batch_id" label="批次" width="90" />
            <el-table-column prop="school_code" label="学校" width="140" />
            <el-table-column prop="total_quota" label="总配额" width="110" />
            <el-table-column prop="used_quota" label="已占位" width="110" />
            <el-table-column prop="remaining_quota" label="剩余" width="110" />
            <el-table-column prop="school_min_score" label="学校最低分" width="130" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button link type="primary" @click="openSchoolDialog(row)">调整</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="专业配额" name="majors">
          <el-table :data="majorRows" border>
            <el-table-column prop="batch_id" label="批次" width="90" />
            <el-table-column prop="school_code" label="学校" width="140" />
            <el-table-column prop="major_code" label="专业" width="160" />
            <el-table-column prop="total_quota" label="总配额" width="110" />
            <el-table-column prop="used_quota" label="已占位" width="110" />
            <el-table-column prop="remaining_quota" label="剩余" width="110" />
            <el-table-column prop="reserve_line" label="录取线" width="100" />
            <el-table-column prop="waitlist_line" label="候补线" width="100" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button link type="primary" @click="openMajorDialog(row)">调整</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="调整日志" name="logs">
          <el-table :data="logRows" border>
            <el-table-column prop="id" label="日志ID" width="90" />
            <el-table-column prop="entity_type" label="实体类型" width="130" />
            <el-table-column prop="entity_id" label="实体ID" width="120" />
            <el-table-column prop="operation_type" label="操作" width="180" />
            <el-table-column prop="operator_role" label="角色" width="150" />
            <el-table-column prop="remark" label="备注" min-width="220" />
            <el-table-column prop="created_at" label="时间" min-width="180" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="quotaType === 'school' ? '调整学校配额' : '调整专业配额'" width="580px">
      <el-form v-if="quotaType === 'school'" label-position="top">
        <el-form-item label="总配额">
          <el-input-number v-model="schoolAdjustForm.totalQuota" :min="0" />
        </el-form-item>
        <el-form-item label="学校最低分">
          <el-input-number v-model="schoolAdjustForm.schoolMinScore" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="调整备注">
          <el-input v-model="schoolAdjustForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>

      <el-form v-else label-position="top">
        <div class="form-grid">
          <el-form-item label="总配额">
            <el-input-number v-model="majorAdjustForm.totalQuota" :min="0" />
          </el-form-item>
          <el-form-item label="录取线">
            <el-input-number v-model="majorAdjustForm.reserveLine" :min="0" :max="100" />
          </el-form-item>
          <el-form-item label="候补线">
            <el-input-number v-model="majorAdjustForm.waitlistLine" :min="0" :max="100" />
          </el-form-item>
          <el-form-item label="允许调剂进入">
            <el-switch v-model="majorAdjustForm.allowAdjustmentIn" />
          </el-form-item>
        </div>
        <el-form-item label="调整备注">
          <el-input v-model="majorAdjustForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.card-body {
  padding: 24px;
}

.filters {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr)) auto;
  gap: 12px;
  margin-bottom: 18px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}
</style>
