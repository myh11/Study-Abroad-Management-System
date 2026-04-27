package com.example.admissionsystem.common.exception;

import com.example.admissionsystem.applications.model.ApplicationStatus;

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(ApplicationStatus from, ApplicationStatus to) {
        super("Invalid application status transition: " + from + " -> " + to);
    }
}
