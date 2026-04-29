<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppButton from '../../components/common/AppButton.vue'
import { getDomesticReviewList, type DomesticReviewListItem } from '../../api/domesticreviews/service'

const router = useRouter()
const loading = ref(false)
const rows = ref<DomesticReviewListItem[]>([])
const total = ref(0)

const query = reactive({
  page: 1,
  pageSize: 20,
  status: '',
})

const statusOptions = [
  { label: '全部', value: '' },
  { label: '待认领', value: 'SUBMITTED' },
  { label: '审核中', value: 'DOMESTIC_REVIEWING' },
  { label: '补件结果', value: 'DOMESTIC_SUPPLEMENT' },
  { label: '拒绝结果', value: 'DOMESTIC_REJECTED' },
]

const statusSummary = computed(() =>
  rows.value.reduce<Record<string, number>>((accumulator, row) => {
    accumulator[row.status] = (accumulator[row.status] ?? 0) + 1
    return accumulator
  }, {}),
)

async function loadData() {
  loading.value = true
  try {
    const result = await getDomesticReviewList({
      page: query.page,
      pageSize: query.pageSize,
      status: query.status || undefined,
    })
    rows.value = result.list || []
    total.value = result.total || 0
  } finally {
    loading.value = false
  }
}

function goToDetail(id: number) {
  router.push(`/domesticreviews/${id}`)
}

onMounted(loadData)
</script>

<template>
  <div class="domestic-review-list page-shell">
    <div class="page-card">
      <div class="page-header">
        <div>
          <h2 class="page-title">国内审核任务池</h2>
          <p class="page-subtitle">当前页面已接入后端正式列表接口，可直接查看待认领、审核中和结果类申请。</p>
        </div>
        <AppButton type="primary" :loading="loading" @click="loadData">刷新列表</AppButton>
      </div>

      <div class="summary-grid">
        <div class="summary-card">
          <span class="summary-label">当前总数</span>
          <strong class="summary-value">{{ total }}</strong>
        </div>
        <div class="summary-card">
          <span class="summary-label">待认领</span>
          <strong class="summary-value">{{ statusSummary.SUBMITTED ?? 0 }}</strong>
        </div>
        <div class="summary-card">
          <span class="summary-label">审核中</span>
          <strong class="summary-value">{{ statusSummary.DOMESTIC_REVIEWING ?? 0 }}</strong>
        </div>
        <div class="summary-card">
          <span class="summary-label">结果类</span>
          <strong class="summary-value">
            {{ (statusSummary.DOMESTIC_SUPPLEMENT ?? 0) + (statusSummary.DOMESTIC_REJECTED ?? 0) }}
          </strong>
        </div>
      </div>

      <div class="toolbar">
        <el-select v-model="query.status" placeholder="筛选状态" style="width: 220px" @change="query.page = 1; loadData()">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </div>

      <el-table v-loading="loading" :data="rows" border class="sample-table">
        <el-table-column prop="applicationId" label="Application ID" width="130" />
        <el-table-column prop="status" label="当前状态" min-width="180" />
        <el-table-column prop="studentName" label="学生" min-width="140" />
        <el-table-column label="学校 / 专业" min-width="220">
          <template #default="{ row }">
            <span>{{ row.targetSchoolCode }} / {{ row.targetMajorCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" min-width="190" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <AppButton @click="goToDetail(row.applicationId)">进入详情</AppButton>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <span>共 {{ total }} 条</span>
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          layout="prev, pager, next"
          @change="loadData()"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.domestic-review-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.page-title {
  margin: 0;
  font-size: 24px;
}

.page-subtitle {
  margin: 8px 0 0;
  color: #64748b;
  line-height: 1.6;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-top: 20px;
}

.summary-card {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 16px;
  background: #fff;
}

.summary-label {
  display: block;
  color: #64748b;
  font-size: 13px;
}

.summary-value {
  display: block;
  margin-top: 8px;
  font-size: 26px;
  color: #0f172a;
}

.toolbar {
  margin-top: 18px;
}

.sample-table {
  margin-top: 18px;
}

.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
}

@media (max-width: 960px) {
  .page-header {
    flex-direction: column;
  }

  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
