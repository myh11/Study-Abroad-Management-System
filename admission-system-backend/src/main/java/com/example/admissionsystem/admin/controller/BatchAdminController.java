package com.example.admissionsystem.admin.controller;

import com.example.admissionsystem.admin.dto.BatchSaveRequest;
import com.example.admissionsystem.admin.dto.StatusUpdateRequest;
import com.example.admissionsystem.admin.service.AdminConfigService;
import com.example.admissionsystem.common.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/batches")
@RequiredArgsConstructor
public class BatchAdminController {

    private final AdminConfigService adminConfigService;

    @GetMapping
    public Result<List<Map<String, Object>>> list() {
        return Result.success(adminConfigService.listBatches());
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody BatchSaveRequest request) {
        return Result.success(adminConfigService.createBatch(request));
    }

    @PutMapping("/{id}")
    public Result<Map<String, Object>> update(@PathVariable Long id, @RequestBody BatchSaveRequest request) {
        return Result.success(adminConfigService.updateBatch(id, request));
    }

    @PostMapping("/{id}/status")
    public Result<Map<String, Object>> updateStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request) {
        return Result.success(adminConfigService.updateBatchStatus(id, request));
    }
}
