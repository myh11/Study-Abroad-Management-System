import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { loginApi, meApi } from '../api/auth/service'
import type { CurrentUser, LoginRequest } from '../types/auth'

const TOKEN_KEY = 'admission-token'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) ?? '')
  const currentUser = ref<CurrentUser | null>(null)

  const hasToken = computed(() => Boolean(token.value))

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

  function logout() {
    token.value = ''
    currentUser.value = null
    localStorage.removeItem(TOKEN_KEY)
  }

  return {
    token,
    currentUser,
    hasToken,
    login,
    fetchMe,
    logout,
    setToken,
  }
})
