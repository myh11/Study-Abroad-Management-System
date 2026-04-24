<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Lock, Promotion, Warning } from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/auth'
import { toast } from '../../components/common/toast'

const router = useRouter()
const authStore = useAuthStore()
const submitting = ref(false)

const formModel = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const strength = computed(() => {
  const password = formModel.newPassword
  let level = 0
  if (password.length >= 8) level += 1
  if (/[A-Z]/.test(password)) level += 1
  if (/\d/.test(password)) level += 1
  if (/[^A-Za-z0-9]/.test(password)) level += 1

  const palette = [
    { label: '未填写', tone: '#d6dce8' },
    { label: '较弱', tone: '#d95f5f' },
    { label: '一般', tone: '#f2a93b' },
    { label: '较强', tone: '#2f7cf6' },
    { label: '很强', tone: '#1c9b62' },
  ]

  return {
    level,
    ...palette[level],
  }
})

async function handleSubmit() {
  if (!formModel.oldPassword || !formModel.newPassword || !formModel.confirmPassword) {
    toast.error('请填写完整密码信息')
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
    toast.success('密码修改成功，请重新登录')
    router.push('/login')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <section class="page-shell">
    <header class="page-header">
      <div>
        <h1 class="page-title">修改密码</h1>
        <p class="page-subtitle">
          首次登录用户必须先完成改密，系统才会开放其余敏感业务接口。
        </p>
      </div>
    </header>

    <div class="password-grid">
      <el-card shadow="never" class="page-card security-card">
        <div class="security-head">
          <div class="security-icon">
            <el-icon><Lock /></el-icon>
          </div>
          <div>
            <div class="security-title">安全提示</div>
            <p class="security-copy">
              新密码至少 8 位，必须与旧密码不同。建议包含大写字母、数字和特殊字符。
            </p>
          </div>
        </div>

        <div class="rule-list">
          <div class="rule-item">
            <el-icon><Promotion /></el-icon>
            <span>连续输错 5 次会被临时锁定 30 分钟。</span>
          </div>
          <div class="rule-item">
            <el-icon><Warning /></el-icon>
            <span>改密成功后当前 token 会失效，请重新登录继续操作。</span>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="page-card form-card">
        <el-form label-position="top" class="password-form">
          <el-form-item label="原密码">
            <el-input
              v-model="formModel.oldPassword"
              type="password"
              show-password
              placeholder="请输入当前密码"
            />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input
              v-model="formModel.newPassword"
              type="password"
              show-password
              placeholder="请输入新密码"
            />
            <div class="strength-panel">
              <div class="strength-bars">
                <span
                  v-for="index in 4"
                  :key="index"
                  class="strength-bar"
                  :style="{ background: index <= strength.level ? strength.tone : '#d6dce8' }"
                />
              </div>
              <span class="strength-text">强度：{{ strength.label }}</span>
            </div>
          </el-form-item>
          <el-form-item label="确认新密码">
            <el-input
              v-model="formModel.confirmPassword"
              type="password"
              show-password
              placeholder="请再次输入新密码"
            />
          </el-form-item>
          <div class="password-actions">
            <el-button @click="router.back()">取消</el-button>
            <el-button type="primary" :loading="submitting" @click="handleSubmit">
              提交修改
            </el-button>
          </div>
        </el-form>
      </el-card>
    </div>
  </section>
</template>

<style scoped>
.password-grid {
  display: grid;
  grid-template-columns: minmax(260px, 360px) minmax(0, 1fr);
  gap: 24px;
}

.security-card,
.form-card {
  padding: 28px;
}

.security-head {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.security-icon {
  display: grid;
  place-items: center;
  width: 48px;
  height: 48px;
  border-radius: 16px;
  background: linear-gradient(135deg, #0f5bd8 0%, #2d7ef7 100%);
  color: #fff;
  font-size: 22px;
}

.security-title {
  font-size: 20px;
  font-weight: 700;
}

.security-copy {
  margin: 10px 0 0;
  color: #60708b;
  line-height: 1.7;
}

.rule-list {
  display: grid;
  gap: 12px;
  margin-top: 28px;
}

.rule-item {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 14px 16px;
  border-radius: 16px;
  background: #f3f7fe;
  color: #31435f;
  font-size: 14px;
}

.password-form {
  max-width: 560px;
}

.strength-panel {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
}

.strength-bars {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  width: 180px;
}

.strength-bar {
  height: 6px;
  border-radius: 999px;
}

.strength-text {
  color: #6b7a92;
  font-size: 13px;
}

.password-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 1080px) {
  .password-grid {
    grid-template-columns: 1fr;
  }
}
</style>
