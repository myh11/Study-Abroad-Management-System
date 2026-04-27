import type { UserRole } from './common'

export interface LoginRequest {
  username: string
  password: string
}

export interface CurrentUser {
  id: number
  username: string
  role: UserRole
  school_code?: string | null
  // 后端可能返回下划线或驼峰，这里都兼容，避免联调时字段名不一致导致逻辑失效
  must_change_password?: boolean
  mustChangePassword?: boolean
}

export interface LoginResponse {
  token: string
  user: CurrentUser
}

export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
}
