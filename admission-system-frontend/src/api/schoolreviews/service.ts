import request from '../request'

export interface SchoolReviewListItem {
  applicationId: number
  studentName: string
  targetSchoolCode: string
  targetMajorCode: string
  status: string
  batchId: number
  updatedAt: string
}

export interface SchoolReviewPageResult {
  list: SchoolReviewListItem[]
  page: number
  pageSize: number
  total: number
}

export interface SchoolReviewSubmitPayload {
  review_result: 'RESERVE' | 'SUGGEST_ADJUSTMENT' | 'WAITLIST' | 'REJECT'
  review_reason: string
  school_threshold_passed: boolean
  major_threshold_passed: boolean
  school_quota_passed: boolean
  major_quota_passed: boolean
  academic_score: number
  material_score: number
  matching_score: number
  total_score: number
  suggested_major_code?: string
}

export function getSchoolReviewList(params?: {
  page?: number
  pageSize?: number
  status?: string
  batchId?: number
  schoolCode?: string
}) {
  return request.get<never, SchoolReviewPageResult>('/school-reviews', { params })
}

export function submitSchoolReview(applicationId: number, payload: SchoolReviewSubmitPayload) {
  return request.post<never, void>(`/school-reviews/${applicationId}/submit`, payload)
}
