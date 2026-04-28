<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppButton from '../../components/common/AppButton.vue'
import AppForm from '../../components/common/AppForm.vue'
import {
  claimDomesticReview,
  getApplicationDetail,
  submitDomesticReview,
  type DomesticReviewApplicationDetail,
  type SubmitDomesticReviewPayload,
} from '../../api/domesticreviews/service'

const route = useRoute()
const applicationId = computed(() => Number(route.params.id))

const loading = ref(false)
const claiming = ref(false)
const submitting = ref(false)
const errorText = ref('')
const detail = ref<DomesticReviewApplicationDetail | null>(null)

const basicInfo = computed<Record<string, unknown>>(() => detail.value?.basicInfo ?? {})
const studentInfo = computed<Record<string, unknown>>(() => detail.value?.studentInfo ?? {})
const applicationInfo = computed<Record<string, unknown>>(() => detail.value?.applicationInfo ?? {})
const scoreSummary = computed<Record<string, unknown>>(() => detail.value?.scoreSummary ?? {})

const currentStatus = computed(() => String(basicInfo.value.status ?? ''))
const canClaim = computed(() => currentStatus.value === 'SUBMITTED')
const canSubmit = computed(() => currentStatus.value === 'DOMESTIC_REVIEWING')

const form = ref<SubmitDomesticReviewPayload>({
  materialComplete: true,
  identityMatched: true,
  basicScorePassed: true,
  authenticityRiskLevel: 'B',
  standardizationPassed: true,
  result: 'PASS',
  comment: '',
})

function displayValue(value: unknown) {
  return value === null || value === undefined || value === '' ? '-' : String(value)
}

async function loadDetail() {
  if (!Number.isFinite(applicationId.value) || applicationId.value <= 0) {
    detail.value = null
    errorText.value = '无效的申请 ID，请检查链接。'
    return
  }

  loading.value = true
  errorText.value = ''
  try {
    detail.value = await getApplicationDetail(applicationId.value)
  } catch (error) {
    detail.value = null
    errorText.value = error instanceof Error ? error.message : '详情加载失败，请稍后重试。'
  } finally {
    loading.value = false
  }
}

async function handleClaim() {
  if (!canClaim.value) {
    ElMessage.warning('只有 SUBMITTED 状态的申请可以认领。')
    return
  }

  claiming.value = true
  try {
    await claimDomesticReview(applicationId.value)
    await loadDetail()
    ElMessage.success('认领成功。')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '认领失败，请稍后重试。')
  } finally {
    claiming.value = false
  }
}

async function handleSubmit() {
  if (!canSubmit.value) {
    ElMessage.warning('只有 DOMESTIC_REVIEWING 状态的申请可以提交审核。')
    return
  }
  if (!form.value.comment.trim()) {
    ElMessage.warning('请填写审核意见。')
    return
  }
  if (!form.value.authenticityRiskLevel.trim()) {
    ElMessage.warning('请选择风险等级。')
    return
  }

  submitting.value = true
  try {
    await submitDomesticReview(applicationId.value, {
      ...form.value,
      authenticityRiskLevel: form.value.authenticityRiskLevel.trim(),
      comment: form.value.comment.trim(),
    })
    await loadDetail()
    ElMessage.success('审核提交成功。')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '提交失败，请稍后重试。')
  } finally {
    submitting.value = false
  }
}

onMounted(loadDetail)
</script>

<template>
  <div class="review-detail-page page-shell">
    <div class="page-card">
      <h2 class="title">国内审核详情</h2>
      <p class="subtitle">申请 ID：{{ Number.isFinite(applicationId) ? applicationId : '-' }}</p>

      <el-skeleton v-if="loading" :rows="6" animated />

      <el-alert v-else-if="errorText" type="error" :closable="false" :title="errorText" class="state-block" />

      <el-alert v-else-if="!detail" type="warning" :closable="false" title="未查询到申请详情" class="state-block" />

      <template v-else>
        <div class="content-grid">
          <div class="info-block">
            <h3>申请信息</h3>
            <p>申请编号：{{ displayValue(basicInfo.applicationId) }}</p>
            <p>当前状态：{{ displayValue(basicInfo.status) }}</p>
            <p>目标学校：{{ displayValue(applicationInfo.targetSchoolCode) }}</p>
            <p>目标专业：{{ displayValue(applicationInfo.targetMajorCode) }}</p>
          </div>

          <div class="info-block">
            <h3>学生信息</h3>
            <p>姓名：{{ displayValue(studentInfo.name) }}</p>
            <p>证件号：{{ displayValue(studentInfo.idCardNo) }}</p>
            <p>平均分：{{ displayValue(scoreSummary.averageScore) }}</p>
            <p>风险等级：{{ displayValue(scoreSummary.authenticityRiskLevel) }}</p>
          </div>
        </div>

        <AppForm class="form-block">
          <el-form-item label="材料完整性"><el-switch v-model="form.materialComplete" /></el-form-item>
          <el-form-item label="身份匹配"><el-switch v-model="form.identityMatched" /></el-form-item>
          <el-form-item label="基础成绩达标"><el-switch v-model="form.basicScorePassed" /></el-form-item>
          <el-form-item label="规范性检查通过"><el-switch v-model="form.standardizationPassed" /></el-form-item>

          <el-form-item label="风险等级">
            <el-select v-model="form.authenticityRiskLevel" style="width: 240px">
              <el-option label="A" value="A" />
              <el-option label="B" value="B" />
              <el-option label="C" value="C" />
              <el-option label="D" value="D" />
            </el-select>
          </el-form-item>

          <el-form-item label="审核结论">
            <el-radio-group v-model="form.result">
              <el-radio value="PASS">通过</el-radio>
              <el-radio value="SUPPLEMENT_REQUIRED">补件</el-radio>
              <el-radio value="REJECT">拒绝</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="审核意见">
            <el-input v-model="form.comment" type="textarea" :rows="4" placeholder="请填写审核意见" />
          </el-form-item>

          <el-form-item>
            <div class="actions">
              <AppButton v-if="canClaim" :loading="claiming" @click="handleClaim">认领审核</AppButton>
              <AppButton type="primary" :loading="submitting" :disabled="!canSubmit" @click="handleSubmit">
                提交审核
              </AppButton>
            </div>
            <p v-if="!canSubmit" class="form-tip">当前状态不是 DOMESTIC_REVIEWING，暂不可提交审核。</p>
          </el-form-item>
        </AppForm>
      </template>
    </div>
  </div>
</template>

<style scoped>
.review-detail-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.title {
  margin: 0;
  font-size: 24px;
}

.subtitle {
  margin: 6px 0 0;
  color: #64748b;
}

.state-block {
  margin-top: 16px;
}

.content-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.info-block {
  padding: 14px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #fff;
}

.info-block h3 {
  margin: 0 0 10px;
  font-size: 16px;
}

.info-block p {
  margin: 6px 0;
  color: #334155;
}

.form-block {
  margin-top: 20px;
}

.actions {
  display: flex;
  gap: 10px;
}

.form-tip {
  margin: 8px 0 0;
  color: #b45309;
  font-size: 13px;
}
</style>
