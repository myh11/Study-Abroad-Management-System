package com.example.admissionsystem.admin.controller;

import com.example.admissionsystem.admin.dto.SchoolSaveRequest;
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
@RequestMapping("/api/schools")
@RequiredArgsConstructor
public class SchoolAdminController {

    private final AdminConfigService adminConfigService;

    @GetMapping
    public Result<List<Map<String, Object>>> list() {
        return Result.success(adminConfigService.listSchools());
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody SchoolSaveRequest request) {
        return Result.success(adminConfigService.createSchool(request));
    }

    @PutMapping("/{schoolCode}")
    public Result<Map<String, Object>> update(@PathVariable String schoolCode, @RequestBody SchoolSaveRequest request) {
        return Result.success(adminConfigService.updateSchool(schoolCode, request));
    }

    @PostMapping("/{schoolCode}/status")
    public Result<Map<String, Object>> updateStatus(@PathVariable String schoolCode, @RequestBody StatusUpdateRequest request) {
        return Result.success(adminConfigService.updateSchoolStatus(schoolCode, request));
    }
}
