<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import {
  createMajor,
  getMajors,
  updateMajor,
  updateMajorStatus,
  type MajorPayload,
} from '../../api/majors/service'
import {
  createSchool,
  getSchools,
  updateSchool,
  updateSchoolStatus,
  type SchoolPayload,
} from '../../api/schools/service'
import { toast } from '../../components/common/toast'

interface SchoolRow {
  school_code: string
  school_name: string
  is_enabled: number | boolean
}

interface MajorRow {
  major_code: string
  school_code: string
  major_name: string
  reserve_line?: number | null
  waitlist_line?: number | null
  allow_adjustment_in?: number | boolean
  is_enabled?: number | boolean
}

const tab = ref<'schools' | 'majors'>('schools')
const loading = ref(false)
const schoolDialogVisible = ref(false)
const majorDialogVisible = ref(false)
const schoolEditingCode = ref<string | null>(null)
const majorEditingCode = ref<string | null>(null)
const schools = ref<SchoolRow[]>([])
const majors = ref<MajorRow[]>([])

const schoolForm = reactive<SchoolPayload>({
  schoolCode: '',
  schoolName: '',
  enabled: true,
})

const majorForm = reactive<MajorPayload>({
  majorCode: '',
  schoolCode: '',
  majorName: '',
  minAverageScore: null,
  minMathScore: null,
  minEnglishScore: null,
  minPhysicsScore: null,
  minLiberalArtsScore: null,
  reserveLine: null,
  waitlistLine: null,
  allowAdjustmentIn: false,
  enabled: true,
})

const schoolOptions = computed(() =>
  schools.value.map((item) => ({ label: `${item.school_code} - ${item.school_name}`, value: item.school_code })),
)

function resetSchoolForm() {
  schoolEditingCode.value = null
  schoolForm.schoolCode = ''
  schoolForm.schoolName = ''
  schoolForm.enabled = true
}

function resetMajorForm() {
  majorEditingCode.value = null
  majorForm.majorCode = ''
  majorForm.schoolCode = ''
  majorForm.majorName = ''
  majorForm.minAverageScore = null
  majorForm.minMathScore = null
  majorForm.minEnglishScore = null
  majorForm.minPhysicsScore = null
  majorForm.minLiberalArtsScore = null
  majorForm.reserveLine = null
  majorForm.waitlistLine = null
  majorForm.allowAdjustmentIn = false
  majorForm.enabled = true
}

async function loadData() {
  loading.value = true
  try {
    schools.value = (await getSchools()) as SchoolRow[]
    majors.value = (await getMajors()) as MajorRow[]
  } finally {
    loading.value = false
  }
}

function openSchoolDialog(row?: SchoolRow) {
  resetSchoolForm()
  if (row) {
    schoolEditingCode.value = row.school_code
    schoolForm.schoolCode = row.school_code
    schoolForm.schoolName = row.school_name
    schoolForm.enabled = Boolean(row.is_enabled)
  }
  schoolDialogVisible.value = true
}

function openMajorDialog(row?: MajorRow) {
  resetMajorForm()
  if (row) {
    majorEditingCode.value = row.major_code
    majorForm.majorCode = row.major_code
    majorForm.schoolCode = row.school_code
    majorForm.majorName = row.major_name
    majorForm.reserveLine = row.reserve_line ?? null
    majorForm.waitlistLine = row.waitlist_line ?? null
    majorForm.allowAdjustmentIn = Boolean(row.allow_adjustment_in)
    majorForm.enabled = Boolean(row.is_enabled)
  }
  majorDialogVisible.value = true
}

async function saveSchool() {
  if (schoolEditingCode.value) {
    await updateSchool(schoolEditingCode.value, {
      schoolName: schoolForm.schoolName,
      enabled: schoolForm.enabled,
    })
    toast.success('学校已更新')
  } else {
    await createSchool({ ...schoolForm })
    toast.success('学校已创建')
  }
  schoolDialogVisible.value = false
  await loadData()
}

async function saveMajor() {
  if (majorEditingCode.value) {
    await updateMajor(majorEditingCode.value, {
      schoolCode: majorForm.schoolCode,
      majorName: majorForm.majorName,
      minAverageScore: majorForm.minAverageScore,
      minMathScore: majorForm.minMathScore,
      minEnglishScore: majorForm.minEnglishScore,
      minPhysicsScore: majorForm.minPhysicsScore,
      minLiberalArtsScore: majorForm.minLiberalArtsScore,
      reserveLine: majorForm.reserveLine,
      waitlistLine: majorForm.waitlistLine,
      allowAdjustmentIn: majorForm.allowAdjustmentIn,
      enabled: majorForm.enabled,
    })
    toast.success('专业已更新')
  } else {
    await createMajor({ ...majorForm })
    toast.success('专业已创建')
  }
  majorDialogVisible.value = false
  await loadData()
}

async function toggleSchoolStatus(row: SchoolRow) {
  await updateSchoolStatus(row.school_code, Boolean(row.is_enabled) ? 'DISABLED' : 'ENABLED')
  toast.success('学校状态已更新')
  await loadData()
}

async function toggleMajorStatus(row: MajorRow) {
  await updateMajorStatus(row.major_code, Boolean(row.is_enabled) ? 'DISABLED' : 'ENABLED')
  toast.success('专业状态已更新')
  await loadData()
}

onMounted(loadData)
</script>

<template>
  <section class="page-shell">
    <header class="page-header">
      <div>
        <h1 class="page-title">学校与专业配置</h1>
        <p class="page-subtitle">学校和专业当前合并在一个管理页，所有增改和启停都走真实管理端接口。</p>
      </div>
    </header>

    <el-card shadow="never" class="page-card card-body" v-loading="loading">
      <el-tabs v-model="tab">
        <el-tab-pane label="学校配置" name="schools">
          <div class="table-toolbar">
            <div class="table-meta">学校状态接口使用统一 `status` 请求体，不直接写库。</div>
            <el-button type="primary" :icon="Plus" @click="openSchoolDialog()">新增学校</el-button>
          </div>
          <el-table :data="schools" border>
            <el-table-column prop="school_code" label="学校代码" width="140" />
            <el-table-column prop="school_name" label="学校名称" min-width="220" />
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <span class="status-pill" :class="Boolean(row.is_enabled) ? 'success' : 'danger'">
                  {{ Boolean(row.is_enabled) ? 'ENABLED' : 'DISABLED' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220">
              <template #default="{ row }">
                <div class="row-actions">
                  <el-button link type="primary" @click="openSchoolDialog(row)">编辑</el-button>
                  <el-button link @click="toggleSchoolStatus(row)">
                    {{ Boolean(row.is_enabled) ? '停用' : '启用' }}
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="专业配置" name="majors">
          <div class="table-toolbar">
            <div class="table-meta">专业字段含阈值、候补线和是否允许调剂。</div>
            <el-button type="primary" :icon="Plus" @click="openMajorDialog()">新增专业</el-button>
          </div>
          <el-table :data="majors" border>
            <el-table-column prop="major_code" label="专业代码" width="160" />
            <el-table-column prop="school_code" label="学校代码" width="140" />
            <el-table-column prop="major_name" label="专业名称" min-width="220" />
            <el-table-column prop="reserve_line" label="录取线" width="100" />
            <el-table-column prop="waitlist_line" label="候补线" width="100" />
            <el-table-column label="调剂" width="100">
              <template #default="{ row }">
                {{ Boolean(row.allow_adjustment_in) ? '允许' : '关闭' }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <span class="status-pill" :class="Boolean(row.is_enabled) ? 'success' : 'danger'">
                  {{ Boolean(row.is_enabled) ? 'ENABLED' : 'DISABLED' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220">
              <template #default="{ row }">
                <div class="row-actions">
                  <el-button link type="primary" @click="openMajorDialog(row)">编辑</el-button>
                  <el-button link @click="toggleMajorStatus(row)">
                    {{ Boolean(row.is_enabled) ? '停用' : '启用' }}
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="schoolDialogVisible" :title="schoolEditingCode ? '编辑学校' : '新增学校'" width="520px">
      <el-form label-position="top">
        <el-form-item label="学校代码">
          <el-input v-model="schoolForm.schoolCode" :disabled="Boolean(schoolEditingCode)" />
        </el-form-item>
        <el-form-item label="学校名称">
          <el-input v-model="schoolForm.schoolName" />
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="schoolForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="schoolDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveSchool">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="majorDialogVisible" :title="majorEditingCode ? '编辑专业' : '新增专业'" width="680px">
      <el-form label-position="top">
        <div class="form-grid">
          <el-form-item label="专业代码">
            <el-input v-model="majorForm.majorCode" :disabled="Boolean(majorEditingCode)" />
          </el-form-item>
          <el-form-item label="归属学校">
            <el-select v-model="majorForm.schoolCode">
              <el-option v-for="item in schoolOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="专业名称">
            <el-input v-model="majorForm.majorName" />
          </el-form-item>
          <el-form-item label="平均分阈值">
            <el-input-number v-model="majorForm.minAverageScore" :min="0" :max="100" />
          </el-form-item>
          <el-form-item label="录取线">
            <el-input-number v-model="majorForm.reserveLine" :min="0" :max="100" />
          </el-form-item>
          <el-form-item label="候补线">
            <el-input-number v-model="majorForm.waitlistLine" :min="0" :max="100" />
          </el-form-item>
        </div>
        <el-form-item label="允许调剂进入">
          <el-switch v-model="majorForm.allowAdjustmentIn" />
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="majorForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="majorDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMajor">保存</el-button>
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

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}
</style>
