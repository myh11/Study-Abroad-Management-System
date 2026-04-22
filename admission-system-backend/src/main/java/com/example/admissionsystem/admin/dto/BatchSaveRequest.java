package com.example.admissionsystem.admin.dto;

import lombok.Data;

@Data
public class BatchSaveRequest {

    private String batchName;
    private String startTime;
    private String endTime;
    private String batchStatus;
}
