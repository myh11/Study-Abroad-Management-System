import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { changePasswordApi, loginApi, meApi } from '../api/auth/service'
import type { ChangePasswordRequest, CurrentUser, LoginRequest } from '../types/auth'

const TOKEN_KEY = 'admission-token'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) ?? '')
  const currentUser = ref<CurrentUser | null>(null)

  const hasToken = computed(() => Boolean(token.value))
  const needChangePassword = computed(() => {
    // 兼容后端返回字段命名差异
    return Boolean(currentUser.value?.must_change_password || currentUser.value?.mustChangePassword)
  })

  function setToken(value: string) {
    token.value = value
    localStorage.setItem(TOKEN_KEY, value)
  }

  async function login(payload: LoginRequest) {
    const response = await loginApi(payload)
    setToken(response.token)
    currentUser.value = response.user
    return response
  }

  async function changePassword(payload: ChangePasswordRequest) {
    await changePasswordApi(payload)
    // 改密成功后立刻刷新 me，确保 mustChangePassword 状态同步
    return fetchMe()
  }

  async function fetchMe() {
    const user = await meApi()
    currentUser.value = user
    return user
  }

  function logout() {
    token.value = ''
    currentUser.value = null
    localStorage.removeItem(TOKEN_KEY)
  }

  return {        //将暴露的方法和状态导出
    token,
    currentUser,
    hasToken,
    needChangePassword,
    login,
    changePassword,
    fetchMe,
    logout,
    setToken,
  }
})
