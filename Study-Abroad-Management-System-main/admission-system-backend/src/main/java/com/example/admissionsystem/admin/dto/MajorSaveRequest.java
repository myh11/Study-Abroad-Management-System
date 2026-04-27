package com.example.admissionsystem.admin.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MajorSaveRequest {

    private String majorCode;
    private String schoolCode;
    private String majorName;
    private BigDecimal minAverageScore;
    private BigDecimal minMathScore;
    private BigDecimal minEnglishScore;
    private BigDecimal minPhysicsScore;
    private BigDecimal minLiberalArtsScore;
    private BigDecimal reserveLine;
    private BigDecimal waitlistLine;
    private Boolean allowAdjustmentIn;
    private Boolean enabled;
}
