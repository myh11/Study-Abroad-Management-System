import type { SubmitDomesticReviewPayload } from '../../api/domesticreviews/service'

export type ReviewResult = SubmitDomesticReviewPayload['result']

export function getExpectedStatusByResult(result: ReviewResult) {
  if (result === 'PASS') return 'SCHOOL_REVIEWING'
  if (result === 'SUPPLEMENT_REQUIRED') return 'DOMESTIC_SUPPLEMENT'
  return 'DOMESTIC_REJECTED'
}

export function canClaimByStatus(status?: string) {
  return status === 'SUBMITTED'
}

export function canSubmitByStatus(status?: string) {
  return status === 'DOMESTIC_REVIEWING'
}

export function validateSubmitPayload(payload: SubmitDomesticReviewPayload) {
  if (!payload.comment.trim()) {
    return { valid: false, reason: 'EMPTY_COMMENT' as const }
  }

  if (!payload.authenticityRiskLevel.trim()) {
    return { valid: false, reason: 'EMPTY_RISK_LEVEL' as const }
  }

  return { valid: true as const }
}
