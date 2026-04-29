package com.example.admissionsystem.domesticreviews.service;

import com.example.admissionsystem.applications.model.ApplicationStatus;
import com.example.admissionsystem.applications.service.ApplicationStatusService;
import com.example.admissionsystem.auth.model.UserRole;
import com.example.admissionsystem.auth.security.AuthUserPrincipal;
import com.example.admissionsystem.domesticreviews.dto.DomesticReviewSubmitRequest;
import com.example.admissionsystem.domesticreviews.vo.DomesticReviewListItemVO;
import com.example.admissionsystem.domesticreviews.vo.DomesticReviewPageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@Service
@RequiredArgsConstructor
public class DomesticReviewService {

    private final JdbcTemplate jdbcTemplate;
    private final ApplicationStatusService applicationStatusService;

    public DomesticReviewPageVO list(Integer page, Integer pageSize, String status, Long batchId) {
        AuthUserPrincipal currentUser = requireDomesticReviewerOrAdmin();
        int safePage = page != null && page > 0 ? page : 1;
        int safePageSize = pageSize != null && pageSize > 0 ? pageSize : 20;
        int offset = (safePage - 1) * safePageSize;

        StringBuilder baseSql = new StringBuilder("""
                FROM applications a
                JOIN students s ON s.id = a.student_id
                WHERE a.current_status IN ('SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_SUPPLEMENT', 'DOMESTIC_REJECTED')
                """);
        List<Object> params = new ArrayList<>();
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
        List<DomesticReviewListItemVO> list = jdbcTemplate.query(querySql, (rs, rowNum) -> DomesticReviewListItemVO.builder()
                .applicationId(rs.getLong("id"))
                .studentName(rs.getString("student_name"))
                .targetSchoolCode(rs.getString("target_school_code"))
                .targetMajorCode(rs.getString("target_major_code"))
                .status(ApplicationStatus.valueOf(rs.getString("current_status")))
                .batchId(rs.getLong("batch_id"))
                .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime().toString())
                .build(), params.toArray());

        return DomesticReviewPageVO.builder()
                .list(list)
                .page(safePage)
                .pageSize(safePageSize)
                .total(total == null ? 0L : total)
                .build();
    }

    @Transactional
    public String claim(Long applicationId) {
        AuthUserPrincipal currentUser = requireDomesticReviewer();
        applicationStatusService.assertCanOperate(ApplicationStatusService.ACTION_CLAIM_DOMESTIC_REVIEW, currentUser);
        ApplicationRecord application = getApplication(applicationId);
        if (application.status() != ApplicationStatus.SUBMITTED) {
            throw new IllegalArgumentException("Only SUBMITTED application can be claimed");
        }

        applicationStatusService.transition(
                application.id(),
                application.status(),
                ApplicationStatus.DOMESTIC_REVIEWING,
                currentUser,
                ApplicationStatusService.ACTION_CLAIM_DOMESTIC_REVIEW,
                "Domestic reviewer claimed the application",
                applicationStateMap(application),
                Map.of("status", ApplicationStatus.DOMESTIC_REVIEWING.name()),
                """
                        UPDATE applications
                        SET current_status = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                ApplicationStatus.DOMESTIC_REVIEWING.name(), application.id()
        );
        return "ok";
    }

    @Transactional
    public String submit(Long applicationId, DomesticReviewSubmitRequest request) {
        validateRequest(request);
        AuthUserPrincipal currentUser = requireDomesticReviewer();
        applicationStatusService.assertCanOperate(ApplicationStatusService.ACTION_SUBMIT_DOMESTIC_REVIEW, currentUser);
        ApplicationRecord application = getApplication(applicationId);
        if (application.status() != ApplicationStatus.DOMESTIC_REVIEWING) {
            throw new IllegalArgumentException("Only DOMESTIC_REVIEWING application can be submitted");
        }

        Long claimedReviewerId = findClaimedReviewerId(application.id());
        if (claimedReviewerId != null && !Objects.equals(claimedReviewerId, currentUser.getUserId())) {
            throw new IllegalArgumentException("Only the reviewer who claimed the application can submit the review");
        }

        ReviewResult reviewResult = ReviewResult.valueOf(request.getResult().trim().toUpperCase());
        Long reviewId = insertDomesticReview(application, currentUser, request, reviewResult);
        ApplicationStatus targetStatus = switch (reviewResult) {
            case PASS -> ApplicationStatus.SCHOOL_REVIEWING;
            case SUPPLEMENT_REQUIRED -> ApplicationStatus.DOMESTIC_SUPPLEMENT;
            case REJECT -> ApplicationStatus.DOMESTIC_REJECTED;
        };

        LocalDateTime now = LocalDateTime.now();
        applicationStatusService.transition(
                application.id(),
                application.status(),
                targetStatus,
                currentUser,
                ApplicationStatusService.ACTION_SUBMIT_DOMESTIC_REVIEW,
                buildStatusRemark(reviewResult, application.transcriptFileId()),
                applicationStateMap(application),
                buildApplicationAuditPayload(targetStatus, application, now),
                """
                        UPDATE applications
                        SET current_status = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                targetStatus.name(), application.id()
        );
        applicationStatusService.insertAuditLog(currentUser, "DOMESTIC_REVIEW", String.valueOf(reviewId),
                ApplicationStatusService.ACTION_SUBMIT_DOMESTIC_REVIEW, null,
                buildReviewAuditPayload(request, reviewResult, application.transcriptFileId()),
                "Domestic review submitted");
        return "ok";
    }

    private void validateRequest(DomesticReviewSubmitRequest request) {
        if (request == null || request.getResult() == null || request.getResult().isBlank()) {
            throw new IllegalArgumentException("review_result is required");
        }
        if (request.getMaterialComplete() == null || request.getIdentityMatched() == null
                || request.getBasicScorePassed() == null || request.getStandardizationPassed() == null) {
            throw new IllegalArgumentException("materialComplete, identityMatched, basicScorePassed and standardizationPassed are required");
        }
        if (request.getAuthenticityRiskLevel() == null || request.getAuthenticityRiskLevel().isBlank()) {
            throw new IllegalArgumentException("authenticityRiskLevel is required");
        }
    }

    private AuthUserPrincipal requireDomesticReviewerOrAdmin() {
        Object principal = getContext().getAuthentication() == null ? null : getContext().getAuthentication().getPrincipal();
        if (principal instanceof AuthUserPrincipal authUserPrincipal
                && (authUserPrincipal.getRole() == UserRole.DOMESTIC_REVIEWER || authUserPrincipal.getRole() == UserRole.ADMIN)) {
            return authUserPrincipal;
        }
        throw new IllegalArgumentException("Only DOMESTIC_REVIEWER or ADMIN can access domestic review list");
    }

    private AuthUserPrincipal requireDomesticReviewer() {
        Object principal = getContext().getAuthentication() == null ? null : getContext().getAuthentication().getPrincipal();
        if (principal instanceof AuthUserPrincipal authUserPrincipal && authUserPrincipal.getRole() == UserRole.DOMESTIC_REVIEWER) {
            return authUserPrincipal;
        }
        throw new IllegalArgumentException("Only DOMESTIC_REVIEWER can operate domestic review");
    }

    private ApplicationRecord getApplication(Long applicationId) {
        return jdbcTemplate.query("""
                SELECT a.id, a.student_id, a.transcript_id, a.personal_statement_id, a.batch_id,
                       a.target_school_code, a.target_major_code, a.created_by_agent_id, a.current_status,
                       a.submit_time, t.file_id
                FROM applications a
                JOIN transcripts t ON t.id = a.transcript_id
                WHERE a.id = ?
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
                    ApplicationStatus.valueOf(rs.getString("current_status")),
                    rs.getTimestamp("submit_time") == null ? null : rs.getTimestamp("submit_time").toLocalDateTime(),
                    rs.getLong("file_id")
            );
        }, applicationId);
    }

    private Long findClaimedReviewerId(Long applicationId) {
        return jdbcTemplate.query("""
                SELECT operator_id
                FROM application_status_histories
                WHERE application_id = ? AND trigger_action = ?
                ORDER BY id DESC
                LIMIT 1
                """, rs -> rs.next() ? (Long) rs.getObject("operator_id") : null,
                applicationId, ApplicationStatusService.ACTION_CLAIM_DOMESTIC_REVIEW);
    }

    private Long insertDomesticReview(
            ApplicationRecord application,
            AuthUserPrincipal currentUser,
            DomesticReviewSubmitRequest request,
            ReviewResult reviewResult
    ) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO domestic_reviews(
                      application_id, reviewer_id, material_complete_passed, identity_matched,
                      basic_score_passed, authenticity_risk_level, standardization_passed,
                      review_result, review_comment, reviewed_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, application.id());
            ps.setLong(2, currentUser.getUserId());
            ps.setBoolean(3, Boolean.TRUE.equals(request.getMaterialComplete()));
            ps.setBoolean(4, Boolean.TRUE.equals(request.getIdentityMatched()));
            ps.setBoolean(5, Boolean.TRUE.equals(request.getBasicScorePassed()));
            ps.setString(6, request.getAuthenticityRiskLevel().trim().toUpperCase());
            ps.setBoolean(7, Boolean.TRUE.equals(request.getStandardizationPassed()));
            ps.setString(8, reviewResult.name());
            ps.setString(9, request.getComment());
            ps.setTimestamp(10, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to create domestic review");
        }
        return key.longValue();
    }

    private String buildStatusRemark(ReviewResult reviewResult, Long transcriptFileId) {
        return switch (reviewResult) {
            case PASS -> "Domestic review passed and sent to school review";
            case SUPPLEMENT_REQUIRED -> "Domestic review requires supplement, current transcript_file_id=" + transcriptFileId;
            case REJECT -> "Domestic review rejected the application";
        };
    }

    private Map<String, Object> buildReviewAuditPayload(
            DomesticReviewSubmitRequest request,
            ReviewResult reviewResult,
            Long transcriptFileId
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("review_result", reviewResult.name());
        payload.put("material_complete", request.getMaterialComplete());
        payload.put("identity_matched", request.getIdentityMatched());
        payload.put("basic_score_passed", request.getBasicScorePassed());
        payload.put("authenticity_risk_level", request.getAuthenticityRiskLevel().trim().toUpperCase());
        payload.put("standardization_passed", request.getStandardizationPassed());
        payload.put("review_comment", request.getComment());
        payload.put("transcript_file_id", transcriptFileId);
        return payload;
    }

    private Map<String, Object> buildApplicationAuditPayload(
            ApplicationStatus targetStatus,
            ApplicationRecord application,
            LocalDateTime operatedAt
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", targetStatus.name());
        payload.put("transcript_file_id", application.transcriptFileId());
        if (targetStatus == ApplicationStatus.DOMESTIC_SUPPLEMENT) {
            payload.put("supplement_required_at", operatedAt.toString());
        }
        return payload;
    }

    private Map<String, Object> applicationStateMap(ApplicationRecord application) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", application.status().name());
        payload.put("target_school_code", application.targetSchoolCode());
        payload.put("target_major_code", application.targetMajorCode());
        payload.put("submit_time", application.submitTime() == null ? null : application.submitTime().toString());
        payload.put("transcript_file_id", application.transcriptFileId());
        return payload;
    }

    private enum ReviewResult {
        PASS,
        SUPPLEMENT_REQUIRED,
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
            ApplicationStatus status,
            LocalDateTime submitTime,
            Long transcriptFileId
    ) {
    }
}
