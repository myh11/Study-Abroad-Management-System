package com.example.admissionsystem.auth.controller;

import com.example.admissionsystem.auth.dto.ChangePasswordRequest;
import com.example.admissionsystem.auth.dto.LoginRequest;
import com.example.admissionsystem.auth.service.AuthService;
import com.example.admissionsystem.auth.vo.AuthUserVO;
import com.example.admissionsystem.common.response.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<AuthUserVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @GetMapping("/me")
    public Result<AuthUserVO> me() {
        return Result.success(authService.me());
    }

    @PostMapping("/change-password")
    public Result<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        return Result.success(authService.changePassword(request));
    }
}
