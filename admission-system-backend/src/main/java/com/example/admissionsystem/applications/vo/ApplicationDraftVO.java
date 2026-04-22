package com.example.admissionsystem.applications.vo;

import com.example.admissionsystem.applications.model.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDraftVO {

    private Long applicationId;
    private ApplicationStatus status;
    private Long batchId;
    private String targetSchoolCode;
    private String targetMajorCode;
}
