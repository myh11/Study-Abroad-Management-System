package com.example.admissionsystem.schoolreviews.controller;

import com.example.admissionsystem.common.response.Result;
import com.example.admissionsystem.schoolreviews.dto.SchoolReviewSubmitRequest;
import com.example.admissionsystem.schoolreviews.service.SchoolReviewService;
import com.example.admissionsystem.schoolreviews.vo.SchoolReviewPageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/school-reviews")
@RequiredArgsConstructor
public class SchoolReviewController {

    private final SchoolReviewService schoolReviewService;

    @GetMapping
    public Result<SchoolReviewPageVO> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) String schoolCode
    ) {
        return Result.success(schoolReviewService.list(page, pageSize, status, batchId, schoolCode));
    }

    @PostMapping("/{id}/submit")
    public Result<String> submit(@PathVariable Long id, @RequestBody(required = false) SchoolReviewSubmitRequest request) {
        return Result.success(schoolReviewService.submit(id, request));
    }
}
