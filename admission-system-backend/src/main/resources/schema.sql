SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'user id',
  username VARCHAR(50) NOT NULL COMMENT 'username',
  password_hash VARCHAR(255) NOT NULL COMMENT 'password hash',
  role_type VARCHAR(32) NOT NULL COMMENT 'role type',
  school_code VARCHAR(32) NULL COMMENT 'bound school code for school reviewer',
  is_enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'enabled flag',
  must_change_password TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'must change password',
  login_fail_count INT NOT NULL DEFAULT 0 COMMENT 'consecutive login fail count',
  locked_until DATETIME NULL COMMENT 'account locked until',
  last_login_at DATETIME NULL COMMENT 'last login time',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'updated time',
  UNIQUE KEY uk_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='users';

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS login_fail_count INT NOT NULL DEFAULT 0 COMMENT 'consecutive login fail count';

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS locked_until DATETIME NULL COMMENT 'account locked until';

CREATE TABLE IF NOT EXISTS admission_batches (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'batch id',
  batch_name VARCHAR(100) NOT NULL COMMENT 'batch name',
  start_time DATETIME NOT NULL COMMENT 'start time',
  end_time DATETIME NOT NULL COMMENT 'end time',
  batch_status VARCHAR(32) NOT NULL COMMENT 'batch status',
  created_by BIGINT NULL COMMENT 'creator user id',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'updated time',
  CONSTRAINT fk_admission_batches_created_by FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='admission batches';

CREATE TABLE IF NOT EXISTS schools (
  school_code VARCHAR(32) PRIMARY KEY COMMENT 'school code',
  school_name VARCHAR(100) NOT NULL COMMENT 'school name',
  is_enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'enabled flag',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'updated time'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='schools';

CREATE TABLE IF NOT EXISTS majors (
  major_code VARCHAR(32) PRIMARY KEY COMMENT 'major code',
  school_code VARCHAR(32) NOT NULL COMMENT 'school code',
  major_name VARCHAR(100) NOT NULL COMMENT 'major name',
  min_average_score DECIMAL(5,2) NULL COMMENT 'minimum average score',
  min_math_score DECIMAL(5,2) NULL COMMENT 'minimum math score',
  min_english_score DECIMAL(5,2) NULL COMMENT 'minimum english score',
  min_physics_score DECIMAL(5,2) NULL COMMENT 'minimum physics score',
  min_liberal_arts_score DECIMAL(5,2) NULL COMMENT 'minimum liberal arts score',
  reserve_line DECIMAL(5,2) NULL COMMENT 'reserve line',
  waitlist_line DECIMAL(5,2) NULL COMMENT 'waitlist line',
  allow_adjustment_in TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'allow adjustment in',
  is_enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'enabled flag',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'updated time',
  CONSTRAINT fk_majors_school_code FOREIGN KEY (school_code) REFERENCES schools(school_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='majors';

CREATE TABLE IF NOT EXISTS school_quotas (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'school quota id',
  batch_id BIGINT NOT NULL COMMENT 'batch id',
  school_code VARCHAR(32) NOT NULL COMMENT 'school code',
  total_quota INT NOT NULL COMMENT 'total quota',
  used_quota INT NOT NULL DEFAULT 0 COMMENT 'used quota',
  remaining_quota INT NOT NULL DEFAULT 0 COMMENT 'remaining quota',
  school_min_score DECIMAL(5,2) NULL COMMENT 'school minimum score',
  school_min_math DECIMAL(5,2) NULL COMMENT 'school minimum math',
  school_min_english DECIMAL(5,2) NULL COMMENT 'school minimum english',
  quota_version INT NOT NULL DEFAULT 1 COMMENT 'quota rule version',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'updated time',
  UNIQUE KEY uk_school_quotas_batch_school (batch_id, school_code),
  CONSTRAINT fk_school_quotas_batch_id FOREIGN KEY (batch_id) REFERENCES admission_batches(id),
  CONSTRAINT fk_school_quotas_school_code FOREIGN KEY (school_code) REFERENCES schools(school_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='school quotas';

CREATE TABLE IF NOT EXISTS major_quotas (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'major quota id',
  batch_id BIGINT NOT NULL COMMENT 'batch id',
  school_code VARCHAR(32) NOT NULL COMMENT 'school code',
  major_code VARCHAR(32) NOT NULL COMMENT 'major code',
  total_quota INT NOT NULL COMMENT 'total quota',
  used_quota INT NOT NULL DEFAULT 0 COMMENT 'used quota',
  remaining_quota INT NOT NULL DEFAULT 0 COMMENT 'remaining quota',
  min_average_score DECIMAL(5,2) NULL COMMENT 'minimum average score',
  min_math_score DECIMAL(5,2) NULL COMMENT 'minimum math score',
  min_english_score DECIMAL(5,2) NULL COMMENT 'minimum english score',
  min_physics_score DECIMAL(5,2) NULL COMMENT 'minimum physics score',
  min_liberal_arts_score DECIMAL(5,2) NULL COMMENT 'minimum liberal arts score',
  reserve_line DECIMAL(5,2) NULL COMMENT 'reserve line',
  waitlist_line DECIMAL(5,2) NULL COMMENT 'waitlist line',
  allow_adjustment_in TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'allow adjustment in',
  quota_version INT NOT NULL DEFAULT 1 COMMENT 'quota rule version',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'updated time',
  UNIQUE KEY uk_major_quotas_batch_major (batch_id, major_code),
  KEY idx_major_quotas_batch_school (batch_id, school_code),
  CONSTRAINT fk_major_quotas_batch_id FOREIGN KEY (batch_id) REFERENCES admission_batches(id),
  CONSTRAINT fk_major_quotas_school_code FOREIGN KEY (school_code) REFERENCES schools(school_code),
  CONSTRAINT fk_major_quotas_major_code FOREIGN KEY (major_code) REFERENCES majors(major_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='major quotas';

CREATE TABLE IF NOT EXISTS files (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'file id',
  file_key VARCHAR(100) NOT NULL COMMENT 'file key',
  original_name VARCHAR(255) NOT NULL COMMENT 'original file name',
  storage_path VARCHAR(500) NOT NULL COMMENT 'storage path',
  file_type VARCHAR(50) NOT NULL COMMENT 'file type',
  file_size BIGINT NOT NULL COMMENT 'file size',
  business_type VARCHAR(50) NULL COMMENT 'business type',
  uploaded_by BIGINT NULL COMMENT 'uploaded by user id',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'updated time',
  UNIQUE KEY uk_files_file_key (file_key),
  CONSTRAINT fk_files_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='files';

CREATE TABLE IF NOT EXISTS students (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'student id',
  name VARCHAR(50) NOT NULL COMMENT 'student name',
  gender VARCHAR(16) NULL COMMENT 'gender',
  birth_date DATE NULL COMMENT 'birth date',
  current_school VARCHAR(100) NOT NULL COMMENT 'current school',
  grade VARCHAR(20) NULL COMMENT 'grade',
  email VARCHAR(100) NOT NULL COMMENT 'email',
  phone VARCHAR(30) NULL COMMENT 'phone',
  id_card_no VARCHAR(64) NULL COMMENT 'id card number',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'updated time',
  KEY idx_students_email (email),
  KEY idx_students_id_card_no (id_card_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='students';

CREATE TABLE IF NOT EXISTS transcripts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'transcript id',
  student_id BIGINT NOT NULL COMMENT 'student id',
  transcript_school_name VARCHAR(100) NOT NULL COMMENT 'school name on transcript',
  term_start DATE NULL COMMENT 'term start',
  term_end DATE NULL COMMENT 'term end',
  chinese_score DECIMAL(5,2) NULL COMMENT 'chinese score',
  math_score DECIMAL(5,2) NULL COMMENT 'math score',
  english_score DECIMAL(5,2) NULL COMMENT 'english score',
  physics_score DECIMAL(5,2) NULL COMMENT 'physics score',
  chemistry_score DECIMAL(5,2) NULL COMMENT 'chemistry score',
  history_score DECIMAL(5,2) NULL COMMENT 'history score',
  average_score DECIMAL(5,2) NULL COMMENT 'average score',
  failed_subject_count INT NULL COMMENT 'failed subject count',
  file_id BIGINT NOT NULL COMMENT 'file id',
  authenticity_risk_level VARCHAR(4) NULL COMMENT 'risk level',
  has_stamp_region TINYINT(1) NULL COMMENT 'has stamp region',
  clarity_level VARCHAR(32) NULL COMMENT 'clarity level',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'updated time',
  CONSTRAINT fk_transcripts_student_id FOREIGN KEY (student_id) REFERENCES students(id),
  CONSTRAINT fk_transcripts_file_id FOREIGN KEY (file_id) REFERENCES files(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='transcripts';

CREATE TABLE IF NOT EXISTS personal_statements (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'personal statement id',
  student_id BIGINT NOT NULL COMMENT 'student id',
  content TEXT NOT NULL COMMENT 'content',
  word_count INT NOT NULL COMMENT 'word count',
  quality_score DECIMAL(5,2) NULL COMMENT 'quality score',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'updated time',
  CONSTRAINT fk_personal_statements_student_id FOREIGN KEY (student_id) REFERENCES students(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='personal statements';

CREATE TABLE IF NOT EXISTS applications (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'application id',
  student_id BIGINT NOT NULL COMMENT 'student id',
  transcript_id BIGINT NOT NULL COMMENT 'transcript id',
  personal_statement_id BIGINT NOT NULL COMMENT 'personal statement id',
  batch_id BIGINT NOT NULL COMMENT 'batch id',
  target_school_code VARCHAR(32) NOT NULL COMMENT 'target school code',
  target_major_code VARCHAR(32) NOT NULL COMMENT 'target major code',
  source_application_id BIGINT NULL COMMENT 'source application id',
  created_by_agent_id BIGINT NOT NULL COMMENT 'agent user id',
  current_status VARCHAR(40) NOT NULL COMMENT 'application status',
  submit_time DATETIME NULL COMMENT 'submit time',
  cancel_reason VARCHAR(255) NULL COMMENT 'cancel reason',
  close_reason VARCHAR(64) NULL COMMENT 'close reason',
  rule_snapshot_id BIGINT NULL COMMENT 'rule snapshot id',
  waitlist_confirm_deadline DATETIME NULL COMMENT 'waitlist confirm deadline',
  reserved_at DATETIME NULL COMMENT 'reserved at',
  canceled_at DATETIME NULL COMMENT 'canceled at',
  closed_at DATETIME NULL COMMENT 'closed at',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'updated time',
  KEY idx_applications_student_id (student_id),
  KEY idx_applications_batch_id (batch_id),
  KEY idx_applications_status (current_status),
  KEY idx_applications_source_application_id (source_application_id),
  CONSTRAINT fk_applications_student_id FOREIGN KEY (student_id) REFERENCES students(id),
  CONSTRAINT fk_applications_transcript_id FOREIGN KEY (transcript_id) REFERENCES transcripts(id),
  CONSTRAINT fk_applications_personal_statement_id FOREIGN KEY (personal_statement_id) REFERENCES personal_statements(id),
  CONSTRAINT fk_applications_batch_id FOREIGN KEY (batch_id) REFERENCES admission_batches(id),
  CONSTRAINT fk_applications_target_school_code FOREIGN KEY (target_school_code) REFERENCES schools(school_code),
  CONSTRAINT fk_applications_target_major_code FOREIGN KEY (target_major_code) REFERENCES majors(major_code),
  CONSTRAINT fk_applications_source_application_id FOREIGN KEY (source_application_id) REFERENCES applications(id),
  CONSTRAINT fk_applications_created_by_agent_id FOREIGN KEY (created_by_agent_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='applications';

CREATE TABLE IF NOT EXISTS rule_snapshots (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'rule snapshot id',
  application_id BIGINT NOT NULL COMMENT 'application id',
  batch_id BIGINT NOT NULL COMMENT 'batch id',
  school_threshold_snapshot JSON NOT NULL COMMENT 'school threshold snapshot',
  major_threshold_snapshot JSON NOT NULL COMMENT 'major threshold snapshot',
  reserve_line_snapshot DECIMAL(5,2) NULL COMMENT 'reserve line snapshot',
  waitlist_line_snapshot DECIMAL(5,2) NULL COMMENT 'waitlist line snapshot',
  quota_rule_version INT NOT NULL COMMENT 'quota rule version',
  adjustment_rule_version INT NOT NULL COMMENT 'adjustment rule version',
  waitlist_sort_rule_version INT NOT NULL COMMENT 'waitlist sort rule version',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
  UNIQUE KEY uk_rule_snapshots_application_id (application_id),
  CONSTRAINT fk_rule_snapshots_application_id FOREIGN KEY (application_id) REFERENCES applications(id),
  CONSTRAINT fk_rule_snapshots_batch_id FOREIGN KEY (batch_id) REFERENCES admission_batches(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='rule snapshots';

CREATE TABLE IF NOT EXISTS domestic_reviews (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'domestic review id',
  application_id BIGINT NOT NULL COMMENT 'application id',
  reviewer_id BIGINT NOT NULL COMMENT 'reviewer id',
  material_complete_passed TINYINT(1) NOT NULL COMMENT 'material complete passed',
  identity_matched TINYINT(1) NOT NULL COMMENT 'identity matched',
  basic_score_passed TINYINT(1) NOT NULL COMMENT 'basic score passed',
  authenticity_risk_level VARCHAR(4) NOT NULL COMMENT 'authenticity risk level',
  standardization_passed TINYINT(1) NOT NULL COMMENT 'standardization passed',
  review_result VARCHAR(32) NOT NULL COMMENT 'review result',
  review_comment VARCHAR(1000) NULL COMMENT 'review comment',
  reviewed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'reviewed time',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'updated time',
  KEY idx_domestic_reviews_application_id (application_id),
  CONSTRAINT fk_domestic_reviews_application_id FOREIGN KEY (application_id) REFERENCES applications(id),
  CONSTRAINT fk_domestic_reviews_reviewer_id FOREIGN KEY (reviewer_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='domestic reviews';

CREATE TABLE IF NOT EXISTS school_reviews (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'school review id',
  application_id BIGINT NOT NULL COMMENT 'application id',
  reviewer_id BIGINT NOT NULL COMMENT 'reviewer id',
  school_threshold_passed TINYINT(1) NOT NULL COMMENT 'school threshold passed',
  major_threshold_passed TINYINT(1) NOT NULL COMMENT 'major threshold passed',
  school_quota_passed TINYINT(1) NOT NULL COMMENT 'school quota passed',
  major_quota_passed TINYINT(1) NOT NULL COMMENT 'major quota passed',
  academic_score DECIMAL(5,2) NOT NULL COMMENT 'academic score',
  material_score DECIMAL(5,2) NOT NULL COMMENT 'material score',
  matching_score DECIMAL(5,2) NOT NULL COMMENT 'matching score',
  total_score DECIMAL(5,2) NOT NULL COMMENT 'total score',
  suggested_major_code VARCHAR(32) NULL COMMENT 'suggested major code',
  waitlist_rank_snapshot INT NULL COMMENT 'waitlist rank snapshot',
  review_result VARCHAR(32) NOT NULL COMMENT 'review result',
  review_reason VARCHAR(1000) NULL COMMENT 'review reason',
  reviewed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'reviewed time',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'updated time',
  KEY idx_school_reviews_application_id (application_id),
  CONSTRAINT fk_school_reviews_application_id FOREIGN KEY (application_id) REFERENCES applications(id),
  CONSTRAINT fk_school_reviews_reviewer_id FOREIGN KEY (reviewer_id) REFERENCES users(id),
  CONSTRAINT fk_school_reviews_suggested_major_code FOREIGN KEY (suggested_major_code) REFERENCES majors(major_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='school reviews';

CREATE TABLE IF NOT EXISTS waitlist_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'waitlist record id',
  application_id BIGINT NOT NULL COMMENT 'application id',
  batch_id BIGINT NOT NULL COMMENT 'batch id',
  school_code VARCHAR(32) NOT NULL COMMENT 'school code',
  major_code VARCHAR(32) NOT NULL COMMENT 'major code',
  total_score DECIMAL(5,2) NOT NULL COMMENT 'total score',
  domestic_approved_at DATETIME NULL COMMENT 'domestic approved time before school review',
  key_subject_score DECIMAL(5,2) NULL COMMENT 'key subject score',
  application_submitted_at DATETIME NULL COMMENT 'application submitted time',
  current_rank INT NULL COMMENT 'current rank',
  rank_reason VARCHAR(500) NULL COMMENT 'rank reason',
  waitlist_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT 'waitlist record status',
  promoted_at DATETIME NULL COMMENT 'promoted time',
  confirm_deadline DATETIME NULL COMMENT 'confirm deadline',
  expired_at DATETIME NULL COMMENT 'expired time',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'updated time',
  UNIQUE KEY uk_waitlist_records_application_id (application_id),
  KEY idx_waitlist_records_batch_school_major (batch_id, school_code, major_code),
  CONSTRAINT fk_waitlist_records_application_id FOREIGN KEY (application_id) REFERENCES applications(id),
  CONSTRAINT fk_waitlist_records_batch_id FOREIGN KEY (batch_id) REFERENCES admission_batches(id),
  CONSTRAINT fk_waitlist_records_school_code FOREIGN KEY (school_code) REFERENCES schools(school_code),
  CONSTRAINT fk_waitlist_records_major_code FOREIGN KEY (major_code) REFERENCES majors(major_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='waitlist records';

CREATE TABLE IF NOT EXISTS application_status_histories (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'application status history id',
  application_id BIGINT NOT NULL COMMENT 'application id',
  old_status VARCHAR(40) NULL COMMENT 'old status',
  new_status VARCHAR(40) NOT NULL COMMENT 'new status',
  trigger_role VARCHAR(32) NOT NULL COMMENT 'trigger role',
  trigger_action VARCHAR(64) NOT NULL COMMENT 'trigger action',
  operator_id BIGINT NULL COMMENT 'operator id',
  operated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'operated time',
  remark VARCHAR(500) NULL COMMENT 'remark',
  KEY idx_application_status_histories_application_id (application_id),
  CONSTRAINT fk_application_status_histories_application_id FOREIGN KEY (application_id) REFERENCES applications(id),
  CONSTRAINT fk_application_status_histories_operator_id FOREIGN KEY (operator_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='application status histories';

CREATE TABLE IF NOT EXISTS audit_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'audit log id',
  operator_id BIGINT NULL COMMENT 'operator id',
  operator_role VARCHAR(32) NOT NULL COMMENT 'operator role',
  entity_type VARCHAR(32) NOT NULL COMMENT 'entity type',
  entity_id VARCHAR(64) NOT NULL COMMENT 'entity id',
  operation_type VARCHAR(64) NOT NULL COMMENT 'operation type',
  old_value JSON NULL COMMENT 'old value',
  new_value JSON NULL COMMENT 'new value',
  remark VARCHAR(500) NULL COMMENT 'remark',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
  KEY idx_audit_logs_entity (entity_type, entity_id),
  KEY idx_audit_logs_operator_id (operator_id),
  CONSTRAINT fk_audit_logs_operator_id FOREIGN KEY (operator_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='audit logs';
