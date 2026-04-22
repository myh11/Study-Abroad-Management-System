package com.example.admissionsystem.applications.service;

import com.example.admissionsystem.applications.model.ApplicationStatus;
import com.example.admissionsystem.auth.model.UserRole;
import com.example.admissionsystem.auth.security.AuthUserPrincipal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ApplicationStatusService {

    public static final String ACTION_SUBMIT_APPLICATION = "SUBMIT_APPLICATION";
    public static final String ACTION_CREATE_DRAFT = "CREATE_DRAFT";
    public static final String ACTION_CANCEL_APPLICATION = "CANCEL_APPLICATION";
    public static final String ACTION_WAITLIST_CONFIRM = "WAITLIST_CONFIRM";
    public static final String ACTION_ACCEPT_ADJUSTMENT = "ACCEPT_ADJUSTMENT";
    public static final String ACTION_REJECT_ADJUSTMENT = "REJECT_ADJUSTMENT";
    public static final String ACTION_CLOSE_APPLICATION = "CLOSE_APPLICATION";
    public static final String ACTION_CLAIM_DOMESTIC_REVIEW = "CLAIM_DOMESTIC_REVIEW";
    public static final String ACTION_SUBMIT_DOMESTIC_REVIEW = "SUBMIT_DOMESTIC_REVIEW";
    public static final String ACTION_SUBMIT_SCHOOL_REVIEW = "SUBMIT_SCHOOL_REVIEW";
    public static final String ACTION_GENERATE_ADJUSTMENT_DRAFT = "GENERATE_ADJUSTMENT_DRAFT";
    public static final String ACTION_PROMOTE_WAITLIST = "PROMOTE_WAITLIST";
    public static final String ACTION_INVALIDATE_WAITLIST = "INVALIDATE_WAITLIST";
    public static final String ACTION_WAITLIST_TIMEOUT = "WAITLIST_TIMEOUT";
    public static final String ACTION_BATCH_FINISH_WAITLIST_CLOSE = "BATCH_FINISH_WAITLIST_CLOSE";

    private final ApplicationStatusFlowValidator applicationStatusFlowValidator;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public void assertCanTransfer(ApplicationStatus from, ApplicationStatus to) {
        applicationStatusFlowValidator.assertCanTransfer(from, to);
    }

    public void assertCanOperate(String action, AuthUserPrincipal currentUser) {
        Set<UserRole> allowedRoles = switch (action) {
            case ACTION_CREATE_DRAFT,
                 ACTION_SUBMIT_APPLICATION,
                 ACTION_CANCEL_APPLICATION,
                 ACTION_WAITLIST_CONFIRM,
                 ACTION_ACCEPT_ADJUSTMENT,
                 ACTION_REJECT_ADJUSTMENT -> Set.of(UserRole.AGENT, UserRole.ADMIN);
            case ACTION_CLOSE_APPLICATION -> Set.of(UserRole.ADMIN);
            case ACTION_CLAIM_DOMESTIC_REVIEW,
                 ACTION_SUBMIT_DOMESTIC_REVIEW -> Set.of(UserRole.DOMESTIC_REVIEWER);
            case ACTION_SUBMIT_SCHOOL_REVIEW,
                 ACTION_GENERATE_ADJUSTMENT_DRAFT,
                 ACTION_PROMOTE_WAITLIST,
                 ACTION_INVALIDATE_WAITLIST -> Set.of(UserRole.SCHOOL_REVIEWER, UserRole.ADMIN);
            case ACTION_WAITLIST_TIMEOUT,
                 ACTION_BATCH_FINISH_WAITLIST_CLOSE -> Set.of(UserRole.ADMIN);
            default -> throw new IllegalArgumentException("Unsupported status action: " + action);
        };
        if (!allowedRoles.contains(currentUser.getRole())) {
            throw new IllegalArgumentException("Current user role is not allowed to trigger action: " + action);
        }
    }

    public void transition(
            Long applicationId,
            ApplicationStatus from,
            ApplicationStatus to,
            AuthUserPrincipal currentUser,
            String triggerAction,
            String remark,
            Map<String, Object> oldValue,
            Map<String, Object> newValue,
            String updateSql,
            Object... updateArgs
    ) {
        assertCanOperate(triggerAction, currentUser);
        assertCanTransfer(from, to);
        jdbcTemplate.update(updateSql, updateArgs);
        insertStatusHistory(applicationId, from, to, currentUser, triggerAction, remark);
        insertAuditLog(currentUser, "APPLICATION", String.valueOf(applicationId), triggerAction, oldValue, newValue, remark);
    }

    public void recordInitialStatus(
            Long applicationId,
            ApplicationStatus initialStatus,
            AuthUserPrincipal currentUser,
            String triggerAction,
            String remark,
            Map<String, Object> newValue
    ) {
        assertCanOperate(triggerAction, currentUser);
        insertStatusHistory(applicationId, null, initialStatus, currentUser, triggerAction, remark);
        insertAuditLog(currentUser, "APPLICATION", String.valueOf(applicationId), triggerAction, null, newValue, remark);
    }

    public void insertAuditLog(
            AuthUserPrincipal currentUser,
            String entityType,
            String entityId,
            String operationType,
            Map<String, Object> oldValue,
            Map<String, Object> newValue,
            String remark
    ) {
        jdbcTemplate.update("""
                INSERT INTO audit_logs(operator_id, operator_role, entity_type, entity_id, operation_type, old_value, new_value, remark)
                VALUES (?, ?, ?, ?, ?, CAST(? AS JSON), CAST(? AS JSON), ?)
                """,
                currentUser.getUserId(),
                currentUser.getRole().name(),
                entityType,
                entityId,
                operationType,
                toJson(oldValue),
                toJson(newValue),
                remark);
    }

    private void insertStatusHistory(
            Long applicationId,
            ApplicationStatus oldStatus,
            ApplicationStatus newStatus,
            AuthUserPrincipal currentUser,
            String triggerAction,
            String remark
    ) {
        jdbcTemplate.update("""
                INSERT INTO application_status_histories(
                  application_id, old_status, new_status, trigger_role, trigger_action, operator_id, operated_at, remark
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                applicationId,
                oldStatus == null ? null : oldStatus.name(),
                newStatus.name(),
                currentUser.getRole().name(),
                triggerAction,
                currentUser.getUserId(),
                Timestamp.valueOf(LocalDateTime.now()),
                remark);
    }

    private String toJson(Map<String, Object> value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize audit payload", e);
        }
    }
}
