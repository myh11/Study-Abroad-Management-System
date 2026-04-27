package com.example.admissionsystem.waitlists.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WaitlistScheduler {

    private final WaitlistService waitlistService;

    @Scheduled(fixedDelayString = "${app.waitlist.timeout-scan-ms:60000}")
    public void expireTimedOutPromotions() {
        try {
            waitlistService.expireTimedOutPromotions();
        } catch (Exception ex) {
            log.error("Failed to expire timed out waitlist promotions", ex);
        }
    }

    @Scheduled(fixedDelayString = "${app.waitlist.batch-close-scan-ms:300000}")
    public void closeFinishedBatchWaitlists() {
        try {
            waitlistService.closeFinishedBatchWaitlists();
        } catch (Exception ex) {
            log.error("Failed to close finished batch waitlists", ex);
        }
    }
}
