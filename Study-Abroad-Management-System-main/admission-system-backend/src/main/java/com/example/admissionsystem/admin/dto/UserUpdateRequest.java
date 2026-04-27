package com.example.admissionsystem.admin.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {

    private String password;
    private String roleType;
    private String schoolCode;
    private Boolean enabled;
    private Boolean mustChangePassword;
}
