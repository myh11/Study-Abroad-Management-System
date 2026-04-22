import request from '../request'

export function getQuotas() {
  return request.get<never, unknown[]>('/quotas')
}

export function getQuotaAdjustments() {
  return request.get<never, unknown[]>('/quotas/adjustments')
}

export function adjustSchoolQuota(id: number, delta: number) {
  return request.post<never, void>(`/quotas/schools/${id}/adjust`, { delta })
}

export function adjustMajorQuota(id: number, delta: number) {
  return request.post<never, void>(`/quotas/majors/${id}/adjust`, { delta })
}
