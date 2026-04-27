import request from '../request'
import type { ApplicationDetail } from '../../types/application'

// 国内审核 Service 层
// submit 的 payload 必须与后端 DomesticReviewSubmitRequest DTO 保持一致，
// 字段名使用 camelCase，不可私自改成其它命名。

/*
 获取国内审核详情页所需的申请详情。
 当前后端未开放 /domesticreviews/{id} 详情接口，统一复用 /applications/{id}。
 */
export function getApplicationDetail(applicationId: number) {
  return request.get<never, ApplicationDetail>(`/applications/${applicationId}`)
}

/*
 认领国内审核任务。
 */
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

/*
 提交国内审核结果。
 */
export function submitDomesticReview(applicationId: number, payload: SubmitDomesticReviewPayload) {
  return request.post<never, void>(`/domesticreviews/${applicationId}/submit`, payload)
}
