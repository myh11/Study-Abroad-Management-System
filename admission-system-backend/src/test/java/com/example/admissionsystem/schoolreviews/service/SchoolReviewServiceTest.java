package com.example.admissionsystem.schoolreviews.service;

import com.example.admissionsystem.applications.model.ApplicationStatus;
import com.example.admissionsystem.applications.service.ApplicationStatusService;
import com.example.admissionsystem.auth.model.UserRole;
import com.example.admissionsystem.quotas.service.QuotaConcurrencyService;
import com.example.admissionsystem.schoolreviews.dto.SchoolReviewSubmitRequest;
import com.example.admissionsystem.support.ServiceTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchoolReviewServiceTest {

    @Mock
    private ApplicationStatusService applicationStatusService;
    @Mock
    private QuotaConcurrencyService quotaConcurrencyService;
    @Mock
    private JdbcTemplate jdbcTemplate;

    private SchoolReviewService schoolReviewService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        schoolReviewService = new SchoolReviewService(applicationStatusService, quotaConcurrencyService, jdbcTemplate);
    }

    @AfterEach
    void tearDown() {
        ServiceTestSupport.clearSecurityContext();
    }

    @Test
    void reserveShouldCallQuotaReservationAndTransition() {
        ServiceTestSupport.loginAs(31L, "school_anu_01", UserRole.SCHOOL_REVIEWER, "ANU");
        mockApplication(301L, "ANU", "ANU_IR");
        mockGeneratedKeySequence(701L);

        SchoolReviewSubmitRequest request = baseRequest("RESERVE");
        schoolReviewService.submit(301L, request);

        verify(quotaConcurrencyService).reserveQuota(eq(1L), eq("ANU"), eq("ANU_IR"), any(), eq(ApplicationStatusService.ACTION_SUBMIT_SCHOOL_REVIEW), contains("301"));
        verify(applicationStatusService).transition(
                eq(301L),
                eq(ApplicationStatus.SCHOOL_REVIEWING),
                eq(ApplicationStatus.RESERVED),
                any(),
                eq(ApplicationStatusService.ACTION_SUBMIT_SCHOOL_REVIEW),
                contains("reserved"),
                anyMap(),
                anyMap(),
                contains("UPDATE applications"),
                any(),
                any(),
                any()
        );
    }

    @Test
    void waitlistShouldCreateWaitlistAndTransition() {
        ServiceTestSupport.loginAs(31L, "school_anu_01", UserRole.SCHOOL_REVIEWER, "ANU");
        mockApplication(302L, "ANU", "ANU_IR");
        mockGeneratedKeySequence(702L, 703L);
        when(jdbcTemplate.queryForObject(contains("COALESCE(MAX(current_rank), 0) + 1"), eq(Integer.class), eq(1L), eq("ANU"), eq("ANU_IR")))
                .thenReturn(1);

        SchoolReviewSubmitRequest request = baseRequest("WAITLIST");
        schoolReviewService.submit(302L, request);

        verify(applicationStatusService).transition(
                eq(302L),
                eq(ApplicationStatus.SCHOOL_REVIEWING),
                eq(ApplicationStatus.WAITLISTED),
                any(),
                eq(ApplicationStatusService.ACTION_SUBMIT_SCHOOL_REVIEW),
                contains("waitlist"),
                anyMap(),
                anyMap(),
                contains("UPDATE applications"),
                any(),
                any()
        );
    }

    private void mockApplication(Long id, String schoolCode, String majorCode) {
        Object applicationRecord = ServiceTestSupport.newPrivateRecord(
                SchoolReviewService.class,
                "ApplicationRecord",
                new Class<?>[]{Long.class, Long.class, Long.class, Long.class, Long.class, String.class, String.class, Long.class, ApplicationStatus.class},
                id, 1L, 1L, 1L, 1L, schoolCode, majorCode, 11L, ApplicationStatus.SCHOOL_REVIEWING
        );
        when(jdbcTemplate.query(contains("FROM applications"), any(ResultSetExtractor.class), eq(id))).thenReturn(applicationRecord);
    }

    private void mockGeneratedKeySequence(Long first, Long... rest) {
        doAnswer(invocation -> {
            GeneratedKeyHolder keyHolder = invocation.getArgument(1);
            ServiceTestSupport.setGeneratedKey(keyHolder, first);
            return 1;
        }).doAnswer(invocation -> {
            GeneratedKeyHolder keyHolder = invocation.getArgument(1);
            ServiceTestSupport.setGeneratedKey(keyHolder, rest.length > 0 ? rest[0] : first + 1);
            return 1;
        }).when(jdbcTemplate).update(any(org.springframework.jdbc.core.PreparedStatementCreator.class), any(GeneratedKeyHolder.class));
    }

    private SchoolReviewSubmitRequest baseRequest(String result) {
        SchoolReviewSubmitRequest request = new SchoolReviewSubmitRequest();
        request.setResult(result);
        request.setSchoolThresholdPassed(true);
        request.setMajorThresholdPassed(true);
        request.setSchoolQuotaPassed(true);
        request.setMajorQuotaPassed(true);
        request.setAcademicScore(90);
        request.setMaterialScore(88);
        request.setMatchingScore(87);
        request.setTotalScore(265);
        request.setReason("test");
        return request;
    }
}
