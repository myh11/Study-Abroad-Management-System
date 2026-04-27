package com.example.admissionsystem.applications.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationConstraintService {

    private static final List<String> ACTIVE_STATUSES = List.of(
            "DRAFT",
            "SUBMITTED",
            "DOMESTIC_REVIEWING",
            "DOMESTIC_SUPPLEMENT",
            "SCHOOL_REVIEWING",
            "WAITLISTED",
            "WAITLIST_PENDING_CONFIRM",
            "RESERVED",
            "ADJUSTMENT_SUGGESTED"
    );

    private final JdbcTemplate jdbcTemplate;

    public void assertNoOtherActiveApplication(
            Long currentApplicationId,
            ApplicantIdentity applicantIdentity
    ) {
        IdentitySql identitySql = buildIdentitySql(applicantIdentity);
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM applications a
                JOIN students s ON s.id = a.student_id
                WHERE a.current_status IN ('DRAFT', 'SUBMITTED', 'DOMESTIC_REVIEWING', 'DOMESTIC_SUPPLEMENT',
                                           'SCHOOL_REVIEWING', 'WAITLISTED', 'WAITLIST_PENDING_CONFIRM',
                                           'RESERVED', 'ADJUSTMENT_SUGGESTED')
                """);
        if (currentApplicationId != null) {
            sql.append(" AND a.id <> ? ");
            args.add(currentApplicationId);
        }
        sql.append(identitySql.sql());
        args.addAll(identitySql.args());

        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, args.toArray());
        if (count != null && count > 0) {
            throw new IllegalStateException("Applicant already has another active application");
        }
    }

    public void assertNotCanceledInSameBatchAndSchool(
            Long currentApplicationId,
            ApplicantIdentity applicantIdentity,
            Long batchId,
            String targetSchoolCode
    ) {
        IdentitySql identitySql = buildIdentitySql(applicantIdentity);
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM applications a
                JOIN students s ON s.id = a.student_id
                WHERE a.batch_id = ?
                  AND a.target_school_code = ?
                  AND (
                        a.current_status = 'CANCELED'
                        OR a.cancel_reason IS NOT NULL
                        OR (a.current_status = 'CLOSED' AND (a.cancel_reason IS NOT NULL OR a.close_reason = 'CANCELED_TERMINAL'))
                  )
                """);
        args.add(batchId);
        args.add(targetSchoolCode);
        if (currentApplicationId != null) {
            sql.append(" AND a.id <> ? ");
            args.add(currentApplicationId);
        }
        sql.append(identitySql.sql());
        args.addAll(identitySql.args());

        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, args.toArray());
        if (count != null && count > 0) {
            throw new IllegalStateException("Canceled application cannot re-apply to the same school in the same batch");
        }
    }

    public ApplicantIdentity loadApplicantIdentity(Long applicationId) {
        return jdbcTemplate.query("""
                SELECT a.id AS application_id, a.student_id, s.id_card_no, s.email, s.phone
                FROM applications a
                JOIN students s ON s.id = a.student_id
                WHERE a.id = ?
                """, rs -> {
            if (!rs.next()) {
                throw new IllegalArgumentException("application not found: " + applicationId);
            }
            return new ApplicantIdentity(
                    rs.getLong("student_id"),
                    normalize(rs.getString("id_card_no")),
                    normalize(rs.getString("email")),
                    normalize(rs.getString("phone"))
            );
        }, applicationId);
    }

    public ApplicantIdentity buildApplicantIdentity(
            Long studentId,
            String idCardNo,
            String email,
            String phone
    ) {
        return new ApplicantIdentity(studentId, normalize(idCardNo), normalize(email), normalize(phone));
    }

    private IdentitySql buildIdentitySql(ApplicantIdentity applicantIdentity) {
        if (applicantIdentity == null) {
            throw new IllegalArgumentException("applicant identity is required");
        }
        if (applicantIdentity.idCardNo() != null) {
            return new IdentitySql(" AND s.id_card_no = ? ", List.of(applicantIdentity.idCardNo()));
        }
        List<String> conditions = new ArrayList<>();
        List<Object> args = new ArrayList<>();
        if (applicantIdentity.email() != null) {
            conditions.add("s.email = ?");
            args.add(applicantIdentity.email());
        }
        if (applicantIdentity.phone() != null) {
            conditions.add("s.phone = ?");
            args.add(applicantIdentity.phone());
        }
        if (!conditions.isEmpty()) {
            return new IdentitySql(" AND (" + String.join(" OR ", conditions) + ") ", args);
        }
        if (applicantIdentity.studentId() != null) {
            return new IdentitySql(" AND s.id = ? ", List.of(applicantIdentity.studentId()));
        }
        throw new IllegalArgumentException("idCardNo, email or phone is required to identify applicant");
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public record ApplicantIdentity(
            Long studentId,
            String idCardNo,
            String email,
            String phone
    ) {
    }

    private record IdentitySql(
            String sql,
            List<Object> args
    ) {
    }
}
