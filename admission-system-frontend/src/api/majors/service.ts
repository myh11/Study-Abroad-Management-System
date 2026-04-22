import request from '../request'

export interface MajorPayload {
  major_code: string
  school_code: string
  major_name: string
  min_average_score?: number | null
  reserve_line?: number | null
  waitlist_line?: number | null
  allow_adjustment_in: boolean
  is_enabled: boolean
}

export function getMajors(schoolCode?: string) {
  return request.get<never, unknown[]>('/majors', {
    params: {
      schoolCode,
    },
  })
}

export function createMajor(payload: MajorPayload) {
  return request.post<never, void>('/majors', payload)
}

export function updateMajor(majorCode: string, payload: Omit<MajorPayload, 'major_code'>) {
  return request.put<never, void>(`/majors/${majorCode}`, payload)
}

export function updateMajorStatus(majorCode: string, is_enabled: boolean) {
  return request.post<never, void>(`/majors/${majorCode}/status`, { is_enabled })
}
