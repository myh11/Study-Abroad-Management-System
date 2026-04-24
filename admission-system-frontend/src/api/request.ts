import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { Result } from '../types/common'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('admission-token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const result = response.data as Result<unknown>
    if (result.code !== 0) {
      ElMessage.error(result.message || '请求失败')
      return Promise.reject(new Error(result.message || 'Request failed'))
    }
    return result.data as never
  },
  (error) => {
    const message = error.response?.data?.message || error.message || '网络异常'
    ElMessage.error(message)
    return Promise.reject(error)
  },
)

export default request
