package com.example.admissionsystem.waitlists.service;

import com.example.admissionsystem.applications.model.ApplicationStatus;
import com.example.admissionsystem.applications.service.ApplicationStatusService;
import com.example.admissionsystem.auth.model.AuthUser;
import com.example.admissionsystem.auth.model.UserRole;
import com.example.admissionsystem.auth.security.AuthUserPrincipal;
import com.example.admissionsystem.quotas.service.QuotaConcurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@Service
@RequiredArgsConstructor
public class WaitlistService {

    private static final int WAITLIST_CONFIRM_HOURS = 48;

    private final JdbcTemplate jdbcTemplate;
    private final ApplicationStatusService applicationStatusService;
    private final QuotaConcurrencyService quotaConcurrencyService;

    @Transactional(readOnly = true)
    public Map<String, Object> getWaitlist(Long applicationId) {
        AuthUserPrincipal currentUser = requireCurrentUser();
        ApplicationRecord application = getApplication(applicationId);
        assertCanViewWaitlist(currentUser, application);
        WaitlistRecord waitlistRecord = getWaitlistRecord(applicationId);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("application_id", application.id());
        payload.put("application_status", application.status().name());
        payload.put("batch_id", application.batchId());
        payload.put("school_code", application.targetSchoolCode());
        payload.put("major_code", application.targetMajorCode());
        payload.put("waitlist_id", waitlistRecord.id());
        payload.put("waitlist_status", waitlistRecord.waitlistStatus());
        payload.put("current_rank", waitlistRecord.currentRank());
        payload.put("rank_reason", waitlistRecord.rankReason());
        payload.put("total_score", waitlistRecord.totalScore());
        payload.put("key_subject_score", waitlistRecord.keySubjectScore());
        payload.put("application_submitted_at", toString(waitlistRecord.applicationSubmittedAt()));
        payload.put("promoted_at", toString(waitlistRecord.promotedAt()));
        payload.put("confirm_deadline", toString(waitlistRecord.confirmDeadline()));
        payload.put("expired_at", toString(waitlistRecord.expiredAt()));
        payload.put("waitlist_confirm_deadline", toString(application.waitlistConfirmDeadline()));
        return payload;
    }

    @Transactional
    public String promote(Long applicationId) {
        AuthUserPrincipal currentUser = requireCurrentUser();
        applicationStatusService.assertCanOperate(ApplicationStatusService.ACTION_PROMOTE_WAITLIST, currentUser);
        ApplicationRecord application = getApplication(applicationId);
        assertCanManageWaitlist(currentUser, application);
        if (application.status() != ApplicationStatus.WAITLISTED) {
            throw new IllegalArgumentException("Only WAITLISTED application can be promoted");
        }

        WaitlistRecord waitlistRecord = getWaitlistRecord(applicationId);
        if (!"ACTIVE".equals(waitlistRecord.waitlistStatus())) {
            throw new IllegalArgumentException("Only ACTIVE waitlist record can be promoted");
        }

        quotaConcurrencyService.reserveQuota(
                application.batchId(),
                application.targetSchoolCode(),
                application.targetMajorCode(),
                currentUser,
                ApplicationStatusService.ACTION_PROMOTE_WAITLIST,
                "Promote waitlist application " + application.id()
        );

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime confirmDeadline = now.plusHours(WAITLIST_CONFIRM_HOURS);
        jdbcTemplate.update("""
                UPDATE waitlist_records
                SET waitlist_status = 'PROMOTED', promoted_at = ?, confirm_deadline = ?, updated_at = CURRENT_TIMESTAMP
                WHERE application_id = ?
                """, Timestamp.valueOf(now), Timestamp.valueOf(confirmDeadline), application.id());

        applicationStatusService.insertAuditLog(
                currentUser,
                "WAITLIST_RECORD",
                String.valueOf(waitlistRecord.id()),
                ApplicationStatusService.ACTION_PROMOTE_WAITLIST,
                waitlistStateMap(waitlistRecord),
                Map.of(
                        "waitlist_status", "PROMOTED",
                        "promoted_at", now.toString(),
                        "confirm_deadline", confirmDeadline.toString()
                ),
                "Waitlist application promoted to pending confirmation"
        );
        applicationStatusService.transition(
                application.id(),
                ApplicationStatus.WAITLISTED,
                ApplicationStatus.WAITLIST_PENDING_CONFIRM,
                currentUser,
                ApplicationStatusService.ACTION_PROMOTE_WAITLIST,
                "Waitlist application promoted and waiting for agent confirmation",
                applicationStateMap(application),
                Map.of(
                        "status", ApplicationStatus.WAITLIST_PENDING_CONFIRM.name(),
                        "waitlist_confirm_deadline", confirmDeadline.toString()
                ),
                """
                        UPDATE applications
                        SET current_status = ?, waitlist_confirm_deadline = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                ApplicationStatus.WAITLIST_PENDING_CONFIRM.name(),
                Timestamp.valueOf(confirmDeadline),
                application.id()
        );
        return "ok";
    }

    @Transactional
    public String invalidate(Long applicationId) {
        AuthUserPrincipal currentUser = requireCurrentUser();
        applicationStatusService.assertCanOperate(ApplicationStatusService.ACTION_INVALIDATE_WAITLIST, currentUser);
        ApplicationRecord application = getApplication(applicationId);
        assertCanManageWaitlist(currentUser, application);
        WaitlistRecord waitlistRecord = getWaitlistRecord(applicationId);
        invalidateInternal(
                application,
                waitlistRecord,
                currentUser,
                ApplicationStatusService.ACTION_INVALIDATE_WAITLIST,
                "WAITLIST_INVALIDATED",
                "CLOSED",
                "Waitlist invalidated manually"
        );
        return "ok";
    }

    @Transactional
    public void expireTimedOutPromotions() {
        AuthUserPrincipal systemUser = systemOperator();
        List<Long> applicationIds = jdbcTemplate.query("""
                SELECT a.id
                FROM applications a
                JOIN waitlist_records w ON w.application_id = a.id
                WHERE a.current_status = 'WAITLIST_PENDING_CONFIRM'
                  AND a.waitlist_confirm_deadline IS NOT NULL
                  AND a.waitlist_confirm_deadline < CURRENT_TIMESTAMP
                  AND w.waitlist_status = 'PROMOTED'
                """, (rs, rowNum) -> rs.getLong("id"));

        for (Long applicationId : applicationIds) {
            ApplicationRecord application = getApplication(applicationId);
            WaitlistRecord waitlistRecord = getWaitlistRecord(applicationId);
            invalidateInternal(
                    application,
                    waitlistRecord,
                    systemUser,
                    ApplicationStatusService.ACTION_WAITLIST_TIMEOUT,
                    "WAITLIST_CONFIRM_TIMEOUT",
                    "EXPIRED",
                    "Waitlist confirmation timed out after 48 hours"
            );
        }
    }

    @Transactional
    public void closeFinishedBatchWaitlists() {
        AuthUserPrincipal systemUser = systemOperator();
        List<Long> applicationIds = jdbcTemplate.query("""
                SELECT a.id
                FROM applications a
                JOIN admission_batches b ON b.id = a.batch_id
                JOIN waitlist_records w ON w.application_id = a.id
                WHERE a.current_status IN ('WAITLISTED', 'WAITLIST_PENDING_CONFIRM')
                  AND w.waitlist_status IN ('ACTIVE', 'PROMOTED')
                  AND (b.batch_status = 'FINISHED' OR b.end_time < CURRENT_TIMESTAMP)
                """, (rs, rowNum) -> rs.getLong("id"));

        for (Long applicationId : applicationIds) {
            ApplicationRecord application = getApplication(applicationId);
            WaitlistRecord waitlistRecord = getWaitlistRecord(applicationId);
            invalidateInternal(
                    application,
                    waitlistRecord,
                    systemUser,
                    ApplicationStatusService.ACTION_BATCH_FINISH_WAITLIST_CLOSE,
                    "BATCH_FINISHED_WAITLIST_CLOSED",
                    "CLOSED",
                    "Waitlist closed automatically because batch finished"
            );
        }
    }

    private void invalidateInternal(
            ApplicationRecord application,
            WaitlistRecord waitlistRecord,
            AuthUserPrincipal operator,
            String action,
            String closeReason,
            String nextWaitlistStatus,
            String remark
    ) {
        if (application.status() != ApplicationStatus.WAITLISTED
                && application.status() != ApplicationStatus.WAITLIST_PENDING_CONFIRM) {
            throw new IllegalArgumentException("Only waitlisted application can be invalidated");
        }

        if (application.status() == ApplicationStatus.WAITLIST_PENDING_CONFIRM
                && "PROMOTED".equals(waitlistRecord.waitlistStatus())) {
            quotaConcurrencyService.releaseQuota(
                    application.batchId(),
                    application.targetSchoolCode(),
                    application.targetMajorCode(),
                    operator,
                    action,
                    "Release reserved waitlist quota for application " + application.id()
            );
        }

        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update("""
                UPDATE waitlist_records
                SET waitlist_status = ?, expired_at = ?, confirm_deadline = NULL, updated_at = CURRENT_TIMESTAMP
                WHERE application_id = ?
                """,
                nextWaitlistStatus,
                "EXPIRED".equals(nextWaitlistStatus) ? Timestamp.valueOf(now) : null,
                application.id()
        );

        applicationStatusService.insertAuditLog(
                operator,
                "WAITLIST_RECORD",
                String.valueOf(waitlistRecord.id()),
                action,
                waitlistStateMap(waitlistRecord),
                waitlistAuditUpdate(
                        nextWaitlistStatus,
                        null,
                        "EXPIRED".equals(nextWaitlistStatus) ? now : null
                ),
                remark
        );

        applicationStatusService.transition(
                application.id(),
                application.status(),
                ApplicationStatus.CLOSED,
                operator,
                action,
                remark,
                applicationStateMap(application),
                Map.of("status", ApplicationStatus.CLOSED.name(), "close_reason", closeReason),
                """
                        UPDATE applications
                        SET current_status = ?, close_reason = ?, closed_at = ?, waitlist_confirm_deadline = NULL, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                ApplicationStatus.CLOSED.name(),
                closeReason,
                Timestamp.valueOf(now),
                application.id()
        );
    }

    private void assertCanViewWaitlist(AuthUserPrincipal currentUser, ApplicationRecord application) {
        if (currentUser.getRole() == UserRole.ADMIN) {
            return;
        }
        if (currentUser.getRole() == UserRole.SCHOOL_REVIEWER) {
            if (!Objects.equals(currentUser.getSchoolCode(), application.targetSchoolCode())) {
                throw new IllegalArgumentException("School reviewer can only view waitlists of their own school");
            }
            return;
        }
        if (currentUser.getRole() == UserRole.AGENT) {
            if (!Objects.equals(currentUser.getUserId(), application.createdByAgentId())) {
                throw new IllegalArgumentException("Agent can only view their own waitlist application");
            }
            return;
        }
        throw new IllegalArgumentException("Current role cannot view waitlist");
    }

    private void assertCanManageWaitlist(AuthUserPrincipal currentUser, ApplicationRecord application) {
        if (currentUser.getRole() == UserRole.ADMIN) {
            return;
        }
        if (currentUser.getRole() == UserRole.SCHOOL_REVIEWER
                && Objects.equals(currentUser.getSchoolCode(), application.targetSchoolCode())) {
            return;
        }
        throw new IllegalArgumentException("Only reviewer of the target school or admin can manage waitlist");
    }

    private AuthUserPrincipal requireCurrentUser() {
        Object principal = getContext().getAuthentication() == null ? null : getContext().getAuthentication().getPrincipal();
        if (principal instanceof AuthUserPrincipal authUserPrincipal) {
            return authUserPrincipal;
        }
        throw new IllegalArgumentException("Current user not found");
    }

    private AuthUserPrincipal systemOperator() {
        return new AuthUserPrincipal(AuthUser.builder()
                .id(null)
                .username("system_waitlist_scheduler")
                .passwordHash("")
                .role(UserRole.ADMIN)
                .schoolCode(null)
                .enabled(true)
                .mustChangePassword(false)
                .build());
    }

    private ApplicationRecord getApplication(Long applicationId) {
        return jdbcTemplate.query("""
                SELECT id, batch_id, target_school_code, target_major_code, created_by_agent_id,
                       current_status, waitlist_confirm_deadline
                FROM applications
                WHERE id = ?
                """, rs -> {
            if (!rs.next()) {
                throw new IllegalArgumentException("application not found: " + applicationId);
            }
            return new ApplicationRecord(
                    rs.getLong("id"),
                    rs.getLong("batch_id"),
                    rs.getString("target_school_code"),
                    rs.getString("target_major_code"),
                    rs.getLong("created_by_agent_id"),
                    ApplicationStatus.valueOf(rs.getString("current_status")),
                    toLocalDateTime(rs.getTimestamp("waitlist_confirm_deadline"))
            );
        }, applicationId);
    }

    private WaitlistRecord getWaitlistRecord(Long applicationId) {
        return jdbcTemplate.query("""
                SELECT id, waitlist_status, current_rank, rank_reason, total_score, key_subject_score,
                       application_submitted_at, promoted_at, confirm_deadline, expired_at
                FROM waitlist_records
                WHERE application_id = ?
                """, rs -> {
            if (!rs.next()) {
                throw new IllegalArgumentException("waitlist record not found for application: " + applicationId);
            }
            return new WaitlistRecord(
                    rs.getLong("id"),
                    rs.getString("waitlist_status"),
                    (Integer) rs.getObject("current_rank"),
                    rs.getString("rank_reason"),
                    rs.getBigDecimal("total_score"),
                    rs.getBigDecimal("key_subject_score"),
                    toLocalDateTime(rs.getTimestamp("application_submitted_at")),
                    toLocalDateTime(rs.getTimestamp("promoted_at")),
                    toLocalDateTime(rs.getTimestamp("confirm_deadline")),
                    toLocalDateTime(rs.getTimestamp("expired_at"))
            );
        }, applicationId);
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private String toString(LocalDateTime value) {
        return value == null ? null : value.toString();
    }

    private Map<String, Object> applicationStateMap(ApplicationRecord application) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", application.status().name());
        payload.put("batch_id", application.batchId());
        payload.put("target_school_code", application.targetSchoolCode());
        payload.put("target_major_code", application.targetMajorCode());
        payload.put("waitlist_confirm_deadline", toString(application.waitlistConfirmDeadline()));
        return payload;
    }

    private Map<String, Object> waitlistStateMap(WaitlistRecord waitlistRecord) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("waitlist_status", waitlistRecord.waitlistStatus());
        payload.put("current_rank", waitlistRecord.currentRank());
        payload.put("rank_reason", waitlistRecord.rankReason());
        payload.put("total_score", waitlistRecord.totalScore());
        payload.put("key_subject_score", waitlistRecord.keySubjectScore());
        payload.put("application_submitted_at", toString(waitlistRecord.applicationSubmittedAt()));
        payload.put("promoted_at", toString(waitlistRecord.promotedAt()));
        payload.put("confirm_deadline", toString(waitlistRecord.confirmDeadline()));
        payload.put("expired_at", toString(waitlistRecord.expiredAt()));
        return payload;
    }

    private Map<String, Object> waitlistAuditUpdate(String status, LocalDateTime confirmDeadline, LocalDateTime expiredAt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("waitlist_status", status);
        payload.put("confirm_deadline", toString(confirmDeadline));
        payload.put("expired_at", toString(expiredAt));
        return payload;
    }

    private record ApplicationRecord(
            Long id,
            Long batchId,
            String targetSchoolCode,
            String targetMajorCode,
            Long createdByAgentId,
            ApplicationStatus status,
            LocalDateTime waitlistConfirmDeadline
    ) {
    }

    private record WaitlistRecord(
            Long id,
            String waitlistStatus,
            Integer currentRank,
            String rankReason,
            java.math.BigDecimal totalScore,
            java.math.BigDecimal keySubjectScore,
            LocalDateTime applicationSubmittedAt,
            LocalDateTime promotedAt,
            LocalDateTime confirmDeadline,
            LocalDateTime expiredAt
    ) {
    }
}
