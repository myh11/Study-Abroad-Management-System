<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Files, Histogram, Opportunity, School } from '@element-plus/icons-vue'
import { getApplicationDetail } from '../../api/applications/service'
import { getMajors } from '../../api/majors/service'
import { submitSchoolReview } from '../../api/schoolreviews/service'
import { useAuthStore } from '../../stores/auth'
import { toast } from '../../components/common/toast'

interface MajorOption {
  major_code: string
  major_name: string
}

interface ApplicationDetailView {
  basicInfo?: Record<string, unknown>
  studentInfo?: Record<string, unknown>
  applicationInfo?: Record<string, unknown>
  scoreSummary?: Record<string, unknown>
  files?: Record<string, unknown>
  domesticReviewSummary?: Record<string, unknown>
  schoolReviewSummary?: Record<string, unknown>
  adjustmentSummary?: Record<string, unknown>
  ruleSnapshot?: Record<string, unknown>
  statusHistory?: Array<Record<string, unknown>>
  auditLogs?: Array<Record<string, unknown>>
}

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const submitting = ref(false)
const application = ref<ApplicationDetailView | null>(null)
const majorOptions = ref<MajorOption[]>([])

const scoreForm = reactive({
  academic: 56,
  material: 18,
  matching: 9,
})

const decisionForm = reactive({
  result: 'RESERVE' as 'RESERVE' | 'WAITLIST' | 'SUGGEST_ADJUSTMENT' | 'REJECT',
  reason: '',
  suggestedMajorCode: '',
})

const applicationId = computed(() => Number(route.params.id))
const totalScore = computed(() => scoreForm.academic + scoreForm.material + scoreForm.matching)
const basicInfo = computed<Record<string, unknown>>(() => application.value?.basicInfo ?? {})
const studentInfo = computed<Record<string, unknown>>(() => application.value?.studentInfo ?? {})
const applicationInfo = computed<Record<string, unknown>>(() => application.value?.applicationInfo ?? {})
const scoreSummary = computed<Record<string, unknown>>(() => application.value?.scoreSummary ?? {})
const schoolReviewSummary = computed<Record<string, unknown>>(() => application.value?.schoolReviewSummary ?? {})
const ruleSnapshot = computed<Record<string, unknown>>(() => application.value?.ruleSnapshot ?? {})
const statusHistory = computed<Array<Record<string, unknown>>>(() => application.value?.statusHistory ?? [])
const auditLogs = computed<Array<Record<string, unknown>>>(() => application.value?.auditLogs ?? [])
const canSubmit = computed(
  () =>
    authStore.currentUser?.role === 'SCHOOL_REVIEWER' &&
    basicInfo.value.status === 'SCHOOL_REVIEWING',
)
const schoolCode = computed(() => String(applicationInfo.value.targetSchoolCode ?? authStore.currentUser?.school_code ?? ''))
const reviewDecisions = [
  { value: 'RESERVE', label: '占位录取', hint: '直接占用学校与专业配额。', tone: 'success' },
  { value: 'WAITLIST', label: '加入候补', hint: '进入候补队列，后续由候补补位接口推进。', tone: 'warning' },
  { value: 'SUGGEST_ADJUSTMENT', label: '建议调剂', hint: '关闭原申请并生成新草稿。', tone: 'info' },
  { value: 'REJECT', label: '拒绝申请', hint: '结束学校审核链路。', tone: 'danger' },
]

const statusToneMap: Record<string, string> = {
  SCHOOL_REVIEWING: 'info',
  WAITLISTED: 'warning',
  RESERVED: 'success',
  SCHOOL_REJECTED: 'danger',
  CLOSED: 'danger',
}

const latestHistoryStatus = computed(() => {
  if (statusHistory.value.length === 0) {
    return String(basicInfo.value.status ?? '')
  }
  const last = statusHistory.value[statusHistory.value.length - 1] ?? {}
  return String(last.newStatus ?? basicInfo.value.status ?? '')
})

function displayValue(value: unknown) {
  if (value === null || value === undefined || value === '') {
    return '-'
  }
  return String(value)
}

async function loadApplication() {
  loading.value = true
  try {
    application.value = await getApplicationDetail(applicationId.value)
    if (schoolCode.value) {
      const majors = await getMajors(schoolCode.value)
      majorOptions.value = (majors as MajorOption[]) || []
    }
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!canSubmit.value) {
    toast.error('只有学校审核中的申请，且当前账号为学校审核员时，才允许提交学校审核结果')
    return
  }
  if (!application.value) {
    return
  }
  if (!decisionForm.reason.trim()) {
    toast.error('请填写审核理由')
    return
  }
  if (decisionForm.result === 'SUGGEST_ADJUSTMENT' && !decisionForm.suggestedMajorCode) {
    toast.error('建议调剂时必须选择目标专业')
    return
  }

  submitting.value = true
  try {
    await submitSchoolReview(applicationId.value, {
      review_result: decisionForm.result,
      review_reason: decisionForm.reason,
      school_threshold_passed: true,
      major_threshold_passed: true,
      school_quota_passed: decisionForm.result !== 'RESERVE' || totalScore.value >= 0,
      major_quota_passed: decisionForm.result !== 'RESERVE' || totalScore.value >= 0,
      academic_score: scoreForm.academic,
      material_score: scoreForm.material,
      matching_score: scoreForm.matching,
      total_score: totalScore.value,
      suggested_major_code:
        decisionForm.result === 'SUGGEST_ADJUSTMENT' ? decisionForm.suggestedMajorCode : undefined,
    })
    toast.success('学校审核结果已提交')
    await loadApplication()
  } finally {
    submitting.value = false
  }
}

onMounted(loadApplication)
</script>

<template>
  <section class="page-shell" v-loading="loading">
    <header class="page-header">
      <div>
        <el-button text @click="router.back()">
          <el-icon><ArrowLeft /></el-icon>
          返回审核入口
        </el-button>
        <h1 class="page-title">学校审核详情</h1>
        <p class="page-subtitle">
          当前复用申请详情接口读取学生、成绩单、状态历史与审计日志，再通过学校审核提交接口推进决策。
        </p>
      </div>
    </header>

    <template v-if="application">
      <div class="detail-grid">
        <div class="detail-column">
          <el-card shadow="never" class="page-card section-card">
            <div class="section-head">
              <div>
                <h2 class="section-title">申请概览</h2>
                <p class="section-subtitle">当前申请编号 {{ displayValue(basicInfo.applicationId) }}</p>
              </div>
              <span class="status-pill" :class="statusToneMap[String(basicInfo.status || '')] || 'info'">
                {{ displayValue(basicInfo.status) }}
              </span>
            </div>
            <div class="info-grid">
              <div class="info-item">
                <span>学校代码</span>
                <strong>{{ displayValue(applicationInfo.targetSchoolCode) }}</strong>
              </div>
              <div class="info-item">
                <span>专业代码</span>
                <strong>{{ displayValue(applicationInfo.targetMajorCode) }}</strong>
              </div>
              <div class="info-item">
                <span>学生姓名</span>
                <strong>{{ displayValue(studentInfo.name) }}</strong>
              </div>
              <div class="info-item">
                <span>当前学校</span>
                <strong>{{ displayValue(studentInfo.currentSchool) }}</strong>
              </div>
              <div class="info-item">
                <span>联系方式</span>
                <strong>{{ displayValue(studentInfo.phone) }}</strong>
              </div>
              <div class="info-item">
                <span>证件号</span>
                <strong>{{ displayValue(studentInfo.idCardNo) }}</strong>
              </div>
            </div>
          </el-card>

          <el-card shadow="never" class="page-card section-card">
            <div class="section-head compact">
              <div class="section-badge">
                <el-icon><Histogram /></el-icon>
                成绩摘要
              </div>
              <span class="score-total">{{ totalScore }}</span>
            </div>
            <div class="score-panel">
              <div class="score-row">
                <div>
                  <strong>学业成绩</strong>
                  <p>GPA / 核心课程 / 语言成绩</p>
                </div>
                <div class="score-input">
                  <el-slider v-model="scoreForm.academic" :min="0" :max="70" />
                  <span>{{ scoreForm.academic }}/70</span>
                </div>
              </div>
              <div class="score-row">
                <div>
                  <strong>材料质量</strong>
                  <p>文书、推荐信、材料完整度</p>
                </div>
                <div class="score-input">
                  <el-slider v-model="scoreForm.material" :min="0" :max="20" />
                  <span>{{ scoreForm.material }}/20</span>
                </div>
              </div>
              <div class="score-row">
                <div>
                  <strong>匹配度</strong>
                  <p>专业背景与目标专业的匹配程度</p>
                </div>
                <div class="score-input">
                  <el-slider v-model="scoreForm.matching" :min="0" :max="10" />
                  <span>{{ scoreForm.matching }}/10</span>
                </div>
              </div>
            </div>
          </el-card>

          <el-card shadow="never" class="page-card section-card">
            <div class="section-head compact">
              <div class="section-badge">
                <el-icon><Files /></el-icon>
                材料与轨迹
              </div>
            </div>
            <div class="timeline-list">
              <div class="timeline-row">
                <span>平均分</span>
                <strong>{{ displayValue(scoreSummary.averageScore) }}</strong>
              </div>
              <div class="timeline-row">
                <span>不及格门数</span>
                <strong>{{ displayValue(scoreSummary.failedSubjectCount) }}</strong>
              </div>
              <div class="timeline-row">
                <span>课程成绩摘要</span>
                <strong>{{ displayValue(scoreSummary.transcriptSchoolName) }}</strong>
              </div>
              <div class="timeline-row">
                <span>文书字数</span>
                <strong>{{ displayValue(schoolReviewSummary.total_score) }}</strong>
              </div>
              <div class="timeline-row">
                <span>最近状态流转</span>
                <strong>{{ latestHistoryStatus || displayValue(basicInfo.status) }}</strong>
              </div>
            </div>
          </el-card>
        </div>

        <div class="decision-column">
          <el-card shadow="never" class="page-card section-card">
            <div class="section-head compact">
              <div class="section-badge">
                <el-icon><Opportunity /></el-icon>
                决策面板
              </div>
            </div>
            <div class="decision-list">
              <button
                v-for="item in reviewDecisions"
                :key="item.value"
                class="decision-item"
                :class="[item.tone, { active: decisionForm.result === item.value }]"
                @click="decisionForm.result = item.value as typeof decisionForm.result"
              >
                <strong>{{ item.label }}</strong>
                <span>{{ item.hint }}</span>
              </button>
            </div>

            <el-form label-position="top" class="decision-form">
              <el-form-item v-if="decisionForm.result === 'SUGGEST_ADJUSTMENT'" label="调剂目标专业">
                <el-select v-model="decisionForm.suggestedMajorCode" placeholder="请选择目标专业">
                  <el-option
                    v-for="item in majorOptions"
                    :key="item.major_code"
                    :label="`${item.major_code} - ${item.major_name}`"
                    :value="item.major_code"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="审核理由">
                <el-input
                  v-model="decisionForm.reason"
                  type="textarea"
                  :rows="5"
                  placeholder="请填写评分依据、决策理由以及是否满足学校/专业阈值。"
                />
              </el-form-item>
            </el-form>

            <el-alert
              v-if="!canSubmit"
              type="warning"
              :closable="false"
              show-icon
              title="当前账号没有学校审核提交权限，或这条申请当前不处于 SCHOOL_REVIEWING 状态。"
            />

            <div class="decision-actions">
              <el-button @click="router.push('/schoolreviews')">返回列表</el-button>
              <el-button type="primary" :loading="submitting" :disabled="!canSubmit" @click="handleSubmit">
                提交学校审核
              </el-button>
            </div>
          </el-card>

          <el-card shadow="never" class="page-card section-card">
            <div class="section-head compact">
              <div class="section-badge">
                <el-icon><School /></el-icon>
                状态与审计说明
              </div>
            </div>
            <div class="timeline-list">
              <div class="timeline-row">
                <span>状态历史数</span>
                <strong>{{ statusHistory.length }}</strong>
              </div>
              <div class="timeline-row">
                <span>审计日志数</span>
                <strong>{{ auditLogs.length }}</strong>
              </div>
              <div class="timeline-row">
                <span>规则快照数</span>
                <strong>{{ Object.keys(ruleSnapshot).length ? 1 : 0 }}</strong>
              </div>
            </div>
            <el-alert type="info" :closable="false" show-icon>
              <template #title>
                学校审核详情当前只复用申请详情接口读数；列表、详情独立查询接口还未开放。
              </template>
            </el-alert>
            <el-alert type="warning" :closable="false" show-icon class="alert-gap">
              <template #title>
                建议调剂会让原申请先进入 `ADJUSTMENT_SUGGESTED`，随后关闭并生成新草稿。
              </template>
            </el-alert>
          </el-card>
        </div>
      </div>
    </template>
  </section>
</template>

<style scoped>
.detail-grid {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 22px;
}

.detail-column,
.decision-column {
  display: grid;
  gap: 22px;
  align-content: start;
}

.section-card {
  padding: 26px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.section-head.compact {
  align-items: center;
}

.section-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #1c396a;
  font-weight: 700;
}

.score-total {
  color: #0f5bd8;
  font-size: 32px;
  font-weight: 700;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.info-item {
  display: grid;
  gap: 8px;
  padding: 16px;
  border: 1px solid #dbe4f1;
  border-radius: 18px;
  background: #f7f9fd;
}

.info-item span,
.timeline-row span {
  color: #697b96;
  font-size: 13px;
}

.score-panel {
  display: grid;
  gap: 24px;
}

.score-row {
  display: grid;
  gap: 10px;
}

.score-row p {
  margin: 6px 0 0;
  color: #6e809a;
  font-size: 13px;
}

.score-input {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 14px;
  align-items: center;
}

.timeline-list {
  display: grid;
  gap: 12px;
}

.timeline-row {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  padding: 14px 16px;
  border-radius: 16px;
  background: #f6f8fc;
}

.decision-list {
  display: grid;
  gap: 12px;
}

.decision-item {
  display: grid;
  gap: 6px;
  padding: 16px;
  border: 2px solid #dbe4f1;
  border-radius: 18px;
  background: #fff;
  color: #293a56;
  text-align: left;
  cursor: pointer;
}

.decision-item.active.info {
  border-color: #2f7cf6;
  background: #edf4ff;
}

.decision-item.active.success {
  border-color: #1d9a66;
  background: #eefaf4;
}

.decision-item.active.warning {
  border-color: #d99d2f;
  background: #fff8e7;
}

.decision-item.active.danger {
  border-color: #d95f5f;
  background: #feefef;
}

.decision-item span {
  color: #70819a;
  font-size: 13px;
  line-height: 1.7;
}

.decision-form {
  margin-top: 18px;
}

.decision-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

.alert-gap {
  margin-top: 14px;
}

@media (max-width: 1280px) {
  .detail-grid,
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
