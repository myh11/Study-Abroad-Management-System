package com.example.admissionsystem.schoolreviews.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class SchoolReviewSubmitRequest {

    @JsonAlias("school_threshold_passed")
    private Boolean schoolThresholdPassed;
    @JsonAlias("major_threshold_passed")
    private Boolean majorThresholdPassed;
    @JsonAlias("school_quota_passed")
    private Boolean schoolQuotaPassed;
    @JsonAlias("major_quota_passed")
    private Boolean majorQuotaPassed;
    @JsonAlias("academic_score")
    private Integer academicScore;
    @JsonAlias("material_score")
    private Integer materialScore;
    @JsonAlias("matching_score")
    private Integer matchingScore;
    @JsonAlias("total_score")
    private Integer totalScore;
    @JsonAlias({"review_result", "result"})
    private String result;
    @JsonAlias({"review_reason", "reason"})
    private String reason;
    @JsonAlias("suggested_major_code")
    private String suggestedMajorCode;
}
