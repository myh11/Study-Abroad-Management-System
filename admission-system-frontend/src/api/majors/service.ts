import request from '../request'

export interface MajorPayload {
  majorCode: string
  schoolCode: string
  majorName: string
  minAverageScore?: number | null
  minMathScore?: number | null
  minEnglishScore?: number | null
  minPhysicsScore?: number | null
  minLiberalArtsScore?: number | null
  reserveLine?: number | null
  waitlistLine?: number | null
  allowAdjustmentIn: boolean
  enabled: boolean
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

export function updateMajor(majorCode: string, payload: Omit<MajorPayload, 'majorCode'>) {
  return request.put<never, void>(`/majors/${majorCode}`, payload)
}

export function updateMajorStatus(majorCode: string, status: 'ENABLED' | 'DISABLED') {
  return request.post<never, void>(`/majors/${majorCode}/status`, { status })
}
