import request from '../request'
import type {
  ApplicationDetail,
  ApplicationDraftPayload,
  ApplicationListItem,
} from '../../types/application'

export function createApplicationDraft(payload: ApplicationDraftPayload) {
  return request.post<never, { application_id: number }>('/applications', payload)
}

export function updateApplicationDraft(applicationId: number, payload: ApplicationDraftPayload) {
  return request.put<never, { application_id: number }>(`/applications/${applicationId}`, payload)
}

export function getMyApplications() {
  return request.get<never, ApplicationListItem[]>('/applications/my')
}

export function getApplicationDetail(applicationId: number) {
  return request.get<never, ApplicationDetail>(`/applications/${applicationId}`)
}

export function submitApplication(applicationId: number) {
  return request.post<never, void>(`/applications/${applicationId}/submit`)
}

export function cancelApplication(applicationId: number, reason?: string) {
  return request.post<never, void>(`/applications/${applicationId}/cancel`, { reason })
}

export function uploadSupplement(applicationId: number, transcript_file_id: number) {
  return request.post<never, void>(`/applications/${applicationId}/supplement`, {
    transcript_file_id,
  })
}

export function waitlistConfirm(applicationId: number, accept: boolean) {
  return request.post<never, void>(`/applications/${applicationId}/waitlist-confirm`, { accept })
}

export function acceptAdjustment(applicationId: number) {
  return request.post<never, void>(`/applications/${applicationId}/accept-adjustment`)
}

export function rejectAdjustment(applicationId: number) {
  return request.post<never, void>(`/applications/${applicationId}/reject-adjustment`)
}

export function closeApplication(applicationId: number, reason?: string) {
  return request.post<never, void>(`/applications/${applicationId}/close`, { reason })
}
