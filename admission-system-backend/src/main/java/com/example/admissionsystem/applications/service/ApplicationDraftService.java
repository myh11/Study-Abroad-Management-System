package com.example.admissionsystem.applications.service;

import com.example.admissionsystem.applications.dto.ApplicationDraftRequest;
import com.example.admissionsystem.applications.dto.ApplicationSupplementRequest;
import com.example.admissionsystem.applications.model.ApplicationStatus;
import com.example.admissionsystem.applications.vo.ApplicationDetailVO;
import com.example.admissionsystem.applications.vo.ApplicationDraftVO;
import com.example.admissionsystem.applications.vo.ApplicationListItemVO;
import com.example.admissionsystem.applications.vo.ApplicationPageVO;
import com.example.admissionsystem.auth.model.UserRole;
import com.example.admissionsystem.auth.security.AuthUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@Service
@RequiredArgsConstructor
public class ApplicationDraftService {

    private final JdbcTemplate jdbcTemplate;
    private final ApplicationConstraintService applicationConstraintService;
    private final ApplicationStatusService applicationStatusService;

    @Transactional
    public ApplicationDraftVO createDraft(ApplicationDraftRequest request) {
        validateDraftRequest(request);
        AuthUserPrincipal currentUser = requireCurrentUser();
        ensureAgentAccess(currentUser);
        ensureBatchAndTargetExists(request.getBatchId(), request.getTargetSchoolCode(), request.getTargetMajorCode());
        ApplicationConstraintService.ApplicantIdentity applicantIdentity = applicationConstraintService.buildApplicantIdentity(
                null,
                request.getStudent().getIdCardNo(),
                request.getStudent().getEmail(),
                request.getStudent().getPhone()
        );
        assertApplicationConstraints(null, applicantIdentity, request.getBatchId(), request.getTargetSchoolCode());

        Long studentId = insertStudent(request.getStudent());
        Long transcriptId = insertTranscript(studentId, request.getScore());
        Long statementId = insertPersonalStatement(studentId, request.getPersonalStatement());
        Long applicationId = insertApplication(studentId, transcriptId, statementId, request, currentUser.getUserId());
        applicationStatusService.recordInitialStatus(
                applicationId,
                ApplicationStatus.DRAFT,
                currentUser,
                ApplicationStatusService.ACTION_CREATE_DRAFT,
                "Draft application created",
                orderedMap(
                        "status", ApplicationStatus.DRAFT.name(),
                        "batch_id", request.getBatchId(),
                        "target_school_code", request.getTargetSchoolCode(),
                        "target_major_code", request.getTargetMajorCode()
                )
        );

        return ApplicationDraftVO.builder()
                .applicationId(applicationId)
                .status(ApplicationStatus.DRAFT)
                .batchId(request.getBatchId())
                .targetSchoolCode(request.getTargetSchoolCode())
                .targetMajorCode(request.getTargetMajorCode())
                .build();
    }

    @Transactional
    public ApplicationDraftVO updateDraft(Long applicationId, ApplicationDraftRequest request) {
        validateDraftRequest(request);
        AuthUserPrincipal currentUser = requireCurrentUser();
        ensureAgentAccess(currentUser);
        ApplicationRecord application = getApplicationRecord(applicationId);
        ensureApplicationReadable(application, currentUser);
        if (application.status() != ApplicationStatus.DRAFT) {
            throw new IllegalArgumentException("Only DRAFT application can be updated");
        }
        ensureBatchAndTargetExists(request.getBatchId(), request.getTargetSchoolCode(), request.getTargetMajorCode());
        ApplicationConstraintService.ApplicantIdentity applicantIdentity = applicationConstraintService.buildApplicantIdentity(
                application.studentId(),
                request.getStudent().getIdCardNo(),
                request.getStudent().getEmail(),
                request.getStudent().getPhone()
        );
        assertApplicationConstraints(applicationId, applicantIdentity, request.getBatchId(), request.getTargetSchoolCode());

        updateStudent(application.studentId(), request.getStudent());
        updateTranscript(application.transcriptId(), request.getScore());
        updatePersonalStatement(application.personalStatementId(), request.getPersonalStatement());
        updateApplicationDraft(applicationId, request);

        return ApplicationDraftVO.builder()
                .applicationId(applicationId)
                .status(ApplicationStatus.DRAFT)
                .batchId(request.getBatchId())
                .targetSchoolCode(request.getTargetSchoolCode())
                .targetMajorCode(request.getTargetMajorCode())
                .build();
    }

    public ApplicationPageVO myApplications(Integer page, Integer pageSize, String status, Long batchId) {
        AuthUserPrincipal currentUser = requireCurrentUser();
        ensureAgentAccess(currentUser);
        int safePage = page != null && page > 0 ? page : 1;
        int safePageSize = pageSize != null && pageSize > 0 ? pageSize : 10;
        int offset = (safePage - 1) * safePageSize;

        StringBuilder baseSql = new StringBuilder("""
                FROM applications a
                JOIN students s ON s.id = a.student_id
                WHERE a.created_by_agent_id = ?
                """);
        List<Object> params = new ArrayList<>();
        params.add(currentUser.getUserId());
        if (status != null && !status.isBlank()) {
            baseSql.append(" AND a.current_status = ?");
            params.add(status);
        }
        if (batchId != null) {
            baseSql.append(" AND a.batch_id = ?");
            params.add(batchId);
        }

        Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) " + baseSql, Long.class, params.toArray());
        String querySql = """
                SELECT a.id, s.name AS student_name, a.target_school_code, a.target_major_code,
                       a.current_status, a.batch_id, a.updated_at
                """ + baseSql + """
                ORDER BY a.updated_at DESC, a.id DESC
                LIMIT ? OFFSET ?
                """;
        params.add(safePageSize);
        params.add(offset);
        List<ApplicationListItemVO> list = jdbcTemplate.query(querySql, (rs, rowNum) -> ApplicationListItemVO.builder()
                .applicationId(rs.getLong("id"))
                .studentName(rs.getString("student_name"))
                .targetSchoolCode(rs.getString("target_school_code"))
                .targetMajorCode(rs.getString("target_major_code"))
                .status(ApplicationStatus.valueOf(rs.getString("current_status")))
                .batchId(rs.getLong("batch_id"))
                .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime().toString())
                .build(), params.toArray());

        return ApplicationPageVO.builder()
                .list(list)
                .page(safePage)
                .pageSize(safePageSize)
                .total(total == null ? 0L : total)
                .build();
    }

    public ApplicationDetailVO detail(Long applicationId) {
        AuthUserPrincipal currentUser = requireCurrentUser();
        ApplicationRecord application = getApplicationRecord(applicationId);
        ensureApplicationReadable(application, currentUser);
        boolean canViewSensitiveFields = canViewSensitiveFields(application, currentUser);

        Map<String, Object> detail = jdbcTemplate.queryForMap("""
                SELECT a.id AS application_id, a.current_status, a.batch_id, a.target_school_code, a.target_major_code,
                       a.source_application_id, a.submit_time, a.cancel_reason, a.close_reason,
                       a.waitlist_confirm_deadline, a.reserved_at, a.canceled_at, a.closed_at, a.created_by_agent_id,
                       s.id AS student_id, s.name, s.gender, s.birth_date, s.current_school, s.grade, s.email, s.phone, s.id_card_no,
                       t.id AS transcript_id, t.transcript_school_name, t.term_start, t.term_end, t.chinese_score, t.math_score,
                       t.english_score, t.physics_score, t.chemistry_score, t.history_score, t.average_score, t.failed_subject_count,
                       t.file_id, t.authenticity_risk_level, t.has_stamp_region, t.clarity_level,
                       ps.id AS personal_statement_id, ps.content, ps.word_count, ps.quality_score
                FROM applications a
                JOIN students s ON s.id = a.student_id
                JOIN transcripts t ON t.id = a.transcript_id
                JOIN personal_statements ps ON ps.id = a.personal_statement_id
                WHERE a.id = ?
                """, applicationId);

        Map<String, Object> basicInfo = orderedMap(
                "applicationId", detail.get("application_id"),
                "status", detail.get("current_status"),
                "createdByAgentId", detail.get("created_by_agent_id"),
                "submitTime", detail.get("submit_time"),
                "cancelReason", detail.get("cancel_reason"),
                "closeReason", detail.get("close_reason")
        );
        Map<String, Object> studentInfo = orderedMap(
                "studentId", detail.get("student_id"),
                "name", detail.get("name"),
                "gender", detail.get("gender"),
                "birthDate", detail.get("birth_date"),
                "currentSchool", detail.get("current_school"),
                "grade", detail.get("grade"),
                "email", maskIfNeeded(detail.get("email"), canViewSensitiveFields, this::maskEmail),
                "phone", maskIfNeeded(detail.get("phone"), canViewSensitiveFields, this::maskPhone),
                "idCardNo", maskIfNeeded(detail.get("id_card_no"), canViewSensitiveFields, this::maskIdCardNo)
        );
        Map<String, Object> applicationInfo = orderedMap(
                "batchId", detail.get("batch_id"),
                "targetSchoolCode", detail.get("target_school_code"),
                "targetMajorCode", detail.get("target_major_code"),
                "sourceApplicationId", detail.get("source_application_id"),
                "waitlistConfirmDeadline", detail.get("waitlist_confirm_deadline"),
                "reservedAt", detail.get("reserved_at"),
                "canceledAt", detail.get("canceled_at"),
                "closedAt", detail.get("closed_at")
        );
        Map<String, Object> scoreSummary = orderedMap(
                "transcriptId", detail.get("transcript_id"),
                "transcriptSchoolName", detail.get("transcript_school_name"),
                "termStart", detail.get("term_start"),
                "termEnd", detail.get("term_end"),
                "chinese", maskIfNeeded(detail.get("chinese_score"), canViewSensitiveFields, this::maskScore),
                "math", maskIfNeeded(detail.get("math_score"), canViewSensitiveFields, this::maskScore),
                "english", maskIfNeeded(detail.get("english_score"), canViewSensitiveFields, this::maskScore),
                "physics", maskIfNeeded(detail.get("physics_score"), canViewSensitiveFields, this::maskScore),
                "chemistry", maskIfNeeded(detail.get("chemistry_score"), canViewSensitiveFields, this::maskScore),
                "history", maskIfNeeded(detail.get("history_score"), canViewSensitiveFields, this::maskScore),
                "averageScore", maskIfNeeded(detail.get("average_score"), canViewSensitiveFields, this::maskScore),
                "failedSubjectCount", detail.get("failed_subject_count"),
                "authenticityRiskLevel", detail.get("authenticity_risk_level"),
                "hasStampRegion", detail.get("has_stamp_region"),
                "clarityLevel", detail.get("clarity_level")
        );
        Map<String, Object> files = orderedMap("transcriptFileId", detail.get("file_id"));
        Map<String, Object> domesticReviewSummary = querySingleMap("""
                SELECT id, review_result, review_comment, authenticity_risk_level, reviewed_at
                FROM domestic_reviews
                WHERE application_id = ?
                ORDER BY reviewed_at DESC, id DESC
                LIMIT 1
                """, applicationId).orElseGet(Map::of);
        Map<String, Object> schoolReviewSummary = querySingleMap("""
                SELECT id, review_result, review_reason, suggested_major_code, total_score, reviewed_at
                FROM school_reviews
                WHERE application_id = ?
                ORDER BY reviewed_at DESC, id DESC
                LIMIT 1
                """, applicationId).orElseGet(Map::of);
        Map<String, Object> adjustmentSummary = orderedMap(
                "personalStatementId", detail.get("personal_statement_id"),
                "content", detail.get("content"),
                "wordCount", detail.get("word_count"),
                "qualityScore", detail.get("quality_score")
        );
        Map<String, Object> ruleSnapshot = querySingleMap("""
                SELECT id, school_threshold_snapshot, major_threshold_snapshot, reserve_line_snapshot,
                       waitlist_line_snapshot, quota_rule_version, adjustment_rule_version, waitlist_sort_rule_version
                FROM rule_snapshots
                WHERE application_id = ?
                """, applicationId).orElseGet(Map::of);
        List<Map<String, Object>> statusHistory = jdbcTemplate.query("""
                SELECT id, old_status, new_status, trigger_role, trigger_action, operator_id, operated_at, remark
                FROM application_status_histories
                WHERE application_id = ?
                ORDER BY operated_at DESC, id DESC
                """, (rs, rowNum) -> orderedMap(
                "id", rs.getLong("id"),
                "oldStatus", rs.getString("old_status"),
                "newStatus", rs.getString("new_status"),
                "triggerRole", rs.getString("trigger_role"),
                "triggerAction", rs.getString("trigger_action"),
                "operatorId", rs.getObject("operator_id"),
                "operatedAt", rs.getTimestamp("operated_at"),
                "remark", rs.getString("remark")
        ), applicationId);
        List<Map<String, Object>> auditLogs = jdbcTemplate.query("""
                SELECT id, operator_id, operator_role, entity_type, entity_id, operation_type, old_value, new_value, remark, created_at
                FROM audit_logs
                WHERE (entity_type = 'APPLICATION' AND entity_id = ?)
                   OR (JSON_UNQUOTE(JSON_EXTRACT(new_value, '$.application_id')) = ?)
                ORDER BY created_at DESC, id DESC
                """, (rs, rowNum) -> orderedMap(
                "id", rs.getLong("id"),
                "operatorId", rs.getObject("operator_id"),
                "operatorRole", rs.getString("operator_role"),
                "entityType", rs.getString("entity_type"),
                "entityId", rs.getString("entity_id"),
                "operationType", rs.getString("operation_type"),
                "oldValue", rs.getString("old_value"),
                "newValue", rs.getString("new_value"),
                "remark", rs.getString("remark"),
                "createdAt", rs.getTimestamp("created_at")
        ), String.valueOf(applicationId), String.valueOf(applicationId));

        return ApplicationDetailVO.builder()
                .basicInfo(basicInfo)
                .studentInfo(studentInfo)
                .applicationInfo(applicationInfo)
                .scoreSummary(scoreSummary)
                .files(files)
                .domesticReviewSummary(domesticReviewSummary)
                .schoolReviewSummary(schoolReviewSummary)
                .adjustmentSummary(adjustmentSummary)
                .ruleSnapshot(ruleSnapshot)
                .statusHistory(statusHistory)
                .auditLogs(auditLogs)
                .build();
    }

    @Transactional
    public ApplicationDraftVO supplement(Long applicationId, ApplicationSupplementRequest request) {
        AuthUserPrincipal currentUser = requireCurrentUser();
        ensureAgentAccess(currentUser);
        ApplicationRecord application = getApplicationRecord(applicationId);
        ensureApplicationReadable(application, currentUser);
        if (application.status() != ApplicationStatus.DOMESTIC_SUPPLEMENT) {
            throw new IllegalArgumentException("Only DOMESTIC_SUPPLEMENT application can submit supplement");
        }
        if (request != null && request.getNewTranscriptFileId() != null) {
            jdbcTemplate.update("UPDATE transcripts SET file_id = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                    request.getNewTranscriptFileId(), application.transcriptId());
        }
        return ApplicationDraftVO.builder()
                .applicationId(applicationId)
                .status(ApplicationStatus.DOMESTIC_SUPPLEMENT)
                .batchId(application.batchId())
                .targetSchoolCode(application.targetSchoolCode())
                .targetMajorCode(application.targetMajorCode())
                .build();
    }

    private void validateDraftRequest(ApplicationDraftRequest request) {
        if (request == null || request.getStudent() == null || request.getScore() == null || request.getPersonalStatement() == null) {
            throw new IllegalArgumentException("student, score and personalStatement are required");
        }
        if (request.getBatchId() == null || request.getTargetSchoolCode() == null || request.getTargetMajorCode() == null) {
            throw new IllegalArgumentException("batchId, targetSchoolCode and targetMajorCode are required");
        }
        if (request.getScore().getFileId() == null) {
            throw new IllegalArgumentException("score.fileId is required");
        }
    }

    private void ensureBatchAndTargetExists(Long batchId, String schoolCode, String majorCode) {
        requireExists("SELECT COUNT(*) FROM admission_batches WHERE id = ?", batchId, "batch not found: " + batchId);
        requireExists("SELECT COUNT(*) FROM schools WHERE school_code = ?", schoolCode, "school not found: " + schoolCode);
        requireExists("SELECT COUNT(*) FROM majors WHERE major_code = ? AND school_code = ?", new Object[]{majorCode, schoolCode},
                "major not found under school: " + schoolCode + "/" + majorCode);
    }

    private void ensureAgentAccess(AuthUserPrincipal principal) {
        if (principal.getRole() != UserRole.AGENT && principal.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Only AGENT or ADMIN can access draft APIs");
        }
    }

    private Long insertStudent(ApplicationDraftRequest.StudentDraftDTO student) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO students(name, gender, birth_date, current_school, grade, email, phone, id_card_no)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, student.getName());
            ps.setString(2, student.getGender());
            ps.setObject(3, parseDate(student.getBirthDate()));
            ps.setString(4, student.getCurrentSchool());
            ps.setString(5, student.getGrade());
            ps.setString(6, student.getEmail());
            ps.setString(7, student.getPhone());
            ps.setString(8, student.getIdCardNo());
            return ps;
        }, keyHolder);
        return requireGeneratedId(keyHolder, "student");
    }

    private Long insertTranscript(Long studentId, ApplicationDraftRequest.ScoreDraftDTO score) {
        requireExists("SELECT COUNT(*) FROM files WHERE id = ?", score.getFileId(), "file not found: " + score.getFileId());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO transcripts(student_id, transcript_school_name, term_start, term_end,
                    chinese_score, math_score, english_score, physics_score, chemistry_score, history_score,
                    average_score, failed_subject_count, file_id, authenticity_risk_level, has_stamp_region, clarity_level)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, studentId);
            ps.setString(2, score.getTranscriptSchoolName());
            ps.setObject(3, parseDate(score.getTermStart()));
            ps.setObject(4, parseDate(score.getTermEnd()));
            ps.setObject(5, score.getChinese());
            ps.setObject(6, score.getMath());
            ps.setObject(7, score.getEnglish());
            ps.setObject(8, score.getPhysics());
            ps.setObject(9, score.getChemistry());
            ps.setObject(10, score.getHistory());
            ps.setObject(11, score.getAverageScore());
            ps.setObject(12, score.getFailedSubjectCount());
            ps.setLong(13, score.getFileId());
            ps.setString(14, null);
            ps.setObject(15, null);
            ps.setString(16, null);
            return ps;
        }, keyHolder);
        return requireGeneratedId(keyHolder, "transcript");
    }

    private Long insertPersonalStatement(Long studentId, ApplicationDraftRequest.PersonalStatementDraftDTO personalStatement) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String content = Objects.requireNonNullElse(personalStatement.getContent(), "");
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO personal_statements(student_id, content, word_count, quality_score)
                    VALUES (?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, studentId);
            ps.setString(2, content);
            ps.setInt(3, countWords(content));
            ps.setObject(4, null);
            return ps;
        }, keyHolder);
        return requireGeneratedId(keyHolder, "personal statement");
    }

    private Long insertApplication(Long studentId, Long transcriptId, Long statementId, ApplicationDraftRequest request, Long agentId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO applications(student_id, transcript_id, personal_statement_id, batch_id,
                    target_school_code, target_major_code, source_application_id, created_by_agent_id, current_status)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, studentId);
            ps.setLong(2, transcriptId);
            ps.setLong(3, statementId);
            ps.setLong(4, request.getBatchId());
            ps.setString(5, request.getTargetSchoolCode());
            ps.setString(6, request.getTargetMajorCode());
            ps.setObject(7, null);
            ps.setLong(8, agentId);
            ps.setString(9, ApplicationStatus.DRAFT.name());
            return ps;
        }, keyHolder);
        return requireGeneratedId(keyHolder, "application");
    }

    private void updateStudent(Long studentId, ApplicationDraftRequest.StudentDraftDTO student) {
        jdbcTemplate.update("""
                UPDATE students
                SET name = ?, gender = ?, birth_date = ?, current_school = ?, grade = ?, email = ?, phone = ?, id_card_no = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                student.getName(), student.getGender(), parseDate(student.getBirthDate()), student.getCurrentSchool(),
                student.getGrade(), student.getEmail(), student.getPhone(), student.getIdCardNo(), studentId);
    }

    private void updateTranscript(Long transcriptId, ApplicationDraftRequest.ScoreDraftDTO score) {
        requireExists("SELECT COUNT(*) FROM files WHERE id = ?", score.getFileId(), "file not found: " + score.getFileId());
        jdbcTemplate.update("""
                UPDATE transcripts
                SET transcript_school_name = ?, term_start = ?, term_end = ?, chinese_score = ?, math_score = ?, english_score = ?,
                    physics_score = ?, chemistry_score = ?, history_score = ?, average_score = ?, failed_subject_count = ?, file_id = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                score.getTranscriptSchoolName(), parseDate(score.getTermStart()), parseDate(score.getTermEnd()),
                score.getChinese(), score.getMath(), score.getEnglish(), score.getPhysics(), score.getChemistry(),
                score.getHistory(), score.getAverageScore(), score.getFailedSubjectCount(), score.getFileId(), transcriptId);
    }

    private void updatePersonalStatement(Long statementId, ApplicationDraftRequest.PersonalStatementDraftDTO personalStatement) {
        String content = Objects.requireNonNullElse(personalStatement.getContent(), "");
        jdbcTemplate.update("""
                UPDATE personal_statements
                SET content = ?, word_count = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """, content, countWords(content), statementId);
    }

    private void updateApplicationDraft(Long applicationId, ApplicationDraftRequest request) {
        jdbcTemplate.update("""
                UPDATE applications
                SET batch_id = ?, target_school_code = ?, target_major_code = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """, request.getBatchId(), request.getTargetSchoolCode(), request.getTargetMajorCode(), applicationId);
    }

    private ApplicationRecord getApplicationRecord(Long applicationId) {
        try {
            return jdbcTemplate.queryForObject("""
                    SELECT id, student_id, transcript_id, personal_statement_id, batch_id,
                           target_school_code, target_major_code, created_by_agent_id, current_status
                    FROM applications
                    WHERE id = ?
                    """, (rs, rowNum) -> new ApplicationRecord(
                    rs.getLong("id"),
                    rs.getLong("student_id"),
                    rs.getLong("transcript_id"),
                    rs.getLong("personal_statement_id"),
                    rs.getLong("batch_id"),
                    rs.getString("target_school_code"),
                    rs.getString("target_major_code"),
                    rs.getLong("created_by_agent_id"),
                    ApplicationStatus.valueOf(rs.getString("current_status"))
            ), applicationId);
        } catch (EmptyResultDataAccessException ex) {
            throw new IllegalArgumentException("application not found: " + applicationId);
        }
    }

    private void ensureApplicationReadable(ApplicationRecord application, AuthUserPrincipal currentUser) {
        UserRole role = currentUser.getRole();
        if (role == UserRole.ADMIN || role == UserRole.DOMESTIC_REVIEWER) {
            return;
        }
        if (role == UserRole.AGENT && Objects.equals(application.createdByAgentId(), currentUser.getUserId())) {
            return;
        }
        if (role == UserRole.SCHOOL_REVIEWER && Objects.equals(application.targetSchoolCode(), currentUser.getSchoolCode())) {
            return;
        }
        throw new IllegalArgumentException("No permission to access this application");
    }

    private void assertApplicationConstraints(
            Long currentApplicationId,
            ApplicationConstraintService.ApplicantIdentity applicantIdentity,
            Long batchId,
            String targetSchoolCode
    ) {
        applicationConstraintService.assertNoOtherActiveApplication(currentApplicationId, applicantIdentity);
        applicationConstraintService.assertNotCanceledInSameBatchAndSchool(
                currentApplicationId,
                applicantIdentity,
                batchId,
                targetSchoolCode
        );
    }

    private boolean canViewSensitiveFields(ApplicationRecord application, AuthUserPrincipal currentUser) {
        UserRole role = currentUser.getRole();
        if (role == UserRole.ADMIN || role == UserRole.DOMESTIC_REVIEWER) {
            return true;
        }
        return role == UserRole.AGENT && Objects.equals(application.createdByAgentId(), currentUser.getUserId());
    }

    private AuthUserPrincipal requireCurrentUser() {
        Object principal = getContext().getAuthentication() == null ? null : getContext().getAuthentication().getPrincipal();
        if (principal instanceof AuthUserPrincipal authUserPrincipal) {
            return authUserPrincipal;
        }
        throw new IllegalArgumentException("Current user not found");
    }

    private Optional<Map<String, Object>> querySingleMap(String sql, Object... args) {
        List<Map<String, Object>> rows = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            int count = rs.getMetaData().getColumnCount();
            for (int i = 1; i <= count; i++) {
                row.put(rs.getMetaData().getColumnLabel(i), rs.getObject(i));
            }
            return row;
        }, args);
        return rows.stream().findFirst();
    }

    private void requireExists(String sql, Object arg, String message) {
        Long count = jdbcTemplate.queryForObject(sql, Long.class, arg);
        if (count == null || count == 0L) {
            throw new IllegalArgumentException(message);
        }
    }

    private void requireExists(String sql, Object[] args, String message) {
        Long count = jdbcTemplate.queryForObject(sql, Long.class, args);
        if (count == null || count == 0L) {
            throw new IllegalArgumentException(message);
        }
    }

    private Long requireGeneratedId(KeyHolder keyHolder, String entity) {
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to create " + entity);
        }
        return key.longValue();
    }

    private LocalDate parseDate(String value) {
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
    }

    private int countWords(String content) {
        String normalized = content == null ? "" : content.trim();
        if (normalized.isEmpty()) {
            return 0;
        }
        return normalized.split("\\s+").length;
    }

    private Object maskIfNeeded(Object value, boolean canViewSensitiveFields, java.util.function.Function<Object, Object> masker) {
        if (canViewSensitiveFields || value == null) {
            return value;
        }
        return masker.apply(value);
    }

    private Object maskEmail(Object value) {
        String email = String.valueOf(value);
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***";
        }
        return email.substring(0, 1) + "***" + email.substring(atIndex);
    }

    private Object maskPhone(Object value) {
        String phone = String.valueOf(value);
        if (phone.length() <= 4) {
            return "***";
        }
        return phone.substring(0, Math.min(3, phone.length())) + "****" + phone.substring(Math.max(phone.length() - 4, 3));
    }

    private Object maskIdCardNo(Object value) {
        String idCardNo = String.valueOf(value);
        if (idCardNo.length() <= 6) {
            return "***";
        }
        return idCardNo.substring(0, Math.min(3, idCardNo.length())) + "********" + idCardNo.substring(Math.max(idCardNo.length() - 3, 3));
    }

    private Object maskScore(Object value) {
        String score = String.valueOf(value);
        int dotIndex = score.indexOf('.');
        String integerPart = dotIndex >= 0 ? score.substring(0, dotIndex) : score;
        if (integerPart.isBlank()) {
            return "*";
        }
        return integerPart.charAt(0) + "*";
    }

    private Map<String, Object> orderedMap(Object... values) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            map.put(String.valueOf(values[i]), values[i + 1]);
        }
        return map;
    }

    private record ApplicationRecord(
            Long id,
            Long studentId,
            Long transcriptId,
            Long personalStatementId,
            Long batchId,
            String targetSchoolCode,
            String targetMajorCode,
            Long createdByAgentId,
            ApplicationStatus status
    ) {
    }
}
