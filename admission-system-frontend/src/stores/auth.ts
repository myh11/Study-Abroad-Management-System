import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { changePasswordApi, loginApi, meApi } from '../api/auth/service'
import type { ChangePasswordRequest, CurrentUser, LoginRequest } from '../types/auth'

const TOKEN_KEY = 'admission-token'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) ?? '')
  const currentUser = ref<CurrentUser | null>(null)

  const hasToken = computed(() => Boolean(token.value))
  const requiresPasswordChange = computed(() => Boolean(currentUser.value?.must_change_password))

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

  async function fetchMe() {
    const user = await meApi()
    currentUser.value = user
    return user
  }

  async function changePassword(payload: ChangePasswordRequest) {
    await changePasswordApi(payload)
    logout()
  }

  function logout() {
    token.value = ''
    currentUser.value = null
    localStorage.removeItem(TOKEN_KEY)
  }

  return {
    token,
    currentUser,
    hasToken,
    requiresPasswordChange,
    login,
    fetchMe,
    changePassword,
    logout,
    setToken,
  }
})
