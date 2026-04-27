package com.example.admissionsystem.admin.controller;

import com.example.admissionsystem.admin.dto.StatusUpdateRequest;
import com.example.admissionsystem.admin.dto.UserSaveRequest;
import com.example.admissionsystem.admin.dto.UserUpdateRequest;
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
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final AdminConfigService adminConfigService;

    @GetMapping
    public Result<List<Map<String, Object>>> list() {
        return Result.success(adminConfigService.listUsers());
    }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody UserSaveRequest request) {
        return Result.success(adminConfigService.createUser(request));
    }

    @PutMapping("/{id}")
    public Result<Map<String, Object>> update(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        return Result.success(adminConfigService.updateUser(id, request));
    }

    @PostMapping("/{id}/status")
    public Result<Map<String, Object>> updateStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request) {
        return Result.success(adminConfigService.updateUserStatus(id, request));
    }
}
