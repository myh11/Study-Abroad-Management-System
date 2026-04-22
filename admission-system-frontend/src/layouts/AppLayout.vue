<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const menuItems = computed(() =>
  router
    .getRoutes()
    .filter((item) => item.meta?.title && !item.meta?.hideInMenu && item.path !== '/')
    .filter((item) => {
      const roles = item.meta?.roles as string[] | undefined
      return !roles?.length || roles.includes(authStore.currentUser?.role || '')
    })
    .sort((a, b) => a.path.localeCompare(b.path)),
)

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<template>
  <el-container class="layout-shell">
    <el-aside width="250px" class="layout-side">
      <div class="brand-block">
        <div class="brand-title">留学申请系统</div>
        <div class="brand-subtitle">前端公共骨架</div>
      </div>
      <el-menu :default-active="route.path" router class="layout-menu">
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          {{ item.meta.title }}
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="layout-header">
        <div>
          <div class="layout-heading">工作区</div>
          <div class="hint-text">按 PRD 和当前后端接口组织页面与模块。</div>
        </div>
        <div class="header-actions">
          <el-tag type="info" round>{{ authStore.currentUser?.role || '未登录' }}</el-tag>
          <span>{{ authStore.currentUser?.username || '访客' }}</span>
          <el-button text @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout-shell {
  min-height: 100vh;
}

.layout-side {
  border-right: 1px solid #dde5f0;
  background: linear-gradient(180deg, #11203f 0%, #172b54 100%);
  color: #fff;
}

.brand-block {
  padding: 28px 24px 20px;
}

.brand-title {
  font-size: 22px;
  font-weight: 700;
}

.brand-subtitle {
  margin-top: 8px;
  color: rgba(255, 255, 255, 0.68);
  font-size: 13px;
}

.layout-menu {
  border-right: none;
  background: transparent;
}

.layout-menu :deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.82);
}

.layout-menu :deep(.el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 28px;
  border-bottom: 1px solid #dde5f0;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(16px);
}

.layout-heading {
  font-size: 22px;
  font-weight: 700;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #394760;
}

.layout-main {
  padding: 28px;
}
</style>
