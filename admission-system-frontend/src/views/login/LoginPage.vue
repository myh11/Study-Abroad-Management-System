<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { DataAnalysis, School, Select, UserFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/auth'
import { toast } from '../../components/common/toast'

const router = useRouter()
const authStore = useAuthStore()
const submitting = ref(false)

const formModel = reactive({
  username: 'admin',
  password: 'admin123',
})

const demoAccounts = [
  { label: '系统管理员', username: 'admin', password: 'admin123' },
  { label: '代理专员', username: 'agent01', password: 'agent123' },
  { label: '国内审查员', username: 'domestic01', password: 'domestic123' },
  { label: '学校专员', username: 'school01', password: 'school123' },
]

async function handleLogin() {
  submitting.value = true
  try {
    await authStore.login(formModel)
    toast.success('登录成功')
    router.push(authStore.requiresPasswordChange ? '/change-password' : '/dashboard')
  } finally {
    submitting.value = false
  }
}

function fillDemoAccount(username: string, password: string) {
  formModel.username = username
  formModel.password = password
}
</script>

<template>
  <div class="login-shell">
    <section class="brand-panel">
      <div class="brand-top">
        <div class="brand-logo">
          <el-icon><DataAnalysis /></el-icon>
        </div>
        <div>
          <div class="brand-name">留学申请与审核系统</div>
          <div class="brand-tag">Study Abroad Management System</div>
        </div>
      </div>

      <div class="brand-copy">
        <div class="brand-badge">当前基线</div>
        <h1>一站式留学申请与全流程审核工作台</h1>
        <p>
          连接代理、国内审查员、学校审核员与管理员，覆盖申请创建、审核、候补、调剂、配额与审计的完整闭环。
        </p>
      </div>

      <div class="feature-grid">
        <article class="feature-card">
          <el-icon><Select /></el-icon>
          <div>
            <h3>统一状态机</h3>
            <p>申请流转、候补补位、调剂与关闭都以真实状态机为准。</p>
          </div>
        </article>
        <article class="feature-card">
          <el-icon><School /></el-icon>
          <div>
            <h3>学校归属校验</h3>
            <p>敏感接口统一做 JWT、RBAC 和学校归属控制。</p>
          </div>
        </article>
        <article class="feature-card">
          <el-icon><UserFilled /></el-icon>
          <div>
            <h3>首次改密与锁定</h3>
            <p>首次登录强制改密，连续输错超过阈值自动锁定账户。</p>
          </div>
        </article>
      </div>
    </section>

    <section class="login-panel">
      <div class="login-card page-card">
        <div class="login-head">
          <h2>账号登录</h2>
          <p>请使用统一测试账号登录系统，首次登录后需立即修改密码。</p>
        </div>

        <el-form label-position="top" class="login-form">
          <el-form-item label="用户名">
            <el-input v-model="formModel.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input
              v-model="formModel.password"
              type="password"
              show-password
              placeholder="请输入密码"
            />
          </el-form-item>
          <el-button type="primary" class="login-submit" :loading="submitting" @click="handleLogin">
            登录系统
          </el-button>
        </el-form>

        <div class="demo-box">
          <div class="demo-title">联调演示账号</div>
          <div class="demo-list">
            <button
              v-for="item in demoAccounts"
              :key="item.label"
              type="button"
              class="demo-item"
              @click="fillDemoAccount(item.username, item.password)"
            >
              <span>{{ item.label }}</span>
              <span>{{ item.username }} / {{ item.password }}</span>
            </button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.login-shell {
  display: grid;
  grid-template-columns: minmax(540px, 1.2fr) minmax(420px, 0.8fr);
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(24, 100, 228, 0.14), transparent 28%),
    linear-gradient(135deg, #f5f8fd 0%, #eff4fb 100%);
}

.brand-panel {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 54px 60px;
  color: #f9fbff;
  background: linear-gradient(135deg, #10408c 0%, #0a2658 48%, #081a3f 100%);
}

.brand-top {
  display: flex;
  gap: 16px;
  align-items: center;
}

.brand-logo {
  display: grid;
  place-items: center;
  width: 52px;
  height: 52px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.12);
  font-size: 24px;
}

.brand-name {
  font-size: 22px;
  font-weight: 700;
}

.brand-tag {
  margin-top: 4px;
  color: rgba(255, 255, 255, 0.68);
  font-size: 12px;
}

.brand-badge {
  display: inline-flex;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  font-size: 12px;
  font-weight: 700;
}

.brand-copy h1 {
  margin: 20px 0 18px;
  max-width: 560px;
  font-size: 44px;
  line-height: 1.14;
}

.brand-copy p {
  max-width: 560px;
  color: rgba(255, 255, 255, 0.78);
  line-height: 1.75;
  font-size: 15px;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.feature-card {
  display: grid;
  gap: 18px;
  padding: 20px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(18px);
}

.feature-card h3 {
  margin: 0;
  font-size: 16px;
}

.feature-card p {
  margin: 8px 0 0;
  color: rgba(255, 255, 255, 0.68);
  font-size: 13px;
  line-height: 1.7;
}

.feature-card :deep(.el-icon) {
  font-size: 20px;
}

.login-panel {
  display: grid;
  place-items: center;
  padding: 32px;
}

.login-card {
  width: min(520px, 100%);
  padding: 38px;
}

.login-head h2 {
  margin: 0;
  font-size: 32px;
  color: #16243a;
}

.login-head p {
  margin: 12px 0 0;
  color: #677892;
  line-height: 1.7;
}

.login-form {
  margin-top: 28px;
}

.login-submit {
  width: 100%;
  height: 46px;
  margin-top: 8px;
  border-radius: 14px;
}

.demo-box {
  margin-top: 26px;
  padding: 20px;
  border-radius: 20px;
  background: #f4f7fc;
}

.demo-title {
  font-size: 14px;
  font-weight: 700;
  color: #22324f;
}

.demo-list {
  display: grid;
  gap: 12px;
  margin-top: 14px;
}

.demo-item {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #dce4f2;
  border-radius: 14px;
  background: #fff;
  color: #4b5c76;
  text-align: left;
  cursor: pointer;
}

.demo-item:hover {
  border-color: #2c78f6;
  color: #16396f;
}

@media (max-width: 1180px) {
  .login-shell {
    grid-template-columns: 1fr;
  }

  .feature-grid {
    grid-template-columns: 1fr;
  }
}
</style>
