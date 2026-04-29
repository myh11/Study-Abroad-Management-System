package com.example.admissionsystem.dashboard.service;

import com.example.admissionsystem.auth.model.UserRole;
import com.example.admissionsystem.auth.security.AuthUserPrincipal;
import com.example.admissionsystem.dashboard.vo.DashboardMetricVO;
import com.example.admissionsystem.dashboard.vo.DashboardSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final JdbcTemplate jdbcTemplate;

    public DashboardSummaryVO summary() {
        AuthUserPrincipal currentUser = requireCurrentUser();
        return switch (currentUser.getRole()) {
            case AGENT -> buildAgentSummary(currentUser);
            case DOMESTIC_REVIEWER -> buildDomesticSummary(currentUser);
            case SCHOOL_REVIEWER -> buildSchoolSummary(currentUser);
            case ADMIN -> buildAdminSummary(currentUser);
        };
    }

    private DashboardSummaryVO buildAgentSummary(AuthUserPrincipal currentUser) {
        return DashboardSummaryVO.builder()
                .role(currentUser.getRole().name())
                .schoolCode(currentUser.getSchoolCode())
                .metrics(List.of(
                        metric("myApplications", "我的申请总数",
                                queryCount("SELECT COUNT(*) FROM applications WHERE created_by_agent_id = ?", currentUser.getUserId())),
                        metric("drafts", "草稿数",
                                queryCount("SELECT COUNT(*) FROM applications WHERE created_by_agent_id = ? AND current_status = 'DRAFT'", currentUser.getUserId())),
                        metric("supplement", "待补件",
                                queryCount("SELECT COUNT(*) FROM applications WHERE created_by_agent_id = ? AND current_status = 'DOMESTIC_SUPPLEMENT'", currentUser.getUserId())),
                        metric("waitlistPending", "候补待确认",
                                queryCount("SELECT COUNT(*) FROM applications WHERE created_by_agent_id = ? AND current_status = 'WAITLIST_PENDING_CONFIRM'", currentUser.getUserId()))
                ))
                .build();
    }

    private DashboardSummaryVO buildDomesticSummary(AuthUserPrincipal currentUser) {
        return DashboardSummaryVO.builder()
                .role(currentUser.getRole().name())
                .schoolCode(currentUser.getSchoolCode())
                .metrics(List.of(
                        metric("submitted", "待认领",
                                queryCount("SELECT COUNT(*) FROM applications WHERE current_status = 'SUBMITTED'")),
                        metric("reviewing", "审核中",
                                queryCount("SELECT COUNT(*) FROM applications WHERE current_status = 'DOMESTIC_REVIEWING'")),
                        metric("supplement", "补件结果",
                                queryCount("SELECT COUNT(*) FROM applications WHERE current_status = 'DOMESTIC_SUPPLEMENT'")),
                        metric("rejected", "拒绝结果",
                                queryCount("SELECT COUNT(*) FROM applications WHERE current_status = 'DOMESTIC_REJECTED'"))
                ))
                .build();
    }

    private DashboardSummaryVO buildSchoolSummary(AuthUserPrincipal currentUser) {
        return DashboardSummaryVO.builder()
                .role(currentUser.getRole().name())
                .schoolCode(currentUser.getSchoolCode())
                .metrics(List.of(
                        metric("reviewing", "学校审核中",
                                queryCount("SELECT COUNT(*) FROM applications WHERE target_school_code = ? AND current_status = 'SCHOOL_REVIEWING'", currentUser.getSchoolCode())),
                        metric("waitlisted", "候补中",
                                queryCount("SELECT COUNT(*) FROM applications WHERE target_school_code = ? AND current_status IN ('WAITLISTED', 'WAITLIST_PENDING_CONFIRM')", currentUser.getSchoolCode())),
                        metric("reserved", "预录取",
                                queryCount("SELECT COUNT(*) FROM applications WHERE target_school_code = ? AND current_status = 'RESERVED'", currentUser.getSchoolCode())),
                        metric("adjustment", "调剂建议",
                                queryCount("SELECT COUNT(*) FROM applications WHERE target_school_code = ? AND current_status = 'ADJUSTMENT_SUGGESTED'", currentUser.getSchoolCode()))
                ))
                .build();
    }

    private DashboardSummaryVO buildAdminSummary(AuthUserPrincipal currentUser) {
        return DashboardSummaryVO.builder()
                .role(currentUser.getRole().name())
                .schoolCode(currentUser.getSchoolCode())
                .metrics(List.of(
                        metric("applications", "申请总数", queryCount("SELECT COUNT(*) FROM applications")),
                        metric("users", "用户总数", queryCount("SELECT COUNT(*) FROM users")),
                        metric("activeWaitlists", "活跃候补", queryCount("SELECT COUNT(*) FROM waitlist_records WHERE waitlist_status = 'ACTIVE'")),
                        metric("batches", "批次数", queryCount("SELECT COUNT(*) FROM admission_batches"))
                ))
                .build();
    }

    private DashboardMetricVO metric(String key, String label, Long value) {
        return DashboardMetricVO.builder().key(key).label(label).value(value).build();
    }

    private Long queryCount(String sql, Object... args) {
        Long count = jdbcTemplate.queryForObject(sql, Long.class, args);
        return count == null ? 0L : count;
    }

    private AuthUserPrincipal requireCurrentUser() {
        Object principal = getContext().getAuthentication() == null ? null : getContext().getAuthentication().getPrincipal();
        if (principal instanceof AuthUserPrincipal authUserPrincipal) {
            return authUserPrincipal;
        }
        throw new IllegalArgumentException("Current user not found");
    }
}
