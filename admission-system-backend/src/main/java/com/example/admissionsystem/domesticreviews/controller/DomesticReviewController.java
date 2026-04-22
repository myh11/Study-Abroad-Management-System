package com.example.admissionsystem.domesticreviews.controller;

import com.example.admissionsystem.common.response.Result;
import com.example.admissionsystem.domesticreviews.dto.DomesticReviewSubmitRequest;
import com.example.admissionsystem.domesticreviews.service.DomesticReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/domesticreviews")
@RequiredArgsConstructor
public class DomesticReviewController {

    private final DomesticReviewService domesticReviewService;

    @PostMapping("/{id}/claim")
    public Result<String> claim(@PathVariable Long id) {
        return Result.success(domesticReviewService.claim(id));
    }

    @PostMapping("/{id}/submit")
    public Result<String> submit(@PathVariable Long id, @RequestBody(required = false) DomesticReviewSubmitRequest request) {
        return Result.success(domesticReviewService.submit(id, request));
    }
}
