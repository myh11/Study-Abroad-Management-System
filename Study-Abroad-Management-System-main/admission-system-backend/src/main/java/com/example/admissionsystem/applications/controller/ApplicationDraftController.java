package com.example.admissionsystem.applications.controller;

import com.example.admissionsystem.applications.dto.ApplicationDraftRequest;
import com.example.admissionsystem.applications.dto.ApplicationSupplementRequest;
import com.example.admissionsystem.applications.service.ApplicationDraftService;
import com.example.admissionsystem.applications.vo.ApplicationDetailVO;
import com.example.admissionsystem.applications.vo.ApplicationDraftVO;
import com.example.admissionsystem.applications.vo.ApplicationPageVO;
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

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationDraftController {

    private final ApplicationDraftService applicationDraftService;

    @PostMapping
    public Result<ApplicationDraftVO> createDraft(@RequestBody(required = false) ApplicationDraftRequest request) {
        return Result.success(applicationDraftService.createDraft(request));
    }

    @PutMapping("/{applicationId}")
    public Result<ApplicationDraftVO> updateDraft(
            @PathVariable Long applicationId,
            @RequestBody(required = false) ApplicationDraftRequest request
    ) {
        return Result.success(applicationDraftService.updateDraft(applicationId, request));
    }

    @GetMapping("/my")
    public Result<ApplicationPageVO> myApplications(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long batchId
    ) {
        return Result.success(applicationDraftService.myApplications(page, pageSize, status, batchId));
    }

    @GetMapping("/{applicationId}")
    public Result<ApplicationDetailVO> detail(@PathVariable Long applicationId) {
        return Result.success(applicationDraftService.detail(applicationId));
    }

    @PostMapping("/{applicationId}/supplement")
    public Result<ApplicationDraftVO> supplement(
            @PathVariable Long applicationId,
            @RequestBody(required = false) ApplicationSupplementRequest request
    ) {
        return Result.success(applicationDraftService.supplement(applicationId, request));
    }
}
