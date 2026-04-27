package com.example.admissionsystem.applications.controller;

import com.example.admissionsystem.applications.dto.AcceptAdjustmentRequest;
import com.example.admissionsystem.applications.dto.CancelApplicationRequest;
import com.example.admissionsystem.applications.dto.WaitlistConfirmRequest;
import com.example.admissionsystem.applications.service.ApplicationCommandService;
import com.example.admissionsystem.common.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationCommandController {

    private final ApplicationCommandService applicationCommandService;

    @PostMapping("/{id}/submit")
    public Result<String> submit(@PathVariable Long id) {
        return Result.success(applicationCommandService.submit(id));
    }

    @PostMapping("/{id}/cancel")
    public Result<String> cancel(@PathVariable Long id, @RequestBody(required = false) CancelApplicationRequest request) {
        return Result.success(applicationCommandService.cancel(id, request));
    }

    @PostMapping("/{id}/waitlist-confirm")
    public Result<String> waitlistConfirm(
            @PathVariable Long id,
            @RequestBody(required = false) WaitlistConfirmRequest request
    ) {
        return Result.success(applicationCommandService.waitlistConfirm(id, request));
    }

    @PostMapping("/{id}/accept-adjustment")
    public Result<String> acceptAdjustment(
            @PathVariable Long id,
            @RequestBody(required = false) AcceptAdjustmentRequest request
    ) {
        return Result.success(applicationCommandService.acceptAdjustment(id, request));
    }

    @PostMapping("/{id}/reject-adjustment")
    public Result<String> rejectAdjustment(@PathVariable Long id) {
        return Result.success(applicationCommandService.rejectAdjustment(id));
    }

    @PostMapping("/{id}/close")
    public Result<String> close(@PathVariable Long id) {
        return Result.success(applicationCommandService.close(id));
    }
}
