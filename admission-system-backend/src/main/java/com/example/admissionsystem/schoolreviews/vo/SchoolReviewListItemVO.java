package com.example.admissionsystem.schoolreviews.vo;

import com.example.admissionsystem.applications.model.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolReviewListItemVO {

    private Long applicationId;
    private String studentName;
    private String targetSchoolCode;
    private String targetMajorCode;
    private ApplicationStatus status;
    private Long batchId;
    private String updatedAt;
}
