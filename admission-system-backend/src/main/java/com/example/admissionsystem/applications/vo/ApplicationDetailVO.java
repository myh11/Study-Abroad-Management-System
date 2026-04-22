package com.example.admissionsystem.applications.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDetailVO {

    private Map<String, Object> basicInfo;
    private Map<String, Object> studentInfo;
    private Map<String, Object> applicationInfo;
    private Map<String, Object> scoreSummary;
    private Map<String, Object> files;
    private Map<String, Object> domesticReviewSummary;
    private Map<String, Object> schoolReviewSummary;
    private Map<String, Object> adjustmentSummary;
    private Map<String, Object> ruleSnapshot;
    private List<Map<String, Object>> statusHistory;
    private List<Map<String, Object>> auditLogs;
}
