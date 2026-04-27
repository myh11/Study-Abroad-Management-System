import request from '../request'

export interface UserPayload {
  username: string
  password?: string
  role: 'AGENT' | 'DOMESTIC_REVIEWER' | 'SCHOOL_REVIEWER' | 'ADMIN'
  school_code?: string | null
  is_enabled: boolean
}

export function getUsers() {
  return request.get<never, unknown[]>('/users')
}

export function createUser(payload: UserPayload) {
  return request.post<never, void>('/users', payload)
}

export function updateUser(userId: number, payload: Omit<UserPayload, 'password'>) {
  return request.put<never, void>(`/users/${userId}`, payload)
}

export function updateUserStatus(userId: number, is_enabled: boolean) {
  return request.post<never, void>(`/users/${userId}/status`, { is_enabled })
}
