package com.example.admissionsystem.admin.service;

import com.example.admissionsystem.admin.dto.BatchSaveRequest;
import com.example.admissionsystem.admin.dto.MajorSaveRequest;
import com.example.admissionsystem.admin.dto.QuotaAdjustRequest;
import com.example.admissionsystem.admin.dto.SchoolSaveRequest;
import com.example.admissionsystem.admin.dto.StatusUpdateRequest;
import com.example.admissionsystem.admin.dto.UserSaveRequest;
import com.example.admissionsystem.admin.dto.UserUpdateRequest;
import com.example.admissionsystem.applications.service.ApplicationStatusService;
import com.example.admissionsystem.auth.model.UserRole;
import com.example.admissionsystem.auth.security.AuthUserPrincipal;
import com.example.admissionsystem.auth.security.PasswordHashService;
import com.example.admissionsystem.quotas.service.QuotaConcurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@Service
@RequiredArgsConstructor
public class AdminConfigService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JdbcTemplate jdbcTemplate;
    private final PasswordHashService passwordHashService;
    private final ApplicationStatusService applicationStatusService;
    private final QuotaConcurrencyService quotaConcurrencyService;

    public List<Map<String, Object>> listBatches() {
        requireAdmin();
        return jdbcTemplate.queryForList("""
                SELECT id, batch_name, start_time, end_time, batch_status, created_by, created_at, updated_at
                FROM admission_batches
                ORDER BY id DESC
                """);
    }

    @Transactional
    public Map<String, Object> createBatch(BatchSaveRequest request) {
        AuthUserPrincipal currentUser = requireAdmin();
        validateBatchRequest(request);
        jdbcTemplate.update("""
                INSERT INTO admission_batches(batch_name, start_time, end_time, batch_status, created_by)
                VALUES (?, ?, ?, ?, ?)
                """,
                request.getBatchName().trim(),
                Timestamp.valueOf(parseDateTime(request.getStartTime())),
                Timestamp.valueOf(parseDateTime(request.getEndTime())),
                request.getBatchStatus().trim(),
                currentUser.getUserId());
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        if (isPublishedBatchStatus(request.getBatchStatus())) {
            assertBatchQuotaConfigPublishable(id);
        }
        Map<String, Object> data = getBatch(id);
        applicationStatusService.insertAuditLog(currentUser, "BATCH", String.valueOf(id), "CREATE_BATCH",
                null, data, "Batch created");
        return data;
    }

    @Transactional
    public Map<String, Object> updateBatch(Long id, BatchSaveRequest request) {
        AuthUserPrincipal currentUser = requireAdmin();
        validateBatchRequest(request);
        if (isPublishedBatchStatus(request.getBatchStatus())) {
            assertBatchQuotaConfigPublishable(id);
        }
        Map<String, Object> oldValue = getBatch(id);
        jdbcTemplate.update("""
                UPDATE admission_batches
                SET batch_name = ?, start_time = ?, end_time = ?, batch_status = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                request.getBatchName().trim(),
                Timestamp.valueOf(parseDateTime(request.getStartTime())),
                Timestamp.valueOf(parseDateTime(request.getEndTime())),
                request.getBatchStatus().trim(),
                id);
        Map<String, Object> data = getBatch(id);
        applicationStatusService.insertAuditLog(currentUser, "BATCH", String.valueOf(id), "UPDATE_BATCH",
                oldValue, data, "Batch updated");
        return data;
    }

    @Transactional
    public Map<String, Object> updateBatchStatus(Long id, StatusUpdateRequest request) {
        AuthUserPrincipal currentUser = requireAdmin();
        if (request == null || request.getStatus() == null || request.getStatus().isBlank()) {
            throw new IllegalArgumentException("status is required");
        }
        if (isPublishedBatchStatus(request.getStatus())) {
            assertBatchQuotaConfigPublishable(id);
        }
        Map<String, Object> oldValue = getBatch(id);
        jdbcTemplate.update("""
                UPDATE admission_batches
                SET batch_status = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """, request.getStatus().trim(), id);
        Map<String, Object> data = getBatch(id);
        applicationStatusService.insertAuditLog(currentUser, "BATCH", String.valueOf(id), "UPDATE_BATCH_STATUS",
                oldValue, data, "Batch status updated");
        return data;
    }

    public List<Map<String, Object>> listSchools() {
        requireAdmin();
        return jdbcTemplate.queryForList("""
                SELECT school_code, school_name, is_enabled, created_at, updated_at
                FROM schools
                ORDER BY school_code
                """);
    }

    @Transactional
    public Map<String, Object> createSchool(SchoolSaveRequest request) {
        AuthUserPrincipal currentUser = requireAdmin();
        validateSchoolRequest(request, true);
        jdbcTemplate.update("""
                INSERT INTO schools(school_code, school_name, is_enabled)
                VALUES (?, ?, ?)
                """, request.getSchoolCode().trim(), request.getSchoolName().trim(), boolToInt(request.getEnabled(), true));
        Map<String, Object> data = getSchool(request.getSchoolCode().trim());
        applicationStatusService.insertAuditLog(currentUser, "SCHOOL", request.getSchoolCode().trim(), "CREATE_SCHOOL",
                null, data, "School created");
        return data;
    }

    @Transactional
    public Map<String, Object> updateSchool(String schoolCode, SchoolSaveRequest request) {
        AuthUserPrincipal currentUser = requireAdmin();
        validateSchoolRequest(request, false);
        Map<String, Object> oldValue = getSchool(schoolCode);
        jdbcTemplate.update("""
                UPDATE schools
                SET school_name = ?, is_enabled = ?, updated_at = CURRENT_TIMESTAMP
                WHERE school_code = ?
                """, request.getSchoolName().trim(), boolToInt(request.getEnabled(), currentEnabled(oldValue)), schoolCode);
        Map<String, Object> data = getSchool(schoolCode);
        applicationStatusService.insertAuditLog(currentUser, "SCHOOL", schoolCode, "UPDATE_SCHOOL",
                oldValue, data, "School updated");
        return data;
    }

    @Transactional
    public Map<String, Object> updateSchoolStatus(String schoolCode, StatusUpdateRequest request) {
        AuthUserPrincipal currentUser = requireAdmin();
        if (request == null || request.getStatus() == null || request.getStatus().isBlank()) {
            throw new IllegalArgumentException("status is required");
        }
        boolean enabled = parseEnabledStatus(request.getStatus());
        Map<String, Object> oldValue = getSchool(schoolCode);
        jdbcTemplate.update("""
                UPDATE schools
                SET is_enabled = ?, updated_at = CURRENT_TIMESTAMP
                WHERE school_code = ?
                """, enabled ? 1 : 0, schoolCode);
        Map<String, Object> data = getSchool(schoolCode);
        applicationStatusService.insertAuditLog(currentUser, "SCHOOL", schoolCode, "UPDATE_SCHOOL_STATUS",
                oldValue, data, "School status updated");
        return data;
    }

    public List<Map<String, Object>> listMajors(String schoolCode) {
        requireAdmin();
        if (schoolCode == null || schoolCode.isBlank()) {
            return jdbcTemplate.queryForList("""
                    SELECT major_code, school_code, major_name, min_average_score, min_math_score, min_english_score,
                           min_physics_score, min_liberal_arts_score, reserve_line, waitlist_line,
                           allow_adjustment_in, is_enabled, created_at, updated_at
                    FROM majors
                    ORDER BY school_code, major_code
                    """);
        }
        return jdbcTemplate.queryForList("""
                SELECT major_code, school_code, major_name, min_average_score, min_math_score, min_english_score,
                       min_physics_score, min_liberal_arts_score, reserve_line, waitlist_line,
                       allow_adjustment_in, is_enabled, created_at, updated_at
                FROM majors
                WHERE school_code = ?
                ORDER BY major_code
                """, schoolCode.trim());
    }

    @Transactional
    public Map<String, Object> createMajor(MajorSaveRequest request) {
        AuthUserPrincipal currentUser = requireAdmin();
        validateMajorRequest(request, true);
        requireExists("SELECT COUNT(*) FROM schools WHERE school_code = ?", request.getSchoolCode().trim(), "school not found");
        jdbcTemplate.update("""
                INSERT INTO majors(
                  major_code, school_code, major_name, min_average_score, min_math_score, min_english_score,
                  min_physics_score, min_liberal_arts_score, reserve_line, waitlist_line, allow_adjustment_in, is_enabled
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                request.getMajorCode().trim(),
                request.getSchoolCode().trim(),
                request.getMajorName().trim(),
                request.getMinAverageScore(),
                request.getMinMathScore(),
                request.getMinEnglishScore(),
                request.getMinPhysicsScore(),
                request.getMinLiberalArtsScore(),
                request.getReserveLine(),
                request.getWaitlistLine(),
                boolToInt(request.getAllowAdjustmentIn(), false),
                boolToInt(request.getEnabled(), true));
        Map<String, Object> data = getMajor(request.getMajorCode().trim());
        applicationStatusService.insertAuditLog(currentUser, "MAJOR", request.getMajorCode().trim(), "CREATE_MAJOR",
                null, data, "Major created");
        return data;
    }

    @Transactional
    public Map<String, Object> updateMajor(String majorCode, MajorSaveRequest request) {
        AuthUserPrincipal currentUser = requireAdmin();
        validateMajorRequest(request, false);
        Map<String, Object> oldValue = getMajor(majorCode);
        String schoolCode = request.getSchoolCode() == null || request.getSchoolCode().isBlank()
                ? (String) oldValue.get("school_code")
                : request.getSchoolCode().trim();
        requireExists("SELECT COUNT(*) FROM schools WHERE school_code = ?", schoolCode, "school not found");
        jdbcTemplate.update("""
                UPDATE majors
                SET school_code = ?, major_name = ?, min_average_score = ?, min_math_score = ?, min_english_score = ?,
                    min_physics_score = ?, min_liberal_arts_score = ?, reserve_line = ?, waitlist_line = ?,
                    allow_adjustment_in = ?, is_enabled = ?, updated_at = CURRENT_TIMESTAMP
                WHERE major_code = ?
                """,
                schoolCode,
                request.getMajorName().trim(),
                request.getMinAverageScore(),
                request.getMinMathScore(),
                request.getMinEnglishScore(),
                request.getMinPhysicsScore(),
                request.getMinLiberalArtsScore(),
                request.getReserveLine(),
                request.getWaitlistLine(),
                boolToInt(request.getAllowAdjustmentIn(), toBoolean(oldValue.get("allow_adjustment_in"))),
                boolToInt(request.getEnabled(), toBoolean(oldValue.get("is_enabled"))),
                majorCode);
        Map<String, Object> data = getMajor(majorCode);
        applicationStatusService.insertAuditLog(currentUser, "MAJOR", majorCode, "UPDATE_MAJOR",
                oldValue, data, "Major updated");
        return data;
    }

    @Transactional
    public Map<String, Object> updateMajorStatus(String majorCode, StatusUpdateRequest request) {
        AuthUserPrincipal currentUser = requireAdmin();
        if (request == null || request.getStatus() == null || request.getStatus().isBlank()) {
            throw new IllegalArgumentException("status is required");
        }
        boolean enabled = parseEnabledStatus(request.getStatus());
        Map<String, Object> oldValue = getMajor(majorCode);
        jdbcTemplate.update("""
                UPDATE majors
                SET is_enabled = ?, updated_at = CURRENT_TIMESTAMP
                WHERE major_code = ?
                """, enabled ? 1 : 0, majorCode);
        Map<String, Object> data = getMajor(majorCode);
        applicationStatusService.insertAuditLog(currentUser, "MAJOR", majorCode, "UPDATE_MAJOR_STATUS",
                oldValue, data, "Major status updated");
        return data;
    }

    public Map<String, Object> listQuotas(Long batchId, String schoolCode) {
        requireAdmin();
        Map<String, Object> data = new LinkedHashMap<>();
        if (batchId == null && (schoolCode == null || schoolCode.isBlank())) {
            data.put("schoolQuotas", jdbcTemplate.queryForList("""
                    SELECT id, batch_id, school_code, total_quota, used_quota, remaining_quota,
                           school_min_score, school_min_math, school_min_english, quota_version, updated_at
                    FROM school_quotas
                    ORDER BY batch_id DESC, school_code
                    """));
            data.put("majorQuotas", jdbcTemplate.queryForList("""
                    SELECT id, batch_id, school_code, major_code, total_quota, used_quota, remaining_quota,
                           min_average_score, min_math_score, min_english_score, min_physics_score, min_liberal_arts_score,
                           reserve_line, waitlist_line, allow_adjustment_in, quota_version, updated_at
                    FROM major_quotas
                    ORDER BY batch_id DESC, school_code, major_code
                    """));
            return data;
        }
        if (schoolCode == null || schoolCode.isBlank()) {
            data.put("schoolQuotas", jdbcTemplate.queryForList("""
                    SELECT id, batch_id, school_code, total_quota, used_quota, remaining_quota,
                           school_min_score, school_min_math, school_min_english, quota_version, updated_at
                    FROM school_quotas
                    WHERE batch_id = ?
                    ORDER BY school_code
                    """, batchId));
            data.put("majorQuotas", jdbcTemplate.queryForList("""
                    SELECT id, batch_id, school_code, major_code, total_quota, used_quota, remaining_quota,
                           min_average_score, min_math_score, min_english_score, min_physics_score, min_liberal_arts_score,
                           reserve_line, waitlist_line, allow_adjustment_in, quota_version, updated_at
                    FROM major_quotas
                    WHERE batch_id = ?
                    ORDER BY school_code, major_code
                    """, batchId));
            return data;
        }
        data.put("schoolQuotas", jdbcTemplate.queryForList("""
                SELECT id, batch_id, school_code, total_quota, used_quota, remaining_quota,
                       school_min_score, school_min_math, school_min_english, quota_version, updated_at
                FROM school_quotas
                WHERE (? IS NULL OR batch_id = ?) AND school_code = ?
                ORDER BY batch_id DESC
                """, batchId, batchId, schoolCode.trim()));
        data.put("majorQuotas", jdbcTemplate.queryForList("""
                SELECT id, batch_id, school_code, major_code, total_quota, used_quota, remaining_quota,
                       min_average_score, min_math_score, min_english_score, min_physics_score, min_liberal_arts_score,
                       reserve_line, waitlist_line, allow_adjustment_in, quota_version, updated_at
                FROM major_quotas
                WHERE (? IS NULL OR batch_id = ?) AND school_code = ?
                ORDER BY batch_id DESC, major_code
                """, batchId, batchId, schoolCode.trim()));
        return data;
    }

    public List<Map<String, Object>> listQuotaAdjustmentLogs(Long batchId, String schoolCode, String majorCode) {
        requireAdmin();
        StringBuilder sql = new StringBuilder("""
                SELECT id, operator_id, operator_role, entity_type, entity_id, operation_type, old_value, new_value, remark, created_at
                FROM audit_logs
                WHERE entity_type IN ('SCHOOL_QUOTA', 'MAJOR_QUOTA')
                """);
        Map<String, Object> params = new LinkedHashMap<>();
        if (schoolCode != null && !schoolCode.isBlank()) {
            sql.append(" AND JSON_EXTRACT(COALESCE(new_value, old_value), '$.school_code') = ? ");
            params.put("school_code", "\"" + schoolCode.trim() + "\"");
        }
        if (majorCode != null && !majorCode.isBlank()) {
            sql.append(" AND JSON_EXTRACT(COALESCE(new_value, old_value), '$.major_code') = ? ");
            params.put("major_code", "\"" + majorCode.trim() + "\"");
        }
        if (batchId != null) {
            sql.append(" AND JSON_EXTRACT(COALESCE(new_value, old_value), '$.batch_id') = ? ");
            params.put("batch_id", batchId);
        }
        sql.append(" ORDER BY id DESC LIMIT 200");
        return jdbcTemplate.queryForList(sql.toString(), params.values().toArray());
    }

    @Transactional
    public Map<String, Object> adjustSchoolQuota(Long quotaId, QuotaAdjustRequest request) {
        AuthUserPrincipal currentUser = requireAdmin();
        Map<String, Object> oldValue = getSchoolQuota(quotaId);
        int totalQuota = resolveTotalQuota(request, asInt(oldValue.get("total_quota")));
        assertQuotaConfigConsistentForPublishedBatch(
                asLong(oldValue.get("batch_id")),
                String.valueOf(oldValue.get("school_code")),
                totalQuota,
                null,
                quotaId
        );
        Map<String, Object> data = quotaConcurrencyService.adjustSchoolQuota(
                quotaId,
                totalQuota,
                request == null ? (java.math.BigDecimal) oldValue.get("school_min_score") : request.getSchoolMinScore(),
                request == null ? (java.math.BigDecimal) oldValue.get("school_min_math") : request.getSchoolMinMath(),
                request == null ? (java.math.BigDecimal) oldValue.get("school_min_english") : request.getSchoolMinEnglish()
        );
        applicationStatusService.insertAuditLog(currentUser, "SCHOOL_QUOTA", String.valueOf(quotaId), "ADJUST_SCHOOL_QUOTA",
                oldValue, data, request == null ? "School quota adjusted" : defaultRemark(request.getRemark(), "School quota adjusted"));
        return data;
    }

    @Transactional
    public Map<String, Object> adjustMajorQuota(Long quotaId, QuotaAdjustRequest request) {
        AuthUserPrincipal currentUser = requireAdmin();
        Map<String, Object> oldValue = getMajorQuota(quotaId);
        int totalQuota = resolveTotalQuota(request, asInt(oldValue.get("total_quota")));
        assertQuotaConfigConsistentForPublishedBatch(
                asLong(oldValue.get("batch_id")),
                String.valueOf(oldValue.get("school_code")),
                null,
                totalQuota,
                quotaId
        );
        Map<String, Object> data = quotaConcurrencyService.adjustMajorQuota(
                quotaId,
                totalQuota,
                request == null ? (java.math.BigDecimal) oldValue.get("min_average_score") : request.getMinAverageScore(),
                request == null ? (java.math.BigDecimal) oldValue.get("min_math_score") : request.getMinMathScore(),
                request == null ? (java.math.BigDecimal) oldValue.get("min_english_score") : request.getMinEnglishScore(),
                request == null ? (java.math.BigDecimal) oldValue.get("min_physics_score") : request.getMinPhysicsScore(),
                request == null ? (java.math.BigDecimal) oldValue.get("min_liberal_arts_score") : request.getMinLiberalArtsScore(),
                request == null ? (java.math.BigDecimal) oldValue.get("reserve_line") : request.getReserveLine(),
                request == null ? (java.math.BigDecimal) oldValue.get("waitlist_line") : request.getWaitlistLine(),
                request == null ? toBoolean(oldValue.get("allow_adjustment_in")) : Boolean.TRUE.equals(request.getAllowAdjustmentIn())
        );
        applicationStatusService.insertAuditLog(currentUser, "MAJOR_QUOTA", String.valueOf(quotaId), "ADJUST_MAJOR_QUOTA",
                oldValue, data, request == null ? "Major quota adjusted" : defaultRemark(request.getRemark(), "Major quota adjusted"));
        return data;
    }

    public List<Map<String, Object>> listUsers() {
        requireAdmin();
        return jdbcTemplate.queryForList("""
                SELECT id, username, role_type, school_code, is_enabled, must_change_password, last_login_at, created_at, updated_at
                FROM users
                ORDER BY id
                """);
    }

    @Transactional
    public Map<String, Object> createUser(UserSaveRequest request) {
        AuthUserPrincipal currentUser = requireAdmin();
        validateUserCreateRequest(request);
        requireUniqueUsername(request.getUsername().trim());
        validateUserSchoolBinding(request.getRoleType().trim(), request.getSchoolCode());
        jdbcTemplate.update("""
                INSERT INTO users(username, password_hash, role_type, school_code, is_enabled, must_change_password)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                request.getUsername().trim(),
                passwordHashService.hash(request.getPassword()),
                request.getRoleType().trim(),
                normalizeSchoolCode(request.getSchoolCode()),
                boolToInt(request.getEnabled(), true),
                boolToInt(request.getMustChangePassword(), false));
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        Map<String, Object> data = getUser(id);
        applicationStatusService.insertAuditLog(currentUser, "USER_ACCOUNT", String.valueOf(id), "CREATE_USER",
                null, data, "User created");
        return data;
    }

    @Transactional
    public Map<String, Object> updateUser(Long id, UserUpdateRequest request) {
        AuthUserPrincipal currentUser = requireAdmin();
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        Map<String, Object> oldValue = getUser(id);
        String roleType = request.getRoleType() == null || request.getRoleType().isBlank()
                ? String.valueOf(oldValue.get("role_type"))
                : request.getRoleType().trim();
        validateUserSchoolBinding(roleType, request.getSchoolCode() == null ? (String) oldValue.get("school_code") : request.getSchoolCode());
        String passwordHash = request.getPassword() == null || request.getPassword().isBlank()
                ? null
                : passwordHashService.hash(request.getPassword());
        jdbcTemplate.update("""
                UPDATE users
                SET password_hash = COALESCE(?, password_hash),
                    role_type = ?,
                    school_code = ?,
                    is_enabled = ?,
                    must_change_password = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                passwordHash,
                roleType,
                normalizeSchoolCode(request.getSchoolCode(), (String) oldValue.get("school_code"), roleType),
                boolToInt(request.getEnabled(), toBoolean(oldValue.get("is_enabled"))),
                boolToInt(request.getMustChangePassword(), toBoolean(oldValue.get("must_change_password"))),
                id);
        Map<String, Object> data = getUser(id);
        applicationStatusService.insertAuditLog(currentUser, "USER_ACCOUNT", String.valueOf(id), "UPDATE_USER",
                oldValue, data, "User updated");
        return data;
    }

    @Transactional
    public Map<String, Object> updateUserStatus(Long id, StatusUpdateRequest request) {
        AuthUserPrincipal currentUser = requireAdmin();
        if (request == null || request.getStatus() == null || request.getStatus().isBlank()) {
            throw new IllegalArgumentException("status is required");
        }
        boolean enabled = parseEnabledStatus(request.getStatus());
        Map<String, Object> oldValue = getUser(id);
        jdbcTemplate.update("""
                UPDATE users
                SET is_enabled = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """, enabled ? 1 : 0, id);
        Map<String, Object> data = getUser(id);
        applicationStatusService.insertAuditLog(currentUser, "USER_ACCOUNT", String.valueOf(id), "UPDATE_USER_STATUS",
                oldValue, data, "User status updated");
        return data;
    }

    public List<Map<String, Object>> listAuditLogs(String entityType, String entityId, Long operatorId, Integer limit) {
        requireAdmin();
        StringBuilder sql = new StringBuilder("""
                SELECT id, operator_id, operator_role, entity_type, entity_id, operation_type, old_value, new_value, remark, created_at
                FROM audit_logs
                WHERE 1 = 1
                """);
        Map<String, Object> params = new LinkedHashMap<>();
        if (entityType != null && !entityType.isBlank()) {
            sql.append(" AND entity_type = ? ");
            params.put("entityType", entityType.trim());
        }
        if (entityId != null && !entityId.isBlank()) {
            sql.append(" AND entity_id = ? ");
            params.put("entityId", entityId.trim());
        }
        if (operatorId != null) {
            sql.append(" AND operator_id = ? ");
            params.put("operatorId", operatorId);
        }
        sql.append(" ORDER BY id DESC LIMIT ? ");
        params.put("limit", resolveLimit(limit));
        return jdbcTemplate.queryForList(sql.toString(), params.values().toArray());
    }

    public List<Map<String, Object>> listStatusHistories(Long applicationId, Integer limit) {
        requireAdmin();
        if (applicationId == null) {
            throw new IllegalArgumentException("applicationId is required");
        }
        return jdbcTemplate.queryForList("""
                SELECT id, application_id, old_status, new_status, trigger_role, trigger_action, operator_id, operated_at, remark
                FROM application_status_histories
                WHERE application_id = ?
                ORDER BY id DESC
                LIMIT ?
                """, applicationId, resolveLimit(limit));
    }

    private Map<String, Object> getBatch(Long id) {
        return querySingle("""
                SELECT id, batch_name, start_time, end_time, batch_status, created_by, created_at, updated_at
                FROM admission_batches
                WHERE id = ?
                """, "batch not found: " + id, id);
    }

    private Map<String, Object> getSchool(String schoolCode) {
        return querySingle("""
                SELECT school_code, school_name, is_enabled, created_at, updated_at
                FROM schools
                WHERE school_code = ?
                """, "school not found: " + schoolCode, schoolCode);
    }

    private Map<String, Object> getMajor(String majorCode) {
        return querySingle("""
                SELECT major_code, school_code, major_name, min_average_score, min_math_score, min_english_score,
                       min_physics_score, min_liberal_arts_score, reserve_line, waitlist_line, allow_adjustment_in, is_enabled, created_at, updated_at
                FROM majors
                WHERE major_code = ?
                """, "major not found: " + majorCode, majorCode);
    }

    private Map<String, Object> getSchoolQuota(Long id) {
        return querySingle("""
                SELECT id, batch_id, school_code, total_quota, used_quota, remaining_quota,
                       school_min_score, school_min_math, school_min_english, quota_version, updated_at
                FROM school_quotas
                WHERE id = ?
                """, "school quota not found: " + id, id);
    }

    private Map<String, Object> getMajorQuota(Long id) {
        return querySingle("""
                SELECT id, batch_id, school_code, major_code, total_quota, used_quota, remaining_quota,
                       min_average_score, min_math_score, min_english_score, min_physics_score, min_liberal_arts_score,
                       reserve_line, waitlist_line, allow_adjustment_in, quota_version, updated_at
                FROM major_quotas
                WHERE id = ?
                """, "major quota not found: " + id, id);
    }

    private Map<String, Object> getUser(Long id) {
        return querySingle("""
                SELECT id, username, role_type, school_code, is_enabled, must_change_password, last_login_at, created_at, updated_at
                FROM users
                WHERE id = ?
                """, "user not found: " + id, id);
    }

    private Map<String, Object> querySingle(String sql, String notFoundMessage, Object... args) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, args);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException(notFoundMessage);
        }
        return rows.get(0);
    }

    private AuthUserPrincipal requireAdmin() {
        Object principal = getContext().getAuthentication() == null ? null : getContext().getAuthentication().getPrincipal();
        if (principal instanceof AuthUserPrincipal authUserPrincipal && authUserPrincipal.getRole() == UserRole.ADMIN) {
            return authUserPrincipal;
        }
        throw new IllegalArgumentException("Only ADMIN can operate admin configuration");
    }

    private void validateBatchRequest(BatchSaveRequest request) {
        if (request == null || request.getBatchName() == null || request.getBatchName().isBlank()) {
            throw new IllegalArgumentException("batchName is required");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new IllegalArgumentException("startTime and endTime are required");
        }
        if (request.getBatchStatus() == null || request.getBatchStatus().isBlank()) {
            throw new IllegalArgumentException("batchStatus is required");
        }
        LocalDateTime start = parseDateTime(request.getStartTime());
        LocalDateTime end = parseDateTime(request.getEndTime());
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }
    }

    private void validateSchoolRequest(SchoolSaveRequest request, boolean creating) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        if (creating && (request.getSchoolCode() == null || request.getSchoolCode().isBlank())) {
            throw new IllegalArgumentException("schoolCode is required");
        }
        if (request.getSchoolName() == null || request.getSchoolName().isBlank()) {
            throw new IllegalArgumentException("schoolName is required");
        }
    }

    private void validateMajorRequest(MajorSaveRequest request, boolean creating) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        if (creating && (request.getMajorCode() == null || request.getMajorCode().isBlank())) {
            throw new IllegalArgumentException("majorCode is required");
        }
        if ((creating || request.getSchoolCode() != null) && (request.getSchoolCode() == null || request.getSchoolCode().isBlank())) {
            throw new IllegalArgumentException("schoolCode is required");
        }
        if (request.getMajorName() == null || request.getMajorName().isBlank()) {
            throw new IllegalArgumentException("majorName is required");
        }
    }

    private void validateUserCreateRequest(UserSaveRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("username is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("password is required");
        }
        if (request.getRoleType() == null || request.getRoleType().isBlank()) {
            throw new IllegalArgumentException("roleType is required");
        }
    }

    private void requireUniqueUsername(String username) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE username = ?", Integer.class, username);
        if (count != null && count > 0) {
            throw new IllegalArgumentException("username already exists");
        }
    }

    private void validateUserSchoolBinding(String roleType, String schoolCode) {
        UserRole role = UserRole.valueOf(roleType.trim());
        if (role == UserRole.SCHOOL_REVIEWER) {
            if (schoolCode == null || schoolCode.isBlank()) {
                throw new IllegalArgumentException("schoolCode is required for SCHOOL_REVIEWER");
            }
            requireExists("SELECT COUNT(*) FROM schools WHERE school_code = ?", schoolCode.trim(), "school not found");
            return;
        }
        if (schoolCode != null && !schoolCode.isBlank()) {
            throw new IllegalArgumentException("schoolCode is only allowed for SCHOOL_REVIEWER");
        }
    }

    private void requireExists(String sql, Object arg, String message) {
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, arg);
        if (count == null || count == 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private int resolveTotalQuota(QuotaAdjustRequest request, int currentTotalQuota) {
        if (request == null) {
            return currentTotalQuota;
        }
        if (request.getTotalQuota() != null) {
            return request.getTotalQuota();
        }
        if (request.getDeltaQuota() != null) {
            return currentTotalQuota + request.getDeltaQuota();
        }
        return currentTotalQuota;
    }

    private int resolveLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 100;
        }
        return Math.min(limit, 500);
    }

    private void assertBatchQuotaConfigPublishable(Long batchId) {
        String sql = """
                SELECT sq.batch_id, sq.school_code, sq.total_quota,
                       COALESCE(SUM(mq.total_quota), 0) AS major_total
                FROM school_quotas sq
                LEFT JOIN major_quotas mq
                  ON mq.batch_id = sq.batch_id AND mq.school_code = sq.school_code
                WHERE (? IS NULL OR sq.batch_id = ?)
                GROUP BY sq.batch_id, sq.school_code, sq.total_quota
                HAVING sq.total_quota <> COALESCE(SUM(mq.total_quota), 0)
                LIMIT 1
                """;
        List<Map<String, Object>> invalidRows = jdbcTemplate.queryForList(sql, batchId, batchId);
        if (!invalidRows.isEmpty()) {
            Map<String, Object> invalidRow = invalidRows.get(0);
            throw new IllegalStateException("Cannot publish quota config when school quota and major quota totals are inconsistent: batchId="
                    + invalidRow.get("batch_id") + ", schoolCode=" + invalidRow.get("school_code"));
        }
    }

    private void assertQuotaConfigConsistentForPublishedBatch(
            Long batchId,
            String schoolCode,
            Integer nextSchoolTotalQuota,
            Integer nextMajorTotalQuota,
            Long quotaId
    ) {
        String batchStatus = jdbcTemplate.query("""
                SELECT batch_status
                FROM admission_batches
                WHERE id = ?
                """, rs -> rs.next() ? rs.getString("batch_status") : null, batchId);
        if (!isPublishedBatchStatus(batchStatus)) {
            return;
        }

        int schoolTotalQuota = nextSchoolTotalQuota != null
                ? nextSchoolTotalQuota
                : jdbcTemplate.queryForObject("""
                        SELECT total_quota
                        FROM school_quotas
                        WHERE batch_id = ? AND school_code = ?
                        """, Integer.class, batchId, schoolCode);

        int majorTotalQuotaSum = jdbcTemplate.queryForObject("""
                SELECT COALESCE(SUM(CASE WHEN id = ? AND ? IS NOT NULL THEN ? ELSE total_quota END), 0)
                FROM major_quotas
                WHERE batch_id = ? AND school_code = ?
                """, Integer.class, quotaId, nextMajorTotalQuota, nextMajorTotalQuota, batchId, schoolCode);

        if (schoolTotalQuota != majorTotalQuotaSum) {
            throw new IllegalStateException("Published batch quota config must keep school total quota equal to sum of major quotas");
        }
    }

    private boolean isPublishedBatchStatus(String status) {
        if (status == null || status.isBlank()) {
            return false;
        }
        String normalized = status.trim().toUpperCase();
        return "IN_PROGRESS".equals(normalized)
                || "ACTIVE".equals(normalized)
                || "OPEN".equals(normalized)
                || "ENABLED".equals(normalized);
    }

    private int boolToInt(Boolean value, boolean defaultValue) {
        return Boolean.TRUE.equals(value == null ? defaultValue : value) ? 1 : 0;
    }

    private boolean parseEnabledStatus(String status) {
        String normalized = status.trim().toUpperCase();
        return switch (normalized) {
            case "ENABLED", "ENABLE", "OPEN", "ACTIVE", "IN_PROGRESS", "TRUE", "1" -> true;
            case "DISABLED", "DISABLE", "CLOSE", "CLOSED", "INACTIVE", "FALSE", "0", "FINISHED" -> false;
            default -> throw new IllegalArgumentException("unsupported status value: " + status);
        };
    }

    private LocalDateTime parseDateTime(String value) {
        try {
            return LocalDateTime.parse(value.trim(), DATE_TIME_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("datetime must use format yyyy-MM-dd HH:mm:ss");
        }
    }

    private boolean currentEnabled(Map<String, Object> row) {
        return toBoolean(row.get("is_enabled"));
    }

    private boolean toBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private int asInt(Object value) {
        return value instanceof Number number ? number.intValue() : Integer.parseInt(String.valueOf(value));
    }

    private long asLong(Object value) {
        return value instanceof Number number ? number.longValue() : Long.parseLong(String.valueOf(value));
    }

    private String normalizeSchoolCode(String schoolCode) {
        return normalizeSchoolCode(schoolCode, null, null);
    }

    private String normalizeSchoolCode(String schoolCode, String fallback, String roleType) {
        String effectiveRole = roleType == null ? null : roleType.trim();
        if (effectiveRole == null || effectiveRole.isBlank()) {
            return schoolCode == null || schoolCode.isBlank() ? fallback : schoolCode.trim();
        }
        if (UserRole.valueOf(effectiveRole) != UserRole.SCHOOL_REVIEWER) {
            return null;
        }
        return schoolCode == null || schoolCode.isBlank() ? fallback : schoolCode.trim();
    }

    private String defaultRemark(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
