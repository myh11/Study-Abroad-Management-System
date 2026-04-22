package com.example.admissionsystem.admin.controller;

import com.example.admissionsystem.admin.dto.QuotaAdjustRequest;
import com.example.admissionsystem.admin.service.AdminConfigService;
import com.example.admissionsystem.common.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quotas")
@RequiredArgsConstructor
public class QuotaAdminController {

    private final AdminConfigService adminConfigService;

    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) String schoolCode
    ) {
        return Result.success(adminConfigService.listQuotas(batchId, schoolCode));
    }

    @GetMapping("/adjustments")
    public Result<List<Map<String, Object>>> adjustments(
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) String schoolCode,
            @RequestParam(required = false) String majorCode
    ) {
        return Result.success(adminConfigService.listQuotaAdjustmentLogs(batchId, schoolCode, majorCode));
    }

    @PostMapping("/schools/{id}/adjust")
    public Result<Map<String, Object>> adjustSchoolQuota(@PathVariable Long id, @RequestBody(required = false) QuotaAdjustRequest request) {
        return Result.success(adminConfigService.adjustSchoolQuota(id, request));
    }

    @PostMapping("/majors/{id}/adjust")
    public Result<Map<String, Object>> adjustMajorQuota(@PathVariable Long id, @RequestBody(required = false) QuotaAdjustRequest request) {
        return Result.success(adminConfigService.adjustMajorQuota(id, request));
    }
}
