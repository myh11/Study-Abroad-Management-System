import request from '../request'

export interface QuotaListResult {
  schoolQuotas: unknown[]
  majorQuotas: unknown[]
}

export function getQuotas(params?: { batchId?: number; schoolCode?: string }) {
  return request.get<never, QuotaListResult>('/quotas', { params })
}

export function getQuotaAdjustments(params?: { batchId?: number; schoolCode?: string; majorCode?: string }) {
  return request.get<never, unknown[]>('/quotas/adjustments', { params })
}

export interface SchoolQuotaAdjustPayload {
  totalQuota?: number
  deltaQuota?: number
  schoolMinScore?: number | null
  schoolMinMath?: number | null
  schoolMinEnglish?: number | null
  remark?: string
}

export interface MajorQuotaAdjustPayload {
  totalQuota?: number
  deltaQuota?: number
  minAverageScore?: number | null
  minMathScore?: number | null
  minEnglishScore?: number | null
  minPhysicsScore?: number | null
  minLiberalArtsScore?: number | null
  reserveLine?: number | null
  waitlistLine?: number | null
  allowAdjustmentIn?: boolean
  remark?: string
}

export function adjustSchoolQuota(id: number, payload?: SchoolQuotaAdjustPayload) {
  return request.post<never, void>(`/quotas/schools/${id}/adjust`, payload)
}

export function adjustMajorQuota(id: number, payload?: MajorQuotaAdjustPayload) {
  return request.post<never, void>(`/quotas/majors/${id}/adjust`, payload)
}
