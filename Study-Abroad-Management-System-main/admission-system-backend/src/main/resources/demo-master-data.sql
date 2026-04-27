SET NAMES utf8mb4;

INSERT INTO users (
  id, username, password_hash, role_type, school_code, is_enabled, must_change_password, last_login_at
) VALUES
  (1, 'admin', 'pbkdf2$65536$92gjp6o+HB4CCAy3Dq3/BQ==$6pCVT8YDMKUqJY0qqllbE0Si04wjKTX+Qtcwu6p5o9o=', 'ADMIN', NULL, 1, 0, NULL),
  (2, 'agent01', 'pbkdf2$65536$Mo77I4TmaHxdZJ6v3XTEDQ==$kkpU/UaOep0sD0b4SdgpNvMduAWn/OZyljvFpu3juhU=', 'AGENT', NULL, 1, 0, NULL),
  (3, 'agent02', 'pbkdf2$65536$Mo77I4TmaHxdZJ6v3XTEDQ==$kkpU/UaOep0sD0b4SdgpNvMduAWn/OZyljvFpu3juhU=', 'AGENT', NULL, 1, 0, NULL),
  (4, 'domestic01', 'pbkdf2$65536$gqWK3DRTKXNyMClalm5wWQ==$+KLSWJL8Jz034J9fBUYiW3uGkBj0gVajvy0wkfOF2LY=', 'DOMESTIC_REVIEWER', NULL, 1, 0, NULL),
  (5, 'school_usyd_01', 'pbkdf2$65536$3hzb5b+uZFnDtlc7S1Z9zA==$FdwmuR1Stw4KGNuklkrLaILMWRlriMuiiNl1dH1wp3U=', 'SCHOOL_REVIEWER', 'USYD', 1, 0, NULL)
ON DUPLICATE KEY UPDATE
  username = VALUES(username),
  password_hash = VALUES(password_hash),
  role_type = VALUES(role_type),
  school_code = VALUES(school_code),
  is_enabled = VALUES(is_enabled),
  must_change_password = VALUES(must_change_password),
  last_login_at = VALUES(last_login_at);

INSERT INTO admission_batches (
  id, batch_name, start_time, end_time, batch_status, created_by
) VALUES
  (1, '2026 Fall Batch', '2026-09-01 00:00:00', '2026-12-31 23:59:59', 'IN_PROGRESS', 1),
  (2, '2025 Fall Batch', '2025-09-01 00:00:00', '2025-12-31 23:59:59', 'FINISHED', 1)
ON DUPLICATE KEY UPDATE
  batch_name = VALUES(batch_name),
  start_time = VALUES(start_time),
  end_time = VALUES(end_time),
  batch_status = VALUES(batch_status),
  created_by = VALUES(created_by);

INSERT INTO schools (
  school_code, school_name, is_enabled
) VALUES
  ('ANU', 'Australian National University', 1),
  ('USYD', 'University of Sydney', 1),
  ('UNSW', 'University of New South Wales', 1)
ON DUPLICATE KEY UPDATE
  school_name = VALUES(school_name),
  is_enabled = VALUES(is_enabled);

INSERT INTO majors (
  major_code, school_code, major_name, min_average_score, min_math_score, min_english_score,
  min_physics_score, min_liberal_arts_score, reserve_line, waitlist_line, allow_adjustment_in, is_enabled
) VALUES
  ('ANU_CS', 'ANU', 'Computer Science', 88.00, 90.00, 80.00, NULL, NULL, 85.00, 82.00, 0, 1),
  ('ANU_IR', 'ANU', 'International Relations', 85.00, NULL, 88.00, NULL, 80.00, 82.00, 79.00, 1, 1),
  ('USYD_SE', 'USYD', 'Software Engineering', 84.00, 88.00, 75.00, NULL, NULL, 82.00, 78.00, 0, 1),
  ('USYD_BIZ', 'USYD', 'Business', 82.00, 78.00, 78.00, NULL, NULL, 80.00, 76.00, 1, 1),
  ('UNSW_DS', 'UNSW', 'Data Science', 85.00, 88.00, 76.00, NULL, NULL, 83.00, 79.00, 0, 1),
  ('UNSW_ME', 'UNSW', 'Mechanical Engineering', 84.00, 87.00, 76.00, 85.00, NULL, 82.00, 78.00, 0, 1)
ON DUPLICATE KEY UPDATE
  school_code = VALUES(school_code),
  major_name = VALUES(major_name),
  min_average_score = VALUES(min_average_score),
  min_math_score = VALUES(min_math_score),
  min_english_score = VALUES(min_english_score),
  min_physics_score = VALUES(min_physics_score),
  min_liberal_arts_score = VALUES(min_liberal_arts_score),
  reserve_line = VALUES(reserve_line),
  waitlist_line = VALUES(waitlist_line),
  allow_adjustment_in = VALUES(allow_adjustment_in),
  is_enabled = VALUES(is_enabled);

INSERT INTO school_quotas (
  id, batch_id, school_code, total_quota, used_quota, remaining_quota,
  school_min_score, school_min_math, school_min_english, quota_version
) VALUES
  (1, 1, 'ANU', 30, 0, 30, 85.00, 85.00, 80.00, 1),
  (2, 1, 'USYD', 35, 0, 35, 80.00, 78.00, 75.00, 1),
  (3, 1, 'UNSW', 32, 0, 32, 82.00, 80.00, 76.00, 1),
  (4, 2, 'ANU', 28, 0, 28, 84.00, 84.00, 80.00, 1),
  (5, 2, 'USYD', 30, 0, 30, 80.00, 78.00, 75.00, 1),
  (6, 2, 'UNSW', 30, 0, 30, 82.00, 80.00, 76.00, 1)
ON DUPLICATE KEY UPDATE
  total_quota = VALUES(total_quota),
  used_quota = VALUES(used_quota),
  remaining_quota = VALUES(remaining_quota),
  school_min_score = VALUES(school_min_score),
  school_min_math = VALUES(school_min_math),
  school_min_english = VALUES(school_min_english),
  quota_version = VALUES(quota_version);

INSERT INTO major_quotas (
  id, batch_id, school_code, major_code, total_quota, used_quota, remaining_quota,
  min_average_score, min_math_score, min_english_score, min_physics_score, min_liberal_arts_score,
  reserve_line, waitlist_line, allow_adjustment_in, quota_version
) VALUES
  (1, 1, 'ANU', 'ANU_CS', 15, 0, 15, 88.00, 90.00, 80.00, NULL, NULL, 85.00, 82.00, 0, 1),
  (2, 1, 'ANU', 'ANU_IR', 15, 0, 15, 85.00, NULL, 88.00, NULL, 80.00, 82.00, 79.00, 1, 1),
  (3, 1, 'USYD', 'USYD_SE', 18, 0, 18, 84.00, 88.00, 75.00, NULL, NULL, 82.00, 78.00, 0, 1),
  (4, 1, 'USYD', 'USYD_BIZ', 17, 0, 17, 82.00, 78.00, 78.00, NULL, NULL, 80.00, 76.00, 1, 1),
  (5, 1, 'UNSW', 'UNSW_DS', 16, 0, 16, 85.00, 88.00, 76.00, NULL, NULL, 83.00, 79.00, 0, 1),
  (6, 1, 'UNSW', 'UNSW_ME', 16, 0, 16, 84.00, 87.00, 76.00, 85.00, NULL, 82.00, 78.00, 0, 1),
  (7, 2, 'ANU', 'ANU_CS', 14, 0, 14, 88.00, 90.00, 80.00, NULL, NULL, 85.00, 82.00, 0, 1),
  (8, 2, 'ANU', 'ANU_IR', 14, 0, 14, 85.00, NULL, 88.00, NULL, 80.00, 82.00, 79.00, 1, 1),
  (9, 2, 'USYD', 'USYD_SE', 15, 0, 15, 84.00, 88.00, 75.00, NULL, NULL, 82.00, 78.00, 0, 1),
  (10, 2, 'USYD', 'USYD_BIZ', 15, 0, 15, 82.00, 78.00, 78.00, NULL, NULL, 80.00, 76.00, 1, 1),
  (11, 2, 'UNSW', 'UNSW_DS', 15, 0, 15, 85.00, 88.00, 76.00, NULL, NULL, 83.00, 79.00, 0, 1),
  (12, 2, 'UNSW', 'UNSW_ME', 15, 0, 15, 84.00, 87.00, 76.00, 85.00, NULL, 82.00, 78.00, 0, 1)
ON DUPLICATE KEY UPDATE
  total_quota = VALUES(total_quota),
  used_quota = VALUES(used_quota),
  remaining_quota = VALUES(remaining_quota),
  min_average_score = VALUES(min_average_score),
  min_math_score = VALUES(min_math_score),
  min_english_score = VALUES(min_english_score),
  min_physics_score = VALUES(min_physics_score),
  min_liberal_arts_score = VALUES(min_liberal_arts_score),
  reserve_line = VALUES(reserve_line),
  waitlist_line = VALUES(waitlist_line),
  allow_adjustment_in = VALUES(allow_adjustment_in),
  quota_version = VALUES(quota_version);

INSERT INTO files (
  id, file_key, original_name, storage_path, file_type, file_size, business_type, uploaded_by
) VALUES
  (1, 'demo-transcript-001', 'demo-transcript.pdf', '/mock/files/demo-transcript.pdf', 'application/pdf', 524288, 'transcript', 2)
ON DUPLICATE KEY UPDATE
  file_key = VALUES(file_key),
  original_name = VALUES(original_name),
  storage_path = VALUES(storage_path),
  file_type = VALUES(file_type),
  file_size = VALUES(file_size),
  business_type = VALUES(business_type),
  uploaded_by = VALUES(uploaded_by);
