<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Calendar,
  Clock,
  DataAnalysis,
  Document,
  HomeFilled,
  House,
  Key,
  List,
  Monitor,
  Operation,
  School,
  Tickets,
  UserFilled,
  User,
} from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'

type MenuRole = 'AGENT' | 'DOMESTIC_REVIEWER' | 'SCHOOL_REVIEWER' | 'ADMIN'

interface MenuEntry {
  path: string
  title: string
  icon: unknown
  roles: MenuRole[]
}

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const collapsed = ref(false)

const roleLabelMap: Record<MenuRole, string> = {
  AGENT: '代理专员',
  DOMESTIC_REVIEWER: '国内审查员',
  SCHOOL_REVIEWER: '学校专员',
  ADMIN: '系统管理员',
}

const menuGroups: { title: string; items: MenuEntry[] }[] = [
  {
    title: '工作区',
    items: [{ path: '/dashboard', title: '工作台', icon: HomeFilled, roles: ['AGENT', 'DOMESTIC_REVIEWER', 'SCHOOL_REVIEWER', 'ADMIN'] }],
  },
  {
    title: '审核中心',
    items: [
      { path: '/schoolreviews', title: '学校审核', icon: School, roles: ['SCHOOL_REVIEWER', 'ADMIN'] },
      { path: '/waitlists', title: '候补管理', icon: Clock, roles: ['SCHOOL_REVIEWER', 'ADMIN'] },
    ],
  },
  {
    title: '系统管理',
    items: [
      { path: '/admin/batches', title: '批次管理', icon: Calendar, roles: ['ADMIN'] },
      { path: '/admin/schools-majors', title: '学校与专业', icon: House, roles: ['ADMIN'] },
      { path: '/admin/quotas', title: '配额管理', icon: DataAnalysis, roles: ['ADMIN'] },
      { path: '/admin/users', title: '用户管理', icon: UserFilled, roles: ['ADMIN'] },
      { path: '/admin/audit-logs', title: '操作日志', icon: Tickets, roles: ['ADMIN'] },
    ],
  },
]

const visibleGroups = computed(() =>
  menuGroups
    .map((group) => ({
      ...group,
      items: group.items.filter((item) =>
        item.roles.includes((authStore.currentUser?.role || 'AGENT') as MenuRole),
      ),
    }))
    .filter((group) => group.items.length > 0),
)

const activeTitle = computed(() => route.meta.title || '工作区')
const currentRoleLabel = computed(() =>
  roleLabelMap[(authStore.currentUser?.role || 'AGENT') as MenuRole] || '未登录',
)

function goToChangePassword() {
  router.push('/change-password')
}

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="app-shell">
    <aside class="app-sidebar" :class="{ collapsed }">
      <div class="brand-bar">
        <div class="brand-mark">
          <el-icon><Monitor /></el-icon>
        </div>
        <div v-if="!collapsed" class="brand-copy">
          <div class="brand-title">留学申请与审核系统</div>
          <div class="brand-subtitle">Study Abroad System</div>
        </div>
      </div>

      <div class="menu-stack">
        <div v-for="group in visibleGroups" :key="group.title" class="menu-group">
          <div v-if="!collapsed" class="menu-group-title">{{ group.title }}</div>
          <router-link
            v-for="item in group.items"
            :key="item.path"
            :to="item.path"
            class="menu-link"
            :class="{ active: route.path.startsWith(item.path) }"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span v-if="!collapsed">{{ item.title }}</span>
          </router-link>
        </div>
      </div>
    </aside>

    <div class="app-main">
      <header class="app-header">
        <div class="header-leading">
          <el-button circle plain class="collapse-btn" @click="collapsed = !collapsed">
            <el-icon><Operation /></el-icon>
          </el-button>
          <div>
            <div class="crumb-line">
              <span>首页</span>
              <span>/</span>
              <span>{{ activeTitle }}</span>
            </div>
            <div class="header-title">{{ activeTitle }}</div>
          </div>
        </div>

        <div class="header-panel">
          <div class="status-chip">
            <el-icon><Document /></el-icon>
            <span>只认当前接口文档 v4</span>
          </div>
          <div class="user-pill">
            <div class="user-badge">
              <el-icon><User /></el-icon>
            </div>
            <div class="user-copy">
              <div class="user-name">{{ authStore.currentUser?.username || '访客' }}</div>
              <div class="user-role">{{ currentRoleLabel }}</div>
            </div>
          </div>
          <el-button plain @click="goToChangePassword">
            <el-icon><Key /></el-icon>
            修改密码
          </el-button>
          <el-button type="primary" plain @click="handleLogout">
            <el-icon><List /></el-icon>
            退出
          </el-button>
        </div>
      </header>

      <main class="app-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
.app-shell {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(23, 91, 216, 0.12), transparent 28%),
    linear-gradient(180deg, #f5f8fd 0%, #edf3fb 100%);
}

.app-sidebar {
  display: flex;
  flex-direction: column;
  padding: 22px 16px;
  border-right: 1px solid rgba(210, 219, 235, 0.9);
  background: linear-gradient(180deg, #12386f 0%, #0d2248 100%);
  color: #f9fbff;
  transition: width 0.2s ease;
}

.app-sidebar.collapsed {
  width: 88px;
  padding-left: 12px;
  padding-right: 12px;
}

.brand-bar {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 10px 10px 22px;
}

.brand-mark {
  display: grid;
  place-items: center;
  width: 44px;
  height: 44px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.14);
  font-size: 20px;
}

.brand-title {
  font-size: 18px;
  font-weight: 700;
}

.brand-subtitle {
  margin-top: 4px;
  color: rgba(255, 255, 255, 0.6);
  font-size: 12px;
}

.menu-stack {
  display: grid;
  gap: 18px;
}

.menu-group {
  display: grid;
  gap: 8px;
}

.menu-group-title {
  padding: 0 12px;
  color: rgba(255, 255, 255, 0.56);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.menu-link {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 46px;
  padding: 0 14px;
  border-radius: 16px;
  color: rgba(255, 255, 255, 0.78);
  transition: all 0.2s ease;
}

.menu-link:hover,
.menu-link.active {
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
}

.app-main {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 24px 28px 16px;
}

.header-leading,
.header-panel {
  display: flex;
  align-items: center;
  gap: 16px;
}

.crumb-line {
  display: flex;
  gap: 8px;
  color: #72839d;
  font-size: 13px;
}

.header-title {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 700;
  color: #16243a;
}

.collapse-btn {
  border: 1px solid #d6e0ee;
  background: rgba(255, 255, 255, 0.86);
}

.status-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid #d6e0ee;
  color: #47617f;
  font-size: 13px;
}

.user-pill {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid #d6e0ee;
}

.user-badge {
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0f5bd8 0%, #297bf7 100%);
  color: #fff;
}

.user-name {
  font-weight: 700;
  color: #1a2942;
}

.user-role {
  margin-top: 2px;
  color: #70819b;
  font-size: 12px;
}

.app-content {
  padding: 0 28px 28px;
}

@media (max-width: 1280px) {
  .app-shell {
    grid-template-columns: 88px minmax(0, 1fr);
  }

  .brand-copy,
  .menu-group-title,
  .menu-link span,
  .status-chip {
    display: none;
  }
}

@media (max-width: 980px) {
  .app-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-panel {
    flex-wrap: wrap;
  }
}
</style>
