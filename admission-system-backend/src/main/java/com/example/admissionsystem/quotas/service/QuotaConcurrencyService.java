package com.example.admissionsystem.quotas.service;

import com.example.admissionsystem.applications.service.ApplicationStatusService;
import com.example.admissionsystem.auth.security.AuthUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuotaConcurrencyService {

    private final JdbcTemplate jdbcTemplate;
    private final ApplicationStatusService applicationStatusService;

    public void reserveQuota(
            Long batchId,
            String schoolCode,
            String majorCode,
            AuthUserPrincipal currentUser,
            String operationType,
            String remark
    ) {
        QuotaRow schoolQuota = lockSchoolQuota(batchId, schoolCode);
        QuotaRow majorQuota = lockMajorQuota(batchId, majorCode);
        if (schoolQuota.remainingQuota() <= 0) {
            throw new IllegalStateException("No remaining school quota");
        }
        if (majorQuota.remainingQuota() <= 0) {
            throw new IllegalStateException("No remaining major quota");
        }

        QuotaRow newSchoolQuota = schoolQuota.reserveOne();
        QuotaRow newMajorQuota = majorQuota.reserveOne();
        updateSchoolQuotaWithVersion(schoolQuota, newSchoolQuota);
        updateMajorQuotaWithVersion(majorQuota, newMajorQuota);

        applicationStatusService.insertAuditLog(
                currentUser,
                "SCHOOL_QUOTA",
                String.valueOf(schoolQuota.id()),
                operationType,
                schoolQuota.toAuditMap(),
                newSchoolQuota.toAuditMap(),
                remark + " - school quota reserved"
        );
        applicationStatusService.insertAuditLog(
                currentUser,
                "MAJOR_QUOTA",
                String.valueOf(majorQuota.id()),
                operationType,
                majorQuota.toAuditMap(),
                newMajorQuota.toAuditMap(),
                remark + " - major quota reserved"
        );
    }

    public void releaseQuota(
            Long batchId,
            String schoolCode,
            String majorCode,
            AuthUserPrincipal currentUser,
            String operationType,
            String remark
    ) {
        QuotaRow schoolQuota = lockSchoolQuota(batchId, schoolCode);
        QuotaRow majorQuota = lockMajorQuota(batchId, majorCode);
        if (schoolQuota.usedQuota() <= 0) {
            throw new IllegalStateException("No used school quota can be released");
        }
        if (majorQuota.usedQuota() <= 0) {
            throw new IllegalStateException("No used major quota can be released");
        }

        QuotaRow newSchoolQuota = schoolQuota.releaseOne();
        QuotaRow newMajorQuota = majorQuota.releaseOne();
        updateSchoolQuotaWithVersion(schoolQuota, newSchoolQuota);
        updateMajorQuotaWithVersion(majorQuota, newMajorQuota);

        applicationStatusService.insertAuditLog(
                currentUser,
                "SCHOOL_QUOTA",
                String.valueOf(schoolQuota.id()),
                operationType,
                schoolQuota.toAuditMap(),
                newSchoolQuota.toAuditMap(),
                remark + " - school quota released"
        );
        applicationStatusService.insertAuditLog(
                currentUser,
                "MAJOR_QUOTA",
                String.valueOf(majorQuota.id()),
                operationType,
                majorQuota.toAuditMap(),
                newMajorQuota.toAuditMap(),
                remark + " - major quota released"
        );
    }

    public void lockQuotaSnapshotForAdjustment(
            Long batchId,
            String schoolCode,
            String majorCode
    ) {
        lockSchoolQuota(batchId, schoolCode);
        lockMajorQuota(batchId, majorCode);
    }

    public Map<String, Object> adjustSchoolQuota(
            Long quotaId,
            Integer totalQuota,
            BigDecimal schoolMinScore,
            BigDecimal schoolMinMath,
            BigDecimal schoolMinEnglish
    ) {
        QuotaRow current = lockSchoolQuotaById(quotaId);
        if (totalQuota < current.usedQuota()) {
            throw new IllegalArgumentException("total_quota cannot be less than used_quota");
        }
        int remainingQuota = totalQuota - current.usedQuota();
        int nextVersion = current.version() + 1;
        int updated = jdbcTemplate.update("""
                UPDATE school_quotas
                SET total_quota = ?, remaining_quota = ?, school_min_score = ?, school_min_math = ?, school_min_english = ?,
                    quota_version = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ? AND quota_version = ?
                """,
                totalQuota,
                remainingQuota,
                schoolMinScore,
                schoolMinMath,
                schoolMinEnglish,
                nextVersion,
                quotaId,
                current.version()
        );
        if (updated == 0) {
            throw new IllegalStateException("Concurrent school quota update detected");
        }
        return getSchoolQuota(quotaId);
    }

    public Map<String, Object> adjustMajorQuota(
            Long quotaId,
            Integer totalQuota,
            BigDecimal minAverageScore,
            BigDecimal minMathScore,
            BigDecimal minEnglishScore,
            BigDecimal minPhysicsScore,
            BigDecimal minLiberalArtsScore,
            BigDecimal reserveLine,
            BigDecimal waitlistLine,
            boolean allowAdjustmentIn
    ) {
        QuotaRow current = lockMajorQuotaById(quotaId);
        if (totalQuota < current.usedQuota()) {
            throw new IllegalArgumentException("total_quota cannot be less than used_quota");
        }
        int remainingQuota = totalQuota - current.usedQuota();
        int nextVersion = current.version() + 1;
        int updated = jdbcTemplate.update("""
                UPDATE major_quotas
                SET total_quota = ?, remaining_quota = ?, min_average_score = ?, min_math_score = ?, min_english_score = ?,
                    min_physics_score = ?, min_liberal_arts_score = ?, reserve_line = ?, waitlist_line = ?, allow_adjustment_in = ?,
                    quota_version = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ? AND quota_version = ?
                """,
                totalQuota,
                remainingQuota,
                minAverageScore,
                minMathScore,
                minEnglishScore,
                minPhysicsScore,
                minLiberalArtsScore,
                reserveLine,
                waitlistLine,
                allowAdjustmentIn ? 1 : 0,
                nextVersion,
                quotaId,
                current.version()
        );
        if (updated == 0) {
            throw new IllegalStateException("Concurrent major quota update detected");
        }
        return getMajorQuota(quotaId);
    }

    private QuotaRow lockSchoolQuota(Long batchId, String schoolCode) {
        return jdbcTemplate.query("""
                SELECT id, batch_id, school_code, total_quota, used_quota, remaining_quota, quota_version
                FROM school_quotas
                WHERE batch_id = ? AND school_code = ?
                FOR UPDATE
                """, rs -> {
            if (!rs.next()) {
                throw new IllegalArgumentException("school quota not found");
            }
            return new QuotaRow(
                    rs.getLong("id"),
                    rs.getLong("batch_id"),
                    rs.getString("school_code"),
                    null,
                    rs.getInt("total_quota"),
                    rs.getInt("used_quota"),
                    rs.getInt("remaining_quota"),
                    rs.getInt("quota_version")
            );
        }, batchId, schoolCode);
    }

    private QuotaRow lockMajorQuota(Long batchId, String majorCode) {
        return jdbcTemplate.query("""
                SELECT id, batch_id, school_code, major_code, total_quota, used_quota, remaining_quota, quota_version
                FROM major_quotas
                WHERE batch_id = ? AND major_code = ?
                FOR UPDATE
                """, rs -> {
            if (!rs.next()) {
                throw new IllegalArgumentException("major quota not found");
            }
            return new QuotaRow(
                    rs.getLong("id"),
                    rs.getLong("batch_id"),
                    rs.getString("school_code"),
                    rs.getString("major_code"),
                    rs.getInt("total_quota"),
                    rs.getInt("used_quota"),
                    rs.getInt("remaining_quota"),
                    rs.getInt("quota_version")
            );
        }, batchId, majorCode);
    }

    private QuotaRow lockSchoolQuotaById(Long quotaId) {
        return jdbcTemplate.query("""
                SELECT id, batch_id, school_code, total_quota, used_quota, remaining_quota, quota_version
                FROM school_quotas
                WHERE id = ?
                FOR UPDATE
                """, rs -> {
            if (!rs.next()) {
                throw new IllegalArgumentException("school quota not found: " + quotaId);
            }
            return new QuotaRow(
                    rs.getLong("id"),
                    rs.getLong("batch_id"),
                    rs.getString("school_code"),
                    null,
                    rs.getInt("total_quota"),
                    rs.getInt("used_quota"),
                    rs.getInt("remaining_quota"),
                    rs.getInt("quota_version")
            );
        }, quotaId);
    }

    private QuotaRow lockMajorQuotaById(Long quotaId) {
        return jdbcTemplate.query("""
                SELECT id, batch_id, school_code, major_code, total_quota, used_quota, remaining_quota, quota_version
                FROM major_quotas
                WHERE id = ?
                FOR UPDATE
                """, rs -> {
            if (!rs.next()) {
                throw new IllegalArgumentException("major quota not found: " + quotaId);
            }
            return new QuotaRow(
                    rs.getLong("id"),
                    rs.getLong("batch_id"),
                    rs.getString("school_code"),
                    rs.getString("major_code"),
                    rs.getInt("total_quota"),
                    rs.getInt("used_quota"),
                    rs.getInt("remaining_quota"),
                    rs.getInt("quota_version")
            );
        }, quotaId);
    }

    private void updateSchoolQuotaWithVersion(QuotaRow current, QuotaRow next) {
        int updated = jdbcTemplate.update("""
                UPDATE school_quotas
                SET used_quota = ?, remaining_quota = ?, quota_version = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ? AND quota_version = ?
                """,
                next.usedQuota(),
                next.remainingQuota(),
                next.version(),
                current.id(),
                current.version()
        );
        if (updated == 0) {
            throw new IllegalStateException("Concurrent school quota reservation detected");
        }
    }

    private void updateMajorQuotaWithVersion(QuotaRow current, QuotaRow next) {
        int updated = jdbcTemplate.update("""
                UPDATE major_quotas
                SET used_quota = ?, remaining_quota = ?, quota_version = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ? AND quota_version = ?
                """,
                next.usedQuota(),
                next.remainingQuota(),
                next.version(),
                current.id(),
                current.version()
        );
        if (updated == 0) {
            throw new IllegalStateException("Concurrent major quota reservation detected");
        }
    }

    private Map<String, Object> getSchoolQuota(Long id) {
        return jdbcTemplate.queryForMap("""
                SELECT id, batch_id, school_code, total_quota, used_quota, remaining_quota, school_min_score, school_min_math,
                       school_min_english, quota_version, updated_at
                FROM school_quotas
                WHERE id = ?
                """, id);
    }

    private Map<String, Object> getMajorQuota(Long id) {
        return jdbcTemplate.queryForMap("""
                SELECT id, batch_id, school_code, major_code, total_quota, used_quota, remaining_quota,
                       min_average_score, min_math_score, min_english_score, min_physics_score, min_liberal_arts_score,
                       reserve_line, waitlist_line, allow_adjustment_in, quota_version, updated_at
                FROM major_quotas
                WHERE id = ?
                """, id);
    }

    private record QuotaRow(
            Long id,
            Long batchId,
            String schoolCode,
            String majorCode,
            Integer totalQuota,
            Integer usedQuota,
            Integer remainingQuota,
            Integer version
    ) {
        private QuotaRow reserveOne() {
            return new QuotaRow(
                    id,
                    batchId,
                    schoolCode,
                    majorCode,
                    totalQuota,
                    usedQuota + 1,
                    remainingQuota - 1,
                    version + 1
            );
        }

        private QuotaRow releaseOne() {
            return new QuotaRow(
                    id,
                    batchId,
                    schoolCode,
                    majorCode,
                    totalQuota,
                    usedQuota - 1,
                    remainingQuota + 1,
                    version + 1
            );
        }

        private Map<String, Object> toAuditMap() {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("batch_id", batchId);
            payload.put("school_code", schoolCode);
            payload.put("major_code", majorCode);
            payload.put("total_quota", totalQuota);
            payload.put("used_quota", usedQuota);
            payload.put("remaining_quota", remainingQuota);
            payload.put("quota_version", version);
            return payload;
        }
    }
}
