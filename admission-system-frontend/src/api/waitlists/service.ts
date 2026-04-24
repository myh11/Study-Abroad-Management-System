import request from '../request'

export interface WaitlistDetail {
  application_id: number
  application_status: string
  batch_id: number
  school_code: string
  major_code: string
  waitlist_id: number
  waitlist_status: string
  current_rank: number
  rank_reason?: string | null
  total_score: number
  key_subject_score: number
  application_submitted_at?: string | null
  promoted_at?: string | null
  confirm_deadline?: string | null
  expired_at?: string | null
  waitlist_confirm_deadline?: string | null
}

export function getWaitlistDetail(applicationId: number) {
  return request.get<never, WaitlistDetail>(`/waitlists/${applicationId}`)
}

export function promoteWaitlist(applicationId: number) {
  return request.post<never, string>(`/waitlists/${applicationId}/promote`)
}

export function invalidateWaitlist(applicationId: number) {
  return request.post<never, string>(`/waitlists/${applicationId}/invalidate`)
}
