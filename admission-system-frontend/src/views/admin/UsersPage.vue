<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { getSchools } from '../../api/schools/service'
import { createUser, getUsers, updateUser, updateUserStatus, type UserPayload } from '../../api/users/service'
import { toast } from '../../components/common/toast'

interface UserRow {
  id: number
  username: string
  role_type: UserPayload['roleType']
  school_code?: string | null
  is_enabled: number | boolean
  must_change_password: number | boolean
  last_login_at?: string | null
}

const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const rows = ref<UserRow[]>([])
const schoolOptions = ref<{ school_code: string; school_name: string }[]>([])

const formModel = reactive<UserPayload>({
  username: '',
  password: '',
  roleType: 'AGENT',
  schoolCode: '',
  enabled: true,
  mustChangePassword: true,
})

const roleOptions = [
  { label: '代理专员', value: 'AGENT' },
  { label: '国内审查员', value: 'DOMESTIC_REVIEWER' },
  { label: '学校审核员', value: 'SCHOOL_REVIEWER' },
  { label: '管理员', value: 'ADMIN' },
]

function resetForm() {
  editingId.value = null
  formModel.username = ''
  formModel.password = ''
  formModel.roleType = 'AGENT'
  formModel.schoolCode = ''
  formModel.enabled = true
  formModel.mustChangePassword = true
}

async function loadData() {
  loading.value = true
  try {
    const [users, schools] = await Promise.all([getUsers(), getSchools()])
    rows.value = (users as UserRow[]) || []
    schoolOptions.value = (schools as { school_code: string; school_name: string }[]) || []
  } finally {
    loading.value = false
  }
}

function openDialog(row?: UserRow) {
  resetForm()
  if (row) {
    editingId.value = row.id
    formModel.username = row.username
    formModel.roleType = row.role_type
    formModel.schoolCode = row.school_code || ''
    formModel.enabled = Boolean(row.is_enabled)
    formModel.mustChangePassword = Boolean(row.must_change_password)
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (editingId.value) {
    await updateUser(editingId.value, {
      username: formModel.username,
      roleType: formModel.roleType,
      schoolCode: formModel.schoolCode || null,
      enabled: formModel.enabled,
      mustChangePassword: formModel.mustChangePassword,
    })
    toast.success('用户已更新')
  } else {
    await createUser({
      ...formModel,
      schoolCode: formModel.schoolCode || null,
    })
    toast.success('用户已创建')
  }
  dialogVisible.value = false
  await loadData()
}

async function toggleStatus(row: UserRow) {
  await updateUserStatus(row.id, Boolean(row.is_enabled) ? 'DISABLED' : 'ENABLED')
  toast.success('用户状态已更新')
  await loadData()
}

onMounted(loadData)
</script>

<template>
  <section class="page-shell">
    <header class="page-header">
      <div>
        <h1 class="page-title">用户管理</h1>
        <p class="page-subtitle">账号角色、归属学校、启停状态和首次改密标记都走真实管理接口。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增用户</el-button>
    </header>

    <el-card shadow="never" class="page-card card-body" v-loading="loading">
      <el-table :data="rows" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="180" />
        <el-table-column prop="role_type" label="角色" width="160" />
        <el-table-column prop="school_code" label="归属学校" width="160" />
        <el-table-column label="启用" width="100">
          <template #default="{ row }">
            <span class="status-pill" :class="Boolean(row.is_enabled) ? 'success' : 'danger'">
              {{ Boolean(row.is_enabled) ? 'ENABLED' : 'DISABLED' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="首次改密" width="120">
          <template #default="{ row }">
            {{ Boolean(row.must_change_password) ? '是' : '否' }}
          </template>
        </el-table-column>
        <el-table-column prop="last_login_at" label="最后登录" min-width="180" />
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
              <el-button link @click="toggleStatus(row)">
                {{ Boolean(row.is_enabled) ? '停用' : '启用' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑用户' : '新增用户'" width="560px">
      <el-form label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="formModel.username" :disabled="Boolean(editingId)" />
        </el-form-item>
        <el-form-item v-if="!editingId" label="初始密码">
          <el-input v-model="formModel.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="formModel.roleType">
            <el-option v-for="item in roleOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="归属学校">
          <el-select v-model="formModel.schoolCode" clearable>
            <el-option
              v-for="item in schoolOptions"
              :key="item.school_code"
              :label="`${item.school_code} - ${item.school_name}`"
              :value="item.school_code"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="formModel.enabled" />
        </el-form-item>
        <el-form-item label="首次登录强制改密">
          <el-switch v-model="formModel.mustChangePassword" />
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

.row-actions {
  display: flex;
  gap: 10px;
}
</style>
