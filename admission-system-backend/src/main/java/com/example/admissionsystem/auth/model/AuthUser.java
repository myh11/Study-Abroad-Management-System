package com.example.admissionsystem.auth.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuthUser {

    private Long id;
    private String username;
    private String passwordHash;
    private UserRole role;
    private String schoolCode;
    private Boolean enabled;
    private Boolean mustChangePassword;
    private Integer loginFailCount;
    private LocalDateTime lockedUntil;
}
