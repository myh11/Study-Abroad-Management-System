SET NAMES utf8mb4;

-- Waitlist demo data
-- Assumption:
-- 1. applications / admission_batches / schools / majors have already been loaded.
-- 2. This script focuses on waitlist_records table only.
-- 3. Applications 107 / 108 / 119 are current waitlist cases,
--    while 113 / 115 are treated as historical waitlist-derived cases for demo purposes.

-- Cleanup for repeated execution
DELETE FROM waitlist_records WHERE id BETWEEN 5601 AND 5605;
DELETE FROM waitlist_records WHERE application_id IN (107, 108, 113, 115, 119);

INSERT INTO waitlist_records (
  id, application_id, batch_id, school_code, major_code, total_score, domestic_approved_at,
  key_subject_score, application_submitted_at, current_rank, rank_reason, waitlist_status,
  promoted_at, confirm_deadline, expired_at, created_at, updated_at
) VALUES
  (
    5601, 107, 1, 'UNSW', 'UNSW_DS', 97.00, '2026-04-11 10:30:00',
    84.00, '2026-04-10 09:50:00', 1,
    'ACTIVE rank 1: highest total_score in the same batch and major; key subject score is strong and submission time is earlier than other candidates.',
    'ACTIVE',
    NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  ),
  (
    5602, 119, 1, 'USYD', 'USYD_BIZ', 88.00, '2026-04-13 09:10:00',
    68.00, '2026-04-12 14:40:00', 2,
    'ACTIVE rank 2: lower total_score than promoted candidate; key subject score is also weaker, so the candidate remains in queue.',
    'ACTIVE',
    NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  ),
  (
    5603, 108, 1, 'USYD', 'USYD_BIZ', 94.00, '2026-04-11 10:40:00',
    83.00, '2026-04-10 10:00:00', 1,
    'PROMOTED rank 1: candidate led the queue by total_score and key subject score, so the released slot was assigned to this application first.',
    'PROMOTED',
    '2026-04-18 10:00:00', '2026-04-19 10:00:00', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  ),
  (
    5604, 113, 1, 'ANU', 'ANU_CS', 92.00, '2026-03-12 11:00:00',
    89.00, '2026-03-10 10:50:00', 1,
    'CLOSED historical waitlist case: candidate was promoted and subsequently archived after the linked application flow was completed.',
    'CLOSED',
    '2026-03-16 09:30:00', '2026-03-18 09:30:00', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  ),
  (
    5605, 115, 1, 'UNSW', 'UNSW_DS', 79.00, '2026-03-12 11:20:00',
    72.00, '2026-03-12 09:30:00', 3,
    'EXPIRED historical waitlist case: candidate stayed behind stronger applicants and the confirmation window expired before promotion.',
    'EXPIRED',
    NULL, '2026-03-16 18:00:00', '2026-03-16 18:00:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  );
