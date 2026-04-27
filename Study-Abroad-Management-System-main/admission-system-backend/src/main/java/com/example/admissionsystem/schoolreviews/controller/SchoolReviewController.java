package com.example.admissionsystem.schoolreviews.controller;

import com.example.admissionsystem.common.response.Result;
import com.example.admissionsystem.schoolreviews.dto.SchoolReviewSubmitRequest;
import com.example.admissionsystem.schoolreviews.service.SchoolReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/school-reviews")
@RequiredArgsConstructor
public class SchoolReviewController {

    private final SchoolReviewService schoolReviewService;

    @PostMapping("/{id}/submit")
    public Result<String> submit(@PathVariable Long id, @RequestBody(required = false) SchoolReviewSubmitRequest request) {
        return Result.success(schoolReviewService.submit(id, request));
    }
}
