import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '../layouts/AppLayout.vue'
import LoginPage from '../views/login/LoginPage.vue'
import ChangePasswordPage from '../views/login/ChangePasswordPage.vue'
import DashboardPage from '../views/dashboard/DashboardPage.vue'
import ApplicationCreatePage from '../views/applications/ApplicationCreatePage.vue'
import ApplicationListPage from '../views/applications/ApplicationListPage.vue'
import ApplicationDetailPage from '../views/applications/ApplicationDetailPage.vue'
import DomesticReviewListPage from '../views/domesticreviews/DomesticReviewListPage.vue'
import DomesticReviewDetailPage from '../views/domesticreviews/DomesticReviewDetailPage.vue'
import SchoolReviewListPage from '../views/schoolreviews/SchoolReviewListPage.vue'
import SchoolReviewDetailPage from '../views/schoolreviews/SchoolReviewDetailPage.vue'
import WaitlistManagePage from '../views/waitlists/WaitlistManagePage.vue'
import BatchesPage from '../views/admin/BatchesPage.vue'
import SchoolsMajorsPage from '../views/admin/SchoolsMajorsPage.vue'
import UsersPage from '../views/admin/UsersPage.vue'
import QuotasPage from '../views/admin/QuotasPage.vue'
import AuditLogsPage from '../views/admin/AuditLogsPage.vue'
import { useAuthStore } from '../stores/auth'
import type { UserRole } from '../types/common'

declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    requiresAuth?: boolean
    roles?: UserRole[]
    hideInMenu?: boolean
  }
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginPage,
      meta: {
        title: '登录',
        hideInMenu: true,
      },
    },
    {
      path: '/change-password',
      name: 'change-password',
      component: ChangePasswordPage,
      meta: {
        title: '修改密码',
        requiresAuth: true,
        hideInMenu: true,
      },
    },
    {
      path: '/',
      component: AppLayout,
      redirect: '/dashboard',
      meta: {
        requiresAuth: true,
      },
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: DashboardPage,
          meta: { title: '首页工作台' },
        },
        {
          path: 'applications/create',
          name: 'application-create',
          component: ApplicationCreatePage,
          meta: { title: '新建申请', roles: ['AGENT', 'ADMIN'] },
        },
        {
          path: 'applications',
          name: 'application-list',
          component: ApplicationListPage,
          meta: { title: '我的申请', roles: ['AGENT', 'ADMIN'] },
        },
        {
          path: 'applications/:id',
          name: 'application-detail',
          component: ApplicationDetailPage,
          meta: {
            title: '申请详情',
            roles: ['AGENT', 'DOMESTIC_REVIEWER', 'SCHOOL_REVIEWER', 'ADMIN'],
            hideInMenu: true,
          },
        },
        {
          path: 'domesticreviews',
          name: 'domestic-review-list',
          component: DomesticReviewListPage,
          meta: { title: '国内审核', roles: ['DOMESTIC_REVIEWER', 'ADMIN'] },
        },
        {
          path: 'domesticreviews/:id',
          name: 'domestic-review-detail',
          component: DomesticReviewDetailPage,
          meta: {
            title: '国内审核详情',
            roles: ['DOMESTIC_REVIEWER', 'ADMIN'],
            hideInMenu: true,
          },
        },
        {
          path: 'schoolreviews',
          name: 'school-review-list',
          component: SchoolReviewListPage,
          meta: { title: '学校审核', roles: ['SCHOOL_REVIEWER', 'ADMIN'] },
        },
        {
          path: 'schoolreviews/:id',
          name: 'school-review-detail',
          component: SchoolReviewDetailPage,
          meta: {
            title: '学校审核详情',
            roles: ['SCHOOL_REVIEWER', 'ADMIN'],
            hideInMenu: true,
          },
        },
        {
          path: 'waitlists',
          name: 'waitlist-manage',
          component: WaitlistManagePage,
          meta: { title: '候补管理', roles: ['ADMIN', 'SCHOOL_REVIEWER'] },
        },
        {
          path: 'admin/batches',
          name: 'admin-batches',
          component: BatchesPage,
          meta: { title: '批次管理', roles: ['ADMIN'] },
        },
        {
          path: 'admin/schools-majors',
          name: 'admin-schools-majors',
          component: SchoolsMajorsPage,
          meta: { title: '学校专业配置', roles: ['ADMIN'] },
        },
        {
          path: 'admin/users',
          name: 'admin-users',
          component: UsersPage,
          meta: { title: '用户管理', roles: ['ADMIN'] },
        },
        {
          path: 'admin/quotas',
          name: 'admin-quotas',
          component: QuotasPage,
          meta: { title: '配额管理', roles: ['ADMIN'] },
        },
        {
          path: 'admin/audit-logs',
          name: 'admin-audit-logs',
          component: AuditLogsPage,
          meta: { title: '日志查询', roles: ['ADMIN'] },
        },
      ],
    },
  ],
})

router.beforeEach(async (to) => {
  document.title = `${to.meta.title ?? '留学申请系统'} - 留学申请系统`
  const authStore = useAuthStore()
  const hasToken = authStore.hasToken

  if (to.meta.requiresAuth !== false && to.path !== '/login') {
    if (!hasToken) {
      return '/login'
    }

    if (!authStore.currentUser) {
      try {
        await authStore.fetchMe()
      } catch {
        authStore.logout()
        return '/login'
      }
    }

    if (authStore.requiresPasswordChange && to.path !== '/change-password') {
      return '/change-password'
    }

    if (to.meta.roles?.length && !to.meta.roles.includes(authStore.currentUser?.role as UserRole)) {
      return '/dashboard'
    }
  }

  if (to.path === '/login' && hasToken) {
    return authStore.requiresPasswordChange ? '/change-password' : '/dashboard'
  }

  if (to.path === '/change-password' && hasToken && !authStore.requiresPasswordChange) {
    return '/dashboard'
  }

  return true
})

export default router
