import request from '../request'

export interface UserPayload {
  username: string
  password?: string
  roleType: 'AGENT' | 'DOMESTIC_REVIEWER' | 'SCHOOL_REVIEWER' | 'ADMIN'
  schoolCode?: string | null
  enabled: boolean
  mustChangePassword?: boolean
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

export function updateUserStatus(userId: number, status: 'ENABLED' | 'DISABLED') {
  return request.post<never, void>(`/users/${userId}/status`, { status })
}
