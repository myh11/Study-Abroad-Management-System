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
  must_change_password?: boolean
}

export interface LoginResponse {
  token: string
  user: CurrentUser
}
