import request from '../request'

export function claimDomesticReview(applicationId: number) {
  return request.post<never, void>(`/domesticreviews/${applicationId}/claim`)
}

export interface DomesticReviewSubmitPayload {
  review_result: 'PASS' | 'SUPPLEMENT_REQUIRED' | 'REJECT'
  review_comment: string
}

export function submitDomesticReview(applicationId: number, payload: DomesticReviewSubmitPayload) {
  return request.post<never, void>(`/domesticreviews/${applicationId}/submit`, payload)
}

// 当前后端还未提供列表/详情接口，这里先保留模块占位，页面可继续走申请详情联调。
