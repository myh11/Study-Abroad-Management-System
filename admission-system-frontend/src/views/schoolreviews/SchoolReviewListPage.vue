<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppButton from '../../components/common/AppButton.vue'
import { getSchoolReviewList, type SchoolReviewListItem } from '../../api/schoolreviews/service'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const rows = ref<SchoolReviewListItem[]>([])
const total = ref(0)

const query = reactive({
  page: 1,
  pageSize: 20,
  status: '',
})

const statusOptions = [
  { label: '全部', value: '' },
  { label: '学校审核中', value: 'SCHOOL_REVIEWING' },
  { label: '候补中', value: 'WAITLISTED' },
  { label: '候补待确认', value: 'WAITLIST_PENDING_CONFIRM' },
  { label: '预录取', value: 'RESERVED' },
  { label: '调剂建议', value: 'ADJUSTMENT_SUGGESTED' },
  { label: '学校拒绝', value: 'SCHOOL_REJECTED' },
]

const currentSchoolLabel = computed(() => authStore.currentUser?.school_code || '当前学校')

async function loadData() {
  loading.value = true
  try {
    const result = await getSchoolReviewList({
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
  router.push(`/schoolreviews/${id}`)
}

onMounted(loadData)
</script>

<template>
  <section class="page-shell">
    <header class="page-header">
      <div>
        <h1 class="page-title">学校审核队列</h1>
        <p class="page-subtitle">当前页面已接入后端正式列表接口，按当前学校账号自动筛出可访问申请。</p>
      </div>
      <AppButton type="primary" :loading="loading" @click="loadData">刷新列表</AppButton>
    </header>

    <div class="metric-grid">
      <article class="metric-card">
        <div class="metric-label">当前学校</div>
        <div class="metric-value">{{ currentSchoolLabel }}</div>
        <div class="hint-text">当前账号只会看到自己学校的申请。</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">当前总数</div>
        <div class="metric-value">{{ total }}</div>
        <div class="hint-text">包含学校审核中、候补、结果态等学校相关申请。</div>
      </article>
      <article class="metric-card">
        <div class="metric-label">详情入口</div>
        <div class="metric-value">真实详情页</div>
        <div class="hint-text">进入详情后可继续提交学校审核或查看结果。</div>
      </article>
    </div>

    <el-card shadow="never" class="page-card list-card">
      <div class="table-toolbar">
        <div>
          <h2 class="section-title">学校审核任务列表</h2>
          <p class="section-subtitle">可以按状态切换，直接进入详情页继续审核或查看结果。</p>
        </div>
        <el-select v-model="query.status" placeholder="筛选状态" style="width: 220px" @change="query.page = 1; loadData()">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </div>

      <el-table v-loading="loading" :data="rows" border>
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
    </el-card>
  </section>
</template>

<style scoped>
.list-card {
  padding: 24px;
}

.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
}
</style>
