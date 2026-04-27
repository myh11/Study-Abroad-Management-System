SET NAMES utf8mb4;

-- Cleanup for repeated demo execution
DELETE FROM waitlist_records WHERE application_id BETWEEN 101 AND 120;
DELETE FROM school_reviews WHERE application_id BETWEEN 101 AND 120;
DELETE FROM domestic_reviews WHERE application_id BETWEEN 101 AND 120;
DELETE FROM rule_snapshots WHERE application_id BETWEEN 101 AND 120;
DELETE FROM application_status_histories WHERE application_id BETWEEN 101 AND 120;
DELETE FROM audit_logs
WHERE entity_type = 'APPLICATION'
  AND entity_id IN (
    '101','102','103','104','105','106','107','108','109','110',
    '111','112','113','114','115','116','117','118','119','120'
  );
DELETE FROM applications WHERE id BETWEEN 101 AND 120;

-- Application 101: DRAFT
INSERT INTO applications (
  id, student_id, transcript_id, personal_statement_id, batch_id,
  target_school_code, target_major_code, source_application_id, created_by_agent_id,
  current_status, submit_time, cancel_reason, close_reason, rule_snapshot_id,
  waitlist_confirm_deadline, reserved_at, canceled_at, closed_at
) VALUES
  (101, 101, 101, 101, 1, 'USYD', 'USYD_SE', NULL, 2, 'DRAFT', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- Application 102: SUBMITTED
INSERT INTO applications VALUES
  (102, 102, 102, 102, 1, 'ANU', 'ANU_CS', NULL, 3, 'SUBMITTED', '2026-04-10 09:00:00', NULL, NULL, 202, NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Application 103: DOMESTIC_REVIEWING
INSERT INTO applications VALUES
  (103, 103, 103, 103, 1, 'UNSW', 'UNSW_DS', NULL, 2, 'DOMESTIC_REVIEWING', '2026-04-10 09:10:00', NULL, NULL, 203, NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Application 104: DOMESTIC_SUPPLEMENT
INSERT INTO applications VALUES
  (104, 104, 104, 104, 1, 'USYD', 'USYD_BIZ', NULL, 3, 'DOMESTIC_SUPPLEMENT', '2026-04-10 09:20:00', NULL, NULL, 204, NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Application 105: DOMESTIC_REJECTED
INSERT INTO applications VALUES
  (105, 105, 105, 105, 1, 'ANU', 'ANU_IR', NULL, 2, 'DOMESTIC_REJECTED', '2026-04-10 09:30:00', NULL, NULL, 205, NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Application 106: SCHOOL_REVIEWING
INSERT INTO applications VALUES
  (106, 106, 106, 106, 1, 'USYD', 'USYD_SE', NULL, 3, 'SCHOOL_REVIEWING', '2026-04-10 09:40:00', NULL, NULL, 206, NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Application 107: WAITLISTED
INSERT INTO applications VALUES
  (107, 107, 107, 107, 1, 'UNSW', 'UNSW_DS', NULL, 2, 'WAITLISTED', '2026-04-10 09:50:00', NULL, NULL, 207, NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Application 108: WAITLIST_PENDING_CONFIRM
INSERT INTO applications VALUES
  (108, 108, 108, 108, 1, 'USYD', 'USYD_BIZ', NULL, 3, 'WAITLIST_PENDING_CONFIRM', '2026-04-10 10:00:00', NULL, NULL, 208, '2026-04-19 10:00:00', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Application 109: RESERVED
INSERT INTO applications VALUES
  (109, 109, 109, 109, 1, 'ANU', 'ANU_CS', NULL, 2, 'RESERVED', '2026-04-10 10:10:00', NULL, NULL, 209, NULL, '2026-04-15 11:00:00', NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Application 110: ADJUSTMENT_SUGGESTED
INSERT INTO applications VALUES
  (110, 110, 110, 110, 1, 'ANU', 'ANU_IR', NULL, 3, 'ADJUSTMENT_SUGGESTED', '2026-04-10 10:20:00', NULL, NULL, 210, NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Application 111: SCHOOL_REJECTED
INSERT INTO applications VALUES
  (111, 111, 111, 111, 1, 'UNSW', 'UNSW_ME', NULL, 2, 'SCHOOL_REJECTED', '2026-04-10 10:30:00', NULL, NULL, 211, NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Application 112: CANCELED
INSERT INTO applications VALUES
  (112, 112, 112, 112, 1, 'USYD', 'USYD_SE', NULL, 3, 'CANCELED', '2026-04-10 10:40:00', 'Student withdrew before domestic review', NULL, 212, NULL, NULL, '2026-04-11 16:00:00', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Application 113: CLOSED (from RESERVED)
INSERT INTO applications VALUES
  (113, 113, 113, 113, 1, 'ANU', 'ANU_CS', NULL, 2, 'CLOSED', '2026-03-10 10:50:00', NULL, 'ARCHIVED_AFTER_RESERVED', 213, NULL, '2026-03-18 12:00:00', NULL, '2026-03-20 18:00:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Application 114: CLOSED (from DOMESTIC_REJECTED)
INSERT INTO applications VALUES
  (114, 114, 114, 114, 1, 'USYD', 'USYD_BIZ', NULL, 3, 'CLOSED', '2026-03-11 09:00:00', NULL, 'ARCHIVED_AFTER_DOMESTIC_REJECT', 214, NULL, NULL, NULL, '2026-03-15 18:00:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Application 115: CLOSED (from SCHOOL_REJECTED)
INSERT INTO applications VALUES
  (115, 115, 115, 115, 1, 'UNSW', 'UNSW_DS', NULL, 2, 'CLOSED', '2026-03-12 09:30:00', NULL, 'ARCHIVED_AFTER_SCHOOL_REJECT', 215, NULL, NULL, NULL, '2026-03-17 18:00:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Application 116: CLOSED (from CANCELED)
INSERT INTO applications VALUES
  (116, 116, 116, 116, 1, 'ANU', 'ANU_IR', NULL, 3, 'CLOSED', '2026-03-13 10:00:00', 'Student canceled after submission', 'ARCHIVED_AFTER_CANCEL', 216, NULL, NULL, '2026-03-14 10:00:00', '2026-03-16 18:00:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Application 117: SUBMITTED
INSERT INTO applications VALUES
  (117, 117, 117, 117, 1, 'USYD', 'USYD_SE', NULL, 2, 'SUBMITTED', '2026-04-12 14:00:00', NULL, NULL, 217, NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Application 118: SCHOOL_REVIEWING
INSERT INTO applications VALUES
  (118, 118, 118, 118, 1, 'ANU', 'ANU_CS', NULL, 3, 'SCHOOL_REVIEWING', '2026-04-12 14:20:00', NULL, NULL, 218, NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Application 119: WAITLISTED
INSERT INTO applications VALUES
  (119, 119, 119, 119, 1, 'USYD', 'USYD_BIZ', NULL, 2, 'WAITLISTED', '2026-04-12 14:40:00', NULL, NULL, 219, NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Application 120: DRAFT
INSERT INTO applications VALUES
  (120, 120, 120, 120, 1, 'UNSW', 'UNSW_DS', NULL, 3, 'DRAFT', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO rule_snapshots (
  id, application_id, batch_id, school_threshold_snapshot, major_threshold_snapshot,
  reserve_line_snapshot, waitlist_line_snapshot, quota_rule_version, adjustment_rule_version,
  waitlist_sort_rule_version, created_at
) VALUES
  (202, 102, 1, '{"schoolCode":"ANU"}', '{"majorCode":"ANU_CS"}', 85.00, 82.00, 1, 1, 1, '2026-04-10 09:00:00'),
  (203, 103, 1, '{"schoolCode":"UNSW"}', '{"majorCode":"UNSW_DS"}', 83.00, 79.00, 1, 1, 1, '2026-04-10 09:10:00'),
  (204, 104, 1, '{"schoolCode":"USYD"}', '{"majorCode":"USYD_BIZ"}', 80.00, 76.00, 1, 1, 1, '2026-04-10 09:20:00'),
  (205, 105, 1, '{"schoolCode":"ANU"}', '{"majorCode":"ANU_IR"}', 82.00, 79.00, 1, 1, 1, '2026-04-10 09:30:00'),
  (206, 106, 1, '{"schoolCode":"USYD"}', '{"majorCode":"USYD_SE"}', 82.00, 78.00, 1, 1, 1, '2026-04-10 09:40:00'),
  (207, 107, 1, '{"schoolCode":"UNSW"}', '{"majorCode":"UNSW_DS"}', 83.00, 79.00, 1, 1, 1, '2026-04-10 09:50:00'),
  (208, 108, 1, '{"schoolCode":"USYD"}', '{"majorCode":"USYD_BIZ"}', 80.00, 76.00, 1, 1, 1, '2026-04-10 10:00:00'),
  (209, 109, 1, '{"schoolCode":"ANU"}', '{"majorCode":"ANU_CS"}', 85.00, 82.00, 1, 1, 1, '2026-04-10 10:10:00'),
  (210, 110, 1, '{"schoolCode":"ANU"}', '{"majorCode":"ANU_IR"}', 82.00, 79.00, 1, 1, 1, '2026-04-10 10:20:00'),
  (211, 111, 1, '{"schoolCode":"UNSW"}', '{"majorCode":"UNSW_ME"}', 82.00, 78.00, 1, 1, 1, '2026-04-10 10:30:00'),
  (212, 112, 1, '{"schoolCode":"USYD"}', '{"majorCode":"USYD_SE"}', 82.00, 78.00, 1, 1, 1, '2026-04-10 10:40:00'),
  (213, 113, 1, '{"schoolCode":"ANU"}', '{"majorCode":"ANU_CS"}', 85.00, 82.00, 1, 1, 1, '2026-03-10 10:50:00'),
  (214, 114, 1, '{"schoolCode":"USYD"}', '{"majorCode":"USYD_BIZ"}', 80.00, 76.00, 1, 1, 1, '2026-03-11 09:00:00'),
  (215, 115, 1, '{"schoolCode":"UNSW"}', '{"majorCode":"UNSW_DS"}', 83.00, 79.00, 1, 1, 1, '2026-03-12 09:30:00'),
  (216, 116, 1, '{"schoolCode":"ANU"}', '{"majorCode":"ANU_IR"}', 82.00, 79.00, 1, 1, 1, '2026-03-13 10:00:00'),
  (217, 117, 1, '{"schoolCode":"USYD"}', '{"majorCode":"USYD_SE"}', 82.00, 78.00, 1, 1, 1, '2026-04-12 14:00:00'),
  (218, 118, 1, '{"schoolCode":"ANU"}', '{"majorCode":"ANU_CS"}', 85.00, 82.00, 1, 1, 1, '2026-04-12 14:20:00'),
  (219, 119, 1, '{"schoolCode":"USYD"}', '{"majorCode":"USYD_BIZ"}', 80.00, 76.00, 1, 1, 1, '2026-04-12 14:40:00');

INSERT INTO domestic_reviews (
  id, application_id, reviewer_id, material_complete_passed, identity_matched, basic_score_passed,
  authenticity_risk_level, standardization_passed, review_result, review_comment, reviewed_at, created_at, updated_at
) VALUES
  (301, 104, 4, 0, 1, 1, 'B', 0, 'SUPPLEMENT_REQUIRED', 'Need clearer transcript and supplementary note', '2026-04-11 10:00:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (302, 105, 4, 1, 1, 0, 'C', 0, 'REJECT', 'Basic score does not meet domestic threshold', '2026-04-11 10:10:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (303, 106, 4, 1, 1, 1, 'B', 1, 'PASS', 'Domestic review passed', '2026-04-11 10:20:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (304, 107, 4, 1, 1, 1, 'B', 1, 'PASS', 'Domestic review passed', '2026-04-11 10:30:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (305, 108, 4, 1, 1, 1, 'B', 1, 'PASS', 'Domestic review passed', '2026-04-11 10:40:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (306, 109, 4, 1, 1, 1, 'B', 1, 'PASS', 'Domestic review passed', '2026-04-11 10:50:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (307, 110, 4, 1, 1, 1, 'B', 1, 'PASS', 'Domestic review passed', '2026-04-11 11:00:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (308, 111, 4, 1, 1, 1, 'B', 1, 'PASS', 'Domestic review passed', '2026-04-11 11:10:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (309, 113, 4, 1, 1, 1, 'A', 1, 'PASS', 'Domestic review passed', '2026-03-12 11:00:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (310, 114, 4, 1, 1, 0, 'C', 0, 'REJECT', 'Domestic review rejected', '2026-03-12 11:10:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (311, 115, 4, 1, 1, 1, 'B', 1, 'PASS', 'Domestic review passed', '2026-03-12 11:20:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (312, 118, 4, 1, 1, 1, 'B', 1, 'PASS', 'Domestic review passed', '2026-04-13 09:00:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (313, 119, 4, 1, 1, 1, 'B', 1, 'PASS', 'Domestic review passed', '2026-04-13 09:10:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO school_reviews (
  id, application_id, reviewer_id, school_threshold_passed, major_threshold_passed, school_quota_passed,
  major_quota_passed, academic_score, material_score, matching_score, total_score, suggested_major_code,
  waitlist_rank_snapshot, review_result, review_reason, reviewed_at, created_at, updated_at
) VALUES
  (401, 107, 5, 1, 1, 1, 0, 76.00, 14.00, 7.00, 97.00, NULL, 1, 'WAITLIST', 'Qualified but no remaining major quota', '2026-04-12 09:00:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (402, 108, 5, 1, 1, 1, 0, 75.00, 13.00, 6.00, 94.00, NULL, 1, 'WAITLIST', 'Qualified but waiting for promotion slot', '2026-04-12 09:10:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (403, 109, 5, 1, 1, 1, 1, 78.00, 14.00, 7.00, 99.00, NULL, NULL, 'RESERVE', 'Qualified and quota available', '2026-04-12 09:20:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (404, 110, 5, 1, 0, 1, 1, 70.00, 12.00, 5.00, 87.00, 'ANU_CS', NULL, 'SUGGEST_ADJUSTMENT', 'Major fit is weak, suggest adjustment', '2026-04-12 09:30:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (405, 111, 5, 1, 0, 1, 1, 63.00, 10.00, 4.00, 77.00, NULL, NULL, 'REJECT', 'School review rejected due to poor major fit', '2026-04-12 09:40:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (406, 113, 5, 1, 1, 1, 1, 80.00, 15.00, 8.00, 103.00, NULL, NULL, 'RESERVE', 'Reserved then archived', '2026-03-15 10:00:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (407, 115, 5, 1, 0, 1, 1, 65.00, 10.00, 4.00, 79.00, NULL, NULL, 'REJECT', 'Rejected at school review', '2026-03-15 10:10:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (408, 119, 5, 1, 1, 1, 0, 72.00, 11.00, 5.00, 88.00, NULL, 2, 'WAITLIST', 'Waitlisted due to quota pressure', '2026-04-13 10:00:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO waitlist_records (
  id, application_id, batch_id, school_code, major_code, total_score, domestic_approved_at,
  key_subject_score, application_submitted_at, current_rank, rank_reason, waitlist_status,
  promoted_at, confirm_deadline, expired_at, created_at, updated_at
) VALUES
  (501, 107, 1, 'UNSW', 'UNSW_DS', 97.00, '2026-04-11 10:30:00', 84.00, '2026-04-10 09:50:00', 1, 'Top score among waitlisted candidates', 'ACTIVE', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (502, 108, 1, 'USYD', 'USYD_BIZ', 94.00, '2026-04-11 10:40:00', 83.00, '2026-04-10 10:00:00', 1, 'Promoted from waitlist, pending confirmation', 'PROMOTED', '2026-04-18 10:00:00', '2026-04-19 10:00:00', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (503, 119, 1, 'USYD', 'USYD_BIZ', 88.00, '2026-04-13 09:10:00', 68.00, '2026-04-12 14:40:00', 2, 'Second in line for current batch', 'ACTIVE', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO application_status_histories (
  id, application_id, old_status, new_status, trigger_role, trigger_action, operator_id, operated_at, remark
) VALUES
  (10001, 101, NULL, 'DRAFT', 'AGENT', 'CREATE_DRAFT', 2, '2026-04-10 08:50:00', 'Draft created'),
  (10002, 102, NULL, 'DRAFT', 'AGENT', 'CREATE_DRAFT', 3, '2026-04-10 08:55:00', 'Draft created'),
  (10003, 102, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 3, '2026-04-10 09:00:00', 'Submitted'),
  (10004, 103, NULL, 'DRAFT', 'AGENT', 'CREATE_DRAFT', 2, '2026-04-10 09:05:00', 'Draft created'),
  (10005, 103, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 2, '2026-04-10 09:10:00', 'Submitted'),
  (10006, 103, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 4, '2026-04-10 11:00:00', 'Claimed by domestic reviewer'),
  (10007, 104, NULL, 'DRAFT', 'AGENT', 'CREATE_DRAFT', 3, '2026-04-10 09:15:00', 'Draft created'),
  (10008, 104, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 3, '2026-04-10 09:20:00', 'Submitted'),
  (10009, 104, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 4, '2026-04-10 11:05:00', 'Claimed by domestic reviewer'),
  (10010, 104, 'DOMESTIC_REVIEWING', 'DOMESTIC_SUPPLEMENT', 'DOMESTIC_REVIEWER', 'SUBMIT_DOMESTIC_REVIEW', 4, '2026-04-11 10:00:00', 'Supplement required'),
  (10011, 105, NULL, 'DRAFT', 'AGENT', 'CREATE_DRAFT', 2, '2026-04-10 09:25:00', 'Draft created'),
  (10012, 105, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 2, '2026-04-10 09:30:00', 'Submitted'),
  (10013, 105, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 4, '2026-04-10 11:10:00', 'Claimed by domestic reviewer'),
  (10014, 105, 'DOMESTIC_REVIEWING', 'DOMESTIC_REJECTED', 'DOMESTIC_REVIEWER', 'SUBMIT_DOMESTIC_REVIEW', 4, '2026-04-11 10:10:00', 'Domestic rejected'),
  (10015, 106, NULL, 'DRAFT', 'AGENT', 'CREATE_DRAFT', 3, '2026-04-10 09:35:00', 'Draft created'),
  (10016, 106, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 3, '2026-04-10 09:40:00', 'Submitted'),
  (10017, 106, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 4, '2026-04-10 11:15:00', 'Claimed by domestic reviewer'),
  (10018, 106, 'DOMESTIC_REVIEWING', 'SCHOOL_REVIEWING', 'DOMESTIC_REVIEWER', 'SUBMIT_DOMESTIC_REVIEW', 4, '2026-04-11 10:20:00', 'Passed to school review'),
  (10019, 107, NULL, 'DRAFT', 'AGENT', 'CREATE_DRAFT', 2, '2026-04-10 09:45:00', 'Draft created'),
  (10020, 107, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 2, '2026-04-10 09:50:00', 'Submitted'),
  (10021, 107, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 4, '2026-04-10 11:20:00', 'Claimed by domestic reviewer'),
  (10022, 107, 'DOMESTIC_REVIEWING', 'SCHOOL_REVIEWING', 'DOMESTIC_REVIEWER', 'SUBMIT_DOMESTIC_REVIEW', 4, '2026-04-11 10:30:00', 'Passed to school review'),
  (10023, 107, 'SCHOOL_REVIEWING', 'WAITLISTED', 'SCHOOL_REVIEWER', 'SUBMIT_SCHOOL_REVIEW', 5, '2026-04-12 09:00:00', 'Waitlisted'),
  (10024, 108, NULL, 'DRAFT', 'AGENT', 'CREATE_DRAFT', 3, '2026-04-10 09:55:00', 'Draft created'),
  (10025, 108, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 3, '2026-04-10 10:00:00', 'Submitted'),
  (10026, 108, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 4, '2026-04-10 11:25:00', 'Claimed by domestic reviewer'),
  (10027, 108, 'DOMESTIC_REVIEWING', 'SCHOOL_REVIEWING', 'DOMESTIC_REVIEWER', 'SUBMIT_DOMESTIC_REVIEW', 4, '2026-04-11 10:40:00', 'Passed to school review'),
  (10028, 108, 'SCHOOL_REVIEWING', 'WAITLISTED', 'SCHOOL_REVIEWER', 'SUBMIT_SCHOOL_REVIEW', 5, '2026-04-12 09:10:00', 'Waitlisted'),
  (10029, 108, 'WAITLISTED', 'WAITLIST_PENDING_CONFIRM', 'SCHOOL_REVIEWER', 'PROMOTE_WAITLIST', 5, '2026-04-18 10:00:00', 'Promoted pending confirmation'),
  (10030, 109, NULL, 'DRAFT', 'AGENT', 'CREATE_DRAFT', 2, '2026-04-10 10:05:00', 'Draft created'),
  (10031, 109, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 2, '2026-04-10 10:10:00', 'Submitted'),
  (10032, 109, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 4, '2026-04-10 11:30:00', 'Claimed by domestic reviewer'),
  (10033, 109, 'DOMESTIC_REVIEWING', 'SCHOOL_REVIEWING', 'DOMESTIC_REVIEWER', 'SUBMIT_DOMESTIC_REVIEW', 4, '2026-04-11 10:50:00', 'Passed to school review'),
  (10034, 109, 'SCHOOL_REVIEWING', 'RESERVED', 'SCHOOL_REVIEWER', 'SUBMIT_SCHOOL_REVIEW', 5, '2026-04-12 09:20:00', 'Reserved'),
  (10035, 110, NULL, 'DRAFT', 'AGENT', 'CREATE_DRAFT', 3, '2026-04-10 10:15:00', 'Draft created'),
  (10036, 110, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 3, '2026-04-10 10:20:00', 'Submitted'),
  (10037, 110, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 4, '2026-04-10 11:35:00', 'Claimed by domestic reviewer'),
  (10038, 110, 'DOMESTIC_REVIEWING', 'SCHOOL_REVIEWING', 'DOMESTIC_REVIEWER', 'SUBMIT_DOMESTIC_REVIEW', 4, '2026-04-11 11:00:00', 'Passed to school review'),
  (10039, 110, 'SCHOOL_REVIEWING', 'ADJUSTMENT_SUGGESTED', 'SCHOOL_REVIEWER', 'SUBMIT_SCHOOL_REVIEW', 5, '2026-04-12 09:30:00', 'Adjustment suggested'),
  (10040, 111, NULL, 'DRAFT', 'AGENT', 'CREATE_DRAFT', 2, '2026-04-10 10:25:00', 'Draft created'),
  (10041, 111, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 2, '2026-04-10 10:30:00', 'Submitted'),
  (10042, 111, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 4, '2026-04-10 11:40:00', 'Claimed by domestic reviewer'),
  (10043, 111, 'DOMESTIC_REVIEWING', 'SCHOOL_REVIEWING', 'DOMESTIC_REVIEWER', 'SUBMIT_DOMESTIC_REVIEW', 4, '2026-04-11 11:10:00', 'Passed to school review'),
  (10044, 111, 'SCHOOL_REVIEWING', 'SCHOOL_REJECTED', 'SCHOOL_REVIEWER', 'SUBMIT_SCHOOL_REVIEW', 5, '2026-04-12 09:40:00', 'School rejected'),
  (10045, 112, NULL, 'DRAFT', 'AGENT', 'CREATE_DRAFT', 3, '2026-04-10 10:35:00', 'Draft created'),
  (10046, 112, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 3, '2026-04-10 10:40:00', 'Submitted'),
  (10047, 112, 'SUBMITTED', 'CANCELED', 'AGENT', 'CANCEL_APPLICATION', 3, '2026-04-11 16:00:00', 'Canceled by student'),
  (10048, 113, NULL, 'DRAFT', 'AGENT', 'CREATE_DRAFT', 2, '2026-03-10 10:40:00', 'Draft created'),
  (10049, 113, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 2, '2026-03-10 10:50:00', 'Submitted'),
  (10050, 113, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 4, '2026-03-11 10:30:00', 'Claimed by domestic reviewer'),
  (10051, 113, 'DOMESTIC_REVIEWING', 'SCHOOL_REVIEWING', 'DOMESTIC_REVIEWER', 'SUBMIT_DOMESTIC_REVIEW', 4, '2026-03-12 11:00:00', 'Passed to school review'),
  (10052, 113, 'SCHOOL_REVIEWING', 'RESERVED', 'SCHOOL_REVIEWER', 'SUBMIT_SCHOOL_REVIEW', 5, '2026-03-15 10:00:00', 'Reserved'),
  (10053, 113, 'RESERVED', 'CLOSED', 'ADMIN', 'CLOSE_APPLICATION', 1, '2026-03-20 18:00:00', 'Closed after reserve'),
  (10054, 114, NULL, 'DRAFT', 'AGENT', 'CREATE_DRAFT', 3, '2026-03-11 08:50:00', 'Draft created'),
  (10055, 114, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 3, '2026-03-11 09:00:00', 'Submitted'),
  (10056, 114, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 4, '2026-03-11 10:40:00', 'Claimed by domestic reviewer'),
  (10057, 114, 'DOMESTIC_REVIEWING', 'DOMESTIC_REJECTED', 'DOMESTIC_REVIEWER', 'SUBMIT_DOMESTIC_REVIEW', 4, '2026-03-12 11:10:00', 'Domestic rejected'),
  (10058, 114, 'DOMESTIC_REJECTED', 'CLOSED', 'ADMIN', 'CLOSE_APPLICATION', 1, '2026-03-15 18:00:00', 'Closed after domestic reject'),
  (10059, 115, NULL, 'DRAFT', 'AGENT', 'CREATE_DRAFT', 2, '2026-03-12 09:20:00', 'Draft created'),
  (10060, 115, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 2, '2026-03-12 09:30:00', 'Submitted'),
  (10061, 115, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 4, '2026-03-12 10:50:00', 'Claimed by domestic reviewer'),
  (10062, 115, 'DOMESTIC_REVIEWING', 'SCHOOL_REVIEWING', 'DOMESTIC_REVIEWER', 'SUBMIT_DOMESTIC_REVIEW', 4, '2026-03-12 11:20:00', 'Passed to school review'),
  (10063, 115, 'SCHOOL_REVIEWING', 'SCHOOL_REJECTED', 'SCHOOL_REVIEWER', 'SUBMIT_SCHOOL_REVIEW', 5, '2026-03-15 10:10:00', 'School rejected'),
  (10064, 115, 'SCHOOL_REJECTED', 'CLOSED', 'ADMIN', 'CLOSE_APPLICATION', 1, '2026-03-17 18:00:00', 'Closed after school reject'),
  (10065, 116, NULL, 'DRAFT', 'AGENT', 'CREATE_DRAFT', 3, '2026-03-13 09:50:00', 'Draft created'),
  (10066, 116, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 3, '2026-03-13 10:00:00', 'Submitted'),
  (10067, 116, 'SUBMITTED', 'CANCELED', 'AGENT', 'CANCEL_APPLICATION', 3, '2026-03-14 10:00:00', 'Canceled by student'),
  (10068, 116, 'CANCELED', 'CLOSED', 'ADMIN', 'CLOSE_APPLICATION', 1, '2026-03-16 18:00:00', 'Closed after cancel'),
  (10069, 117, NULL, 'DRAFT', 'AGENT', 'CREATE_DRAFT', 2, '2026-04-12 13:50:00', 'Draft created'),
  (10070, 117, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 2, '2026-04-12 14:00:00', 'Submitted'),
  (10071, 118, NULL, 'DRAFT', 'AGENT', 'CREATE_DRAFT', 3, '2026-04-12 14:10:00', 'Draft created'),
  (10072, 118, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 3, '2026-04-12 14:20:00', 'Submitted'),
  (10073, 118, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 4, '2026-04-12 16:00:00', 'Claimed by domestic reviewer'),
  (10074, 118, 'DOMESTIC_REVIEWING', 'SCHOOL_REVIEWING', 'DOMESTIC_REVIEWER', 'SUBMIT_DOMESTIC_REVIEW', 4, '2026-04-13 09:00:00', 'Passed to school review'),
  (10075, 119, NULL, 'DRAFT', 'AGENT', 'CREATE_DRAFT', 2, '2026-04-12 14:30:00', 'Draft created'),
  (10076, 119, 'DRAFT', 'SUBMITTED', 'AGENT', 'SUBMIT_APPLICATION', 2, '2026-04-12 14:40:00', 'Submitted'),
  (10077, 119, 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_REVIEWER', 'CLAIM_DOMESTIC_REVIEW', 4, '2026-04-12 16:10:00', 'Claimed by domestic reviewer'),
  (10078, 119, 'DOMESTIC_REVIEWING', 'SCHOOL_REVIEWING', 'DOMESTIC_REVIEWER', 'SUBMIT_DOMESTIC_REVIEW', 4, '2026-04-13 09:10:00', 'Passed to school review'),
  (10079, 119, 'SCHOOL_REVIEWING', 'WAITLISTED', 'SCHOOL_REVIEWER', 'SUBMIT_SCHOOL_REVIEW', 5, '2026-04-13 10:00:00', 'Waitlisted'),
  (10080, 120, NULL, 'DRAFT', 'AGENT', 'CREATE_DRAFT', 3, '2026-04-12 14:50:00', 'Draft created');

INSERT INTO audit_logs (
  id, operator_id, operator_role, entity_type, entity_id, operation_type, old_value, new_value, remark, created_at
) VALUES
  (11001, 2, 'AGENT', 'APPLICATION', '101', 'CREATE_DRAFT', NULL, JSON_OBJECT('status', 'DRAFT'), 'Draft created', '2026-04-10 08:50:00'),
  (11002, 3, 'AGENT', 'APPLICATION', '102', 'SUBMIT_APPLICATION', JSON_OBJECT('status', 'DRAFT'), JSON_OBJECT('status', 'SUBMITTED'), 'Application submitted', '2026-04-10 09:00:00'),
  (11003, 4, 'DOMESTIC_REVIEWER', 'APPLICATION', '103', 'CLAIM_DOMESTIC_REVIEW', JSON_OBJECT('status', 'SUBMITTED'), JSON_OBJECT('status', 'DOMESTIC_REVIEWING'), 'Domestic review claimed', '2026-04-10 11:00:00'),
  (11004, 4, 'DOMESTIC_REVIEWER', 'APPLICATION', '104', 'SUPPLEMENT_REQUIRED', JSON_OBJECT('status', 'DOMESTIC_REVIEWING'), JSON_OBJECT('status', 'DOMESTIC_SUPPLEMENT'), 'Supplement required', '2026-04-11 10:00:00'),
  (11005, 4, 'DOMESTIC_REVIEWER', 'APPLICATION', '105', 'DOMESTIC_REJECT', JSON_OBJECT('status', 'DOMESTIC_REVIEWING'), JSON_OBJECT('status', 'DOMESTIC_REJECTED'), 'Domestic rejected', '2026-04-11 10:10:00'),
  (11006, 4, 'DOMESTIC_REVIEWER', 'APPLICATION', '106', 'PASS_TO_SCHOOL', JSON_OBJECT('status', 'DOMESTIC_REVIEWING'), JSON_OBJECT('status', 'SCHOOL_REVIEWING'), 'Passed to school review', '2026-04-11 10:20:00'),
  (11007, 5, 'SCHOOL_REVIEWER', 'APPLICATION', '107', 'WAITLIST', JSON_OBJECT('status', 'SCHOOL_REVIEWING'), JSON_OBJECT('status', 'WAITLISTED'), 'Waitlisted', '2026-04-12 09:00:00'),
  (11008, 5, 'SCHOOL_REVIEWER', 'APPLICATION', '108', 'PROMOTE_WAITLIST', JSON_OBJECT('status', 'WAITLISTED'), JSON_OBJECT('status', 'WAITLIST_PENDING_CONFIRM'), 'Promoted pending confirmation', '2026-04-18 10:00:00'),
  (11009, 5, 'SCHOOL_REVIEWER', 'APPLICATION', '109', 'RESERVE', JSON_OBJECT('status', 'SCHOOL_REVIEWING'), JSON_OBJECT('status', 'RESERVED'), 'Reserved', '2026-04-12 09:20:00'),
  (11010, 5, 'SCHOOL_REVIEWER', 'APPLICATION', '110', 'SUGGEST_ADJUSTMENT', JSON_OBJECT('status', 'SCHOOL_REVIEWING'), JSON_OBJECT('status', 'ADJUSTMENT_SUGGESTED'), 'Adjustment suggested', '2026-04-12 09:30:00'),
  (11011, 5, 'SCHOOL_REVIEWER', 'APPLICATION', '111', 'REJECT', JSON_OBJECT('status', 'SCHOOL_REVIEWING'), JSON_OBJECT('status', 'SCHOOL_REJECTED'), 'School rejected', '2026-04-12 09:40:00'),
  (11012, 3, 'AGENT', 'APPLICATION', '112', 'CANCEL_APPLICATION', JSON_OBJECT('status', 'SUBMITTED'), JSON_OBJECT('status', 'CANCELED'), 'Canceled by student', '2026-04-11 16:00:00'),
  (11013, 1, 'ADMIN', 'APPLICATION', '113', 'CLOSE_APPLICATION', JSON_OBJECT('status', 'RESERVED'), JSON_OBJECT('status', 'CLOSED'), 'Closed after reserve', '2026-03-20 18:00:00'),
  (11014, 1, 'ADMIN', 'APPLICATION', '114', 'CLOSE_APPLICATION', JSON_OBJECT('status', 'DOMESTIC_REJECTED'), JSON_OBJECT('status', 'CLOSED'), 'Closed after domestic reject', '2026-03-15 18:00:00'),
  (11015, 1, 'ADMIN', 'APPLICATION', '115', 'CLOSE_APPLICATION', JSON_OBJECT('status', 'SCHOOL_REJECTED'), JSON_OBJECT('status', 'CLOSED'), 'Closed after school reject', '2026-03-17 18:00:00'),
  (11016, 1, 'ADMIN', 'APPLICATION', '116', 'CLOSE_APPLICATION', JSON_OBJECT('status', 'CANCELED'), JSON_OBJECT('status', 'CLOSED'), 'Closed after cancel', '2026-03-16 18:00:00'),
  (11017, 2, 'AGENT', 'APPLICATION', '117', 'SUBMIT_APPLICATION', JSON_OBJECT('status', 'DRAFT'), JSON_OBJECT('status', 'SUBMITTED'), 'Application submitted', '2026-04-12 14:00:00'),
  (11018, 4, 'DOMESTIC_REVIEWER', 'APPLICATION', '118', 'PASS_TO_SCHOOL', JSON_OBJECT('status', 'DOMESTIC_REVIEWING'), JSON_OBJECT('status', 'SCHOOL_REVIEWING'), 'Passed to school review', '2026-04-13 09:00:00'),
  (11019, 5, 'SCHOOL_REVIEWER', 'APPLICATION', '119', 'WAITLIST', JSON_OBJECT('status', 'SCHOOL_REVIEWING'), JSON_OBJECT('status', 'WAITLISTED'), 'Waitlisted', '2026-04-13 10:00:00'),
  (11020, 3, 'AGENT', 'APPLICATION', '120', 'CREATE_DRAFT', NULL, JSON_OBJECT('status', 'DRAFT'), 'Draft created', '2026-04-12 14:50:00');
