package com.example.admissionsystem.domesticreviews.dto;

import lombok.Data;

@Data
public class DomesticReviewSubmitRequest {

    private Boolean materialComplete;
    private Boolean identityMatched;
    private Boolean basicScorePassed;
    private String authenticityRiskLevel;
    private Boolean standardizationPassed;
    private String result;
    private String comment;
}
