import request from '../request'

export interface SchoolPayload {
  schoolCode: string
  schoolName: string
  enabled: boolean
}

export function getSchools() {
  return request.get<never, unknown[]>('/schools')
}

export function createSchool(payload: SchoolPayload) {
  return request.post<never, void>('/schools', payload)
}

export function updateSchool(schoolCode: string, payload: Omit<SchoolPayload, 'schoolCode'>) {
  return request.put<never, void>(`/schools/${schoolCode}`, payload)
}

export function updateSchoolStatus(schoolCode: string, status: 'ENABLED' | 'DISABLED') {
  return request.post<never, void>(`/schools/${schoolCode}/status`, { status })
}
