package com.example.admissionsystem.auth.security;

import com.example.admissionsystem.auth.model.AuthUser;
import com.example.admissionsystem.auth.model.UserRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Getter
public class AuthUserPrincipal implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final UserRole role;
    private final String schoolCode;
    private final boolean enabled;
    private final boolean mustChangePassword;
    private final int loginFailCount;
    private final LocalDateTime lockedUntil;
    private final List<GrantedAuthority> authorities;

    public AuthUserPrincipal(AuthUser authUser) {
        this.userId = authUser.getId();
        this.username = authUser.getUsername();
        this.password = authUser.getPasswordHash();
        this.role = authUser.getRole();
        this.schoolCode = authUser.getSchoolCode();
        this.enabled = Boolean.TRUE.equals(authUser.getEnabled());
        this.mustChangePassword = Boolean.TRUE.equals(authUser.getMustChangePassword());
        this.loginFailCount = authUser.getLoginFailCount() == null ? 0 : authUser.getLoginFailCount();
        this.lockedUntil = authUser.getLockedUntil();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return lockedUntil == null || lockedUntil.isBefore(LocalDateTime.now());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
