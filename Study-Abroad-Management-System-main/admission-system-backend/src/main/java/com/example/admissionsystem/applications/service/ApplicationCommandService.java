package com.example.admissionsystem.applications.service;

import com.example.admissionsystem.applications.dto.AcceptAdjustmentRequest;
import com.example.admissionsystem.applications.dto.CancelApplicationRequest;
import com.example.admissionsystem.applications.dto.WaitlistConfirmRequest;
import com.example.admissionsystem.applications.model.ApplicationStatus;
import com.example.admissionsystem.auth.model.UserRole;
import com.example.admissionsystem.auth.security.AuthUserPrincipal;
import com.example.admissionsystem.quotas.service.QuotaConcurrencyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@Service
@RequiredArgsConstructor
public class ApplicationCommandService {

    private final ApplicationStatusService applicationStatusService;
    private final QuotaConcurrencyService quotaConcurrencyService;
    private final ApplicationConstraintService applicationConstraintService;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public String submit(Long applicationId) {
        AuthUserPrincipal currentUser = requireAgentOrAdmin();
        applicationStatusService.assertCanOperate(ApplicationStatusService.ACTION_SUBMIT_APPLICATION, currentUser);
        ApplicationRecord application = getApplication(applicationId);
        assertAgentOwnsApplication(currentUser, application);

        if (application.status() != ApplicationStatus.DRAFT && application.status() != ApplicationStatus.DOMESTIC_SUPPLEMENT) {
            throw new IllegalArgumentException("Only DRAFT or DOMESTIC_SUPPLEMENT application can be submitted");
        }

        assertApplicationConstraints(application);
        Long ruleSnapshotId = upsertRuleSnapshot(application);
        transitionApplication(
                application.id(),
                application.status(),
                ApplicationStatus.SUBMITTED,
                currentUser,
                ApplicationStatusService.ACTION_SUBMIT_APPLICATION,
                "Application submitted",
                """
                        UPDATE applications
                        SET current_status = ?, submit_time = ?, rule_snapshot_id = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                ApplicationStatus.SUBMITTED.name(),
                Timestamp.valueOf(LocalDateTime.now()),
                ruleSnapshotId,
                application.id()
        );
        return "ok";
    }

    @Transactional
    public String cancel(Long applicationId, CancelApplicationRequest request) {
        AuthUserPrincipal currentUser = requireAgentOrAdmin();
        applicationStatusService.assertCanOperate(ApplicationStatusService.ACTION_CANCEL_APPLICATION, currentUser);
        ApplicationRecord application = getApplication(applicationId);
        assertAgentOwnsApplication(currentUser, application);

        applicationStatusService.assertCanTransfer(application.status(), ApplicationStatus.CANCELED);
        String reason = normalizeReason(request == null ? null : request.getReason(), "USER_CANCELED");

        if (application.status() == ApplicationStatus.WAITLISTED) {
            jdbcTemplate.update("""
                    UPDATE waitlist_records
                    SET waitlist_status = 'CLOSED', updated_at = CURRENT_TIMESTAMP
                    WHERE application_id = ?
                    """, application.id());
            findWaitlistRecord(application.id()).ifPresent(record -> insertAuditLog(
                    currentUser,
                    "WAITLIST_RECORD",
                    String.valueOf(record.id()),
                    ApplicationStatusService.ACTION_CANCEL_APPLICATION,
                    waitlistStateMap(record),
                    Map.of("waitlist_status", "CLOSED"),
                    "Waitlist record closed because application was canceled"
            ));
        }

        applicationStatusService.transition(
                application.id(),
                application.status(),
                ApplicationStatus.CANCELED,
                currentUser,
                ApplicationStatusService.ACTION_CANCEL_APPLICATION,
                "Application canceled: " + reason,
                applicationStateMap(application),
                Map.of("status", ApplicationStatus.CANCELED.name(), "cancel_reason", reason),
                """
                        UPDATE applications
                        SET current_status = ?, cancel_reason = ?, canceled_at = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                ApplicationStatus.CANCELED.name(), reason, Timestamp.valueOf(LocalDateTime.now()), application.id()
        );
        return "ok";
    }

    @Transactional
    public String waitlistConfirm(Long applicationId, WaitlistConfirmRequest request) {
        AuthUserPrincipal currentUser = requireAgentOrAdmin();
        applicationStatusService.assertCanOperate(ApplicationStatusService.ACTION_WAITLIST_CONFIRM, currentUser);
        ApplicationRecord application = getApplication(applicationId);
        assertAgentOwnsApplication(currentUser, application);
        if (request == null || request.getAccept() == null) {
            throw new IllegalArgumentException("accept is required");
        }
        if (application.status() != ApplicationStatus.WAITLIST_PENDING_CONFIRM) {
            throw new IllegalArgumentException("Only WAITLIST_PENDING_CONFIRM application can be confirmed");
        }

        WaitlistRecord waitlistRecord = getWaitlistRecord(application.id());
        if (!"PROMOTED".equals(waitlistRecord.status())) {
            throw new IllegalArgumentException("Only PROMOTED waitlist record can be confirmed");
        }
        if (Boolean.TRUE.equals(request.getAccept())) {
            LocalDateTime now = LocalDateTime.now();
            jdbcTemplate.update("""
                    UPDATE waitlist_records
                    SET waitlist_status = 'CLOSED', confirm_deadline = NULL, updated_at = CURRENT_TIMESTAMP
                    WHERE application_id = ?
                    """, application.id());

            applicationStatusService.insertAuditLog(currentUser, "WAITLIST_RECORD", String.valueOf(waitlistRecord.id()),
                    ApplicationStatusService.ACTION_WAITLIST_CONFIRM, waitlistStateMap(waitlistRecord),
                    waitlistAuditUpdate("CLOSED", null, null), "Waitlist record finalized after acceptance");
            applicationStatusService.transition(
                    application.id(),
                    application.status(),
                    ApplicationStatus.RESERVED,
                    currentUser,
                    ApplicationStatusService.ACTION_WAITLIST_CONFIRM,
                    "Agent accepted waitlist offer",
                    applicationStateMap(application),
                    Map.of("status", ApplicationStatus.RESERVED.name()),
                    """
                            UPDATE applications
                            SET current_status = ?, reserved_at = ?, waitlist_confirm_deadline = NULL, updated_at = CURRENT_TIMESTAMP
                            WHERE id = ?
                            """,
                    ApplicationStatus.RESERVED.name(), Timestamp.valueOf(now), application.id()
            );
            return "ok";
        }

        LocalDateTime now = LocalDateTime.now();
        quotaConcurrencyService.releaseQuota(
                application.batchId(),
                application.targetSchoolCode(),
                application.targetMajorCode(),
                currentUser,
                ApplicationStatusService.ACTION_WAITLIST_CONFIRM,
                "Release waitlist seat after decline for application " + application.id()
        );
        jdbcTemplate.update("""
                UPDATE waitlist_records
                SET waitlist_status = 'CLOSED', confirm_deadline = NULL, updated_at = CURRENT_TIMESTAMP
                WHERE application_id = ?
                """, application.id());

        applicationStatusService.insertAuditLog(currentUser, "WAITLIST_RECORD", String.valueOf(waitlistRecord.id()),
                ApplicationStatusService.ACTION_WAITLIST_CONFIRM, waitlistStateMap(waitlistRecord),
                waitlistAuditUpdate("CLOSED", null, null), "Waitlist record closed after decline");
        applicationStatusService.transition(
                application.id(),
                application.status(),
                ApplicationStatus.CLOSED,
                currentUser,
                ApplicationStatusService.ACTION_WAITLIST_CONFIRM,
                "Agent declined waitlist offer",
                applicationStateMap(application),
                Map.of("status", ApplicationStatus.CLOSED.name(), "close_reason", "WAITLIST_DECLINED"),
                """
                        UPDATE applications
                        SET current_status = ?, close_reason = ?, closed_at = ?, waitlist_confirm_deadline = NULL, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                ApplicationStatus.CLOSED.name(), "WAITLIST_DECLINED", Timestamp.valueOf(now), application.id()
        );
        return "ok";
    }

    @Transactional
    public String acceptAdjustment(Long applicationId, AcceptAdjustmentRequest request) {
        AuthUserPrincipal currentUser = requireAgentOrAdmin();
        applicationStatusService.assertCanOperate(ApplicationStatusService.ACTION_ACCEPT_ADJUSTMENT, currentUser);
        ApplicationRecord application = getApplication(applicationId);
        assertAgentOwnsApplication(currentUser, application);

        if (application.sourceApplicationId() == null) {
            throw new IllegalArgumentException("Only adjustment draft can be accepted");
        }
        if (application.status() != ApplicationStatus.DRAFT) {
            throw new IllegalArgumentException("Only DRAFT adjustment application can be accepted");
        }

        String targetMajorCode = resolveAdjustmentTargetMajor(application, request);
        validateAdjustmentTargetMajor(application.targetSchoolCode(), targetMajorCode);
        quotaConcurrencyService.lockQuotaSnapshotForAdjustment(
                application.batchId(),
                application.targetSchoolCode(),
                targetMajorCode
        );
        assertApplicationConstraints(application.withTargetMajorCode(targetMajorCode));
        Long ruleSnapshotId = upsertRuleSnapshot(application.withTargetMajorCode(targetMajorCode));

        applicationStatusService.transition(
                application.id(),
                ApplicationStatus.DRAFT,
                ApplicationStatus.SUBMITTED,
                currentUser,
                ApplicationStatusService.ACTION_ACCEPT_ADJUSTMENT,
                "Adjustment draft accepted and submitted",
                applicationStateMap(application),
                Map.of(
                        "status", ApplicationStatus.SUBMITTED.name(),
                        "target_major_code", targetMajorCode,
                        "source_application_id", application.sourceApplicationId()
                ),
                """
                        UPDATE applications
                        SET target_major_code = ?, current_status = ?, submit_time = ?, rule_snapshot_id = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                targetMajorCode, ApplicationStatus.SUBMITTED.name(), Timestamp.valueOf(LocalDateTime.now()), ruleSnapshotId, application.id()
        );
        return "ok";
    }

    @Transactional
    public String rejectAdjustment(Long applicationId) {
        AuthUserPrincipal currentUser = requireAgentOrAdmin();
        applicationStatusService.assertCanOperate(ApplicationStatusService.ACTION_REJECT_ADJUSTMENT, currentUser);
        ApplicationRecord application = getApplication(applicationId);
        assertAgentOwnsApplication(currentUser, application);

        if (application.status() == ApplicationStatus.ADJUSTMENT_SUGGESTED) {
            applicationStatusService.transition(
                    application.id(),
                    ApplicationStatus.ADJUSTMENT_SUGGESTED,
                    ApplicationStatus.CLOSED,
                    currentUser,
                    ApplicationStatusService.ACTION_REJECT_ADJUSTMENT,
                    "Adjustment suggestion rejected",
                    applicationStateMap(application),
                    Map.of("status", ApplicationStatus.CLOSED.name(), "close_reason", "ADJUSTMENT_REJECTED"),
                    """
                            UPDATE applications
                            SET current_status = ?, close_reason = ?, closed_at = ?, updated_at = CURRENT_TIMESTAMP
                            WHERE id = ?
                            """,
                    ApplicationStatus.CLOSED.name(), "ADJUSTMENT_REJECTED", Timestamp.valueOf(LocalDateTime.now()), application.id()
            );
            return "ok";
        }

        if (application.sourceApplicationId() == null || application.status() != ApplicationStatus.DRAFT) {
            throw new IllegalArgumentException("Only adjustment suggestion or adjustment draft can be rejected");
        }

        applicationStatusService.transition(
                application.id(),
                ApplicationStatus.DRAFT,
                ApplicationStatus.CLOSED,
                currentUser,
                ApplicationStatusService.ACTION_REJECT_ADJUSTMENT,
                "Adjustment draft rejected before submission",
                applicationStateMap(application),
                Map.of(
                        "status", ApplicationStatus.CLOSED.name(),
                        "close_reason", "ADJUSTMENT_REJECTED",
                        "source_application_id", application.sourceApplicationId()
                ),
                """
                        UPDATE applications
                        SET current_status = ?, close_reason = ?, closed_at = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                ApplicationStatus.CLOSED.name(), "ADJUSTMENT_REJECTED", Timestamp.valueOf(LocalDateTime.now()), application.id()
        );
        return "ok";
    }

    @Transactional
    public String close(Long applicationId) {
        AuthUserPrincipal currentUser = requireAdmin();
        applicationStatusService.assertCanOperate(ApplicationStatusService.ACTION_CLOSE_APPLICATION, currentUser);
        ApplicationRecord application = getApplication(applicationId);
        ApplicationStatus targetStatus = ApplicationStatus.CLOSED;

        String closeReason = resolveCloseReason(application);
        LocalDateTime now = LocalDateTime.now();

        if (application.status() == ApplicationStatus.WAITLIST_PENDING_CONFIRM) {
            WaitlistRecord waitlistRecord = getWaitlistRecord(application.id());
            if ("PROMOTED".equals(waitlistRecord.status())) {
                quotaConcurrencyService.releaseQuota(
                        application.batchId(),
                        application.targetSchoolCode(),
                        application.targetMajorCode(),
                        currentUser,
                        ApplicationStatusService.ACTION_CLOSE_APPLICATION,
                        "Release waitlist seat while closing application " + application.id()
                );
            }
            jdbcTemplate.update("""
                    UPDATE waitlist_records
                    SET waitlist_status = 'EXPIRED', expired_at = COALESCE(expired_at, ?), confirm_deadline = NULL, updated_at = CURRENT_TIMESTAMP
                    WHERE application_id = ?
                    """, Timestamp.valueOf(now), application.id());
            Optional<WaitlistRecord> waitlistRecordForAudit = findWaitlistRecord(application.id());
            waitlistRecordForAudit.ifPresent(record -> applicationStatusService.insertAuditLog(
                    currentUser,
                    "WAITLIST_RECORD",
                    String.valueOf(record.id()),
                    ApplicationStatusService.ACTION_CLOSE_APPLICATION,
                    waitlistStateMap(record),
                    Map.of("waitlist_status", "EXPIRED"),
                    "Waitlist record expired when application was closed"
            ));
        } else if (application.status() == ApplicationStatus.WAITLISTED) {
            jdbcTemplate.update("""
                    UPDATE waitlist_records
                    SET waitlist_status = 'CLOSED', updated_at = CURRENT_TIMESTAMP
                    WHERE application_id = ?
                    """, application.id());
        }

        applicationStatusService.transition(
                application.id(),
                application.status(),
                targetStatus,
                currentUser,
                ApplicationStatusService.ACTION_CLOSE_APPLICATION,
                "Application closed by admin: " + closeReason,
                applicationStateMap(application),
                Map.of("status", targetStatus.name(), "close_reason", closeReason),
                """
                        UPDATE applications
                        SET current_status = ?, close_reason = ?, closed_at = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                targetStatus.name(), closeReason, Timestamp.valueOf(now), application.id()
        );
        return "ok";
    }

    private AuthUserPrincipal requireAgentOrAdmin() {
        AuthUserPrincipal currentUser = requireCurrentUser();
        if (currentUser.getRole() != UserRole.AGENT && currentUser.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Only AGENT or ADMIN can operate application commands");
        }
        return currentUser;
    }

    private AuthUserPrincipal requireAdmin() {
        AuthUserPrincipal currentUser = requireCurrentUser();
        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Only ADMIN can close application");
        }
        return currentUser;
    }

    private AuthUserPrincipal requireCurrentUser() {
        Object principal = getContext().getAuthentication() == null ? null : getContext().getAuthentication().getPrincipal();
        if (principal instanceof AuthUserPrincipal authUserPrincipal) {
            return authUserPrincipal;
        }
        throw new IllegalArgumentException("Current user not found");
    }

    private void assertAgentOwnsApplication(AuthUserPrincipal currentUser, ApplicationRecord application) {
        if (currentUser.getRole() == UserRole.ADMIN) {
            return;
        }
        if (!Objects.equals(application.createdByAgentId(), currentUser.getUserId())) {
            throw new IllegalArgumentException("Agent can only operate their own application");
        }
    }

    private ApplicationRecord getApplication(Long applicationId) {
        return jdbcTemplate.query("""
                SELECT id, student_id, transcript_id, personal_statement_id, batch_id,
                       target_school_code, target_major_code, source_application_id,
                       created_by_agent_id, current_status, rule_snapshot_id, waitlist_confirm_deadline
                FROM applications
                WHERE id = ?
                """, rs -> {
            if (!rs.next()) {
                throw new IllegalArgumentException("application not found: " + applicationId);
            }
            return new ApplicationRecord(
                    rs.getLong("id"),
                    rs.getLong("student_id"),
                    rs.getLong("transcript_id"),
                    rs.getLong("personal_statement_id"),
                    rs.getLong("batch_id"),
                    rs.getString("target_school_code"),
                    rs.getString("target_major_code"),
                    (Long) rs.getObject("source_application_id"),
                    rs.getLong("created_by_agent_id"),
                    ApplicationStatus.valueOf(rs.getString("current_status")),
                    (Long) rs.getObject("rule_snapshot_id"),
                    rs.getTimestamp("waitlist_confirm_deadline") == null
                            ? null
                            : rs.getTimestamp("waitlist_confirm_deadline").toLocalDateTime()
            );
        }, applicationId);
    }

    private Optional<WaitlistRecord> findWaitlistRecord(Long applicationId) {
        return jdbcTemplate.query("""
                SELECT id, waitlist_status, promoted_at, confirm_deadline, expired_at
                FROM waitlist_records
                WHERE application_id = ?
                """, rs -> {
            if (!rs.next()) {
                return Optional.empty();
            }
            return Optional.of(new WaitlistRecord(
                    rs.getLong("id"),
                    rs.getString("waitlist_status"),
                    toLocalDateTime(rs.getTimestamp("promoted_at")),
                    toLocalDateTime(rs.getTimestamp("confirm_deadline")),
                    toLocalDateTime(rs.getTimestamp("expired_at"))
            ));
        }, applicationId);
    }

    private WaitlistRecord getWaitlistRecord(Long applicationId) {
        return findWaitlistRecord(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("waitlist record not found for application: " + applicationId));
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private void assertApplicationConstraints(ApplicationRecord application) {
        ApplicationConstraintService.ApplicantIdentity applicantIdentity = applicationConstraintService.loadApplicantIdentity(application.id());
        applicationConstraintService.assertNoOtherActiveApplication(application.id(), applicantIdentity);
        applicationConstraintService.assertNotCanceledInSameBatchAndSchool(
                application.id(),
                applicantIdentity,
                application.batchId(),
                application.targetSchoolCode()
        );
    }

    private Long upsertRuleSnapshot(ApplicationRecord application) {
        Map<String, Object> schoolQuota = jdbcTemplate.query("""
                SELECT total_quota, used_quota, remaining_quota, school_min_score, school_min_math, school_min_english, quota_version
                FROM school_quotas
                WHERE batch_id = ? AND school_code = ?
                """, rs -> {
            if (!rs.next()) {
                throw new IllegalArgumentException("school quota not found");
            }
            Map<String, Object> values = new LinkedHashMap<>();
            values.put("school_code", application.targetSchoolCode());
            values.put("total_quota", rs.getInt("total_quota"));
            values.put("used_quota", rs.getInt("used_quota"));
            values.put("remaining_quota", rs.getInt("remaining_quota"));
            values.put("school_min_score", rs.getBigDecimal("school_min_score"));
            values.put("school_min_math", rs.getBigDecimal("school_min_math"));
            values.put("school_min_english", rs.getBigDecimal("school_min_english"));
            values.put("quota_version", rs.getInt("quota_version"));
            return values;
        }, application.batchId(), application.targetSchoolCode());

        Map<String, Object> majorQuota = jdbcTemplate.query("""
                SELECT major_code, min_average_score, min_math_score, min_english_score, min_physics_score,
                       min_liberal_arts_score, reserve_line, waitlist_line, allow_adjustment_in, quota_version
                FROM major_quotas
                WHERE batch_id = ? AND major_code = ?
                """, rs -> {
            if (!rs.next()) {
                throw new IllegalArgumentException("major quota not found");
            }
            Map<String, Object> values = new LinkedHashMap<>();
            values.put("major_code", rs.getString("major_code"));
            values.put("min_average_score", rs.getBigDecimal("min_average_score"));
            values.put("min_math_score", rs.getBigDecimal("min_math_score"));
            values.put("min_english_score", rs.getBigDecimal("min_english_score"));
            values.put("min_physics_score", rs.getBigDecimal("min_physics_score"));
            values.put("min_liberal_arts_score", rs.getBigDecimal("min_liberal_arts_score"));
            values.put("reserve_line", rs.getBigDecimal("reserve_line"));
            values.put("waitlist_line", rs.getBigDecimal("waitlist_line"));
            values.put("allow_adjustment_in", rs.getBoolean("allow_adjustment_in"));
            values.put("quota_version", rs.getInt("quota_version"));
            return values;
        }, application.batchId(), application.targetMajorCode());

        Integer existingId = jdbcTemplate.query("""
                SELECT id
                FROM rule_snapshots
                WHERE application_id = ?
                """, rs -> rs.next() ? rs.getInt("id") : null, application.id());

        int schoolQuotaVersion = (Integer) schoolQuota.get("quota_version");
        int majorQuotaVersion = (Integer) majorQuota.get("quota_version");
        if (existingId == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement("""
                        INSERT INTO rule_snapshots(
                          application_id, batch_id, school_threshold_snapshot, major_threshold_snapshot,
                          reserve_line_snapshot, waitlist_line_snapshot, quota_rule_version,
                          adjustment_rule_version, waitlist_sort_rule_version
                        ) VALUES (?, ?, CAST(? AS JSON), CAST(? AS JSON), ?, ?, ?, ?, ?)
                        """, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, application.id());
                ps.setLong(2, application.batchId());
                ps.setString(3, json(schoolQuota));
                ps.setString(4, json(majorQuota));
                ps.setObject(5, majorQuota.get("reserve_line"));
                ps.setObject(6, majorQuota.get("waitlist_line"));
                ps.setInt(7, Math.max(schoolQuotaVersion, majorQuotaVersion));
                ps.setInt(8, Boolean.TRUE.equals(majorQuota.get("allow_adjustment_in")) ? 1 : 0);
                ps.setInt(9, 1);
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key == null) {
                throw new IllegalStateException("Failed to create rule snapshot");
            }
            return key.longValue();
        }

        jdbcTemplate.update("""
                UPDATE rule_snapshots
                SET batch_id = ?,
                    school_threshold_snapshot = CAST(? AS JSON),
                    major_threshold_snapshot = CAST(? AS JSON),
                    reserve_line_snapshot = ?,
                    waitlist_line_snapshot = ?,
                    quota_rule_version = ?,
                    adjustment_rule_version = ?,
                    waitlist_sort_rule_version = ?
                WHERE id = ?
                """, application.batchId(), json(schoolQuota), json(majorQuota),
                majorQuota.get("reserve_line"), majorQuota.get("waitlist_line"),
                Math.max(schoolQuotaVersion, majorQuotaVersion),
                Boolean.TRUE.equals(majorQuota.get("allow_adjustment_in")) ? 1 : 0,
                1,
                existingId);
        return existingId.longValue();
    }

    private String resolveAdjustmentTargetMajor(ApplicationRecord application, AcceptAdjustmentRequest request) {
        if (request != null && request.getTargetMajorCode() != null && !request.getTargetMajorCode().isBlank()) {
            return request.getTargetMajorCode().trim();
        }
        return application.targetMajorCode();
    }

    private void validateAdjustmentTargetMajor(String schoolCode, String targetMajorCode) {
        Map<String, Object> major = jdbcTemplate.query("""
                SELECT school_code, allow_adjustment_in, is_enabled
                FROM majors
                WHERE major_code = ?
                """, rs -> {
            if (!rs.next()) {
                throw new IllegalArgumentException("target major not found: " + targetMajorCode);
            }
            Map<String, Object> values = new LinkedHashMap<>();
            values.put("school_code", rs.getString("school_code"));
            values.put("allow_adjustment_in", rs.getBoolean("allow_adjustment_in"));
            values.put("is_enabled", rs.getBoolean("is_enabled"));
            return values;
        }, targetMajorCode);

        if (!Objects.equals(schoolCode, major.get("school_code"))) {
            throw new IllegalArgumentException("Adjusted major must belong to the same school");
        }
        if (!Boolean.TRUE.equals(major.get("is_enabled"))) {
            throw new IllegalArgumentException("Adjusted major is disabled");
        }
        if (!Boolean.TRUE.equals(major.get("allow_adjustment_in"))) {
            throw new IllegalArgumentException("Adjusted major does not allow adjustment in");
        }
    }

    private String resolveCloseReason(ApplicationRecord application) {
        if (application.status() == ApplicationStatus.WAITLIST_PENDING_CONFIRM) {
            return "WAITLIST_CONFIRM_TIMEOUT";
        }
        if (application.status() == ApplicationStatus.DOMESTIC_REJECTED) {
            return "DOMESTIC_REJECTED_TERMINAL";
        }
        if (application.status() == ApplicationStatus.SCHOOL_REJECTED) {
            return "SCHOOL_REJECTED_TERMINAL";
        }
        if (application.status() == ApplicationStatus.CANCELED) {
            return "CANCELED_TERMINAL";
        }
        if (application.status() == ApplicationStatus.RESERVED) {
            return "RESERVED_TERMINAL";
        }
        if (application.status() == ApplicationStatus.ADJUSTMENT_SUGGESTED) {
            return "ADJUSTMENT_EXPIRED";
        }
        return "ADMIN_MANUAL_CLOSE";
    }

    private void transitionApplication(
            Long applicationId,
            ApplicationStatus from,
            ApplicationStatus to,
            AuthUserPrincipal currentUser,
            String operationType,
            String remark,
            String updateSql,
            Object... args
    ) {
        applicationStatusService.transition(
                applicationId,
                from,
                to,
                currentUser,
                operationType,
                remark,
                Map.of("status", from.name()),
                Map.of("status", to.name()),
                updateSql,
                args
        );
    }

    private void insertAuditLog(
            AuthUserPrincipal currentUser,
            String entityType,
            String entityId,
            String operationType,
            Map<String, Object> oldValue,
            Map<String, Object> newValue,
            String remark
    ) {
        applicationStatusService.insertAuditLog(currentUser, entityType, entityId, operationType, oldValue, newValue, remark);
    }

    private Map<String, Object> applicationStateMap(ApplicationRecord application) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", application.status().name());
        payload.put("target_school_code", application.targetSchoolCode());
        payload.put("target_major_code", application.targetMajorCode());
        payload.put("source_application_id", application.sourceApplicationId());
        payload.put("rule_snapshot_id", application.ruleSnapshotId());
        payload.put("waitlist_confirm_deadline", application.waitlistConfirmDeadline() == null
                ? null
                : application.waitlistConfirmDeadline().toString());
        return payload;
    }

    private Map<String, Object> waitlistStateMap(WaitlistRecord waitlistRecord) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("waitlist_status", waitlistRecord.status());
        payload.put("promoted_at", waitlistRecord.promotedAt() == null ? null : waitlistRecord.promotedAt().toString());
        payload.put("confirm_deadline", waitlistRecord.confirmDeadline() == null ? null : waitlistRecord.confirmDeadline().toString());
        payload.put("expired_at", waitlistRecord.expiredAt() == null ? null : waitlistRecord.expiredAt().toString());
        return payload;
    }

    private Map<String, Object> waitlistAuditUpdate(String status, LocalDateTime confirmDeadline, LocalDateTime expiredAt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("waitlist_status", status);
        payload.put("confirm_deadline", confirmDeadline == null ? null : confirmDeadline.toString());
        payload.put("expired_at", expiredAt == null ? null : expiredAt.toString());
        return payload;
    }

    private String normalizeReason(String reason, String defaultReason) {
        if (reason == null || reason.isBlank()) {
            return defaultReason;
        }
        return reason.trim();
    }

    private String json(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize rule snapshot payload", e);
        }
    }

    private record ApplicationRecord(
            Long id,
            Long studentId,
            Long transcriptId,
            Long personalStatementId,
            Long batchId,
            String targetSchoolCode,
            String targetMajorCode,
            Long sourceApplicationId,
            Long createdByAgentId,
            ApplicationStatus status,
            Long ruleSnapshotId,
            LocalDateTime waitlistConfirmDeadline
    ) {
        private ApplicationRecord withTargetMajorCode(String newMajorCode) {
            return new ApplicationRecord(
                    id,
                    studentId,
                    transcriptId,
                    personalStatementId,
                    batchId,
                    targetSchoolCode,
                    newMajorCode,
                    sourceApplicationId,
                    createdByAgentId,
                    status,
                    ruleSnapshotId,
                    waitlistConfirmDeadline
            );
        }
    }

    private record WaitlistRecord(
            Long id,
            String status,
            LocalDateTime promotedAt,
            LocalDateTime confirmDeadline,
            LocalDateTime expiredAt
    ) {
    }
}
