package com.example.admissionsystem.admin.dto;

import lombok.Data;

@Data
public class UserSaveRequest {

    private String username;
    private String password;
    private String roleType;
    private String schoolCode;
    private Boolean enabled;
    private Boolean mustChangePassword;
}
