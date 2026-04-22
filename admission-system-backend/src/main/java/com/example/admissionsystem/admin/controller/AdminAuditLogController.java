package com.example.admissionsystem.admin.controller;

import com.example.admissionsystem.admin.service.AdminConfigService;
import com.example.admissionsystem.common.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final AdminConfigService adminConfigService;

    @GetMapping
    public Result<List<Map<String, Object>>> list(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String entityId,
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) Integer limit
    ) {
        return Result.success(adminConfigService.listAuditLogs(entityType, entityId, operatorId, limit));
    }

    @GetMapping("/status-histories")
    public Result<List<Map<String, Object>>> statusHistories(
            @RequestParam Long applicationId,
            @RequestParam(required = false) Integer limit
    ) {
        return Result.success(adminConfigService.listStatusHistories(applicationId, limit));
    }
}
