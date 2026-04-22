export type ApplicationStatus =
  | 'DRAFT'
  | 'SUBMITTED'
  | 'DOMESTIC_REVIEWING'
  | 'DOMESTIC_SUPPLEMENT'
  | 'DOMESTIC_REJECTED'
  | 'SCHOOL_REVIEWING'
  | 'WAITLISTED'
  | 'WAITLIST_PENDING_CONFIRM'
  | 'RESERVED'
  | 'ADJUSTMENT_SUGGESTED'
  | 'SCHOOL_REJECTED'
  | 'CANCELED'
  | 'CLOSED'

export interface StudentInfo {
  full_name: string
  gender: 'MALE' | 'FEMALE'
  birth_date: string
  current_school: string
  current_grade: string
  email: string
  phone: string
  id_number: string
}

export interface TranscriptInfo {
  file_id?: number | null
  average_score: number
  failed_subject_count: number
  course_scores: string
}

export interface PersonalStatementInfo {
  file_id?: number | null
  statement_content: string
  word_count: number
}

export interface ApplicationDraftPayload {
  batch_id: number
  school_code: string
  major_code: string
  student: StudentInfo
  transcript: TranscriptInfo
  personal_statement: PersonalStatementInfo
}

export interface ApplicationListItem {
  id: number
  current_status: ApplicationStatus
  school_code: string
  major_code: string
  submitted_at?: string | null
  updated_at?: string | null
}

export interface StatusHistoryItem {
  id: number
  from_status?: ApplicationStatus | null
  to_status: ApplicationStatus
  action_name: string
  operated_by: number
  operated_at: string
}

export interface AuditLogItem {
  id: number
  entity_type: string
  entity_id: string
  action_name: string
  operated_by: number
  operated_at: string
  detail?: string | null
}

export interface RuleSnapshotItem {
  id: number
  batch_id: number
  school_code: string
  major_code: string
  snapshot_json: string
}

export interface ApplicationDetail {
  id: number
  batch_id: number
  school_code: string
  major_code: string
  current_status: ApplicationStatus
  student: StudentInfo
  transcript: TranscriptInfo
  personal_statement: PersonalStatementInfo
  rule_snapshots: RuleSnapshotItem[]
  status_histories: StatusHistoryItem[]
  audit_logs: AuditLogItem[]
}
