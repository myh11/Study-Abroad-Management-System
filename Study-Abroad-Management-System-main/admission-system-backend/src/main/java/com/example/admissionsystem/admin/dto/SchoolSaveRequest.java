package com.example.admissionsystem.admin.dto;

import lombok.Data;

@Data
public class SchoolSaveRequest {

    private String schoolCode;
    private String schoolName;
    private Boolean enabled;
}
