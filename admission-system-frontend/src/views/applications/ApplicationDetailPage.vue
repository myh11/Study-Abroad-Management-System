<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Document, Calendar, Upload, RefreshLeft, CircleCheck, CloseBold, School } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  acceptAdjustment,
  cancelApplication,
  getApplicationDetail,
  rejectAdjustment,
  submitApplication,
  uploadSupplement,
  waitlistConfirm,
} from '../../api/applications/service'
import { uploadTranscript } from '../../api/files/service'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<any>(null)

const applicationId = computed(() => Number(route.params.id))
const status = computed(() => detail.value?.basicInfo?.status as string)

const statusMeta: Record<string, { label: string; color: string }> = {
  DRAFT: { label: '草稿', color: '#909399' },
  SUBMITTED: { label: '已提交', color: '#409eff' },
  DOMESTIC_REVIEWING: { label: '国内审核中', color: '#409eff' },
  DOMESTIC_SUPPLEMENT: { label: '待补件', color: '#e6a23c' },
  DOMESTIC_REJECTED: { label: '国内拒绝', color: '#f56c6c' },
  SCHOOL_REVIEWING: { label: '学校审核中', color: '#409eff' },
  WAITLISTED: { label: '候补中', color: '#e6a23c' },
  WAITLIST_PENDING_CONFIRM: { label: '候补待确认', color: '#e6a23c' },
  RESERVED: { label: '已预录取', color: '#67c23a' },
  ADJUSTMENT_SUGGESTED: { label: '调剂建议', color: '#9254de' },
  SCHOOL_REJECTED: { label: '学校拒绝', color: '#f56c6c' },
  CANCELED: { label: '已撤销', color: '#909399' },
  CLOSED: { label: '已关闭', color: '#909399' },
}

function statusLabel(s?: string) {
  return statusMeta[s || '']?.label || s || '-'
}

function statusColor(s?: string) {
  return statusMeta[s || '']?.color || '#909399'
}

function canSubmit() {
  return status.value === 'DRAFT' || status.value === 'DOMESTIC_SUPPLEMENT'
}

function canCancel() {
  return status.value === 'SUBMITTED' || status.value === 'SCHOOL_REVIEWING' || status.value === 'WAITLISTED'
}

function canSupplement() {
  return status.value === 'DOMESTIC_SUPPLEMENT'
}

function canWaitlistConfirm() {
  return status.value === 'WAITLIST_PENDING_CONFIRM'
}

function isAdjustmentDraft() {
  return status.value === 'DRAFT' && Boolean(detail.value?.applicationInfo?.sourceApplicationId)
}

function canAcceptAdjustment() {
  return isAdjustmentDraft()
}

function canRejectAdjustment() {
  return status.value === 'ADJUSTMENT_SUGGESTED' || isAdjustmentDraft()
}

async function loadDetail() {
  loading.value = true
  try {
    detail.value = await getApplicationDetail(applicationId.value)
  } catch (err: any) {
    ElMessage.error(err.message || '加载详情失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadDetail)

async function handleSubmit() {
  try {
    await submitApplication(applicationId.value)
    ElMessage.success('提交成功')
    loadDetail()
  } catch (err: any) {
    ElMessage.error(err.message || '提交失败')
  }
}

async function handleCancel() {
  const reason = window.prompt('请输入撤销原因：')
  if (reason === null) return
  try {
    await cancelApplication(applicationId.value, reason || undefined)
    ElMessage.success('撤销成功')
    loadDetail()
  } catch (err: any) {
    ElMessage.error(err.message || '撤销失败')
  }
}

const supplementFile = ref<File | null>(null)
const supplementNote = ref('')
const uploadingSupplement = ref(false)

async function handleSupplement() {
  if (!supplementFile.value) {
    ElMessage.error('请选择补件成绩单')
    return
  }
  uploadingSupplement.value = true
  try {
    const fileRes = await uploadTranscript(supplementFile.value)
    await uploadSupplement(applicationId.value, {
      newTranscriptFileId: fileRes.fileId,
      supplementNote: supplementNote.value,
    })
    ElMessage.success('补件成功')
    supplementFile.value = null
    supplementNote.value = ''
    loadDetail()
  } catch (err: any) {
    ElMessage.error(err.message || '补件失败')
  } finally {
    uploadingSupplement.value = false
  }
}

async function handleWaitlistConfirm(accept: boolean) {
  try {
    await waitlistConfirm(applicationId.value, accept)
    ElMessage.success(accept ? '已接受候补' : '已拒绝候补')
    loadDetail()
  } catch (err: any) {
    ElMessage.error(err.message || '操作失败')
  }
}

async function handleAcceptAdjustment() {
  try {
    await acceptAdjustment(applicationId.value)
    ElMessage.success('已接受调剂')
    loadDetail()
  } catch (err: any) {
    ElMessage.error(err.message || '操作失败')
  }
}

async function handleRejectAdjustment() {
  try {
    await rejectAdjustment(applicationId.value)
    ElMessage.success('已拒绝调剂')
    loadDetail()
  } catch (err: any) {
    ElMessage.error(err.message || '操作失败')
  }
}

function renderMap(map: Record<string, any>) {
  if (!map) return []
  return Object.entries(map).map(([key, value]) => ({ key, value: value ?? '-' }))
}

function formatKey(key: string) {
  const labels: Record<string, string> = {
    applicationId: '申请编号',
    status: '状态',
    createdAt: '创建时间',
    updatedAt: '更新时间',
    batchId: '批次ID',
    targetSchoolCode: '目标学校',
    targetMajorCode: '目标专业',
    agentId: '代理ID',
    submittedAt: '提交时间',
    name: '姓名',
    gender: '性别',
    birthDate: '出生日期',
    currentSchool: '当前学校',
    grade: '年级',
    email: '邮箱',
    phone: '电话',
    idCardNo: '身份证号',
    transcriptSchoolName: '成绩单学校',
    termStart: '学期开始',
    termEnd: '学期结束',
    chinese: '语文',
    math: '数学',
    english: '英语',
    physics: '物理',
    chemistry: '化学',
    history: '历史',
    averageScore: '平均分',
    failedSubjectCount: '挂科数',
    reviewerName: '审核人',
    reviewedAt: '审核时间',
    result: '结果',
    comment: '备注',
    score: '评分',
    adjustmentSchoolCode: '调剂学校',
    adjustmentMajorCode: '调剂专业',
    suggestedBy: '建议人',
    suggestedAt: '建议时间',
    reason: '原因',
    fileId: '文件ID',
    fileName: '文件名',
    fileType: '文件类型',
    fileSize: '文件大小',
    oldStatus: '原状态',
    newStatus: '新状态',
    triggerAction: '动作',
    triggerRole: '触发角色',
    operatedAt: '操作时间',
    operationType: '操作类型',
    entityType: '实体类型',
    entityId: '实体ID',
    operatorId: '操作人ID',
  }
  return labels[key] || key
}

const applicationInfoItems = computed(() => renderMap(detail.value?.applicationInfo))
const studentInfoItems = computed(() => renderMap(detail.value?.studentInfo))
const scoreItems = computed(() => renderMap(detail.value?.scoreSummary))
const fileItems = computed(() => renderMap(detail.value?.files))
const domesticItems = computed(() => renderMap(detail.value?.domesticReviewSummary))
const schoolItems = computed(() => renderMap(detail.value?.schoolReviewSummary))
const adjustmentItems = computed(() => renderMap(detail.value?.adjustmentSummary))
</script>

<template>
  <div v-loading="loading" class="page-shell">
    <template v-if="detail">
      <!-- Header -->
      <div>
        <button class="back-link" @click="router.push('/applications')">
          <el-icon><ArrowLeft /></el-icon> 返回列表
        </button>
        <div class="detail-header">
          <div>
            <div class="detail-title-row">
              <h1 class="page-title">{{ detail.studentInfo?.name || '学生' }} 的留学申请</h1>
              <span
                class="status-badge-lg"
                :style="{ background: statusColor(status) + '1a', color: statusColor(status), borderColor: statusColor(status) + '33' }"
              >
                <span class="status-dot" :style="{ background: statusColor(status) }" />
                {{ statusLabel(status) }}
              </span>
            </div>
            <p class="page-subtitle">
              申请编号 <span class="mono">{{ applicationId }}</span>
              · 提交时间 {{ detail.applicationInfo?.submittedAt || '—' }}
              · 代理 {{ detail.basicInfo?.agentId || '—' }}
            </p>
          </div>
          <div class="header-actions">
            <el-button v-if="canSubmit()" type="primary" @click="handleSubmit">
              <el-icon><CircleCheck /></el-icon> 提交申请
            </el-button>
            <el-button v-if="canCancel()" type="danger" plain @click="handleCancel">
              <el-icon><RefreshLeft /></el-icon> 撤销申请
            </el-button>
            <el-button @click="router.push('/applications')">返回列表</el-button>
          </div>
        </div>
      </div>

      <!-- Status banner -->
      <div class="status-banner" :style="{ borderLeftColor: statusColor(status) }">
        <div class="banner-content">
          <div>
            <div class="banner-label">当前状态</div>
            <div class="banner-status">{{ statusLabel(status) }}</div>
            <div class="banner-hint">{{ detail.applicationInfo?.targetSchoolCode }} / {{ detail.applicationInfo?.targetMajorCode }}</div>
          </div>
        </div>
      </div>

      <!-- Two column layout -->
      <div class="detail-grid">
        <!-- Left column -->
        <div class="detail-main">
          <!-- Student info -->
          <div class="section-card">
            <div class="section-title-row">
              <el-icon><School /></el-icon>
              <h3>学生基本信息</h3>
            </div>
            <div class="info-grid">
              <div v-for="item in studentInfoItems" :key="item.key" class="info-row">
                <span class="info-label">{{ formatKey(item.key) }}</span>
                <span class="info-value">{{ item.value }}</span>
              </div>
            </div>
          </div>

          <!-- Application info -->
          <div class="section-card">
            <div class="section-title-row">
              <el-icon><Document /></el-icon>
              <h3>申请信息</h3>
            </div>
            <div class="info-grid">
              <div v-for="item in applicationInfoItems" :key="item.key" class="info-row">
                <span class="info-label">{{ formatKey(item.key) }}</span>
                <span class="info-value">{{ item.value }}</span>
              </div>
            </div>
          </div>

          <!-- Score info -->
          <div class="section-card">
            <div class="section-title-row">
              <el-icon><Calendar /></el-icon>
              <h3>成绩摘要</h3>
            </div>
            <div class="info-grid">
              <div v-for="item in scoreItems" :key="item.key" class="info-row">
                <span class="info-label">{{ formatKey(item.key) }}</span>
                <span class="info-value">{{ item.value }}</span>
              </div>
            </div>
          </div>

          <!-- Files -->
          <div class="section-card">
            <div class="section-title-row">
              <el-icon><Document /></el-icon>
              <h3>申请材料</h3>
            </div>
            <div class="info-grid">
              <div v-for="item in fileItems" :key="item.key" class="info-row">
                <span class="info-label">{{ formatKey(item.key) }}</span>
                <span class="info-value">{{ item.value }}</span>
              </div>
            </div>
          </div>

          <!-- Review summaries -->
          <div v-if="domesticItems.length > 0 || schoolItems.length > 0" class="review-grid">
            <div v-if="domesticItems.length > 0" class="section-card review-card" style="border-left-color: #67c23a;">
              <div class="section-title-row">
                <el-icon color="#67c23a"><CircleCheck /></el-icon>
                <h3>国内审核摘要</h3>
              </div>
              <div class="info-grid compact">
                <div v-for="item in domesticItems" :key="item.key" class="info-row">
                  <span class="info-label">{{ formatKey(item.key) }}</span>
                  <span class="info-value">{{ item.value }}</span>
                </div>
              </div>
            </div>
            <div v-if="schoolItems.length > 0" class="section-card review-card" style="border-left-color: #409eff;">
              <div class="section-title-row">
                <el-icon color="#409eff"><School /></el-icon>
                <h3>学校审核摘要</h3>
              </div>
              <div class="info-grid compact">
                <div v-for="item in schoolItems" :key="item.key" class="info-row">
                  <span class="info-label">{{ formatKey(item.key) }}</span>
                  <span class="info-value">{{ item.value }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Adjustment summary -->
          <div v-if="adjustmentItems.length > 0" class="section-card" style="border-left: 4px solid #9254de;">
            <div class="section-title-row">
              <el-icon color="#9254de"><Document /></el-icon>
              <h3>调剂摘要</h3>
            </div>
            <div class="info-grid compact">
              <div v-for="item in adjustmentItems" :key="item.key" class="info-row">
                <span class="info-label">{{ formatKey(item.key) }}</span>
                <span class="info-value">{{ item.value }}</span>
              </div>
            </div>
          </div>

          <!-- Supplement upload -->
          <div v-if="canSupplement()" class="section-card">
            <div class="section-title-row">
              <el-icon><Upload /></el-icon>
              <h3>补件上传</h3>
            </div>
            <div class="supplement-form">
              <el-upload :auto-upload="false" :on-change="(file: any) => { supplementFile = file.raw }" :show-file-list="false">
                <el-button size="small">选择补件成绩单</el-button>
              </el-upload>
              <span v-if="supplementFile" class="file-name">{{ supplementFile.name }}</span>
              <el-input v-model="supplementNote" placeholder="补件说明（可选）" style="max-width: 320px" size="small" />
              <el-button type="primary" size="small" :loading="uploadingSupplement" @click="handleSupplement">上传补件</el-button>
            </div>
          </div>

          <!-- Waitlist confirm -->
          <div v-if="canWaitlistConfirm()" class="section-card">
            <div class="section-title-row">
              <el-icon><CircleCheck /></el-icon>
              <h3>候补确认</h3>
            </div>
            <div class="action-row">
              <el-button type="primary" @click="handleWaitlistConfirm(true)">
                <el-icon><CircleCheck /></el-icon> 接受候补
              </el-button>
              <el-button type="danger" plain @click="handleWaitlistConfirm(false)">
                <el-icon><CloseBold /></el-icon> 拒绝候补
              </el-button>
            </div>
          </div>

          <!-- Adjustment actions -->
          <div v-if="canAcceptAdjustment() || canRejectAdjustment()" class="section-card">
            <div class="section-title-row">
              <el-icon><Document /></el-icon>
              <h3>调剂处理</h3>
            </div>
            <div class="action-row">
              <el-button v-if="canAcceptAdjustment()" type="primary" @click="handleAcceptAdjustment">
                <el-icon><CircleCheck /></el-icon> 接受调剂
              </el-button>
              <el-button v-if="canRejectAdjustment()" type="danger" plain @click="handleRejectAdjustment">
                <el-icon><CloseBold /></el-icon> 拒绝调剂
              </el-button>
            </div>
          </div>

          <!-- Status history -->
          <div class="section-card">
            <div class="section-title-row">
              <el-icon><Calendar /></el-icon>
              <h3>状态历史</h3>
            </div>
            <el-table :data="detail.statusHistory || []" size="small" border>
              <el-table-column prop="oldStatus" label="原状态" min-width="120" />
              <el-table-column prop="newStatus" label="新状态" min-width="120" />
              <el-table-column prop="triggerAction" label="动作" min-width="120" />
              <el-table-column prop="triggerRole" label="触发角色" min-width="120" />
              <el-table-column prop="operatedAt" label="操作时间" min-width="160" />
            </el-table>
          </div>

          <!-- Audit logs -->
          <div class="section-card">
            <div class="section-title-row">
              <el-icon><Document /></el-icon>
              <h3>审计日志</h3>
            </div>
            <el-table :data="detail.auditLogs || []" size="small" border>
              <el-table-column prop="operationType" label="操作类型" min-width="120" />
              <el-table-column prop="entityType" label="实体类型" min-width="100" />
              <el-table-column prop="entityId" label="实体ID" min-width="100" />
              <el-table-column prop="operatorId" label="操作人ID" min-width="100" />
              <el-table-column prop="createdAt" label="操作时间" min-width="160" />
            </el-table>
          </div>
        </div>

        <!-- Right column: Timeline -->
        <div class="detail-side">
          <div class="section-card">
            <div class="section-title-row">
              <el-icon><Calendar /></el-icon>
              <h3>状态流转时间线</h3>
            </div>
            <div class="timeline">
              <div
                v-for="(e, idx) in (detail.statusHistory || []).slice().reverse()"
                :key="idx"
                class="timeline-item"
              >
                <div class="timeline-dot" :class="{ done: (idx as number) + 1 < (detail.statusHistory || []).length }" />
                <div class="timeline-content">
                  <div class="timeline-time">{{ e.operatedAt }}</div>
                  <div class="timeline-title">{{ e.triggerAction }}</div>
                  <div class="timeline-desc">{{ statusLabel(e.oldStatus) }} → {{ statusLabel(e.newStatus) }}</div>
                  <div class="timeline-meta">操作人：{{ e.triggerRole }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.back-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 8px;
  font-size: 13px;
  color: #5e6b82;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
}
.back-link:hover {
  color: #162033;
}

.detail-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.detail-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.status-badge-lg {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 500;
  border: 1px solid transparent;
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
}

.header-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.mono {
  font-family: monospace;
}

.status-banner {
  border: 1px solid #d9e2ef;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.94);
  padding: 20px 24px;
  border-left-width: 4px;
}

.banner-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.banner-label {
  font-size: 12px;
  color: #7b879c;
}

.banner-status {
  margin-top: 6px;
  font-size: 18px;
  font-weight: 600;
  color: #1a2b4c;
}

.banner-hint {
  margin-top: 4px;
  font-size: 12px;
  color: #7b879c;
}

.detail-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

@media (max-width: 1200px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
}

.detail-main {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-side {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-card {
  border: 1px solid #d9e2ef;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.94);
  padding: 20px 24px;
}

.section-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.section-title-row h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #1a2b4c;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 24px;
}

.info-grid.compact {
  grid-template-columns: 1fr;
}

.info-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #f0f3f8;
  font-size: 13px;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  min-width: 100px;
  max-width: 120px;
  flex-shrink: 0;
  color: #7b879c;
  word-break: break-word;
}

.info-value {
  flex: 1;
  min-width: 0;
  color: #1a2b4c;
  font-weight: 500;
  word-break: break-all;
}

.review-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

@media (max-width: 900px) {
  .review-grid {
    grid-template-columns: 1fr;
  }
}

.review-card {
  border-left-width: 4px;
}

.supplement-form {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.file-name {
  color: #394760;
  font-size: 13px;
}

.action-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

/* Timeline */
.timeline {
  position: relative;
  padding-left: 20px;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 5px;
  top: 4px;
  bottom: 4px;
  width: 2px;
  background: #e4eaf3;
}

.timeline-item {
  position: relative;
  padding-bottom: 20px;
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-dot {
  position: absolute;
  left: -20px;
  top: 4px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #fff;
  border: 2px solid #d1d9e6;
}

.timeline-dot.done {
  background: #67c23a;
  border-color: #67c23a;
}

.timeline-content {
  margin-left: 4px;
}

.timeline-time {
  font-size: 11px;
  color: #9aa8bf;
}

.timeline-title {
  margin-top: 2px;
  font-size: 13px;
  font-weight: 500;
  color: #1a2b4c;
}

.timeline-desc {
  margin-top: 2px;
  font-size: 12px;
  color: #5e6b82;
}

.timeline-meta {
  margin-top: 2px;
  font-size: 11px;
  color: #9aa8bf;
}
</style>
