<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import AppButton from '../../components/common/AppButton.vue'
import AppForm from '../../components/common/AppForm.vue'
import { toast } from '../../components/common/toast'

const router = useRouter()
const authStore = useAuthStore()
const submitting = ref(false)

const formModel = reactive({
  username: 'agent01',
  password: 'agent123',
})

async function handleLogin() {
  submitting.value = true
  try {
    await authStore.login(formModel)
    toast.success('登录成功')
    router.push('/dashboard')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="login-shell">
    <div class="login-panel page-card">
      <div class="login-copy">
        <div class="badge">PRD 对齐</div>
        <h1>留学申请与审核系统</h1>
        <p>基于当前后端接口、角色权限和分工文档整理的前端公共骨架。</p>
      </div>
      <AppForm>
        <el-form-item label="用户名">
          <el-input v-model="formModel.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="formModel.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item>
          <AppButton type="primary" :loading="submitting" @click="handleLogin">登录</AppButton>
        </el-form-item>
      </AppForm>
    </div>
  </div>
</template>

<style scoped>
.login-shell {
  display: grid;
  place-items: center;
  min-height: 100vh;
  padding: 24px;
  background:
    radial-gradient(circle at top left, rgba(25, 88, 190, 0.22), transparent 30%),
    radial-gradient(circle at bottom right, rgba(17, 32, 63, 0.18), transparent 35%),
    linear-gradient(180deg, #edf3fb 0%, #f9fbff 100%);
}

.login-panel {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 32px;
  width: min(980px, 100%);
  padding: 36px;
}

.login-copy h1 {
  margin: 18px 0 12px;
  font-size: 40px;
  line-height: 1.1;
}

.login-copy p {
  margin: 0;
  color: #61718c;
  font-size: 15px;
}

.badge {
  display: inline-flex;
  padding: 6px 12px;
  border-radius: 999px;
  background: #e2ecfb;
  color: #17366f;
  font-size: 12px;
  font-weight: 700;
}
</style>
