package com.example.admissionsystem.auth.service;

import com.example.admissionsystem.auth.dto.LoginRequest;
import com.example.admissionsystem.auth.dto.ChangePasswordRequest;
import com.example.admissionsystem.auth.repository.UserRepository;
import com.example.admissionsystem.auth.vo.AuthUserVO;
import com.example.admissionsystem.auth.security.AuthUserPrincipal;
import com.example.admissionsystem.auth.security.DatabaseUserDetailsService;
import com.example.admissionsystem.auth.security.JwtTokenUtil;
import com.example.admissionsystem.auth.security.PasswordHashService;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final DatabaseUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final PasswordHashService passwordHashService;
    private final JwtTokenUtil jwtTokenUtil;

    @Value("${app.security.login-fail-threshold:5}")
    private int loginFailThreshold;

    @Value("${app.security.lock-minutes:30}")
    private int lockMinutes;

    public AuthUserVO login(LoginRequest request) {
        AuthUserPrincipal principal;
        try {
            principal = (AuthUserPrincipal) userDetailsService.loadUserByUsername(request.getUsername());
        } catch (UsernameNotFoundException ex) {
            throw new BadCredentialsException("用户名或密码错误");
        }
        if (!principal.isEnabled()) {
            throw new BadCredentialsException("账号已停用");
        }
        if (!principal.isAccountNonLocked()) {
            throw new BadCredentialsException(buildLockedMessage(principal));
        }
        if (!passwordHashService.matches(request.getPassword(), principal.getPassword())) {
            handleLoginFailure(principal);
        }
        userRepository.recordLoginSuccess(principal.getUserId());
        return toAuthUser(principal, jwtTokenUtil.generateToken(principal));
    }

    public AuthUserVO me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUserPrincipal principal)) {
            throw new BadCredentialsException("未登录");
        }
        return toAuthUser(principal, null);
    }

    public String changePassword(ChangePasswordRequest request) {
        AuthUserPrincipal principal = requireCurrentUser();
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        if (!passwordHashService.matches(request.getOldPassword(), principal.getPassword())) {
            throw new BadCredentialsException("旧密码错误");
        }
        validateNewPassword(request.getOldPassword(), request.getNewPassword());
        userRepository.changePassword(principal.getUserId(), passwordHashService.hash(request.getNewPassword()));
        return "ok";
    }

    private AuthUserVO toAuthUser(AuthUserPrincipal user, String token) {
        return AuthUserVO.builder()
                .token(token)
                .userId(user.getUserId())
                .username(user.getUsername())
                .role(user.getRole())
                .schoolCode(user.getSchoolCode())
                .mustChangePassword(user.isMustChangePassword())
                .build();
    }

    private AuthUserPrincipal requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUserPrincipal principal)) {
            throw new BadCredentialsException("未登录");
        }
        return principal;
    }

    private void handleLoginFailure(AuthUserPrincipal principal) {
        boolean lockExpired = principal.getLockedUntil() != null && principal.getLockedUntil().isBefore(LocalDateTime.now());
        int nextFailCount = lockExpired ? 1 : principal.getLoginFailCount() + 1;
        LocalDateTime lockedUntil = nextFailCount >= loginFailThreshold
                ? LocalDateTime.now().plusMinutes(lockMinutes)
                : null;
        userRepository.recordLoginFailure(principal.getUserId(), nextFailCount, lockedUntil);
        if (lockedUntil != null) {
            throw new BadCredentialsException("连续输错超过" + loginFailThreshold + "次，账号已锁定至 " + lockedUntil);
        }
        throw new BadCredentialsException("用户名或密码错误");
    }

    private String buildLockedMessage(AuthUserPrincipal principal) {
        return principal.getLockedUntil() == null
                ? "账号已被锁定"
                : "账号已锁定至 " + principal.getLockedUntil();
    }

    private void validateNewPassword(String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("newPassword must not be blank");
        }
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("newPassword length must be at least 8");
        }
        if (newPassword.equals(oldPassword)) {
            throw new IllegalArgumentException("newPassword must be different from oldPassword");
        }
    }
}
