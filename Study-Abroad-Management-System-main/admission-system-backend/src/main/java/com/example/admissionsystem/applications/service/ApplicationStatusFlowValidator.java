package com.example.admissionsystem.applications.service;

import com.example.admissionsystem.applications.model.ApplicationStatus;
import com.example.admissionsystem.common.exception.InvalidStatusTransitionException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class ApplicationStatusFlowValidator {

    private static final Map<ApplicationStatus, Set<ApplicationStatus>> ALLOWED_TRANSITIONS =
            new EnumMap<>(ApplicationStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(ApplicationStatus.DRAFT, EnumSet.of(ApplicationStatus.SUBMITTED));
        ALLOWED_TRANSITIONS.put(ApplicationStatus.SUBMITTED, EnumSet.of(
                ApplicationStatus.DOMESTIC_REVIEWING,
                ApplicationStatus.CANCELED
        ));
        ALLOWED_TRANSITIONS.put(ApplicationStatus.DOMESTIC_REVIEWING, EnumSet.of(
                ApplicationStatus.SCHOOL_REVIEWING,
                ApplicationStatus.DOMESTIC_SUPPLEMENT,
                ApplicationStatus.DOMESTIC_REJECTED
        ));
        ALLOWED_TRANSITIONS.put(ApplicationStatus.DOMESTIC_SUPPLEMENT, EnumSet.of(ApplicationStatus.SUBMITTED));
        ALLOWED_TRANSITIONS.put(ApplicationStatus.DOMESTIC_REJECTED, EnumSet.of(ApplicationStatus.CLOSED));
        ALLOWED_TRANSITIONS.put(ApplicationStatus.SCHOOL_REVIEWING, EnumSet.of(
                ApplicationStatus.RESERVED,
                ApplicationStatus.ADJUSTMENT_SUGGESTED,
                ApplicationStatus.WAITLISTED,
                ApplicationStatus.SCHOOL_REJECTED,
                ApplicationStatus.CANCELED
        ));
        ALLOWED_TRANSITIONS.put(ApplicationStatus.WAITLISTED, EnumSet.of(
                ApplicationStatus.WAITLIST_PENDING_CONFIRM,
                ApplicationStatus.CANCELED,
                ApplicationStatus.CLOSED
        ));
        ALLOWED_TRANSITIONS.put(ApplicationStatus.WAITLIST_PENDING_CONFIRM, EnumSet.of(
                ApplicationStatus.RESERVED,
                ApplicationStatus.CLOSED
        ));
        ALLOWED_TRANSITIONS.put(ApplicationStatus.RESERVED, EnumSet.of(ApplicationStatus.CLOSED));
        ALLOWED_TRANSITIONS.put(ApplicationStatus.ADJUSTMENT_SUGGESTED, EnumSet.of(ApplicationStatus.CLOSED));
        ALLOWED_TRANSITIONS.put(ApplicationStatus.SCHOOL_REJECTED, EnumSet.of(ApplicationStatus.CLOSED));
        ALLOWED_TRANSITIONS.put(ApplicationStatus.CANCELED, EnumSet.of(ApplicationStatus.CLOSED));
        ALLOWED_TRANSITIONS.put(ApplicationStatus.CLOSED, EnumSet.noneOf(ApplicationStatus.class));
    }

    public boolean canTransfer(ApplicationStatus from, ApplicationStatus to) {
        return ALLOWED_TRANSITIONS.getOrDefault(from, EnumSet.noneOf(ApplicationStatus.class)).contains(to);
    }

    public void assertCanTransfer(ApplicationStatus from, ApplicationStatus to) {
        if (!canTransfer(from, to)) {
            throw new InvalidStatusTransitionException(from, to);
        }
    }
}
