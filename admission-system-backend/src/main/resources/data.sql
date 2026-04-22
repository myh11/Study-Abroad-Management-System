SET NAMES utf8mb4;

INSERT INTO users (
  id, username, password_hash, role_type, school_code, is_enabled, must_change_password, last_login_at
) VALUES
  (1, 'admin', 'pbkdf2$65536$92gjp6o+HB4CCAy3Dq3/BQ==$6pCVT8YDMKUqJY0qqllbE0Si04wjKTX+Qtcwu6p5o9o=', 'ADMIN', NULL, 1, 1, NULL),
  (2, 'agent01', 'pbkdf2$65536$Mo77I4TmaHxdZJ6v3XTEDQ==$kkpU/UaOep0sD0b4SdgpNvMduAWn/OZyljvFpu3juhU=', 'AGENT', NULL, 1, 1, NULL),
  (3, 'domestic01', 'pbkdf2$65536$gqWK3DRTKXNyMClalm5wWQ==$+KLSWJL8Jz034J9fBUYiW3uGkBj0gVajvy0wkfOF2LY=', 'DOMESTIC_REVIEWER', NULL, 1, 1, NULL),
  (4, 'school_usyd_01', 'pbkdf2$65536$3hzb5b+uZFnDtlc7S1Z9zA==$FdwmuR1Stw4KGNuklkrLaILMWRlriMuiiNl1dH1wp3U=', 'SCHOOL_REVIEWER', 'USYD', 1, 1, NULL),
  (5, 'school_anu_01', 'pbkdf2$65536$3hzb5b+uZFnDtlc7S1Z9zA==$FdwmuR1Stw4KGNuklkrLaILMWRlriMuiiNl1dH1wp3U=', 'SCHOOL_REVIEWER', 'ANU', 1, 1, NULL),
  (6, 'school_unsw_01', 'pbkdf2$65536$3hzb5b+uZFnDtlc7S1Z9zA==$FdwmuR1Stw4KGNuklkrLaILMWRlriMuiiNl1dH1wp3U=', 'SCHOOL_REVIEWER', 'UNSW', 1, 1, NULL)
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
  (1, 1, 'ANU', 30, 28, 2, 85.00, 85.00, 80.00, 1),
  (2, 1, 'USYD', 35, 34, 1, 80.00, 78.00, 75.00, 1),
  (3, 1, 'UNSW', 32, 32, 0, 82.00, 80.00, 76.00, 1),
  (4, 2, 'ANU', 28, 28, 0, 84.00, 84.00, 80.00, 1),
  (5, 2, 'USYD', 30, 30, 0, 80.00, 78.00, 75.00, 1),
  (6, 2, 'UNSW', 30, 30, 0, 82.00, 80.00, 76.00, 1)
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
  (1, 1, 'ANU', 'ANU_CS', 15, 13, 2, 88.00, 90.00, 80.00, NULL, NULL, 85.00, 82.00, 0, 1),
  (2, 1, 'ANU', 'ANU_IR', 15, 14, 1, 85.00, NULL, 88.00, NULL, 80.00, 82.00, 79.00, 1, 1),
  (3, 1, 'USYD', 'USYD_SE', 18, 17, 1, 84.00, 88.00, 75.00, NULL, NULL, 82.00, 78.00, 0, 1),
  (4, 1, 'USYD', 'USYD_BIZ', 17, 16, 1, 82.00, 78.00, 78.00, NULL, NULL, 80.00, 76.00, 1, 1),
  (5, 1, 'UNSW', 'UNSW_DS', 16, 16, 0, 85.00, 88.00, 76.00, NULL, NULL, 83.00, 79.00, 0, 1),
  (6, 1, 'UNSW', 'UNSW_ME', 16, 15, 1, 84.00, 87.00, 76.00, 85.00, NULL, 82.00, 78.00, 0, 1),
  (7, 2, 'ANU', 'ANU_CS', 14, 14, 0, 88.00, 90.00, 80.00, NULL, NULL, 85.00, 82.00, 0, 1),
  (8, 2, 'ANU', 'ANU_IR', 14, 14, 0, 85.00, NULL, 88.00, NULL, 80.00, 82.00, 79.00, 1, 1),
  (9, 2, 'USYD', 'USYD_SE', 15, 15, 0, 84.00, 88.00, 75.00, NULL, NULL, 82.00, 78.00, 0, 1),
  (10, 2, 'USYD', 'USYD_BIZ', 15, 15, 0, 82.00, 78.00, 78.00, NULL, NULL, 80.00, 76.00, 1, 1),
  (11, 2, 'UNSW', 'UNSW_DS', 15, 15, 0, 85.00, 88.00, 76.00, NULL, NULL, 83.00, 79.00, 0, 1),
  (12, 2, 'UNSW', 'UNSW_ME', 15, 15, 0, 84.00, 87.00, 76.00, 85.00, NULL, 82.00, 78.00, 0, 1)
ON DUPLICATE KEY UPDATE
  school_code = VALUES(school_code),
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
  (1, 'mock-transcript-1', 'transcript-li-si.pdf', '/mock/files/transcript-li-si.pdf', 'application/pdf', 524288, 'transcript', 2)
ON DUPLICATE KEY UPDATE
  file_key = VALUES(file_key),
  original_name = VALUES(original_name),
  storage_path = VALUES(storage_path),
  file_type = VALUES(file_type),
  file_size = VALUES(file_size),
  business_type = VALUES(business_type),
  uploaded_by = VALUES(uploaded_by);

INSERT INTO files (
  id, file_key, original_name, storage_path, file_type, file_size, business_type, uploaded_by
) VALUES
  (121, 'student-transcript-121', 'zhang-wei-transcript.pdf', '/mock/files/students/zhang-wei-transcript.pdf', 'application/pdf', 512345, 'transcript', 2),
  (122, 'student-transcript-122', 'liu-yiran-transcript.pdf', '/mock/files/students/liu-yiran-transcript.pdf', 'application/pdf', 498120, 'transcript', 2),
  (123, 'student-transcript-123', 'chen-xinyue-transcript.pdf', '/mock/files/students/chen-xinyue-transcript.pdf', 'application/pdf', 503876, 'transcript', 2),
  (124, 'student-transcript-124', 'wang-zikun-transcript.pdf', '/mock/files/students/wang-zikun-transcript.pdf', 'application/pdf', 521004, 'transcript', 2),
  (125, 'student-transcript-125', 'zhao-jingyi-transcript.pdf', '/mock/files/students/zhao-jingyi-transcript.pdf', 'application/pdf', 489332, 'transcript', 2),
  (126, 'student-transcript-126', 'sun-haoran-transcript.pdf', '/mock/files/students/sun-haoran-transcript.pdf', 'application/pdf', 476280, 'transcript', 2),
  (127, 'student-transcript-127', 'zhou-mengyao-transcript.pdf', '/mock/files/students/zhou-mengyao-transcript.pdf', 'application/pdf', 506712, 'transcript', 2),
  (128, 'student-transcript-128', 'wu-jiacheng-transcript.pdf', '/mock/files/students/wu-jiacheng-transcript.pdf', 'application/pdf', 495845, 'transcript', 2),
  (129, 'student-transcript-129', 'xu-siyu-transcript.pdf', '/mock/files/students/xu-siyu-transcript.pdf', 'application/pdf', 487500, 'transcript', 2),
  (130, 'student-transcript-130', 'he-linxi-transcript.pdf', '/mock/files/students/he-linxi-transcript.pdf', 'application/pdf', 492118, 'transcript', 2),
  (131, 'student-transcript-131', 'gao-yutong-transcript.pdf', '/mock/files/students/gao-yutong-transcript.pdf', 'application/pdf', 481227, 'transcript', 2),
  (132, 'student-transcript-132', 'ma-xiaonan-transcript.pdf', '/mock/files/students/ma-xiaonan-transcript.pdf', 'application/pdf', 475944, 'transcript', 2),
  (133, 'student-transcript-133', 'guo-zhehan-transcript.pdf', '/mock/files/students/guo-zhehan-transcript.pdf', 'application/pdf', 468731, 'transcript', 2),
  (134, 'student-transcript-134', 'tang-yuqi-transcript.pdf', '/mock/files/students/tang-yuqi-transcript.pdf', 'application/pdf', 471286, 'transcript', 2),
  (135, 'student-transcript-135', 'feng-zihan-transcript.pdf', '/mock/files/students/feng-zihan-transcript.pdf', 'application/pdf', 462014, 'transcript', 2),
  (136, 'student-transcript-136', 'luo-xichen-transcript.pdf', '/mock/files/students/luo-xichen-transcript.pdf', 'application/pdf', 458336, 'transcript', 2),
  (137, 'student-transcript-137', 'peng-ruoxi-transcript.pdf', '/mock/files/students/peng-ruoxi-transcript.pdf', 'application/pdf', 455120, 'transcript', 2),
  (138, 'student-transcript-138', 'deng-jiarui-transcript.pdf', '/mock/files/students/deng-jiarui-transcript.pdf', 'application/pdf', 449812, 'transcript', 2),
  (139, 'student-transcript-139', 'cao-yihan-transcript.pdf', '/mock/files/students/cao-yihan-transcript.pdf', 'application/pdf', 446508, 'transcript', 2),
  (140, 'student-transcript-140', 'lin-shuo-transcript.pdf', '/mock/files/students/lin-shuo-transcript.pdf', 'application/pdf', 442900, 'transcript', 2)
ON DUPLICATE KEY UPDATE
  file_key = VALUES(file_key),
  original_name = VALUES(original_name),
  storage_path = VALUES(storage_path),
  file_type = VALUES(file_type),
  file_size = VALUES(file_size),
  business_type = VALUES(business_type),
  uploaded_by = VALUES(uploaded_by);

INSERT INTO files (
  id, file_key, original_name, storage_path, file_type, file_size, business_type, uploaded_by
) VALUES
  (141, 'student-transcript-141', 'qin-yumo-transcript.pdf', '/mock/files/students/qin-yumo-transcript.pdf', 'application/pdf', 441220, 'transcript', 2),
  (142, 'student-transcript-142', 'xie-anqi-transcript.pdf', '/mock/files/students/xie-anqi-transcript.pdf', 'application/pdf', 438640, 'transcript', 2),
  (143, 'student-transcript-143', 'jiang-chenxi-transcript.pdf', '/mock/files/students/jiang-chenxi-transcript.pdf', 'application/pdf', 437510, 'transcript', 2),
  (144, 'student-transcript-144', 'mo-zhiyuan-transcript.pdf', '/mock/files/students/mo-zhiyuan-transcript.pdf', 'application/pdf', 439880, 'transcript', 2)
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
  (1, 'Li Si', 'MALE', '2008-01-01', 'Beijing No.1 High School', 'Grade 12', 'lisi@example.com', '13800000000', '110101200801010011')
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  gender = VALUES(gender),
  birth_date = VALUES(birth_date),
  current_school = VALUES(current_school),
  grade = VALUES(grade),
  email = VALUES(email),
  phone = VALUES(phone),
  id_card_no = VALUES(id_card_no);

INSERT INTO students (
  id, name, gender, birth_date, current_school, grade, email, phone, id_card_no
) VALUES
  (121, 'Zhang Wei', 'MALE', '2008-02-14', 'Beijing Experimental High School', 'Grade 12', 'zhang.wei121@example.com', '13810000121', '110101200802140121'),
  (122, 'Liu Yiran', 'FEMALE', '2008-05-09', 'Shanghai No.2 High School', 'Grade 12', 'liu.yiran122@example.com', '13810000122', '310101200805090122'),
  (123, 'Chen Xinyue', 'FEMALE', '2007-11-21', 'Guangzhou Tianhe High School', 'Grade 12', 'chen.xinyue123@example.com', '13810000123', '440106200711210123'),
  (124, 'Wang Zikun', 'MALE', '2008-03-03', 'Shenzhen Nanshan High School', 'Grade 12', 'wang.zikun124@example.com', '13810000124', '440305200803030124'),
  (125, 'Zhao Jingyi', 'FEMALE', '2008-06-18', 'Hangzhou Foreign Language School', 'Grade 12', 'zhao.jingyi125@example.com', '13810000125', '330106200806180125'),
  (126, 'Sun Haoran', 'MALE', '2008-01-28', 'Nanjing Jinling High School', 'Grade 12', 'sun.haoran126@example.com', '13810000126', '320102200801280126'),
  (127, 'Zhou Mengyao', 'FEMALE', '2008-04-12', 'Chengdu No.7 High School', 'Grade 12', 'zhou.mengyao127@example.com', '13810000127', '510107200804120127'),
  (128, 'Wu Jiacheng', 'MALE', '2007-12-07', 'Wuhan Foreign Languages School', 'Grade 12', 'wu.jiacheng128@example.com', '13810000128', '420106200712070128'),
  (129, 'Xu Siyu', 'FEMALE', '2008-08-30', 'Xiamen No.1 High School', 'Grade 12', 'xu.siyu129@example.com', '13810000129', '350203200808300129'),
  (130, 'He Linxi', 'FEMALE', '2008-02-02', 'Tianjin Yaohua High School', 'Grade 12', 'he.linxi130@example.com', '13810000130', '120101200802020130'),
  (131, 'Gao Yutong', 'MALE', '2008-07-15', 'Jinan Lixia High School', 'Grade 12', 'gao.yutong131@example.com', '13810000131', '370102200807150131'),
  (132, 'Ma Xiaonan', 'FEMALE', '2008-09-11', 'Qingdao No.58 High School', 'Grade 12', 'ma.xiaonan132@example.com', '13810000132', '370203200809110132'),
  (133, 'Guo Zhehan', 'MALE', '2007-10-26', 'Harbin No.3 High School', 'Grade 12', 'guo.zhehan133@example.com', '13810000133', '230103200710260133'),
  (134, 'Tang Yuqi', 'FEMALE', '2008-01-05', 'Changsha Yali High School', 'Grade 12', 'tang.yuqi134@example.com', '13810000134', '430102200801050134'),
  (135, 'Feng Zihan', 'MALE', '2008-11-19', 'Suzhou High School', 'Grade 12', 'feng.zihan135@example.com', '13810000135', '320508200811190135'),
  (136, 'Luo Xichen', 'MALE', '2008-03-27', 'Zhengzhou No.1 High School', 'Grade 12', 'luo.xichen136@example.com', '13810000136', '410102200803270136'),
  (137, 'Peng Ruoxi', 'FEMALE', '2008-06-06', 'Kunming No.1 High School', 'Grade 12', 'peng.ruoxi137@example.com', '13810000137', '530102200806060137'),
  (138, 'Deng Jiarui', 'MALE', '2007-09-13', 'Nanchang No.2 High School', 'Grade 12', 'deng.jiarui138@example.com', '13810000138', '360102200709130138'),
  (139, 'Cao Yihan', 'FEMALE', '2008-12-24', 'Shijiazhuang No.2 High School', 'Grade 12', 'cao.yihan139@example.com', '13810000139', '130102200812240139'),
  (140, 'Lin Shuo', 'MALE', '2008-05-01', 'Fuzhou No.1 High School', 'Grade 12', 'lin.shuo140@example.com', '13810000140', '350102200805010140')
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  gender = VALUES(gender),
  birth_date = VALUES(birth_date),
  current_school = VALUES(current_school),
  grade = VALUES(grade),
  email = VALUES(email),
  phone = VALUES(phone),
  id_card_no = VALUES(id_card_no);

INSERT INTO students (
  id, name, gender, birth_date, current_school, grade, email, phone, id_card_no
) VALUES
  (141, 'Qin Yumo', 'FEMALE', '2008-02-08', 'Xi''an High School', 'Grade 12', 'qin.yumo141@example.com', '13810000141', '610102200802080141'),
  (142, 'Xie Anqi', 'FEMALE', '2008-09-16', 'Ningbo High School', 'Grade 12', 'xie.anqi142@example.com', '13810000142', '330203200809160142'),
  (143, 'Jiang Chenxi', 'MALE', '2007-12-03', 'Hefei No.1 High School', 'Grade 12', 'jiang.chenxi143@example.com', '13810000143', '340102200712030143'),
  (144, 'Mo Zhiyuan', 'MALE', '2008-04-22', 'Nanning No.2 High School', 'Grade 12', 'mo.zhiyuan144@example.com', '13810000144', '450102200804220144')
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
  (1, 1, 'Beijing No.1 High School', '2025-09-01', '2026-01-15',
   88.00, 92.00, 86.00, 90.00, 85.00, 80.00,
   88.00, 0, 1, 'B', 1, 'HIGH')
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

INSERT INTO transcripts (
  id, student_id, transcript_school_name, term_start, term_end,
  chinese_score, math_score, english_score, physics_score, chemistry_score, history_score,
  average_score, failed_subject_count, file_id, authenticity_risk_level, has_stamp_region, clarity_level
) VALUES
  (121, 121, 'Beijing Experimental High School', '2025-09-01', '2026-01-20', 92.00, 96.00, 91.00, 94.00, 90.00, 88.00, 91.83, 0, 121, 'A', 1, 'HIGH'),
  (122, 122, 'Shanghai No.2 High School', '2025-09-01', '2026-01-20', 90.00, 93.00, 92.00, 91.00, 88.00, 89.00, 90.50, 0, 122, 'A', 1, 'HIGH'),
  (123, 123, 'Guangzhou Tianhe High School', '2025-09-01', '2026-01-20', 89.00, 95.00, 90.00, 93.00, 87.00, 86.00, 90.00, 0, 123, 'B', 1, 'HIGH'),
  (124, 124, 'Shenzhen Nanshan High School', '2025-09-01', '2026-01-20', 88.00, 94.00, 88.00, 92.00, 86.00, 84.00, 88.67, 0, 124, 'A', 1, 'HIGH'),
  (125, 125, 'Hangzhou Foreign Language School', '2025-09-01', '2026-01-20', 91.00, 90.00, 94.00, 88.00, 85.00, 90.00, 89.67, 0, 125, 'A', 1, 'HIGH'),
  (126, 126, 'Nanjing Jinling High School', '2025-09-01', '2026-01-20', 84.00, 88.00, 83.00, 86.00, 82.00, 80.00, 83.83, 0, 126, 'B', 1, 'HIGH'),
  (127, 127, 'Chengdu No.7 High School', '2025-09-01', '2026-01-20', 85.00, 87.00, 84.00, 84.00, 81.00, 82.00, 83.83, 0, 127, 'B', 1, 'HIGH'),
  (128, 128, 'Wuhan Foreign Languages School', '2025-09-01', '2026-01-20', 83.00, 86.00, 82.00, 85.00, 80.00, 79.00, 82.50, 0, 128, 'B', 1, 'MEDIUM'),
  (129, 129, 'Xiamen No.1 High School', '2025-09-01', '2026-01-20', 82.00, 85.00, 84.00, 83.00, 79.00, 78.00, 81.83, 0, 129, 'B', 1, 'MEDIUM'),
  (130, 130, 'Tianjin Yaohua High School', '2025-09-01', '2026-01-20', 81.00, 84.00, 83.00, 82.00, 78.00, 77.00, 80.83, 0, 130, 'B', 1, 'MEDIUM'),
  (131, 131, 'Jinan Lixia High School', '2025-09-01', '2026-01-20', 80.00, 82.00, 79.00, 78.00, 76.00, 77.00, 78.67, 0, 131, 'B', 1, 'MEDIUM'),
  (132, 132, 'Qingdao No.58 High School', '2025-09-01', '2026-01-20', 79.00, 81.00, 78.00, 77.00, 75.00, 76.00, 77.67, 1, 132, 'C', 1, 'MEDIUM'),
  (133, 133, 'Harbin No.3 High School', '2025-09-01', '2026-01-20', 78.00, 80.00, 77.00, 76.00, 74.00, 75.00, 76.67, 1, 133, 'C', 1, 'MEDIUM'),
  (134, 134, 'Changsha Yali High School', '2025-09-01', '2026-01-20', 77.00, 79.00, 76.00, 75.00, 73.00, 74.00, 75.67, 1, 134, 'C', 1, 'LOW'),
  (135, 135, 'Suzhou High School', '2025-09-01', '2026-01-20', 76.00, 78.00, 75.00, 74.00, 72.00, 73.00, 74.67, 1, 135, 'C', 0, 'LOW'),
  (136, 136, 'Zhengzhou No.1 High School', '2025-09-01', '2026-01-20', 74.00, 75.00, 73.00, 71.00, 70.00, 72.00, 72.50, 2, 136, 'C', 0, 'LOW'),
  (137, 137, 'Kunming No.1 High School', '2025-09-01', '2026-01-20', 72.00, 74.00, 71.00, 70.00, 69.00, 71.00, 71.17, 2, 137, 'D', 0, 'LOW'),
  (138, 138, 'Nanchang No.2 High School', '2025-09-01', '2026-01-20', 70.00, 72.00, 69.00, 68.00, 67.00, 70.00, 69.33, 2, 138, 'D', 0, 'LOW'),
  (139, 139, 'Shijiazhuang No.2 High School', '2025-09-01', '2026-01-20', 68.00, 71.00, 70.00, 66.00, 65.00, 69.00, 68.17, 3, 139, 'D', 0, 'LOW'),
  (140, 140, 'Fuzhou No.1 High School', '2025-09-01', '2026-01-20', 73.00, 76.00, 72.00, 69.00, 68.00, 70.00, 71.33, 2, 140, 'C', 0, 'LOW')
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

INSERT INTO transcripts (
  id, student_id, transcript_school_name, term_start, term_end,
  chinese_score, math_score, english_score, physics_score, chemistry_score, history_score,
  average_score, failed_subject_count, file_id, authenticity_risk_level, has_stamp_region, clarity_level
) VALUES
  (141, 141, 'Xi''an High School', '2025-09-01', '2026-01-20', 81.00, 78.00, 79.00, 82.00, 80.00, 82.00, 80.33, 0, 141, 'A', 1, 'HIGH'),
  (142, 142, 'Ningbo High School', '2025-09-01', '2026-01-20', 79.00, 77.00, 78.00, 81.00, 78.00, 78.00, 78.50, 1, 142, 'B', 1, 'MEDIUM'),
  (143, 143, 'Hefei No.1 High School', '2025-09-01', '2026-01-20', 84.00, 88.00, 76.00, 85.00, 81.00, 79.00, 82.17, 2, 143, 'C', 1, 'MEDIUM'),
  (144, 144, 'Nanning No.2 High School', '2025-09-01', '2026-01-20', 82.00, 86.00, 77.00, 83.00, 79.00, 78.00, 80.83, 3, 144, 'D', 0, 'LOW')
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
  (1, 1, 'I want to apply for Software Engineering at USYD and I have a strong interest in mathematics and programming.', 300, 8.50)
ON DUPLICATE KEY UPDATE
  student_id = VALUES(student_id),
  content = VALUES(content),
  word_count = VALUES(word_count),
  quality_score = VALUES(quality_score);

INSERT INTO personal_statements (
  id, student_id, content, word_count, quality_score
) VALUES
  (121, 121, 'I have built multiple programming projects and want to study computer science in a research-focused environment.', 520, 9.30),
  (122, 122, 'My long-term goal is to combine quantitative reasoning with social impact, and I have maintained strong academic discipline.', 500, 9.10),
  (123, 123, 'I enjoy algorithm contests and team projects, and I can clearly explain how I prepared for overseas study.', 480, 8.90),
  (124, 124, 'I have led robotics activities and want to continue engineering study with stronger research resources.', 470, 8.80),
  (125, 125, 'I am interested in international communication and can present a coherent study plan with strong language ability.', 510, 9.00),
  (126, 126, 'I have stable grades and a clear interest in technology, with a practical plan for undergraduate study abroad.', 420, 8.10),
  (127, 127, 'I can describe my academic strengths and extracurricular participation clearly, though my project depth is still moderate.', 410, 7.90),
  (128, 128, 'My statement explains why I chose the major and what skills I want to improve, with acceptable but not outstanding detail.', 395, 7.60),
  (129, 129, 'I want a business and data-related program and can provide a reasonable study motivation and career outline.', 380, 7.40),
  (130, 130, 'I have an interest in interdisciplinary study and can explain basic motivation, but the expression is somewhat generic.', 360, 7.10),
  (131, 131, 'I want to study abroad to broaden my perspective, but my statement lacks specific evidence and project examples.', 330, 6.80),
  (132, 132, 'The statement communicates willingness to improve, but the structure is weak and several supporting details are missing.', 310, 6.40),
  (133, 133, 'I wrote about my target major briefly, but the content is repetitive and the academic plan is not concrete enough.', 295, 6.10),
  (134, 134, 'The material shows interest in the program, but it does not explain personal strengths or long-term planning clearly.', 280, 5.90),
  (135, 135, 'I only described a general wish to study abroad, with limited detail and weak connection to the applied major.', 260, 5.60),
  (136, 136, 'My statement is short and missing examples of achievement, making the overall persuasiveness insufficient.', 240, 5.20),
  (137, 137, 'The content is incomplete and loosely organized, which may require the agent to supplement supporting materials.', 230, 4.90),
  (138, 138, 'The statement is generic, very short, and lacks concrete academic evidence, creating a clear rejection risk.', 220, 4.70),
  (139, 139, 'I provided only a minimal explanation for my application and did not connect my background with the intended program.', 210, 4.50),
  (140, 140, 'The personal statement is weak in structure and evidence, but still provides a basic motivation for application.', 235, 5.00)
ON DUPLICATE KEY UPDATE
  student_id = VALUES(student_id),
  content = VALUES(content),
  word_count = VALUES(word_count),
  quality_score = VALUES(quality_score);

INSERT INTO personal_statements (
  id, student_id, content, word_count, quality_score
) VALUES
  (141, 141, 'I have stable business-related coursework and want a program with strong interdisciplinary exposure.', 260, 5.80),
  (142, 142, 'I hope to study abroad and improve myself.', 180, 4.80),
  (143, 143, 'I am interested in engineering, but my current statement still lacks enough major-specific detail and supporting projects.', 245, 5.30),
  (144, 144, 'I want an overseas program, but my materials are incomplete and my long-term plan is not clearly presented.', 170, 4.20)
ON DUPLICATE KEY UPDATE
  student_id = VALUES(student_id),
  content = VALUES(content),
  word_count = VALUES(word_count),
  quality_score = VALUES(quality_score);

INSERT INTO applications (
  id, student_id, transcript_id, personal_statement_id, batch_id,
  target_school_code, target_major_code, source_application_id, created_by_agent_id,
  current_status, submit_time, cancel_reason, close_reason, rule_snapshot_id,
  waitlist_confirm_deadline, reserved_at, canceled_at, closed_at
) VALUES
  (1, 1, 1, 1, 1,
   'USYD', 'USYD_SE', NULL, 2,
   'DRAFT', NULL, NULL, NULL, NULL,
   NULL, NULL, NULL, NULL)
ON DUPLICATE KEY UPDATE
  student_id = VALUES(student_id),
  transcript_id = VALUES(transcript_id),
  personal_statement_id = VALUES(personal_statement_id),
  batch_id = VALUES(batch_id),
  target_school_code = VALUES(target_school_code),
  target_major_code = VALUES(target_major_code),
  source_application_id = VALUES(source_application_id),
  created_by_agent_id = VALUES(created_by_agent_id),
  current_status = VALUES(current_status),
  submit_time = VALUES(submit_time),
  cancel_reason = VALUES(cancel_reason),
  close_reason = VALUES(close_reason),
  rule_snapshot_id = VALUES(rule_snapshot_id),
  waitlist_confirm_deadline = VALUES(waitlist_confirm_deadline),
  reserved_at = VALUES(reserved_at),
  canceled_at = VALUES(canceled_at),
  closed_at = VALUES(closed_at);

INSERT INTO applications (
  id, student_id, transcript_id, personal_statement_id, batch_id,
  target_school_code, target_major_code, source_application_id, created_by_agent_id,
  current_status, submit_time, cancel_reason, close_reason, rule_snapshot_id,
  waitlist_confirm_deadline, reserved_at, canceled_at, closed_at
) VALUES
  (121, 121, 121, 121, 1, 'ANU', 'ANU_CS', NULL, 2, 'DRAFT', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
  (122, 122, 122, 122, 1, 'USYD', 'USYD_BIZ', NULL, 2, 'SUBMITTED', '2026-09-10 09:00:00', NULL, NULL, 221, NULL, NULL, NULL, NULL),
  (123, 123, 123, 123, 1, 'USYD', 'USYD_SE', NULL, 2, 'DOMESTIC_REVIEWING', '2026-09-10 09:30:00', NULL, NULL, 222, NULL, NULL, NULL, NULL),
  (124, 124, 124, 124, 1, 'ANU', 'ANU_IR', NULL, 2, 'DOMESTIC_SUPPLEMENT', '2026-09-11 10:00:00', NULL, NULL, 223, NULL, NULL, NULL, NULL),
  (125, 125, 125, 125, 1, 'UNSW', 'UNSW_ME', NULL, 2, 'DOMESTIC_REJECTED', '2026-09-11 11:00:00', NULL, NULL, 224, NULL, NULL, NULL, NULL),
  (126, 126, 126, 126, 1, 'ANU', 'ANU_CS', NULL, 2, 'SCHOOL_REVIEWING', '2026-09-12 08:45:00', NULL, NULL, 225, NULL, NULL, NULL, NULL),
  (127, 127, 127, 127, 1, 'USYD', 'USYD_BIZ', NULL, 2, 'WAITLISTED', '2026-09-12 09:10:00', NULL, NULL, 226, NULL, NULL, NULL, NULL),
  (128, 128, 128, 128, 1, 'UNSW', 'UNSW_DS', NULL, 2, 'WAITLIST_PENDING_CONFIRM', '2026-09-12 10:00:00', NULL, NULL, 227, '2026-09-20 18:00:00', NULL, NULL, NULL),
  (129, 129, 129, 129, 1, 'USYD', 'USYD_SE', NULL, 2, 'RESERVED', '2026-09-13 09:20:00', NULL, NULL, 228, NULL, '2026-09-18 14:00:00', NULL, NULL),
  (130, 130, 130, 130, 1, 'ANU', 'ANU_IR', NULL, 2, 'ADJUSTMENT_SUGGESTED', '2026-09-13 10:15:00', NULL, NULL, 229, NULL, NULL, NULL, NULL),
  (131, 131, 131, 131, 1, 'UNSW', 'UNSW_ME', NULL, 2, 'SCHOOL_REJECTED', '2026-09-13 11:00:00', NULL, NULL, 230, NULL, NULL, NULL, NULL),
  (132, 132, 132, 132, 1, 'USYD', 'USYD_BIZ', NULL, 2, 'CANCELED', '2026-09-14 08:30:00', 'Agent withdrew before domestic assignment', NULL, 231, NULL, NULL, '2026-09-14 16:20:00', NULL),
  (133, 133, 133, 133, 2, 'ANU', 'ANU_CS', NULL, 2, 'CLOSED', '2025-09-18 09:00:00', NULL, 'ARCHIVED_AFTER_DOMESTIC_REJECTED', 232, NULL, NULL, NULL, '2025-10-02 18:00:00'),
  (134, 134, 134, 134, 1, 'UNSW', 'UNSW_DS', NULL, 2, 'SUBMITTED', '2026-09-14 09:40:00', NULL, NULL, 233, NULL, NULL, NULL, NULL),
  (135, 135, 135, 135, 1, 'USYD', 'USYD_SE', NULL, 2, 'CLOSED', '2026-09-14 10:30:00', NULL, 'WAITLIST_CONFIRM_TIMEOUT', 234, '2026-09-21 18:00:00', NULL, NULL, '2026-09-21 18:00:00'),
  (136, 136, 136, 136, 1, 'ANU', 'ANU_IR', NULL, 2, 'DOMESTIC_REVIEWING', '2026-09-15 08:10:00', NULL, NULL, 235, NULL, NULL, NULL, NULL),
  (137, 137, 137, 137, 1, 'ANU', 'ANU_IR', NULL, 2, 'WAITLISTED', '2026-09-15 08:40:00', NULL, NULL, 236, NULL, NULL, NULL, NULL),
  (138, 138, 138, 138, 1, 'UNSW', 'UNSW_DS', NULL, 2, 'RESERVED', '2026-09-15 09:20:00', NULL, NULL, 237, NULL, '2026-09-22 15:30:00', NULL, NULL),
  (139, 139, 139, 139, 1, 'USYD', 'USYD_BIZ', NULL, 2, 'CLOSED', '2026-09-15 10:00:00', NULL, 'ADJUSTMENT_ACCEPTED_NEW_APPLICATION_CREATED', 238, NULL, NULL, NULL, '2026-09-25 17:45:00'),
  (140, 140, 140, 140, 1, 'UNSW', 'UNSW_DS', 139, 2, 'DRAFT', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL)
ON DUPLICATE KEY UPDATE
  student_id = VALUES(student_id),
  transcript_id = VALUES(transcript_id),
  personal_statement_id = VALUES(personal_statement_id),
  batch_id = VALUES(batch_id),
  target_school_code = VALUES(target_school_code),
  target_major_code = VALUES(target_major_code),
  source_application_id = VALUES(source_application_id),
  created_by_agent_id = VALUES(created_by_agent_id),
  current_status = VALUES(current_status),
  submit_time = VALUES(submit_time),
  cancel_reason = VALUES(cancel_reason),
  close_reason = VALUES(close_reason),
  rule_snapshot_id = VALUES(rule_snapshot_id),
  waitlist_confirm_deadline = VALUES(waitlist_confirm_deadline),
  reserved_at = VALUES(reserved_at),
  canceled_at = VALUES(canceled_at),
  closed_at = VALUES(closed_at);

INSERT INTO applications (
  id, student_id, transcript_id, personal_statement_id, batch_id,
  target_school_code, target_major_code, source_application_id, created_by_agent_id,
  current_status, submit_time, cancel_reason, close_reason, rule_snapshot_id,
  waitlist_confirm_deadline, reserved_at, canceled_at, closed_at
) VALUES
  (141, 141, 141, 141, 1, 'USYD', 'USYD_BIZ', NULL, 2, 'WAITLISTED', '2026-09-16 09:10:00', NULL, NULL, 239, NULL, NULL, NULL, NULL),
  (142, 142, 142, 142, 1, 'USYD', 'USYD_BIZ', NULL, 2, 'DOMESTIC_SUPPLEMENT', '2026-09-16 09:40:00', NULL, NULL, 240, NULL, NULL, NULL, NULL),
  (143, 143, 143, 143, 1, 'UNSW', 'UNSW_ME', NULL, 2, 'DOMESTIC_REJECTED', '2026-09-16 10:20:00', NULL, NULL, 241, NULL, NULL, NULL, NULL),
  (144, 144, 144, 144, 1, 'UNSW', 'UNSW_DS', NULL, 2, 'ADJUSTMENT_SUGGESTED', '2026-09-16 11:00:00', NULL, NULL, 242, NULL, NULL, NULL, NULL)
ON DUPLICATE KEY UPDATE
  student_id = VALUES(student_id),
  transcript_id = VALUES(transcript_id),
  personal_statement_id = VALUES(personal_statement_id),
  batch_id = VALUES(batch_id),
  target_school_code = VALUES(target_school_code),
  target_major_code = VALUES(target_major_code),
  source_application_id = VALUES(source_application_id),
  created_by_agent_id = VALUES(created_by_agent_id),
  current_status = VALUES(current_status),
  submit_time = VALUES(submit_time),
  cancel_reason = VALUES(cancel_reason),
  close_reason = VALUES(close_reason),
  rule_snapshot_id = VALUES(rule_snapshot_id),
  waitlist_confirm_deadline = VALUES(waitlist_confirm_deadline),
  reserved_at = VALUES(reserved_at),
  canceled_at = VALUES(canceled_at),
  closed_at = VALUES(closed_at);

INSERT INTO rule_snapshots (
  id, application_id, batch_id, school_threshold_snapshot, major_threshold_snapshot,
  reserve_line_snapshot, waitlist_line_snapshot, quota_rule_version, adjustment_rule_version, waitlist_sort_rule_version
) VALUES
  (221, 122, 1, '{"school_code":"USYD","school_min_score":80.00,"school_min_math":78.00,"school_min_english":75.00}', '{"major_code":"USYD_BIZ","min_average_score":82.00,"min_math_score":78.00,"min_english_score":78.00}', 80.00, 76.00, 1, 1, 1),
  (222, 123, 1, '{"school_code":"USYD","school_min_score":80.00,"school_min_math":78.00,"school_min_english":75.00}', '{"major_code":"USYD_SE","min_average_score":84.00,"min_math_score":88.00,"min_english_score":75.00}', 82.00, 78.00, 1, 1, 1),
  (223, 124, 1, '{"school_code":"ANU","school_min_score":85.00,"school_min_math":85.00,"school_min_english":80.00}', '{"major_code":"ANU_IR","min_average_score":85.00,"min_english_score":88.00,"min_liberal_arts_score":80.00}', 82.00, 79.00, 1, 1, 1),
  (224, 125, 1, '{"school_code":"UNSW","school_min_score":82.00,"school_min_math":80.00,"school_min_english":76.00}', '{"major_code":"UNSW_ME","min_average_score":84.00,"min_math_score":87.00,"min_english_score":76.00,"min_physics_score":85.00}', 82.00, 78.00, 1, 1, 1),
  (225, 126, 1, '{"school_code":"ANU","school_min_score":85.00,"school_min_math":85.00,"school_min_english":80.00}', '{"major_code":"ANU_CS","min_average_score":88.00,"min_math_score":90.00,"min_english_score":80.00}', 85.00, 82.00, 1, 1, 1),
  (226, 127, 1, '{"school_code":"USYD","school_min_score":80.00,"school_min_math":78.00,"school_min_english":75.00}', '{"major_code":"USYD_BIZ","min_average_score":82.00,"min_math_score":78.00,"min_english_score":78.00}', 80.00, 76.00, 1, 1, 1),
  (227, 128, 1, '{"school_code":"UNSW","school_min_score":82.00,"school_min_math":80.00,"school_min_english":76.00}', '{"major_code":"UNSW_DS","min_average_score":85.00,"min_math_score":88.00,"min_english_score":76.00}', 83.00, 79.00, 1, 1, 1),
  (228, 129, 1, '{"school_code":"USYD","school_min_score":80.00,"school_min_math":78.00,"school_min_english":75.00}', '{"major_code":"USYD_SE","min_average_score":84.00,"min_math_score":88.00,"min_english_score":75.00}', 82.00, 78.00, 1, 1, 1),
  (229, 130, 1, '{"school_code":"ANU","school_min_score":85.00,"school_min_math":85.00,"school_min_english":80.00}', '{"major_code":"ANU_IR","min_average_score":85.00,"min_english_score":88.00,"min_liberal_arts_score":80.00}', 82.00, 79.00, 1, 1, 1),
  (230, 131, 1, '{"school_code":"UNSW","school_min_score":82.00,"school_min_math":80.00,"school_min_english":76.00}', '{"major_code":"UNSW_ME","min_average_score":84.00,"min_math_score":87.00,"min_english_score":76.00,"min_physics_score":85.00}', 82.00, 78.00, 1, 1, 1),
  (231, 132, 1, '{"school_code":"USYD","school_min_score":80.00,"school_min_math":78.00,"school_min_english":75.00}', '{"major_code":"USYD_BIZ","min_average_score":82.00,"min_math_score":78.00,"min_english_score":78.00}', 80.00, 76.00, 1, 1, 1),
  (232, 133, 2, '{"school_code":"ANU","school_min_score":84.00,"school_min_math":84.00,"school_min_english":80.00}', '{"major_code":"ANU_CS","min_average_score":88.00,"min_math_score":90.00,"min_english_score":80.00}', 85.00, 82.00, 1, 1, 1),
  (233, 134, 1, '{"school_code":"UNSW","school_min_score":82.00,"school_min_math":80.00,"school_min_english":76.00}', '{"major_code":"UNSW_DS","min_average_score":85.00,"min_math_score":88.00,"min_english_score":76.00}', 83.00, 79.00, 1, 1, 1),
  (234, 135, 1, '{"school_code":"USYD","school_min_score":80.00,"school_min_math":78.00,"school_min_english":75.00}', '{"major_code":"USYD_SE","min_average_score":84.00,"min_math_score":88.00,"min_english_score":75.00}', 82.00, 78.00, 1, 1, 1),
  (235, 136, 1, '{"school_code":"ANU","school_min_score":85.00,"school_min_math":85.00,"school_min_english":80.00}', '{"major_code":"ANU_IR","min_average_score":85.00,"min_english_score":88.00,"min_liberal_arts_score":80.00}', 82.00, 79.00, 1, 1, 1),
  (236, 137, 1, '{"school_code":"ANU","school_min_score":85.00,"school_min_math":85.00,"school_min_english":80.00}', '{"major_code":"ANU_IR","min_average_score":85.00,"min_english_score":88.00,"min_liberal_arts_score":80.00}', 82.00, 79.00, 1, 1, 1),
  (237, 138, 1, '{"school_code":"UNSW","school_min_score":82.00,"school_min_math":80.00,"school_min_english":76.00}', '{"major_code":"UNSW_DS","min_average_score":85.00,"min_math_score":88.00,"min_english_score":76.00}', 83.00, 79.00, 1, 1, 1),
  (238, 139, 1, '{"school_code":"USYD","school_min_score":80.00,"school_min_math":78.00,"school_min_english":75.00}', '{"major_code":"USYD_BIZ","min_average_score":82.00,"min_math_score":78.00,"min_english_score":78.00}', 80.00, 76.00, 1, 1, 1)
ON DUPLICATE KEY UPDATE
  application_id = VALUES(application_id),
  batch_id = VALUES(batch_id),
  school_threshold_snapshot = VALUES(school_threshold_snapshot),
  major_threshold_snapshot = VALUES(major_threshold_snapshot),
  reserve_line_snapshot = VALUES(reserve_line_snapshot),
  waitlist_line_snapshot = VALUES(waitlist_line_snapshot),
  quota_rule_version = VALUES(quota_rule_version),
  adjustment_rule_version = VALUES(adjustment_rule_version),
  waitlist_sort_rule_version = VALUES(waitlist_sort_rule_version);

INSERT INTO rule_snapshots (
  id, application_id, batch_id, school_threshold_snapshot, major_threshold_snapshot,
  reserve_line_snapshot, waitlist_line_snapshot, quota_rule_version, adjustment_rule_version, waitlist_sort_rule_version
) VALUES
  (239, 141, 1, '{"school_code":"USYD","school_min_score":80.00,"school_min_math":78.00,"school_min_english":75.00,"remaining_quota":1}', '{"major_code":"USYD_BIZ","min_average_score":82.00,"min_math_score":78.00,"min_english_score":78.00,"remaining_quota":1}', 80.00, 76.00, 1, 1, 1),
  (240, 142, 1, '{"school_code":"USYD","school_min_score":80.00,"school_min_math":78.00,"school_min_english":75.00,"remaining_quota":1}', '{"major_code":"USYD_BIZ","min_average_score":82.00,"min_math_score":78.00,"min_english_score":78.00,"remaining_quota":1}', 80.00, 76.00, 1, 1, 1),
  (241, 143, 1, '{"school_code":"UNSW","school_min_score":82.00,"school_min_math":80.00,"school_min_english":76.00,"remaining_quota":0}', '{"major_code":"UNSW_ME","min_average_score":84.00,"min_math_score":87.00,"min_english_score":76.00,"min_physics_score":85.00,"remaining_quota":1}', 82.00, 78.00, 1, 1, 1),
  (242, 144, 1, '{"school_code":"UNSW","school_min_score":82.00,"school_min_math":80.00,"school_min_english":76.00,"remaining_quota":0}', '{"major_code":"UNSW_DS","min_average_score":85.00,"min_math_score":88.00,"min_english_score":76.00,"remaining_quota":0}', 83.00, 79.00, 1, 1, 1)
ON DUPLICATE KEY UPDATE
  application_id = VALUES(application_id),
  batch_id = VALUES(batch_id),
  school_threshold_snapshot = VALUES(school_threshold_snapshot),
  major_threshold_snapshot = VALUES(major_threshold_snapshot),
  reserve_line_snapshot = VALUES(reserve_line_snapshot),
  waitlist_line_snapshot = VALUES(waitlist_line_snapshot),
  quota_rule_version = VALUES(quota_rule_version),
  adjustment_rule_version = VALUES(adjustment_rule_version),
  waitlist_sort_rule_version = VALUES(waitlist_sort_rule_version);

INSERT INTO domestic_reviews (
  id, application_id, reviewer_id, material_complete_passed, identity_matched, basic_score_passed,
  authenticity_risk_level, standardization_passed, review_result, review_comment, reviewed_at
) VALUES
  (3515, 123, 3, 1, 1, 1, 'B', 1, 'PASS', 'Domestic review opinion has been prepared. Academic threshold is met and the case is pending final submission to school review.', '2026-09-10 16:30:00'),
  (3516, 124, 3, 0, 1, 1, 'A', 0, 'SUPPLEMENT_REQUIRED', 'Transcript stamp page and supporting explanation are missing. Supplementary materials are required before further review.', '2026-09-12 09:20:00'),
  (3517, 136, 3, 1, 1, 1, 'C', 1, 'PASS', 'Domestic review notes are complete. Scores are borderline but still meet the basic rule snapshot and can proceed after final confirmation.', '2026-09-15 15:40:00')
ON DUPLICATE KEY UPDATE
  application_id = VALUES(application_id),
  reviewer_id = VALUES(reviewer_id),
  material_complete_passed = VALUES(material_complete_passed),
  identity_matched = VALUES(identity_matched),
  basic_score_passed = VALUES(basic_score_passed),
  authenticity_risk_level = VALUES(authenticity_risk_level),
  standardization_passed = VALUES(standardization_passed),
  review_result = VALUES(review_result),
  review_comment = VALUES(review_comment),
  reviewed_at = VALUES(reviewed_at);

INSERT INTO domestic_reviews (
  id, application_id, reviewer_id, material_complete_passed, identity_matched, basic_score_passed,
  authenticity_risk_level, standardization_passed, review_result, review_comment, reviewed_at
) VALUES
  (3518, 141, 3, 1, 1, 1, 'A', 1, 'PASS', 'Average score is just above the business threshold and all required materials are complete, so the application can enter school-side ranking.', '2026-09-17 09:30:00'),
  (3519, 142, 3, 0, 1, 0, 'B', 0, 'SUPPLEMENT_REQUIRED', 'Average score is just below the line and the personal statement is too short, so supplementary materials are required before a final decision.', '2026-09-17 10:00:00'),
  (3520, 143, 3, 1, 1, 0, 'C', 1, 'REJECT', 'Average score is slightly below the UNSW_ME rule, and two failed subjects increase rejection risk.', '2026-09-17 10:30:00'),
  (3521, 144, 3, 1, 1, 1, 'D', 0, 'PASS', 'Base academic floor is met, but the authenticity risk is very high and the case is escalated for school-side adjustment consideration.', '2026-09-17 11:00:00')
ON DUPLICATE KEY UPDATE
  application_id = VALUES(application_id),
  reviewer_id = VALUES(reviewer_id),
  material_complete_passed = VALUES(material_complete_passed),
  identity_matched = VALUES(identity_matched),
  basic_score_passed = VALUES(basic_score_passed),
  authenticity_risk_level = VALUES(authenticity_risk_level),
  standardization_passed = VALUES(standardization_passed),
  review_result = VALUES(review_result),
  review_comment = VALUES(review_comment),
  reviewed_at = VALUES(reviewed_at);

INSERT INTO school_reviews (
  id, application_id, reviewer_id, school_threshold_passed, major_threshold_passed, school_quota_passed, major_quota_passed,
  academic_score, material_score, matching_score, total_score, suggested_major_code, waitlist_rank_snapshot,
  review_result, review_reason, reviewed_at
) VALUES
  (4511, 126, 5, 1, 1, 1, 1, 87.50, 81.00, 84.00, 84.90, NULL, NULL, 'RESERVE', 'School reviewer completed scoring. Candidate meets ANU_CS thresholds and is now waiting final reservation dispatch.', '2026-09-17 10:00:00'),
  (4512, 127, 4, 1, 1, 1, 0, 81.20, 78.50, 79.00, 79.92, NULL, 2, 'WAITLIST', 'Comprehensive score is close to reserve line but major quota is tight, so the application remains on waitlist.', '2026-09-14 15:00:00'),
  (4513, 128, 6, 1, 1, 1, 0, 82.40, 77.80, 80.50, 80.67, NULL, 1, 'WAITLIST', 'Initial school review placed the application on waitlist before later promotion to pending confirmation.', '2026-09-16 10:00:00'),
  (4514, 129, 4, 1, 1, 1, 1, 86.80, 79.60, 83.50, 83.97, NULL, NULL, 'RESERVE', 'Academic performance and school-major matching both support direct reservation.', '2026-09-18 14:00:00'),
  (4515, 130, 5, 1, 0, 1, 0, 79.50, 75.00, 77.20, 77.23, 'ANU_CS', NULL, 'SUGGEST_ADJUSTMENT', 'Current major matching is weak, but the applicant fits ANU_CS better under the same batch rule snapshot.', '2026-09-17 15:30:00'),
  (4516, 131, 6, 0, 0, 1, 1, 72.60, 68.50, 70.20, 70.43, NULL, NULL, 'REJECT', 'The applicant does not meet the required school and major fit for UNSW_ME.', '2026-09-18 16:20:00'),
  (4517, 135, 4, 1, 1, 1, 1, 80.80, 71.50, 76.40, 76.23, NULL, NULL, 'WAITLIST', 'Preliminary school review has been recorded. Final seat decision is pending, so the application is still kept in school reviewing.', '2026-09-18 11:30:00'),
  (4518, 137, 5, 1, 0, 1, 0, 76.40, 66.20, 73.80, 72.13, NULL, 3, 'WAITLIST', 'Borderline liberal arts profile enters waitlist because direct reservation threshold is not reached.', '2026-09-19 14:10:00'),
  (4519, 138, 6, 1, 1, 1, 1, 84.10, 70.40, 79.30, 77.93, NULL, NULL, 'RESERVE', 'Reserved after school review confirmed that the applicant still satisfies UNSW_DS quota and score requirements.', '2026-09-22 15:30:00'),
  (4520, 139, 4, 1, 0, 1, 0, 78.60, 69.10, 75.20, 74.30, 'USYD_SE', NULL, 'SUGGEST_ADJUSTMENT', 'Original application was reviewed as adjustment-suggested and later closed after the agent accepted adjustment.', '2026-09-21 16:00:00')
ON DUPLICATE KEY UPDATE
  application_id = VALUES(application_id),
  reviewer_id = VALUES(reviewer_id),
  school_threshold_passed = VALUES(school_threshold_passed),
  major_threshold_passed = VALUES(major_threshold_passed),
  school_quota_passed = VALUES(school_quota_passed),
  major_quota_passed = VALUES(major_quota_passed),
  academic_score = VALUES(academic_score),
  material_score = VALUES(material_score),
  matching_score = VALUES(matching_score),
  total_score = VALUES(total_score),
  suggested_major_code = VALUES(suggested_major_code),
  waitlist_rank_snapshot = VALUES(waitlist_rank_snapshot),
  review_result = VALUES(review_result),
  review_reason = VALUES(review_reason),
  reviewed_at = VALUES(reviewed_at);

INSERT INTO school_reviews (
  id, application_id, reviewer_id, school_threshold_passed, major_threshold_passed, school_quota_passed, major_quota_passed,
  academic_score, material_score, matching_score, total_score, suggested_major_code, waitlist_rank_snapshot,
  review_result, review_reason, reviewed_at
) VALUES
  (4521, 141, 4, 1, 1, 1, 0, 80.40, 57.00, 76.80, 71.40, NULL, 4, 'WAITLIST', 'The score is only slightly above the line and both school and major quotas are at the final seat boundary, so the case enters waitlist.', '2026-09-18 10:00:00'),
  (4522, 144, 6, 0, 0, 0, 0, 76.20, 42.00, 70.10, 62.77, 'UNSW_ME', NULL, 'SUGGEST_ADJUSTMENT', 'UNSW_DS is already full and the current material quality is weak, so adjustment to UNSW_ME is suggested instead of direct rejection.', '2026-09-18 11:20:00')
ON DUPLICATE KEY UPDATE
  application_id = VALUES(application_id),
  reviewer_id = VALUES(reviewer_id),
  school_threshold_passed = VALUES(school_threshold_passed),
  major_threshold_passed = VALUES(major_threshold_passed),
  school_quota_passed = VALUES(school_quota_passed),
  major_quota_passed = VALUES(major_quota_passed),
  academic_score = VALUES(academic_score),
  material_score = VALUES(material_score),
  matching_score = VALUES(matching_score),
  total_score = VALUES(total_score),
  suggested_major_code = VALUES(suggested_major_code),
  waitlist_rank_snapshot = VALUES(waitlist_rank_snapshot),
  review_result = VALUES(review_result),
  review_reason = VALUES(review_reason),
  reviewed_at = VALUES(reviewed_at);

INSERT INTO waitlist_records (
  id, application_id, batch_id, school_code, major_code, total_score,
  domestic_approved_at, key_subject_score, application_submitted_at, current_rank,
  rank_reason, waitlist_status, promoted_at, confirm_deadline, expired_at
) VALUES
  (5611, 127, 1, 'USYD', 'USYD_BIZ', 82.35, '2026-09-13 09:00:00', 87.00, '2026-09-12 09:10:00', 2, 'Tied near reserve line; ranked by statement quality and submission time', 'ACTIVE', NULL, NULL, NULL),
  (5612, 128, 1, 'UNSW', 'UNSW_DS', 83.10, '2026-09-13 10:30:00', 86.00, '2026-09-12 10:00:00', 1, 'Promoted after one reserved applicant withdrew', 'PROMOTED', '2026-09-18 12:00:00', '2026-09-20 18:00:00', NULL),
  (5613, 137, 1, 'ANU', 'ANU_IR', 78.45, '2026-09-16 10:15:00', 74.00, '2026-09-15 08:40:00', 3, 'Below reserve line but still in quota contention', 'ACTIVE', NULL, NULL, NULL),
  (5614, 129, 1, 'USYD', 'USYD_SE', 83.97, '2026-09-14 09:00:00', 85.00, '2026-09-13 09:20:00', 1, 'Promoted from waitlist and confirmed within deadline', 'CLOSED', '2026-09-17 09:30:00', '2026-09-18 18:00:00', NULL),
  (5615, 135, 1, 'USYD', 'USYD_SE', 76.23, '2026-09-16 09:00:00', 78.00, '2026-09-14 10:30:00', 4, 'Promoted late from waitlist but agent did not confirm before deadline', 'EXPIRED', '2026-09-20 09:00:00', '2026-09-21 18:00:00', '2026-09-21 18:00:00'),
  (5616, 138, 1, 'UNSW', 'UNSW_DS', 77.93, '2026-09-17 09:50:00', 72.00, '2026-09-15 09:20:00', 2, 'Promoted from waitlist after quota rebalancing and then confirmed', 'CLOSED', '2026-09-21 10:00:00', '2026-09-22 18:00:00', NULL)
ON DUPLICATE KEY UPDATE
  application_id = VALUES(application_id),
  batch_id = VALUES(batch_id),
  school_code = VALUES(school_code),
  major_code = VALUES(major_code),
  total_score = VALUES(total_score),
  domestic_approved_at = VALUES(domestic_approved_at),
  key_subject_score = VALUES(key_subject_score),
  application_submitted_at = VALUES(application_submitted_at),
  current_rank = VALUES(current_rank),
  rank_reason = VALUES(rank_reason),
  waitlist_status = VALUES(waitlist_status),
  promoted_at = VALUES(promoted_at),
  confirm_deadline = VALUES(confirm_deadline),
  expired_at = VALUES(expired_at);

INSERT INTO waitlist_records (
  id, application_id, batch_id, school_code, major_code, total_score,
  domestic_approved_at, key_subject_score, application_submitted_at, current_rank,
  rank_reason, waitlist_status, promoted_at, confirm_deadline, expired_at
) VALUES
  (5617, 141, 1, 'USYD', 'USYD_BIZ', 71.40, '2026-09-17 09:30:00', 78.00, '2026-09-16 09:10:00', 4, 'Average score is just above the line but material score is weak, so the application sits behind stronger borderline cases.', 'ACTIVE', NULL, NULL, NULL)
ON DUPLICATE KEY UPDATE
  application_id = VALUES(application_id),
  batch_id = VALUES(batch_id),
  school_code = VALUES(school_code),
  major_code = VALUES(major_code),
  total_score = VALUES(total_score),
  domestic_approved_at = VALUES(domestic_approved_at),
  key_subject_score = VALUES(key_subject_score),
  application_submitted_at = VALUES(application_submitted_at),
  current_rank = VALUES(current_rank),
  rank_reason = VALUES(rank_reason),
  waitlist_status = VALUES(waitlist_status),
  promoted_at = VALUES(promoted_at),
  confirm_deadline = VALUES(confirm_deadline),
  expired_at = VALUES(expired_at);

INSERT INTO application_status_histories (
  id, application_id, old_status, new_status, trigger_role, trigger_action, operator_id, operated_at, remark
) VALUES
  (12175, 141, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 2, '2026-09-16 09:10:00', 'Boundary case application 141 submitted'),
  (12176, 141, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 3, '2026-09-16 13:00:00', 'Boundary case application 141 claimed for domestic review'),
  (12177, 141, 'DOMESTIC_REVIEWING', 'SCHOOL_REVIEWING', 'DOMESTIC_REVIEWER', 'SUBMIT_DOMESTIC_REVIEW', 3, '2026-09-17 09:30:00', 'Boundary case application 141 passed domestic review'),
  (12178, 141, 'SCHOOL_REVIEWING', 'WAITLISTED', 'SCHOOL_REVIEWER', 'SUBMIT_SCHOOL_REVIEW', 4, '2026-09-18 10:00:00', 'Boundary case application 141 entered waitlist'),
  (12179, 142, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 2, '2026-09-16 09:40:00', 'Boundary case application 142 submitted'),
  (12180, 142, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 3, '2026-09-16 13:20:00', 'Boundary case application 142 claimed for domestic review'),
  (12181, 142, 'DOMESTIC_REVIEWING', 'DOMESTIC_SUPPLEMENT', 'DOMESTIC_REVIEWER', 'SUBMIT_DOMESTIC_REVIEW', 3, '2026-09-17 10:00:00', 'Boundary case application 142 requires supplement'),
  (12182, 143, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 2, '2026-09-16 10:20:00', 'Boundary case application 143 submitted'),
  (12183, 143, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 3, '2026-09-16 14:00:00', 'Boundary case application 143 claimed for domestic review'),
  (12184, 143, 'DOMESTIC_REVIEWING', 'DOMESTIC_REJECTED', 'DOMESTIC_REVIEWER', 'SUBMIT_DOMESTIC_REVIEW', 3, '2026-09-17 10:30:00', 'Boundary case application 143 rejected at domestic review'),
  (12185, 144, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 2, '2026-09-16 11:00:00', 'Boundary case application 144 submitted'),
  (12186, 144, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 3, '2026-09-16 14:20:00', 'Boundary case application 144 claimed for domestic review'),
  (12187, 144, 'DOMESTIC_REVIEWING', 'SCHOOL_REVIEWING', 'DOMESTIC_REVIEWER', 'SUBMIT_DOMESTIC_REVIEW', 3, '2026-09-17 11:00:00', 'Boundary case application 144 passed domestic review'),
  (12188, 144, 'SCHOOL_REVIEWING', 'ADJUSTMENT_SUGGESTED', 'SCHOOL_REVIEWER', 'SUBMIT_SCHOOL_REVIEW', 6, '2026-09-18 11:20:00', 'Boundary case application 144 received adjustment suggestion'),
  (12162, 126, 'SCHOOL_REVIEWING', 'SCHOOL_REVIEWING', 'SCHOOL_REVIEWER', 'SAVE_SCHOOL_REVIEW', 5, '2026-09-17 10:00:00', 'School review record saved for application 126'),
  (12163, 127, 'WAITLISTED', 'WAITLISTED', 'SCHOOL_REVIEWER', 'SAVE_SCHOOL_REVIEW', 4, '2026-09-14 15:00:00', 'School review record saved for application 127'),
  (12164, 128, 'WAITLIST_PENDING_CONFIRM', 'WAITLIST_PENDING_CONFIRM', 'SCHOOL_REVIEWER', 'SAVE_SCHOOL_REVIEW', 6, '2026-09-16 10:00:00', 'Initial school review record saved for application 128'),
  (12165, 129, 'RESERVED', 'RESERVED', 'SCHOOL_REVIEWER', 'SAVE_SCHOOL_REVIEW', 4, '2026-09-18 14:00:00', 'School review record saved for application 129'),
  (12166, 130, 'ADJUSTMENT_SUGGESTED', 'ADJUSTMENT_SUGGESTED', 'SCHOOL_REVIEWER', 'SAVE_SCHOOL_REVIEW', 5, '2026-09-17 15:30:00', 'School review record saved for application 130'),
  (12167, 131, 'SCHOOL_REJECTED', 'SCHOOL_REJECTED', 'SCHOOL_REVIEWER', 'SAVE_SCHOOL_REVIEW', 6, '2026-09-18 16:20:00', 'School review record saved for application 131'),
  (12168, 135, 'SCHOOL_REVIEWING', 'SCHOOL_REVIEWING', 'SCHOOL_REVIEWER', 'SAVE_SCHOOL_REVIEW', 4, '2026-09-18 11:30:00', 'School review record saved for application 135'),
  (12169, 137, 'WAITLISTED', 'WAITLISTED', 'SCHOOL_REVIEWER', 'SAVE_SCHOOL_REVIEW', 5, '2026-09-19 14:10:00', 'School review record saved for application 137'),
  (12170, 138, 'RESERVED', 'RESERVED', 'SCHOOL_REVIEWER', 'SAVE_SCHOOL_REVIEW', 6, '2026-09-22 15:30:00', 'School review record saved for application 138'),
  (12171, 139, 'CLOSED', 'CLOSED', 'SCHOOL_REVIEWER', 'SAVE_SCHOOL_REVIEW', 4, '2026-09-21 16:00:00', 'School review record saved for original application 139 before adjustment closure'),
  (12172, 135, 'SCHOOL_REVIEWING', 'WAITLISTED', 'SCHOOL_REVIEWER', 'SUBMIT_SCHOOL_REVIEW', 4, '2026-09-18 11:30:00', 'Application 135 entered waitlist after school review'),
  (12173, 135, 'WAITLISTED', 'WAITLIST_PENDING_CONFIRM', 'SCHOOL_REVIEWER', 'PROMOTE_WAITLIST', 4, '2026-09-20 09:00:00', 'Application 135 was promoted from waitlist and entered pending confirmation'),
  (12174, 135, 'WAITLIST_PENDING_CONFIRM', 'CLOSED', 'SYSTEM', 'EXPIRE_WAITLIST_CONFIRM', NULL, '2026-09-21 18:00:00', 'Application 135 was closed after missing the waitlist confirmation deadline'),
  (12159, 123, 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'SAVE_DOMESTIC_REVIEW', 3, '2026-09-10 16:30:00', 'Domestic review record saved; waiting final routing decision'),
  (12160, 124, 'DOMESTIC_REVIEWING', 'DOMESTIC_SUPPLEMENT', 'DOMESTIC_REVIEWER', 'SAVE_DOMESTIC_REVIEW', 3, '2026-09-12 09:20:00', 'Domestic review record saved with supplement-required conclusion'),
  (12161, 136, 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'SAVE_DOMESTIC_REVIEW', 3, '2026-09-15 15:40:00', 'Domestic review record saved; case remains in active domestic reviewing'),
  (12101, 122, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 2, '2026-09-10 09:00:00', 'Application submitted by agent'),
  (12102, 123, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 2, '2026-09-10 09:30:00', 'Application submitted by agent'),
  (12103, 123, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 3, '2026-09-10 11:00:00', 'Domestic reviewer claimed the application'),
  (12104, 124, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 2, '2026-09-11 10:00:00', 'Application submitted by agent'),
  (12105, 124, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 3, '2026-09-11 12:00:00', 'Domestic reviewer claimed the application'),
  (12106, 124, 'DOMESTIC_REVIEWING', 'DOMESTIC_SUPPLEMENT', 'DOMESTIC_REVIEWER', 'SUBMIT_DOMESTIC_REVIEW', 3, '2026-09-12 09:20:00', 'Supplemental materials required'),
  (12107, 125, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 2, '2026-09-11 11:00:00', 'Application submitted by agent'),
  (12108, 125, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 3, '2026-09-11 14:00:00', 'Domestic reviewer claimed the application'),
  (12109, 125, 'DOMESTIC_REVIEWING', 'DOMESTIC_REJECTED', 'DOMESTIC_REVIEWER', 'SUBMIT_DOMESTIC_REVIEW', 3, '2026-09-12 10:30:00', 'Rejected due to low scores and weak materials'),
  (12110, 126, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 2, '2026-09-12 08:45:00', 'Application submitted by agent'),
  (12111, 126, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 3, '2026-09-12 10:00:00', 'Domestic reviewer claimed the application'),
  (12112, 126, 'DOMESTIC_REVIEWING', 'SCHOOL_REVIEWING', 'DOMESTIC_REVIEWER', 'SUBMIT_DOMESTIC_REVIEW', 3, '2026-09-13 09:30:00', 'Domestic review passed and sent to school'),
  (12113, 127, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 2, '2026-09-12 09:10:00', 'Application submitted by agent'),
  (12114, 127, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 3, '2026-09-12 11:00:00', 'Domestic reviewer claimed the application'),
  (12115, 127, 'DOMESTIC_REVIEWING', 'SCHOOL_REVIEWING', 'DOMESTIC_REVIEWER', 'SUBMIT_DOMESTIC_REVIEW', 3, '2026-09-13 09:00:00', 'Domestic review passed and sent to school'),
  (12116, 127, 'SCHOOL_REVIEWING', 'WAITLISTED', 'SCHOOL_REVIEWER', 'SUBMIT_SCHOOL_REVIEW', 4, '2026-09-14 15:00:00', 'Placed on waitlist due to quota pressure'),
  (12117, 128, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 2, '2026-09-12 10:00:00', 'Application submitted by agent'),
  (12118, 128, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 3, '2026-09-12 13:00:00', 'Domestic reviewer claimed the application'),
  (12119, 128, 'DOMESTIC_REVIEWING', 'SCHOOL_REVIEWING', 'DOMESTIC_REVIEWER', 'SUBMIT_DOMESTIC_REVIEW', 3, '2026-09-13 10:30:00', 'Domestic review passed and sent to school'),
  (12120, 128, 'SCHOOL_REVIEWING', 'WAITLISTED', 'SCHOOL_REVIEWER', 'SUBMIT_SCHOOL_REVIEW', 6, '2026-09-16 10:00:00', 'Initially added to waitlist'),
  (12121, 128, 'WAITLISTED', 'WAITLIST_PENDING_CONFIRM', 'SCHOOL_REVIEWER', 'PROMOTE_WAITLIST', 6, '2026-09-18 12:00:00', 'Promoted from waitlist and awaiting agent confirmation'),
  (12122, 129, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 2, '2026-09-13 09:20:00', 'Application submitted by agent'),
  (12123, 129, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 3, '2026-09-13 11:00:00', 'Domestic reviewer claimed the application'),
  (12124, 129, 'DOMESTIC_REVIEWING', 'SCHOOL_REVIEWING', 'DOMESTIC_REVIEWER', 'SUBMIT_DOMESTIC_REVIEW', 3, '2026-09-14 09:00:00', 'Domestic review passed and sent to school'),
  (12125, 129, 'SCHOOL_REVIEWING', 'RESERVED', 'SCHOOL_REVIEWER', 'SUBMIT_SCHOOL_REVIEW', 4, '2026-09-18 14:00:00', 'Reserved directly by school'),
  (12126, 130, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 2, '2026-09-13 10:15:00', 'Application submitted by agent'),
  (12127, 130, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 3, '2026-09-13 13:00:00', 'Domestic reviewer claimed the application'),
  (12128, 130, 'DOMESTIC_REVIEWING', 'SCHOOL_REVIEWING', 'DOMESTIC_REVIEWER', 'SUBMIT_DOMESTIC_REVIEW', 3, '2026-09-14 10:00:00', 'Domestic review passed and sent to school'),
  (12129, 130, 'SCHOOL_REVIEWING', 'ADJUSTMENT_SUGGESTED', 'SCHOOL_REVIEWER', 'SUBMIT_SCHOOL_REVIEW', 5, '2026-09-17 15:30:00', 'School suggested adjustment to a related major'),
  (12130, 131, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 2, '2026-09-13 11:00:00', 'Application submitted by agent'),
  (12131, 131, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 3, '2026-09-13 14:00:00', 'Domestic reviewer claimed the application'),
  (12132, 131, 'DOMESTIC_REVIEWING', 'SCHOOL_REVIEWING', 'DOMESTIC_REVIEWER', 'SUBMIT_DOMESTIC_REVIEW', 3, '2026-09-14 11:00:00', 'Domestic review passed and sent to school'),
  (12133, 131, 'SCHOOL_REVIEWING', 'SCHOOL_REJECTED', 'SCHOOL_REVIEWER', 'SUBMIT_SCHOOL_REVIEW', 6, '2026-09-18 16:20:00', 'School review rejected the application'),
  (12134, 132, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 2, '2026-09-14 08:30:00', 'Application submitted by agent'),
  (12135, 132, 'SUBMITTED', 'CANCELED', 'AGENT', 'CANCEL_APPLICATION', 2, '2026-09-14 16:20:00', 'Agent canceled before formal review'),
  (12136, 133, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 2, '2025-09-18 09:00:00', 'Application submitted by agent'),
  (12137, 133, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 3, '2025-09-18 11:00:00', 'Domestic reviewer claimed the application'),
  (12138, 133, 'DOMESTIC_REVIEWING', 'DOMESTIC_REJECTED', 'DOMESTIC_REVIEWER', 'SUBMIT_DOMESTIC_REVIEW', 3, '2025-09-25 10:30:00', 'Domestic review rejected the application'),
  (12139, 133, 'DOMESTIC_REJECTED', 'CLOSED', 'ADMIN', 'CLOSE_APPLICATION', 1, '2025-10-02 18:00:00', 'Archived after rejection'),
  (12140, 134, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 2, '2026-09-14 09:40:00', 'Application submitted by agent'),
  (12141, 135, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 2, '2026-09-14 10:30:00', 'Application submitted by agent'),
  (12142, 135, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 3, '2026-09-14 13:30:00', 'Domestic reviewer claimed the application'),
  (12143, 135, 'DOMESTIC_REVIEWING', 'SCHOOL_REVIEWING', 'DOMESTIC_REVIEWER', 'SUBMIT_DOMESTIC_REVIEW', 3, '2026-09-16 09:00:00', 'Domestic review passed and sent to school'),
  (12144, 136, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 2, '2026-09-15 08:10:00', 'Application submitted by agent'),
  (12145, 136, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 3, '2026-09-15 10:00:00', 'Domestic reviewer claimed the application'),
  (12146, 137, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 2, '2026-09-15 08:40:00', 'Application submitted by agent'),
  (12147, 137, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 3, '2026-09-15 11:00:00', 'Domestic reviewer claimed the application'),
  (12148, 137, 'DOMESTIC_REVIEWING', 'SCHOOL_REVIEWING', 'DOMESTIC_REVIEWER', 'SUBMIT_DOMESTIC_REVIEW', 3, '2026-09-16 10:15:00', 'Domestic review passed and sent to school'),
  (12149, 137, 'SCHOOL_REVIEWING', 'WAITLISTED', 'SCHOOL_REVIEWER', 'SUBMIT_SCHOOL_REVIEW', 5, '2026-09-19 14:10:00', 'Placed on waitlist as marginal candidate'),
  (12150, 138, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 2, '2026-09-15 09:20:00', 'Application submitted by agent'),
  (12151, 138, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 3, '2026-09-15 12:20:00', 'Domestic reviewer claimed the application'),
  (12152, 138, 'DOMESTIC_REVIEWING', 'SCHOOL_REVIEWING', 'DOMESTIC_REVIEWER', 'SUBMIT_DOMESTIC_REVIEW', 3, '2026-09-17 09:50:00', 'Domestic review passed and sent to school'),
  (12153, 138, 'SCHOOL_REVIEWING', 'RESERVED', 'SCHOOL_REVIEWER', 'SUBMIT_SCHOOL_REVIEW', 6, '2026-09-22 15:30:00', 'Reserved directly by school'),
  (12154, 139, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 2, '2026-09-15 10:00:00', 'Application submitted by agent'),
  (12155, 139, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 3, '2026-09-15 13:00:00', 'Domestic reviewer claimed the application'),
  (12156, 139, 'DOMESTIC_REVIEWING', 'SCHOOL_REVIEWING', 'DOMESTIC_REVIEWER', 'SUBMIT_DOMESTIC_REVIEW', 3, '2026-09-17 10:20:00', 'Domestic review passed and sent to school'),
  (12157, 139, 'SCHOOL_REVIEWING', 'ADJUSTMENT_SUGGESTED', 'SCHOOL_REVIEWER', 'SUBMIT_SCHOOL_REVIEW', 4, '2026-09-21 16:00:00', 'Adjustment recommended due to quota mismatch'),
  (12158, 139, 'ADJUSTMENT_SUGGESTED', 'CLOSED', 'AGENT', 'ACCEPT_ADJUSTMENT', 2, '2026-09-25 17:45:00', 'Agent accepted adjustment and original application was closed')
ON DUPLICATE KEY UPDATE
  application_id = VALUES(application_id),
  old_status = VALUES(old_status),
  new_status = VALUES(new_status),
  trigger_role = VALUES(trigger_role),
  trigger_action = VALUES(trigger_action),
  operator_id = VALUES(operator_id),
  operated_at = VALUES(operated_at),
  remark = VALUES(remark);

INSERT INTO audit_logs (
  id, operator_id, operator_role, entity_type, entity_id, operation_type, old_value, new_value, remark
) VALUES
  (13155, 3, 'DOMESTIC_REVIEWER', 'DOMESTIC_REVIEW', '3518', 'SAVE_DOMESTIC_REVIEW', NULL, '{"application_id":141,"review_result":"PASS","authenticity_risk_level":"A"}', 'Boundary domestic review created for application 141'),
  (13156, 3, 'DOMESTIC_REVIEWER', 'DOMESTIC_REVIEW', '3519', 'SAVE_DOMESTIC_REVIEW', NULL, '{"application_id":142,"review_result":"SUPPLEMENT_REQUIRED","authenticity_risk_level":"B"}', 'Boundary domestic review created for application 142'),
  (13157, 3, 'DOMESTIC_REVIEWER', 'DOMESTIC_REVIEW', '3520', 'SAVE_DOMESTIC_REVIEW', NULL, '{"application_id":143,"review_result":"REJECT","authenticity_risk_level":"C"}', 'Boundary domestic review created for application 143'),
  (13158, 3, 'DOMESTIC_REVIEWER', 'DOMESTIC_REVIEW', '3521', 'SAVE_DOMESTIC_REVIEW', NULL, '{"application_id":144,"review_result":"PASS","authenticity_risk_level":"D"}', 'Boundary domestic review created for application 144'),
  (13159, 4, 'SCHOOL_REVIEWER', 'SCHOOL_REVIEW', '4521', 'SAVE_SCHOOL_REVIEW', NULL, '{"application_id":141,"review_result":"WAITLIST","total_score":71.40}', 'Boundary school review created for application 141'),
  (13160, 6, 'SCHOOL_REVIEWER', 'SCHOOL_REVIEW', '4522', 'SAVE_SCHOOL_REVIEW', NULL, '{"application_id":144,"review_result":"SUGGEST_ADJUSTMENT","suggested_major_code":"UNSW_ME","total_score":62.77}', 'Boundary school review created for application 144'),
  (13161, 4, 'SCHOOL_REVIEWER', 'WAITLIST_RECORD', '5617', 'CREATE_WAITLIST_RECORD', NULL, '{"application_id":141,"waitlist_status":"ACTIVE","current_rank":4}', 'Boundary waitlist record created for application 141'),
  (13142, 5, 'SCHOOL_REVIEWER', 'SCHOOL_REVIEW', '4511', 'SAVE_SCHOOL_REVIEW', NULL, '{"application_id":126,"review_result":"RESERVE","total_score":84.90}', 'School review record created for application 126'),
  (13143, 4, 'SCHOOL_REVIEWER', 'SCHOOL_REVIEW', '4512', 'SAVE_SCHOOL_REVIEW', NULL, '{"application_id":127,"review_result":"WAITLIST","total_score":79.92}', 'School review record created for application 127'),
  (13144, 6, 'SCHOOL_REVIEWER', 'SCHOOL_REVIEW', '4513', 'SAVE_SCHOOL_REVIEW', NULL, '{"application_id":128,"review_result":"WAITLIST","total_score":80.67}', 'School review record created for application 128'),
  (13145, 4, 'SCHOOL_REVIEWER', 'SCHOOL_REVIEW', '4514', 'SAVE_SCHOOL_REVIEW', NULL, '{"application_id":129,"review_result":"RESERVE","total_score":83.97}', 'School review record created for application 129'),
  (13146, 5, 'SCHOOL_REVIEWER', 'SCHOOL_REVIEW', '4515', 'SAVE_SCHOOL_REVIEW', NULL, '{"application_id":130,"review_result":"SUGGEST_ADJUSTMENT","suggested_major_code":"ANU_CS","total_score":77.23}', 'School review record created for application 130'),
  (13147, 6, 'SCHOOL_REVIEWER', 'SCHOOL_REVIEW', '4516', 'SAVE_SCHOOL_REVIEW', NULL, '{"application_id":131,"review_result":"REJECT","total_score":70.43}', 'School review record created for application 131'),
  (13148, 4, 'SCHOOL_REVIEWER', 'SCHOOL_REVIEW', '4517', 'SAVE_SCHOOL_REVIEW', NULL, '{"application_id":135,"review_result":"WAITLIST","total_score":76.23}', 'School review record created for application 135'),
  (13149, 5, 'SCHOOL_REVIEWER', 'SCHOOL_REVIEW', '4518', 'SAVE_SCHOOL_REVIEW', NULL, '{"application_id":137,"review_result":"WAITLIST","total_score":72.13}', 'School review record created for application 137'),
  (13150, 6, 'SCHOOL_REVIEWER', 'SCHOOL_REVIEW', '4519', 'SAVE_SCHOOL_REVIEW', NULL, '{"application_id":138,"review_result":"RESERVE","total_score":77.93}', 'School review record created for application 138'),
  (13151, 4, 'SCHOOL_REVIEWER', 'SCHOOL_REVIEW', '4520', 'SAVE_SCHOOL_REVIEW', NULL, '{"application_id":139,"review_result":"SUGGEST_ADJUSTMENT","suggested_major_code":"USYD_SE","total_score":74.30}', 'School review record created for application 139'),
  (13152, 4, 'SCHOOL_REVIEWER', 'APPLICATION', '135', 'SUBMIT_SCHOOL_REVIEW', '{"status":"SCHOOL_REVIEWING"}', '{"status":"WAITLISTED"}', 'Application 135 entered waitlist after school review'),
  (13153, 4, 'SCHOOL_REVIEWER', 'WAITLIST_RECORD', '5615', 'PROMOTE_WAITLIST', '{"waitlist_status":"ACTIVE"}', '{"waitlist_status":"EXPIRED","promoted_at":"2026-09-20 09:00:00","confirm_deadline":"2026-09-21 18:00:00"}', 'Waitlist record 5615 was promoted and later expired'),
  (13154, NULL, 'SYSTEM', 'APPLICATION', '135', 'EXPIRE_WAITLIST_CONFIRM', '{"status":"WAITLIST_PENDING_CONFIRM"}', '{"status":"CLOSED"}', 'Application 135 closed after missing waitlist confirmation deadline'),
  (13139, 3, 'DOMESTIC_REVIEWER', 'DOMESTIC_REVIEW', '3515', 'SAVE_DOMESTIC_REVIEW', NULL, '{"application_id":123,"review_result":"PASS","authenticity_risk_level":"B"}', 'Domestic review record created for application 123'),
  (13140, 3, 'DOMESTIC_REVIEWER', 'DOMESTIC_REVIEW', '3516', 'SAVE_DOMESTIC_REVIEW', NULL, '{"application_id":124,"review_result":"SUPPLEMENT_REQUIRED","authenticity_risk_level":"A"}', 'Domestic review record created for application 124'),
  (13141, 3, 'DOMESTIC_REVIEWER', 'DOMESTIC_REVIEW', '3517', 'SAVE_DOMESTIC_REVIEW', NULL, '{"application_id":136,"review_result":"PASS","authenticity_risk_level":"C"}', 'Domestic review record created for application 136'),
  (13121, 2, 'AGENT', 'APPLICATION', '122', 'SUBMIT_APPLICATION', '{"status":"DRAFT"}', '{"status":"SUBMITTED"}', 'Application 122 submitted'),
  (13122, 3, 'DOMESTIC_REVIEWER', 'APPLICATION', '123', 'CLAIM_DOMESTIC_REVIEW', '{"status":"SUBMITTED"}', '{"status":"DOMESTIC_REVIEWING"}', 'Domestic reviewer claimed application 123'),
  (13123, 3, 'DOMESTIC_REVIEWER', 'APPLICATION', '124', 'SUBMIT_DOMESTIC_REVIEW', '{"status":"DOMESTIC_REVIEWING"}', '{"status":"DOMESTIC_SUPPLEMENT"}', 'Supplement requested for application 124'),
  (13124, 3, 'DOMESTIC_REVIEWER', 'APPLICATION', '125', 'SUBMIT_DOMESTIC_REVIEW', '{"status":"DOMESTIC_REVIEWING"}', '{"status":"DOMESTIC_REJECTED"}', 'Domestic rejection for application 125'),
  (13125, 3, 'DOMESTIC_REVIEWER', 'APPLICATION', '126', 'SUBMIT_DOMESTIC_REVIEW', '{"status":"DOMESTIC_REVIEWING"}', '{"status":"SCHOOL_REVIEWING"}', 'Domestic pass for application 126'),
  (13126, 4, 'SCHOOL_REVIEWER', 'APPLICATION', '127', 'SUBMIT_SCHOOL_REVIEW', '{"status":"SCHOOL_REVIEWING"}', '{"status":"WAITLISTED"}', 'Waitlist decision for application 127'),
  (13127, 6, 'SCHOOL_REVIEWER', 'APPLICATION', '128', 'PROMOTE_WAITLIST', '{"status":"WAITLISTED"}', '{"status":"WAITLIST_PENDING_CONFIRM"}', 'Waitlist promotion for application 128'),
  (13128, 4, 'SCHOOL_REVIEWER', 'APPLICATION', '129', 'SUBMIT_SCHOOL_REVIEW', '{"status":"SCHOOL_REVIEWING"}', '{"status":"RESERVED"}', 'Reservation created for application 129'),
  (13129, 5, 'SCHOOL_REVIEWER', 'APPLICATION', '130', 'SUBMIT_SCHOOL_REVIEW', '{"status":"SCHOOL_REVIEWING"}', '{"status":"ADJUSTMENT_SUGGESTED"}', 'Adjustment suggested for application 130'),
  (13130, 6, 'SCHOOL_REVIEWER', 'APPLICATION', '131', 'SUBMIT_SCHOOL_REVIEW', '{"status":"SCHOOL_REVIEWING"}', '{"status":"SCHOOL_REJECTED"}', 'School rejection for application 131'),
  (13131, 2, 'AGENT', 'APPLICATION', '132', 'CANCEL_APPLICATION', '{"status":"SUBMITTED"}', '{"status":"CANCELED"}', 'Application 132 canceled by agent'),
  (13132, 1, 'ADMIN', 'APPLICATION', '133', 'CLOSE_APPLICATION', '{"status":"DOMESTIC_REJECTED"}', '{"status":"CLOSED"}', 'Application 133 archived by admin'),
  (13133, 2, 'AGENT', 'APPLICATION', '134', 'SUBMIT_APPLICATION', '{"status":"DRAFT"}', '{"status":"SUBMITTED"}', 'Application 134 submitted'),
  (13134, 3, 'DOMESTIC_REVIEWER', 'APPLICATION', '135', 'SUBMIT_DOMESTIC_REVIEW', '{"status":"DOMESTIC_REVIEWING"}', '{"status":"SCHOOL_REVIEWING"}', 'Domestic pass for application 135'),
  (13135, 3, 'DOMESTIC_REVIEWER', 'APPLICATION', '136', 'CLAIM_DOMESTIC_REVIEW', '{"status":"SUBMITTED"}', '{"status":"DOMESTIC_REVIEWING"}', 'Domestic reviewer claimed application 136'),
  (13136, 5, 'SCHOOL_REVIEWER', 'APPLICATION', '137', 'SUBMIT_SCHOOL_REVIEW', '{"status":"SCHOOL_REVIEWING"}', '{"status":"WAITLISTED"}', 'Waitlist decision for application 137'),
  (13137, 6, 'SCHOOL_REVIEWER', 'APPLICATION', '138', 'SUBMIT_SCHOOL_REVIEW', '{"status":"SCHOOL_REVIEWING"}', '{"status":"RESERVED"}', 'Reservation created for application 138'),
  (13138, 2, 'AGENT', 'APPLICATION', '139', 'ACCEPT_ADJUSTMENT', '{"status":"ADJUSTMENT_SUGGESTED"}', '{"status":"CLOSED"}', 'Original application 139 closed after adjustment acceptance')
ON DUPLICATE KEY UPDATE
  operator_id = VALUES(operator_id),
  operator_role = VALUES(operator_role),
  entity_type = VALUES(entity_type),
  entity_id = VALUES(entity_id),
  operation_type = VALUES(operation_type),
  old_value = VALUES(old_value),
  new_value = VALUES(new_value),
  remark = VALUES(remark);
