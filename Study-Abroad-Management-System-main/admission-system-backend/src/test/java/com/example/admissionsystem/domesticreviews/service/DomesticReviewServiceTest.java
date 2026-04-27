package com.example.admissionsystem.domesticreviews.service;

import com.example.admissionsystem.applications.model.ApplicationStatus;
import com.example.admissionsystem.applications.service.ApplicationStatusService;
import com.example.admissionsystem.auth.model.UserRole;
import com.example.admissionsystem.domesticreviews.dto.DomesticReviewSubmitRequest;
import com.example.admissionsystem.support.ServiceTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DomesticReviewServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private ApplicationStatusService applicationStatusService;

    private DomesticReviewService domesticReviewService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        domesticReviewService = new DomesticReviewService(jdbcTemplate, applicationStatusService);
    }

    @AfterEach
    void tearDown() {
        ServiceTestSupport.clearSecurityContext();
    }

    @Test
    void claimShouldMoveSubmittedApplicationToDomesticReviewing() {
        ServiceTestSupport.loginAs(21L, "domestic01", UserRole.DOMESTIC_REVIEWER, null);
        mockApplication(201L, ApplicationStatus.SUBMITTED, 9001L);

        domesticReviewService.claim(201L);

        verify(applicationStatusService).transition(
                eq(201L),
                eq(ApplicationStatus.SUBMITTED),
                eq(ApplicationStatus.DOMESTIC_REVIEWING),
                any(),
                eq(ApplicationStatusService.ACTION_CLAIM_DOMESTIC_REVIEW),
                contains("claimed"),
                anyMap(),
                anyMap(),
                contains("UPDATE applications"),
                any(),
                any()
        );
    }

    @Test
    void submitPassShouldMoveToSchoolReviewingAndInsertDomesticReviewAudit() {
        ServiceTestSupport.loginAs(21L, "domestic01", UserRole.DOMESTIC_REVIEWER, null);
        mockApplication(202L, ApplicationStatus.DOMESTIC_REVIEWING, 9002L);
        when(jdbcTemplate.query(contains("FROM application_status_histories"), any(ResultSetExtractor.class), eq(202L), eq(ApplicationStatusService.ACTION_CLAIM_DOMESTIC_REVIEW)))
                .thenReturn(21L);
        doAnswer(invocation -> {
            GeneratedKeyHolder keyHolder = invocation.getArgument(1);
            ServiceTestSupport.setGeneratedKey(keyHolder, 501L);
            return 1;
        }).when(jdbcTemplate).update(any(org.springframework.jdbc.core.PreparedStatementCreator.class), any(GeneratedKeyHolder.class));

        DomesticReviewSubmitRequest request = new DomesticReviewSubmitRequest();
        request.setMaterialComplete(true);
        request.setIdentityMatched(true);
        request.setBasicScorePassed(true);
        request.setAuthenticityRiskLevel("B");
        request.setStandardizationPassed(true);
        request.setResult("PASS");
        request.setComment("ok");

        domesticReviewService.submit(202L, request);

        verify(applicationStatusService).transition(
                eq(202L),
                eq(ApplicationStatus.DOMESTIC_REVIEWING),
                eq(ApplicationStatus.SCHOOL_REVIEWING),
                any(),
                eq(ApplicationStatusService.ACTION_SUBMIT_DOMESTIC_REVIEW),
                contains("passed"),
                anyMap(),
                anyMap(),
                contains("UPDATE applications"),
                any(),
                any()
        );
        verify(applicationStatusService).insertAuditLog(any(), eq("DOMESTIC_REVIEW"), eq("501"), eq(ApplicationStatusService.ACTION_SUBMIT_DOMESTIC_REVIEW), isNull(), anyMap(), contains("submitted"));
    }

    private void mockApplication(Long id, ApplicationStatus status, Long fileId) {
        Object applicationRecord = ServiceTestSupport.newPrivateRecord(
                DomesticReviewService.class,
                "ApplicationRecord",
                new Class<?>[]{
                        Long.class, Long.class, Long.class, Long.class, Long.class,
                        String.class, String.class, Long.class, ApplicationStatus.class, LocalDateTime.class, Long.class
                },
                id, 1L, 1L, 1L, 1L, "USYD", "USYD_SE", 11L, status, LocalDateTime.now(), fileId
        );
        when(jdbcTemplate.query(contains("FROM applications a"), any(ResultSetExtractor.class), eq(id))).thenReturn(applicationRecord);
    }
}
