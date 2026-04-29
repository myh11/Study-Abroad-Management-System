import request from '../request'

export interface DomesticReviewListItem {
  applicationId: number
  studentName: string
  targetSchoolCode: string
  targetMajorCode: string
  status: string
  batchId: number
  updatedAt: string
}

export interface DomesticReviewPageResult {
  list: DomesticReviewListItem[]
  page: number
  pageSize: number
  total: number
}

export interface DomesticReviewApplicationDetail {
  basicInfo?: Record<string, unknown>
  studentInfo?: Record<string, unknown>
  applicationInfo?: Record<string, unknown>
  scoreSummary?: Record<string, unknown>
}

export function getDomesticReviewList(params?: { page?: number; pageSize?: number; status?: string; batchId?: number }) {
  return request.get<never, DomesticReviewPageResult>('/domesticreviews', { params })
}

export function getApplicationDetail(applicationId: number) {
  return request.get<never, DomesticReviewApplicationDetail>(`/applications/${applicationId}`)
}

export function claimDomesticReview(applicationId: number) {
  return request.post<never, void>(`/domesticreviews/${applicationId}/claim`)
}

export interface SubmitDomesticReviewPayload {
  materialComplete: boolean
  identityMatched: boolean
  basicScorePassed: boolean
  authenticityRiskLevel: string
  standardizationPassed: boolean
  result: 'PASS' | 'SUPPLEMENT_REQUIRED' | 'REJECT'
  comment: string
}

export function submitDomesticReview(applicationId: number, payload: SubmitDomesticReviewPayload) {
  return request.post<never, void>(`/domesticreviews/${applicationId}/submit`, payload)
}
