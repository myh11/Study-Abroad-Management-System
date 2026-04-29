package com.example.admissionsystem.applications.service;

import com.example.admissionsystem.applications.dto.WaitlistConfirmRequest;
import com.example.admissionsystem.applications.model.ApplicationStatus;
import com.example.admissionsystem.auth.model.UserRole;
import com.example.admissionsystem.quotas.service.QuotaConcurrencyService;
import com.example.admissionsystem.support.ServiceTestSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationCommandServiceTest {

    @Mock
    private ApplicationStatusService applicationStatusService;
    @Mock
    private QuotaConcurrencyService quotaConcurrencyService;
    @Mock
    private ApplicationConstraintService applicationConstraintService;
    @Mock
    private JdbcTemplate jdbcTemplate;

    private ApplicationCommandService applicationCommandService;

    @BeforeEach
    void setUp() {
        applicationCommandService = new ApplicationCommandService(
                applicationStatusService,
                quotaConcurrencyService,
                applicationConstraintService,
                jdbcTemplate,
                new ObjectMapper()
        );
    }

    @AfterEach
    void tearDown() {
        ServiceTestSupport.clearSecurityContext();
    }

    @Test
    void waitlistConfirmAcceptShouldMoveToReservedWithoutDoubleReserve() {
        ServiceTestSupport.loginAs(11L, "agent01", UserRole.AGENT, null);
        mockApplication(101L, ApplicationStatus.WAITLIST_PENDING_CONFIRM, 11L, 1L, "ANU", "ANU_IR");
        mockWaitlist(1001L, "PROMOTED");

        WaitlistConfirmRequest request = new WaitlistConfirmRequest();
        request.setAccept(true);

        applicationCommandService.waitlistConfirm(101L, request);

        verify(quotaConcurrencyService, never()).reserveQuota(anyLong(), anyString(), anyString(), any(), anyString(), anyString());
        verify(quotaConcurrencyService, never()).releaseQuota(anyLong(), anyString(), anyString(), any(), anyString(), anyString());
        verify(applicationStatusService).transition(
                eq(101L),
                eq(ApplicationStatus.WAITLIST_PENDING_CONFIRM),
                eq(ApplicationStatus.RESERVED),
                any(),
                eq(ApplicationStatusService.ACTION_WAITLIST_CONFIRM),
                contains("accepted"),
                anyMap(),
                anyMap(),
                contains("UPDATE applications"),
                any(),
                any(),
                any()
        );
    }

    @Test
    void waitlistConfirmDeclineShouldReleaseQuota() {
        ServiceTestSupport.loginAs(11L, "agent01", UserRole.AGENT, null);
        mockApplication(102L, ApplicationStatus.WAITLIST_PENDING_CONFIRM, 11L, 1L, "ANU", "ANU_IR");
        mockWaitlist(1002L, "PROMOTED");

        WaitlistConfirmRequest request = new WaitlistConfirmRequest();
        request.setAccept(false);

        applicationCommandService.waitlistConfirm(102L, request);

        verify(quotaConcurrencyService).releaseQuota(eq(1L), eq("ANU"), eq("ANU_IR"), any(), eq(ApplicationStatusService.ACTION_WAITLIST_CONFIRM), contains("102"));
        verify(applicationStatusService).transition(
                eq(102L),
                eq(ApplicationStatus.WAITLIST_PENDING_CONFIRM),
                eq(ApplicationStatus.CLOSED),
                any(),
                eq(ApplicationStatusService.ACTION_WAITLIST_CONFIRM),
                contains("declined"),
                anyMap(),
                anyMap(),
                contains("UPDATE applications"),
                any(),
                any(),
                any(),
                any()
        );
    }

    @SuppressWarnings("unchecked")
    private void mockApplication(Long id, ApplicationStatus status, Long createdByAgentId, Long batchId, String schoolCode, String majorCode) {
        Object applicationRecord = ServiceTestSupport.newPrivateRecord(
                ApplicationCommandService.class,
                "ApplicationRecord",
                new Class<?>[]{
                        Long.class, Long.class, Long.class, Long.class, Long.class,
                        String.class, String.class, Long.class, Long.class,
                        ApplicationStatus.class, Long.class, LocalDateTime.class
                },
                id, 1L, 1L, 1L, batchId, schoolCode, majorCode, null, createdByAgentId, status, null, ServiceTestSupport.nowPlusHours(24)
        );
        when(jdbcTemplate.query(contains("FROM applications"), any(ResultSetExtractor.class), eq(id))).thenReturn(applicationRecord);
    }

    @SuppressWarnings("unchecked")
    private void mockWaitlist(Long id, String status) {
        Object waitlistRecord = ServiceTestSupport.newPrivateRecord(
                ApplicationCommandService.class,
                "WaitlistRecord",
                new Class<?>[]{Long.class, String.class, LocalDateTime.class, LocalDateTime.class, LocalDateTime.class},
                id, status, LocalDateTime.now(), ServiceTestSupport.nowPlusHours(24), null
        );
        when(jdbcTemplate.query(contains("FROM waitlist_records"), any(ResultSetExtractor.class), anyLong())).thenReturn(Optional.of(waitlistRecord));
    }
}
