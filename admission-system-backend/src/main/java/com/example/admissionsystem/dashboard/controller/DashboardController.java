package com.example.admissionsystem.dashboard.controller;

import com.example.admissionsystem.common.response.Result;
import com.example.admissionsystem.dashboard.service.DashboardService;
import com.example.admissionsystem.dashboard.vo.DashboardSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public Result<DashboardSummaryVO> summary() {
        return Result.success(dashboardService.summary());
    }
}
