package com.example.admissionsystem.admin.controller;

import com.example.admissionsystem.admin.dto.MajorSaveRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/majors")
@RequiredArgsConstructor
public class MajorAdminController {

    private final AdminConfigService adminConfigService;

    @GetMapping
    public Result<List<Map<String, Object>>> list(@RequestParam(required = false) String schoolCode) {
        return Result.success(adminConfigService.listMajors(schoolCode));
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody MajorSaveRequest request) {
        return Result.success(adminConfigService.createMajor(request));
    }

    @PutMapping("/{majorCode}")
    public Result<Map<String, Object>> update(@PathVariable String majorCode, @RequestBody MajorSaveRequest request) {
        return Result.success(adminConfigService.updateMajor(majorCode, request));
    }

    @PostMapping("/{majorCode}/status")
    public Result<Map<String, Object>> updateStatus(@PathVariable String majorCode, @RequestBody StatusUpdateRequest request) {
        return Result.success(adminConfigService.updateMajorStatus(majorCode, request));
    }
}
