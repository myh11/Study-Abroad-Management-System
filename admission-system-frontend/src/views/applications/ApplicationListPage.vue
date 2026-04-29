<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Search, View, Upload, RefreshLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { cancelApplication, getMyApplications, submitApplication } from '../../api/applications/service'

const router = useRouter()
const loading = ref(false)

const query = reactive({
  page: 1,
  pageSize: 50,
  status: '',
  keyword: '',
})

const pageResult = reactive({
  list: [] as any[],
  page: 1,
  pageSize: 50,
  total: 0,
})

const tabs = [
  { key: '', label: '全部' },
  { key: 'DRAFT', label: '草稿' },
  { key: 'SUBMITTED', label: '已提交' },
  { key: 'DOMESTIC_REVIEWING', label: '国内审核中' },
  { key: 'DOMESTIC_SUPPLEMENT', label: '待补件' },
  { key: 'SCHOOL_REVIEWING', label: '学校审核中' },
  { key: 'WAITLISTED', label: '候补' },
  { key: 'RESERVED', label: '已预录取' },
  { key: 'SCHOOL_REJECTED', label: '已拒绝' },
]

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

function statusLabel(status: string) {
  return statusMeta[status]?.label || status
}

function statusColor(status: string) {
  return statusMeta[status]?.color || '#909399'
}

function tabCount(key: string) {
  const source = displayList.value
  if (!key) return source.length
  return source.filter((a) => a.status === key).length
}

const displayList = computed(() => {
  const keyword = query.keyword.trim().toLowerCase()
  if (!keyword) return pageResult.list
  return pageResult.list.filter((row) =>
    [row.applicationId, row.studentName, row.studentId, row.targetSchoolCode, row.targetMajorCode]
      .filter(Boolean)
      .some((value) => String(value).toLowerCase().includes(keyword)),
  )
})

function canSubmit(status: string) {
  return status === 'DRAFT' || status === 'DOMESTIC_SUPPLEMENT'
}

function canCancel(status: string) {
  return status === 'SUBMITTED' || status === 'SCHOOL_REVIEWING' || status === 'WAITLISTED'
}

async function loadData() {
  loading.value = true
  try {
    const res = await getMyApplications({
      page: query.page,
      pageSize: query.pageSize,
      status: query.status || undefined,
    })
    pageResult.list = res.list || []
    pageResult.page = res.page || 1
    pageResult.pageSize = res.pageSize || 10
    pageResult.total = res.total || 0
  } catch (err: any) {
    ElMessage.error(err.message || '加载列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadData)

async function handleSubmit(id: number) {
  try {
    await submitApplication(id)
    ElMessage.success('提交成功')
    loadData()
  } catch (err: any) {
    ElMessage.error(err.message || '提交失败')
  }
}

async function handleCancel(id: number) {
  const reason = window.prompt('请输入撤销原因：')
  if (reason === null) return
  try {
    await cancelApplication(id, reason || undefined)
    ElMessage.success('撤销成功')
    loadData()
  } catch (err: any) {
    ElMessage.error(err.message || '撤销失败')
  }
}

function goDetail(id: number) {
  router.push(`/applications/${id}`)
}

function onTabClick(key: string) {
  query.status = key
  query.page = 1
  loadData()
}

</script>

<template>
  <div class="page-shell">
    <!-- Header -->
    <div class="page-header">
      <div>
        <h1 class="page-title">申请列表</h1>
        <p class="page-subtitle">
          当前账号共 {{ pageResult.total }} 条申请记录，当前页显示 {{ displayList.length }} 条
        </p>
      </div>
      <el-button type="primary" @click="$router.push('/applications/create')">
        <el-icon><Plus /></el-icon> 新建申请
      </el-button>
    </div>

    <!-- Filter card -->
    <div class="section-card">
      <div class="filter-bar">
        <div class="search-box">
          <el-icon class="search-icon"><Search /></el-icon>
          <el-input
            v-model="query.keyword"
            placeholder="当前页快速筛选：学生 / 申请编号 / 学校 / 专业"
            clearable
            style="width: 320px"
          />
        </div>
        <el-button type="primary" @click="query.page = 1; loadData()">刷新数据</el-button>
        <el-button @click="query.keyword = ''; query.status = ''; query.page = 1; loadData()">重置</el-button>
      </div>
    </div>

    <!-- Tabs + Table card -->
    <div class="section-card table-card">
      <!-- Tabs -->
      <div class="tabs-bar">
        <button
          v-for="t in tabs"
          :key="t.key"
          class="tab-btn"
          :class="{ active: query.status === t.key }"
          @click="onTabClick(t.key)"
        >
          {{ t.label }}
          <span class="tab-count">{{ tabCount(t.key) }}</span>
        </button>
      </div>

      <!-- Table -->
      <el-table v-loading="loading" :data="displayList" style="width: 100%">
        <el-table-column prop="applicationId" label="申请编号" min-width="110">
          <template #default="{ row }">
            <span class="mono-id">{{ row.applicationId }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="studentName" label="学生" min-width="140">
          <template #default="{ row }">
            <div class="student-name">{{ row.studentName || '-' }}</div>
            <div class="student-id">ID: {{ row.studentId || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="targetSchoolCode" label="学校 / 专业" min-width="180">
          <template #default="{ row }">
            <div>{{ row.targetSchoolCode }}</div>
            <div class="text-muted">{{ row.targetMajorCode }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="130">
          <template #default="{ row }">
            <span class="status-badge" :style="{ background: statusColor(row.status) + '1a', color: statusColor(row.status), borderColor: statusColor(row.status) + '33' }">
              <span class="status-dot" :style="{ background: statusColor(row.status) }" />
              {{ statusLabel(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" min-width="170">
          <template #default="{ row }">
            <span class="text-muted">{{ row.updatedAt }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="180" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button size="small" @click="goDetail(row.applicationId)">
                <el-icon><View /></el-icon> 查看
              </el-button>
              <el-button v-if="canSubmit(row.status)" type="primary" size="small" @click="handleSubmit(row.applicationId)">
                <el-icon><Upload /></el-icon> 提交
              </el-button>
              <el-button v-if="canCancel(row.status)" type="danger" size="small" plain @click="handleCancel(row.applicationId)">
                <el-icon><RefreshLeft /></el-icon> 撤销
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-bar">
        <div class="pagination-info">
          <template v-if="query.keyword.trim()">
            当前页快速筛选结果 {{ displayList.length }} 条
          </template>
          <template v-else>
            共 {{ pageResult.total }} 条记录
          </template>
        </div>
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="pageResult.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @change="loadData()"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.section-card {
  border: 1px solid #d9e2ef;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.94);
  padding: 20px 24px;
}

.table-card {
  padding: 0;
  overflow: hidden;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.search-box {
  position: relative;
  display: inline-flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 10px;
  color: #9aa8bf;
  z-index: 1;
}

.search-box :deep(.el-input__wrapper) {
  padding-left: 30px;
}

.tabs-bar {
  display: flex;
  gap: 4px;
  overflow-x: auto;
  border-bottom: 1px solid #e4eaf3;
  padding: 0 16px;
}

.tab-btn {
  position: relative;
  white-space: nowrap;
  border: none;
  border-bottom: 2px solid transparent;
  background: none;
  padding: 10px 14px;
  font-size: 13px;
  color: #5e6b82;
  cursor: pointer;
  transition: all 0.2s;
}

.tab-btn:hover {
  color: #162033;
}

.tab-btn.active {
  color: #409eff;
  border-bottom-color: #409eff;
  font-weight: 600;
}

.tab-count {
  margin-left: 4px;
  font-size: 11px;
  color: #9aa8bf;
}

.tab-btn.active .tab-count {
  color: #409eff;
}

:deep(.el-table) {
  --el-table-border-color: #e4eaf3;
  --el-table-header-bg-color: #f8fafd;
  --el-table-header-text-color: #5e6b82;
  font-size: 13px;
}

:deep(.el-table th) {
  font-weight: 500;
}

.mono-id {
  font-family: monospace;
  font-size: 12px;
  color: #5e6b82;
}

.student-name {
  font-weight: 500;
  color: #1a2b4c;
}

.student-id {
  font-size: 11px;
  color: #9aa8bf;
  margin-top: 2px;
}

.text-muted {
  color: #9aa8bf;
  font-size: 12px;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 500;
  border: 1px solid transparent;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.action-btns {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-top: 1px solid #e4eaf3;
  font-size: 12px;
  color: #5e6b82;
}

.pagination-info {
  font-size: 12px;
  color: #5e6b82;
}
</style>
