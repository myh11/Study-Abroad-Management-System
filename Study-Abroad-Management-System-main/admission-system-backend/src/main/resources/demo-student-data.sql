SET NAMES utf8mb4;

INSERT INTO files (
  id, file_key, original_name, storage_path, file_type, file_size, business_type, uploaded_by
) VALUES
  (101, 'student-transcript-001', 'student-001-transcript.pdf', '/mock/files/student-001-transcript.pdf', 'application/pdf', 420000, 'transcript', 2),
  (102, 'student-transcript-002', 'student-002-transcript.pdf', '/mock/files/student-002-transcript.pdf', 'application/pdf', 421000, 'transcript', 2),
  (103, 'student-transcript-003', 'student-003-transcript.pdf', '/mock/files/student-003-transcript.pdf', 'application/pdf', 422000, 'transcript', 2),
  (104, 'student-transcript-004', 'student-004-transcript.pdf', '/mock/files/student-004-transcript.pdf', 'application/pdf', 423000, 'transcript', 2),
  (105, 'student-transcript-005', 'student-005-transcript.pdf', '/mock/files/student-005-transcript.pdf', 'application/pdf', 424000, 'transcript', 2),
  (106, 'student-transcript-006', 'student-006-transcript.pdf', '/mock/files/student-006-transcript.pdf', 'application/pdf', 425000, 'transcript', 2),
  (107, 'student-transcript-007', 'student-007-transcript.pdf', '/mock/files/student-007-transcript.pdf', 'application/pdf', 426000, 'transcript', 2),
  (108, 'student-transcript-008', 'student-008-transcript.pdf', '/mock/files/student-008-transcript.pdf', 'application/pdf', 427000, 'transcript', 2),
  (109, 'student-transcript-009', 'student-009-transcript.pdf', '/mock/files/student-009-transcript.pdf', 'application/pdf', 428000, 'transcript', 2),
  (110, 'student-transcript-010', 'student-010-transcript.pdf', '/mock/files/student-010-transcript.pdf', 'application/pdf', 429000, 'transcript', 2),
  (111, 'student-transcript-011', 'student-011-transcript.pdf', '/mock/files/student-011-transcript.pdf', 'application/pdf', 430000, 'transcript', 2),
  (112, 'student-transcript-012', 'student-012-transcript.pdf', '/mock/files/student-012-transcript.pdf', 'application/pdf', 431000, 'transcript', 2),
  (113, 'student-transcript-013', 'student-013-transcript.pdf', '/mock/files/student-013-transcript.pdf', 'application/pdf', 432000, 'transcript', 2),
  (114, 'student-transcript-014', 'student-014-transcript.pdf', '/mock/files/student-014-transcript.pdf', 'application/pdf', 433000, 'transcript', 2),
  (115, 'student-transcript-015', 'student-015-transcript.pdf', '/mock/files/student-015-transcript.pdf', 'application/pdf', 434000, 'transcript', 2),
  (116, 'student-transcript-016', 'student-016-transcript.pdf', '/mock/files/student-016-transcript.pdf', 'application/pdf', 435000, 'transcript', 2),
  (117, 'student-transcript-017', 'student-017-transcript.pdf', '/mock/files/student-017-transcript.pdf', 'application/pdf', 436000, 'transcript', 2),
  (118, 'student-transcript-018', 'student-018-transcript.pdf', '/mock/files/student-018-transcript.pdf', 'application/pdf', 437000, 'transcript', 2),
  (119, 'student-transcript-019', 'student-019-transcript.pdf', '/mock/files/student-019-transcript.pdf', 'application/pdf', 438000, 'transcript', 2),
  (120, 'student-transcript-020', 'student-020-transcript.pdf', '/mock/files/student-020-transcript.pdf', 'application/pdf', 439000, 'transcript', 2)
ON DUPLICATE KEY UPDATE
  file_key = VALUES(file_key),
  original_name = VALUES(original_name),
  storage_path = VALUES(storage_path),
  file_type = VALUES(file_type),
  file_size = VALUES(file_size),
  business_type = VALUES(business_type),
  uploaded_by = VALUES(uploaded_by);

INSERT INTO students (
  id, name, gender, birth_date, current_school, grade, email, phone, id_card_no
) VALUES
  (101, 'Student 01', 'MALE', '2008-01-05', 'Beijing No.1 High School', 'Grade 12', 'student01@example.com', '13800000001', '110101200801050101'),
  (102, 'Student 02', 'FEMALE', '2008-02-08', 'Shanghai Experimental High School', 'Grade 12', 'student02@example.com', '13800000002', '110101200802080102'),
  (103, 'Student 03', 'MALE', '2008-03-11', 'Guangzhou Foreign Language School', 'Grade 12', 'student03@example.com', '13800000003', '110101200803110103'),
  (104, 'Student 04', 'FEMALE', '2008-04-14', 'Shenzhen Senior High School', 'Grade 12', 'student04@example.com', '13800000004', '110101200804140104'),
  (105, 'Student 05', 'MALE', '2008-05-17', 'Nanjing No.1 High School', 'Grade 12', 'student05@example.com', '13800000005', '110101200805170105'),
  (106, 'Student 06', 'FEMALE', '2008-06-20', 'Hangzhou No.2 High School', 'Grade 12', 'student06@example.com', '13800000006', '110101200806200106'),
  (107, 'Student 07', 'MALE', '2008-07-23', 'Wuhan High School', 'Grade 12', 'student07@example.com', '13800000007', '110101200807230107'),
  (108, 'Student 08', 'FEMALE', '2008-08-26', 'Chengdu No.7 High School', 'Grade 12', 'student08@example.com', '13800000008', '110101200808260108'),
  (109, 'Student 09', 'MALE', '2008-09-02', 'Xian High School', 'Grade 12', 'student09@example.com', '13800000009', '110101200809020109'),
  (110, 'Student 10', 'FEMALE', '2008-10-05', 'Tianjin Nankai High School', 'Grade 12', 'student10@example.com', '13800000010', '110101200810050110'),
  (111, 'Student 11', 'MALE', '2008-11-08', 'Suzhou High School', 'Grade 12', 'student11@example.com', '13800000011', '110101200811080111'),
  (112, 'Student 12', 'FEMALE', '2008-12-11', 'Qingdao No.2 High School', 'Grade 12', 'student12@example.com', '13800000012', '110101200812110112'),
  (113, 'Student 13', 'MALE', '2008-01-13', 'Changsha Yali High School', 'Grade 12', 'student13@example.com', '13800000013', '110101200801130113'),
  (114, 'Student 14', 'FEMALE', '2008-02-16', 'Zhengzhou Foreign Language School', 'Grade 12', 'student14@example.com', '13800000014', '110101200802160114'),
  (115, 'Student 15', 'MALE', '2008-03-19', 'Harbin No.3 High School', 'Grade 12', 'student15@example.com', '13800000015', '110101200803190115'),
  (116, 'Student 16', 'FEMALE', '2008-04-22', 'Jinan Experimental High School', 'Grade 12', 'student16@example.com', '13800000016', '110101200804220116'),
  (117, 'Student 17', 'MALE', '2008-05-25', 'Xiamen No.1 High School', 'Grade 12', 'student17@example.com', '13800000017', '110101200805250117'),
  (118, 'Student 18', 'FEMALE', '2008-06-28', 'Fuzhou No.8 High School', 'Grade 12', 'student18@example.com', '13800000018', '110101200806280118'),
  (119, 'Student 19', 'MALE', '2008-07-03', 'Kunming No.1 High School', 'Grade 12', 'student19@example.com', '13800000019', '110101200807030119'),
  (120, 'Student 20', 'FEMALE', '2008-08-06', 'Ningbo High School', 'Grade 12', 'student20@example.com', '13800000020', '110101200808060120')
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  gender = VALUES(gender),
  birth_date = VALUES(birth_date),
  current_school = VALUES(current_school),
  grade = VALUES(grade),
  email = VALUES(email),
  phone = VALUES(phone),
  id_card_no = VALUES(id_card_no);

INSERT INTO transcripts (
  id, student_id, transcript_school_name, term_start, term_end,
  chinese_score, math_score, english_score, physics_score, chemistry_score, history_score,
  average_score, failed_subject_count, file_id, authenticity_risk_level, has_stamp_region, clarity_level
) VALUES
  (101, 101, 'Beijing No.1 High School', '2025-09-01', '2026-01-15', 91, 95, 90, 94, 92, 86, 91.00, 0, 101, 'A', 1, 'HIGH'),
  (102, 102, 'Shanghai Experimental High School', '2025-09-01', '2026-01-15', 89, 93, 91, 90, 88, 85, 89.33, 0, 102, 'A', 1, 'HIGH'),
  (103, 103, 'Guangzhou Foreign Language School', '2025-09-01', '2026-01-15', 88, 92, 89, 91, 87, 84, 88.50, 0, 103, 'A', 1, 'HIGH'),
  (104, 104, 'Shenzhen Senior High School', '2025-09-01', '2026-01-15', 86, 90, 87, 89, 85, 83, 86.67, 0, 104, 'A', 1, 'HIGH'),
  (105, 105, 'Nanjing No.1 High School', '2025-09-01', '2026-01-15', 84, 88, 85, 86, 84, 80, 84.50, 0, 105, 'B', 1, 'HIGH'),
  (106, 106, 'Hangzhou No.2 High School', '2025-09-01', '2026-01-15', 83, 87, 84, 85, 83, 79, 83.50, 0, 106, 'B', 1, 'HIGH'),
  (107, 107, 'Wuhan High School', '2025-09-01', '2026-01-15', 81, 86, 82, 84, 82, 78, 82.17, 0, 107, 'B', 1, 'MEDIUM'),
  (108, 108, 'Chengdu No.7 High School', '2025-09-01', '2026-01-15', 80, 85, 81, 83, 80, 77, 81.00, 0, 108, 'B', 1, 'MEDIUM'),
  (109, 109, 'Xian High School', '2025-09-01', '2026-01-15', 79, 84, 80, 82, 79, 76, 80.00, 1, 109, 'B', 1, 'MEDIUM'),
  (110, 110, 'Tianjin Nankai High School', '2025-09-01', '2026-01-15', 78, 83, 79, 81, 78, 75, 79.00, 1, 110, 'B', 1, 'MEDIUM'),
  (111, 111, 'Suzhou High School', '2025-09-01', '2026-01-15', 76, 79, 77, 78, 76, 72, 76.33, 1, 111, 'C', 1, 'MEDIUM'),
  (112, 112, 'Qingdao No.2 High School', '2025-09-01', '2026-01-15', 75, 78, 76, 77, 75, 71, 75.33, 1, 112, 'C', 1, 'MEDIUM'),
  (113, 113, 'Changsha Yali High School', '2025-09-01', '2026-01-15', 74, 77, 74, 76, 74, 70, 74.17, 1, 113, 'C', 1, 'MEDIUM'),
  (114, 114, 'Zhengzhou Foreign Language School', '2025-09-01', '2026-01-15', 73, 76, 73, 75, 73, 69, 73.17, 1, 114, 'C', 1, 'LOW'),
  (115, 115, 'Harbin No.3 High School', '2025-09-01', '2026-01-15', 72, 75, 72, 74, 72, 68, 72.17, 1, 115, 'C', 1, 'LOW'),
  (116, 116, 'Jinan Experimental High School', '2025-09-01', '2026-01-15', 70, 73, 71, 72, 70, 67, 70.50, 1, 116, 'C', 1, 'LOW'),
  (117, 117, 'Xiamen No.1 High School', '2025-09-01', '2026-01-15', 69, 72, 70, 71, 69, 66, 69.50, 2, 117, 'D', 1, 'LOW'),
  (118, 118, 'Fuzhou No.8 High School', '2025-09-01', '2026-01-15', 67, 70, 68, 69, 67, 65, 67.67, 2, 118, 'D', 1, 'LOW'),
  (119, 119, 'Kunming No.1 High School', '2025-09-01', '2026-01-15', 65, 68, 66, 67, 65, 63, 65.67, 3, 119, 'D', 0, 'LOW'),
  (120, 120, 'Ningbo High School', '2025-09-01', '2026-01-15', 62, 66, 64, 65, 63, 60, 63.33, 3, 120, 'D', 0, 'LOW')
ON DUPLICATE KEY UPDATE
  student_id = VALUES(student_id),
  transcript_school_name = VALUES(transcript_school_name),
  term_start = VALUES(term_start),
  term_end = VALUES(term_end),
  chinese_score = VALUES(chinese_score),
  math_score = VALUES(math_score),
  english_score = VALUES(english_score),
  physics_score = VALUES(physics_score),
  chemistry_score = VALUES(chemistry_score),
  history_score = VALUES(history_score),
  average_score = VALUES(average_score),
  failed_subject_count = VALUES(failed_subject_count),
  file_id = VALUES(file_id),
  authenticity_risk_level = VALUES(authenticity_risk_level),
  has_stamp_region = VALUES(has_stamp_region),
  clarity_level = VALUES(clarity_level);

INSERT INTO personal_statements (
  id, student_id, content, word_count, quality_score
) VALUES
  (101, 101, 'Strong academic profile with clear motivation for computing and strong extracurricular coding background.', 850, 9.40),
  (102, 102, 'Clear study plan, strong language expression, and solid understanding of target majors.', 820, 9.10),
  (103, 103, 'Well structured statement showing strong interest in data and engineering disciplines.', 800, 8.90),
  (104, 104, 'Good narrative, strong career goal, and clear fit for international study.', 780, 8.70),
  (105, 105, 'Good motivation and organized content with minor repetition.', 720, 8.30),
  (106, 106, 'Reasonable content quality and acceptable structure for submission.', 700, 8.00),
  (107, 107, 'Complete statement with understandable motivation and average polish.', 650, 7.60),
  (108, 108, 'General quality personal statement with moderate clarity.', 620, 7.30),
  (109, 109, 'Mostly complete statement but limited depth in professional understanding.', 580, 6.90),
  (110, 110, 'Average statement, basic motivation present, weak examples.', 560, 6.70),
  (111, 111, 'Borderline quality statement with simple structure and limited detail.', 520, 6.20),
  (112, 112, 'Basic statement, enough to submit but lacks strong evidence and reflection.', 500, 6.00),
  (113, 113, 'Weak but usable statement with minimal detail.', 460, 5.80),
  (114, 114, 'Low quality content and weak logic, likely needs supplement or revision.', 420, 5.30),
  (115, 115, 'Short statement with vague major understanding and weak expression.', 390, 5.00),
  (116, 116, 'Insufficient detail and weak motivation, likely difficult to score well.', 360, 4.80),
  (117, 117, 'Low quality statement with sparse content and poor structure.', 340, 4.30),
  (118, 118, 'Very weak statement, barely reaches minimum communication quality.', 320, 4.00),
  (119, 119, 'Weak and incomplete expression, low persuasive value.', 310, 3.60),
  (120, 120, 'Poor quality statement with limited coherence and insufficient support.', 300, 3.20)
ON DUPLICATE KEY UPDATE
  student_id = VALUES(student_id),
  content = VALUES(content),
  word_count = VALUES(word_count),
  quality_score = VALUES(quality_score);
