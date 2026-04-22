package com.example.admissionsystem.waitlists.controller;

import com.example.admissionsystem.common.response.Result;
import com.example.admissionsystem.waitlists.service.WaitlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/waitlists")
@RequiredArgsConstructor
public class WaitlistController {

    private final WaitlistService waitlistService;

    @GetMapping("/{applicationId}")
    public Result<Map<String, Object>> detail(@PathVariable Long applicationId) {
        return Result.success(waitlistService.getWaitlist(applicationId));
    }

    @PostMapping("/{applicationId}/promote")
    public Result<String> promote(@PathVariable Long applicationId) {
        return Result.success(waitlistService.promote(applicationId));
    }

    @PostMapping("/{applicationId}/invalidate")
    public Result<String> invalidate(@PathVariable Long applicationId) {
        return Result.success(waitlistService.invalidate(applicationId));
    }
}
