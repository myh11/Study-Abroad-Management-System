package com.example.admissionsystem.dashboard.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryVO {

    private String role;
    private String schoolCode;
    private List<DashboardMetricVO> metrics;
}
