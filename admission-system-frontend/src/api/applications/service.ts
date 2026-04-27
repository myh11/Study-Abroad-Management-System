import request from '../request'

function toBackendDraftPayload(payload: any) {
  const s = payload.student || {}
  const sc = payload.score || payload.transcript || {}
  const ps = payload.personalStatement || payload.personal_statement || {}
  return {
    batchId: payload.batchId ?? payload.batch_id,
    targetSchoolCode: payload.targetSchoolCode ?? payload.school_code,
    targetMajorCode: payload.targetMajorCode ?? payload.major_code,
    student: {
      name: s.name ?? s.full_name,
      gender: s.gender,
      birthDate: s.birthDate ?? s.birth_date,
      currentSchool: s.currentSchool ?? s.current_school,
      grade: s.grade ?? s.current_grade,
      email: s.email,
      phone: s.phone,
      idCardNo: s.idCardNo ?? s.id_number,
    },
    score: {
      transcriptSchoolName: sc.transcriptSchoolName ?? sc.transcript_school_name ?? '',
      termStart: sc.termStart ?? sc.term_start ?? '',
      termEnd: sc.termEnd ?? sc.term_end ?? '',
      chinese: sc.chinese ?? 0,
      math: sc.math ?? 0,
      english: sc.english ?? 0,
      physics: sc.physics ?? 0,
      chemistry: sc.chemistry ?? 0,
      history: sc.history ?? 0,
      averageScore: sc.averageScore ?? sc.average_score,
      failedSubjectCount: sc.failedSubjectCount ?? sc.failed_subject_count,
      fileId: sc.fileId ?? sc.file_id,
    },
    personalStatement: {
      content: ps.content ?? ps.statement_content ?? '',
    },
  }
}

export function createApplicationDraft(payload: any) {
  return request.post<any, any>('/applications', toBackendDraftPayload(payload))
}

export function updateApplicationDraft(applicationId: number, payload: any) {
  return request.put<any, any>(`/applications/${applicationId}`, toBackendDraftPayload(payload))
}

export function getMyApplications(params?: any) {
  return request.get<any, any>('/applications/my', { params })
}

export function getApplicationDetail(applicationId: number) {
  return request.get<any, any>(`/applications/${applicationId}`)
}

export function submitApplication(applicationId: number) {
  return request.post<any, string>(`/applications/${applicationId}/submit`)
}

export function cancelApplication(applicationId: number, reason?: string) {
  return request.post<any, string>(`/applications/${applicationId}/cancel`, { reason })
}

export function uploadSupplement(applicationId: number, payload: any) {
  const backendPayload = {
    newTranscriptFileId: payload.newTranscriptFileId ?? payload.transcript_file_id,
    supplementNote: payload.supplementNote ?? payload.supplement_note ?? '',
  }
  return request.post<any, any>(`/applications/${applicationId}/supplement`, backendPayload)
}

export function waitlistConfirm(applicationId: number, accept: boolean) {
  return request.post<any, string>(`/applications/${applicationId}/waitlist-confirm`, { accept })
}

export function acceptAdjustment(applicationId: number, payload?: any) {
  const backendPayload = payload?.targetMajorCode || payload?.target_major_code
    ? { targetMajorCode: payload.targetMajorCode ?? payload.target_major_code }
    : undefined
  return request.post<any, string>(`/applications/${applicationId}/accept-adjustment`, backendPayload)
}

export function rejectAdjustment(applicationId: number) {
  return request.post<any, string>(`/applications/${applicationId}/reject-adjustment`)
}
