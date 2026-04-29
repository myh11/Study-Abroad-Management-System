package com.example.admissionsystem.domesticreviews.controller;

import com.example.admissionsystem.common.response.Result;
import com.example.admissionsystem.domesticreviews.dto.DomesticReviewSubmitRequest;
import com.example.admissionsystem.domesticreviews.service.DomesticReviewService;
import com.example.admissionsystem.domesticreviews.vo.DomesticReviewPageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/domesticreviews")
@RequiredArgsConstructor
public class DomesticReviewController {

    private final DomesticReviewService domesticReviewService;

    @GetMapping
    public Result<DomesticReviewPageVO> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long batchId
    ) {
        return Result.success(domesticReviewService.list(page, pageSize, status, batchId));
    }

    @PostMapping("/{id}/claim")
    public Result<String> claim(@PathVariable Long id) {
        return Result.success(domesticReviewService.claim(id));
    }

    @PostMapping("/{id}/submit")
    public Result<String> submit(@PathVariable Long id, @RequestBody(required = false) DomesticReviewSubmitRequest request) {
        return Result.success(domesticReviewService.submit(id, request));
    }
}
