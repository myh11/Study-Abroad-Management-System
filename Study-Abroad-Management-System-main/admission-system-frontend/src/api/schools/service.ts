import request from '../request'

export interface SchoolPayload {
  school_code: string
  school_name: string
  is_enabled: boolean
}

export function getSchools() {
  return request.get<never, unknown[]>('/schools')
}

export function createSchool(payload: SchoolPayload) {
  return request.post<never, void>('/schools', payload)
}

export function updateSchool(schoolCode: string, payload: Omit<SchoolPayload, 'school_code'>) {
  return request.put<never, void>(`/schools/${schoolCode}`, payload)
}

export function updateSchoolStatus(schoolCode: string, is_enabled: boolean) {
  return request.post<never, void>(`/schools/${schoolCode}/status`, { is_enabled })
}
