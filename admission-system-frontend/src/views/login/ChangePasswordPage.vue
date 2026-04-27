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
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

async function handleSubmit() {
  if (!formModel.oldPassword || !formModel.newPassword || !formModel.confirmPassword) {
    toast.error('请完整填写密码信息')
    return
  }

  if (formModel.newPassword !== formModel.confirmPassword) {
    toast.error('两次输入的新密码不一致')
    return
  }

  submitting.value = true
  try {
    await authStore.changePassword({
      oldPassword: formModel.oldPassword,
      newPassword: formModel.newPassword,
    })
    toast.success('密码修改成功，请继续使用系统')
    router.replace('/dashboard')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="login-shell">
    <div class="login-panel page-card">
      <div class="login-copy">
        <div class="badge">首次登录</div>
        <h1>请先修改初始密码</h1>
        <p>为保障账号安全，首次登录后必须先修改密码，修改成功后才能访问业务页面。</p>
      </div>

      <AppForm>
        <el-form-item label="旧密码">
          <el-input v-model="formModel.oldPassword" type="password" show-password placeholder="请输入旧密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="formModel.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input v-model="formModel.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
        <el-form-item>
          <AppButton type="primary" :loading="submitting" @click="handleSubmit">确认修改</AppButton>
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
  font-size: 34px;
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
