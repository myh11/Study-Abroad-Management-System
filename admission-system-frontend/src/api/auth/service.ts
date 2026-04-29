import request from '../request'
import type {
  ChangePasswordRequest,
  CurrentUser,
  LoginRequest,
  LoginResponse,
} from '../../types/auth'

interface BackendAuthUser {
  token?: string | null
  user?: CurrentUser
  userId?: number
  username?: string
  role?: CurrentUser['role']
  schoolCode?: string | null
  school_code?: string | null
  mustChangePassword?: boolean
  must_change_password?: boolean
}

function normalizeCurrentUser(payload: BackendAuthUser): CurrentUser {
  return {
    id: payload.user?.id ?? payload.userId ?? 0,
    username: payload.user?.username ?? payload.username ?? '',
    role: payload.user?.role ?? payload.role ?? 'ADMIN',
    school_code: payload.user?.school_code ?? payload.school_code ?? payload.schoolCode ?? null,
    must_change_password:
      payload.user?.must_change_password ??
      payload.must_change_password ??
      payload.mustChangePassword ??
      false,
  }
}

export function loginApi(payload: LoginRequest) {
  return request.post<never, BackendAuthUser>('/auth/login', payload).then((response) => ({
    token: response.token ?? '',
    user: normalizeCurrentUser(response),
  }) as LoginResponse)
}

export function meApi() {
  return request.get<never, BackendAuthUser>('/auth/me').then(normalizeCurrentUser)
}

export function changePasswordApi(payload: ChangePasswordRequest) {
  return request.post<never, string>('/auth/change-password', payload)
}
