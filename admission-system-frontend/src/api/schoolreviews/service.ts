import request from '../request'

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

export function submitSchoolReview(applicationId: number, payload: SchoolReviewSubmitPayload) {
  return request.post<never, void>(`/school-reviews/${applicationId}/submit`, payload)
}

// 当前后端还未提供列表/详情接口，这里先保留模块占位，页面可继续走申请详情联调。
