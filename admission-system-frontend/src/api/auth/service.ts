import request from '../request'
import type { ChangePasswordRequest, CurrentUser, LoginRequest, LoginResponse } from '../../types/auth'

export function loginApi(payload: LoginRequest) {
  return request.post<never, LoginResponse>('/auth/login', payload)
}

export function changePasswordApi(payload: ChangePasswordRequest) {
  return request.post<never, boolean>('/auth/change-password', payload)
}

export function meApi() {
  return request.get<never, CurrentUser>('/auth/me')
}
