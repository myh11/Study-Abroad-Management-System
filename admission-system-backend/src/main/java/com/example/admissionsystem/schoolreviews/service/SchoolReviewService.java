package com.example.admissionsystem.schoolreviews.service;

import com.example.admissionsystem.applications.model.ApplicationStatus;
import com.example.admissionsystem.applications.service.ApplicationStatusService;
import com.example.admissionsystem.auth.model.UserRole;
import com.example.admissionsystem.auth.security.AuthUserPrincipal;
import com.example.admissionsystem.quotas.service.QuotaConcurrencyService;
import com.example.admissionsystem.schoolreviews.dto.SchoolReviewSubmitRequest;
import com.example.admissionsystem.schoolreviews.vo.SchoolReviewListItemVO;
import com.example.admissionsystem.schoolreviews.vo.SchoolReviewPageVO;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@Service
@RequiredArgsConstructor
public class SchoolReviewService {

    private final ApplicationStatusService applicationStatusService;
    private final QuotaConcurrencyService quotaConcurrencyService;
    private final JdbcTemplate jdbcTemplate;

    public SchoolReviewPageVO list(Integer page, Integer pageSize, String status, Long batchId, String schoolCode) {
        AuthUserPrincipal currentUser = requireSchoolReviewerOrAdmin();
        int safePage = page != null && page > 0 ? page : 1;
        int safePageSize = pageSize != null && pageSize > 0 ? pageSize : 20;
        int offset = (safePage - 1) * safePageSize;

        StringBuilder baseSql = new StringBuilder("""
                FROM applications a
                JOIN students s ON s.id = a.student_id
                WHERE a.current_status IN ('SCHOOL_REVIEWING', 'WAITLISTED', 'WAITLIST_PENDING_CONFIRM',
                                           'RESERVED', 'ADJUSTMENT_SUGGESTED', 'SCHOOL_REJECTED', 'CLOSED')
                """);
        List<Object> params = new ArrayList<>();
        if (currentUser.getRole() == UserRole.SCHOOL_REVIEWER) {
            baseSql.append(" AND a.target_school_code = ?");
            params.add(currentUser.getSchoolCode());
        } else if (schoolCode != null && !schoolCode.isBlank()) {
            baseSql.append(" AND a.target_school_code = ?");
            params.add(schoolCode.trim());
        }
        if (status != null && !status.isBlank()) {
            baseSql.append(" AND a.current_status = ?");
            params.add(status.trim());
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
        List<SchoolReviewListItemVO> list = jdbcTemplate.query(querySql, (rs, rowNum) -> SchoolReviewListItemVO.builder()
                .applicationId(rs.getLong("id"))
                .studentName(rs.getString("student_name"))
                .targetSchoolCode(rs.getString("target_school_code"))
                .targetMajorCode(rs.getString("target_major_code"))
                .status(ApplicationStatus.valueOf(rs.getString("current_status")))
                .batchId(rs.getLong("batch_id"))
                .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime().toString())
                .build(), params.toArray());

        return SchoolReviewPageVO.builder()
                .list(list)
                .page(safePage)
                .pageSize(safePageSize)
                .total(total == null ? 0L : total)
                .build();
    }

    @Transactional
    public String submit(Long applicationId, SchoolReviewSubmitRequest request) {
        validateRequest(request);
        AuthUserPrincipal currentUser = requireCurrentUser();
        applicationStatusService.assertCanOperate(ApplicationStatusService.ACTION_SUBMIT_SCHOOL_REVIEW, currentUser);

        ApplicationRecord application = getApplication(applicationId);
        if (application.status() != ApplicationStatus.SCHOOL_REVIEWING) {
            throw new IllegalArgumentException("Only SCHOOL_REVIEWING application can be submitted");
        }
        if (!Objects.equals(application.targetSchoolCode(), currentUser.getSchoolCode())) {
            throw new IllegalArgumentException("School reviewer can only review applications of their own school");
        }

        ReviewResult reviewResult = ReviewResult.valueOf(request.getResult().trim().toUpperCase());
        ensureSuggestedMajorValid(reviewResult, request.getSuggestedMajorCode(), application.targetSchoolCode());

        Long schoolReviewId = insertSchoolReview(application, currentUser, request, reviewResult);
        return switch (reviewResult) {
            case RESERVE -> handleReserve(application, currentUser, request, schoolReviewId);
            case WAITLIST -> handleWaitlist(application, currentUser, request, schoolReviewId);
            case REJECT -> handleReject(application, currentUser, request, schoolReviewId);
            case SUGGEST_ADJUSTMENT -> handleAdjustment(application, currentUser, request, schoolReviewId);
        };
    }

    private String handleReserve(
            ApplicationRecord application,
            AuthUserPrincipal currentUser,
            SchoolReviewSubmitRequest request,
            Long schoolReviewId
    ) {
        quotaConcurrencyService.reserveQuota(
                application.batchId(),
                application.targetSchoolCode(),
                application.targetMajorCode(),
                currentUser,
                ApplicationStatusService.ACTION_SUBMIT_SCHOOL_REVIEW,
                "Reserve seat for application " + application.id()
        );

        LocalDateTime reservedAt = LocalDateTime.now();
        applicationStatusService.insertAuditLog(currentUser, "SCHOOL_REVIEW", String.valueOf(schoolReviewId),
                ApplicationStatusService.ACTION_SUBMIT_SCHOOL_REVIEW, null,
                Map.of("application_id", application.id(), "review_result", "RESERVE"),
                "School review submitted with RESERVE");
        applicationStatusService.transition(
                application.id(),
                application.status(),
                ApplicationStatus.RESERVED,
                currentUser,
                ApplicationStatusService.ACTION_SUBMIT_SCHOOL_REVIEW,
                "School review reserved the application",
                applicationStateMap(application),
                Map.of("status", ApplicationStatus.RESERVED.name()),
                """
                        UPDATE applications
                        SET current_status = ?, reserved_at = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                ApplicationStatus.RESERVED.name(), Timestamp.valueOf(reservedAt), application.id()
        );
        return "ok";
    }

    private String handleWaitlist(
            ApplicationRecord application,
            AuthUserPrincipal currentUser,
            SchoolReviewSubmitRequest request,
            Long schoolReviewId
    ) {
        int rank = nextWaitlistRank(application.batchId(), application.targetSchoolCode(), application.targetMajorCode());
        Long waitlistId = insertWaitlistRecord(application, request, rank);

        applicationStatusService.insertAuditLog(currentUser, "SCHOOL_REVIEW", String.valueOf(schoolReviewId),
                ApplicationStatusService.ACTION_SUBMIT_SCHOOL_REVIEW, null,
                Map.of("application_id", application.id(), "review_result", "WAITLIST"),
                "School review submitted with WAITLIST");
        applicationStatusService.insertAuditLog(currentUser, "WAITLIST_RECORD", String.valueOf(waitlistId),
                "CREATE_WAITLIST_RECORD", null,
                Map.of("application_id", application.id(), "waitlist_status", "ACTIVE"),
                "Waitlist record created");
        applicationStatusService.transition(
                application.id(),
                application.status(),
                ApplicationStatus.WAITLISTED,
                currentUser,
                ApplicationStatusService.ACTION_SUBMIT_SCHOOL_REVIEW,
                "School review put the application onto waitlist",
                applicationStateMap(application),
                Map.of("status", ApplicationStatus.WAITLISTED.name()),
                """
                        UPDATE applications
                        SET current_status = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                ApplicationStatus.WAITLISTED.name(), application.id()
        );
        return "ok";
    }

    private String handleReject(
            ApplicationRecord application,
            AuthUserPrincipal currentUser,
            SchoolReviewSubmitRequest request,
            Long schoolReviewId
    ) {
        applicationStatusService.insertAuditLog(currentUser, "SCHOOL_REVIEW", String.valueOf(schoolReviewId),
                ApplicationStatusService.ACTION_SUBMIT_SCHOOL_REVIEW, null,
                Map.of("application_id", application.id(), "review_result", "REJECT"),
                "School review submitted with REJECT");
        applicationStatusService.transition(
                application.id(),
                application.status(),
                ApplicationStatus.SCHOOL_REJECTED,
                currentUser,
                ApplicationStatusService.ACTION_SUBMIT_SCHOOL_REVIEW,
                "School review rejected the application",
                applicationStateMap(application),
                Map.of("status", ApplicationStatus.SCHOOL_REJECTED.name()),
                """
                        UPDATE applications
                        SET current_status = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                ApplicationStatus.SCHOOL_REJECTED.name(), application.id()
        );
        return "ok";
    }

    private String handleAdjustment(
            ApplicationRecord application,
            AuthUserPrincipal currentUser,
            SchoolReviewSubmitRequest request,
            Long schoolReviewId
    ) {
        LocalDateTime now = LocalDateTime.now();
        quotaConcurrencyService.lockQuotaSnapshotForAdjustment(
                application.batchId(),
                application.targetSchoolCode(),
                request.getSuggestedMajorCode()
        );
        applicationStatusService.insertAuditLog(currentUser, "SCHOOL_REVIEW", String.valueOf(schoolReviewId),
                ApplicationStatusService.ACTION_SUBMIT_SCHOOL_REVIEW, null,
                Map.of(
                        "application_id", application.id(),
                        "review_result", "SUGGEST_ADJUSTMENT",
                        "suggested_major_code", request.getSuggestedMajorCode()
                ),
                "School review submitted with SUGGEST_ADJUSTMENT");
        applicationStatusService.transition(
                application.id(),
                application.status(),
                ApplicationStatus.ADJUSTMENT_SUGGESTED,
                currentUser,
                ApplicationStatusService.ACTION_SUBMIT_SCHOOL_REVIEW,
                "School review suggested adjustment",
                applicationStateMap(application),
                Map.of("status", ApplicationStatus.ADJUSTMENT_SUGGESTED.name(), "suggested_major_code", request.getSuggestedMajorCode()),
                """
                        UPDATE applications
                        SET current_status = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                ApplicationStatus.ADJUSTMENT_SUGGESTED.name(), application.id()
        );
        Long newApplicationId = createAdjustmentDraft(application, request.getSuggestedMajorCode());
        applicationStatusService.transition(
                application.id(),
                ApplicationStatus.ADJUSTMENT_SUGGESTED,
                ApplicationStatus.CLOSED,
                currentUser,
                ApplicationStatusService.ACTION_GENERATE_ADJUSTMENT_DRAFT,
                "Original application closed after generating adjustment draft",
                Map.of("status", ApplicationStatus.ADJUSTMENT_SUGGESTED.name()),
                Map.of("status", ApplicationStatus.CLOSED.name(), "close_reason", "SUGGEST_ADJUSTMENT_CREATED_NEW_DRAFT"),
                """
                        UPDATE applications
                        SET current_status = ?, close_reason = ?, closed_at = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                ApplicationStatus.CLOSED.name(), "SUGGEST_ADJUSTMENT_CREATED_NEW_DRAFT", Timestamp.valueOf(now), application.id()
        );
        applicationStatusService.recordInitialStatus(
                newApplicationId,
                ApplicationStatus.DRAFT,
                currentUser,
                ApplicationStatusService.ACTION_GENERATE_ADJUSTMENT_DRAFT,
                "New draft application generated from adjustment suggestion",
                Map.of("status", ApplicationStatus.DRAFT.name(), "source_application_id", application.id())
        );
        return "ok";
    }

    private void validateRequest(SchoolReviewSubmitRequest request) {
        if (request == null || request.getResult() == null || request.getResult().isBlank()) {
            throw new IllegalArgumentException("review_result is required");
        }
        if (request.getAcademicScore() == null || request.getMaterialScore() == null
                || request.getMatchingScore() == null || request.getTotalScore() == null) {
            throw new IllegalArgumentException("academic_score, material_score, matching_score and total_score are required");
        }
    }

    private AuthUserPrincipal requireSchoolReviewerOrAdmin() {
        Object principal = getContext().getAuthentication() == null ? null : getContext().getAuthentication().getPrincipal();
        if (principal instanceof AuthUserPrincipal authUserPrincipal
                && (authUserPrincipal.getRole() == UserRole.SCHOOL_REVIEWER || authUserPrincipal.getRole() == UserRole.ADMIN)) {
            return authUserPrincipal;
        }
        throw new IllegalArgumentException("Only SCHOOL_REVIEWER or ADMIN can access school review list");
    }

    private void ensureSuggestedMajorValid(ReviewResult reviewResult, String suggestedMajorCode, String schoolCode) {
        if (reviewResult != ReviewResult.SUGGEST_ADJUSTMENT) {
            return;
        }
        if (suggestedMajorCode == null || suggestedMajorCode.isBlank()) {
            throw new IllegalArgumentException("suggested_major_code is required when review_result is SUGGEST_ADJUSTMENT");
        }
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM majors
                WHERE major_code = ? AND school_code = ?
                """, Long.class, suggestedMajorCode, schoolCode);
        if (count == null || count == 0L) {
            throw new IllegalArgumentException("suggested_major_code must belong to reviewer's school");
        }
    }

    private AuthUserPrincipal requireCurrentUser() {
        Object principal = getContext().getAuthentication() == null ? null : getContext().getAuthentication().getPrincipal();
        if (principal instanceof AuthUserPrincipal authUserPrincipal) {
            return authUserPrincipal;
        }
        throw new IllegalArgumentException("Current user not found");
    }

    private ApplicationRecord getApplication(Long applicationId) {
        return jdbcTemplate.query("""
                SELECT id, student_id, transcript_id, personal_statement_id, batch_id,
                       target_school_code, target_major_code, created_by_agent_id, current_status
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
                    rs.getLong("created_by_agent_id"),
                    ApplicationStatus.valueOf(rs.getString("current_status"))
            );
        }, applicationId);
    }

    private Long insertSchoolReview(
            ApplicationRecord application,
            AuthUserPrincipal currentUser,
            SchoolReviewSubmitRequest request,
            ReviewResult reviewResult
    ) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO school_reviews(
                      application_id, reviewer_id, school_threshold_passed, major_threshold_passed,
                      school_quota_passed, major_quota_passed, academic_score, material_score,
                      matching_score, total_score, suggested_major_code, waitlist_rank_snapshot,
                      review_result, review_reason, reviewed_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, application.id());
            ps.setLong(2, currentUser.getUserId());
            ps.setBoolean(3, Boolean.TRUE.equals(request.getSchoolThresholdPassed()));
            ps.setBoolean(4, Boolean.TRUE.equals(request.getMajorThresholdPassed()));
            ps.setBoolean(5, Boolean.TRUE.equals(request.getSchoolQuotaPassed()));
            ps.setBoolean(6, Boolean.TRUE.equals(request.getMajorQuotaPassed()));
            ps.setBigDecimal(7, java.math.BigDecimal.valueOf(request.getAcademicScore()));
            ps.setBigDecimal(8, java.math.BigDecimal.valueOf(request.getMaterialScore()));
            ps.setBigDecimal(9, java.math.BigDecimal.valueOf(request.getMatchingScore()));
            ps.setBigDecimal(10, java.math.BigDecimal.valueOf(request.getTotalScore()));
            ps.setString(11, request.getSuggestedMajorCode());
            ps.setObject(12, reviewResult == ReviewResult.WAITLIST ? nextWaitlistRank(application.batchId(), application.targetSchoolCode(), application.targetMajorCode()) : null);
            ps.setString(13, reviewResult.name());
            ps.setString(14, request.getReason());
            ps.setTimestamp(15, Timestamp.valueOf(LocalDateTime.now()));
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to create school review");
        }
        return key.longValue();
    }

    private Long insertWaitlistRecord(ApplicationRecord application, SchoolReviewSubmitRequest request, int rank) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO waitlist_records(
                      application_id, batch_id, school_code, major_code, total_score,
                      domestic_approved_at, key_subject_score, application_submitted_at,
                      current_rank, rank_reason, waitlist_status
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, application.id());
            ps.setLong(2, application.batchId());
            ps.setString(3, application.targetSchoolCode());
            ps.setString(4, application.targetMajorCode());
            ps.setBigDecimal(5, java.math.BigDecimal.valueOf(request.getTotalScore()));
            ps.setTimestamp(6, now);
            ps.setBigDecimal(7, java.math.BigDecimal.valueOf(request.getAcademicScore()));
            ps.setTimestamp(8, now);
            ps.setInt(9, rank);
            ps.setString(10, "Created by school review submit");
            ps.setString(11, "ACTIVE");
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to create waitlist record");
        }
        return key.longValue();
    }

    private Long createAdjustmentDraft(ApplicationRecord application, String suggestedMajorCode) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO applications(
                      student_id, transcript_id, personal_statement_id, batch_id,
                      target_school_code, target_major_code, source_application_id,
                      created_by_agent_id, current_status
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, application.studentId());
            ps.setLong(2, application.transcriptId());
            ps.setLong(3, application.personalStatementId());
            ps.setLong(4, application.batchId());
            ps.setString(5, application.targetSchoolCode());
            ps.setString(6, suggestedMajorCode);
            ps.setLong(7, application.id());
            ps.setLong(8, application.createdByAgentId());
            ps.setString(9, ApplicationStatus.DRAFT.name());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to create adjustment draft");
        }
        return key.longValue();
    }

    private int nextWaitlistRank(Long batchId, String schoolCode, String majorCode) {
        Integer rank = jdbcTemplate.queryForObject("""
                SELECT COALESCE(MAX(current_rank), 0) + 1
                FROM waitlist_records
                WHERE batch_id = ? AND school_code = ? AND major_code = ?
                """, Integer.class, batchId, schoolCode, majorCode);
        return rank == null ? 1 : rank;
    }

    private Map<String, Object> applicationStateMap(ApplicationRecord application) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", application.status().name());
        payload.put("target_school_code", application.targetSchoolCode());
        payload.put("target_major_code", application.targetMajorCode());
        payload.put("batch_id", application.batchId());
        return payload;
    }

    private enum ReviewResult {
        RESERVE,
        SUGGEST_ADJUSTMENT,
        WAITLIST,
        REJECT
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
