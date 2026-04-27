package com.example.admissionsystem.admin.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class QuotaAdjustRequest {

    private Integer totalQuota;
    private Integer deltaQuota;
    private BigDecimal schoolMinScore;
    private BigDecimal schoolMinMath;
    private BigDecimal schoolMinEnglish;
    private BigDecimal minAverageScore;
    private BigDecimal minMathScore;
    private BigDecimal minEnglishScore;
    private BigDecimal minPhysicsScore;
    private BigDecimal minLiberalArtsScore;
    private BigDecimal reserveLine;
    private BigDecimal waitlistLine;
    private Boolean allowAdjustmentIn;
    private String remark;
}
