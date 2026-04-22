package com.example.admissionsystem.support;

import com.example.admissionsystem.auth.model.AuthUser;
import com.example.admissionsystem.auth.model.UserRole;
import com.example.admissionsystem.auth.security.AuthUserPrincipal;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.Map;

public final class ServiceTestSupport {

    private ServiceTestSupport() {
    }

    public static AuthUserPrincipal loginAs(Long userId, String username, UserRole role, String schoolCode) {
        AuthUserPrincipal principal = new AuthUserPrincipal(AuthUser.builder()
                .id(userId)
                .username(username)
                .passwordHash("")
                .role(role)
                .schoolCode(schoolCode)
                .enabled(true)
                .mustChangePassword(false)
                .build());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
        return principal;
    }

    public static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    public static Object newPrivateRecord(
            Class<?> owner,
            String simpleName,
            Class<?>[] parameterTypes,
            Object... args
    ) {
        try {
            Class<?> nestedClass = findNestedClass(owner, simpleName);
            Constructor<?> constructor = nestedClass.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create private record " + simpleName, e);
        }
    }

    public static void setGeneratedKey(GeneratedKeyHolder keyHolder, long id) {
        keyHolder.getKeyList().add(Map.of("id", id));
    }

    public static LocalDateTime nowPlusHours(long hours) {
        return LocalDateTime.now().plusHours(hours);
    }

    private static Class<?> findNestedClass(Class<?> owner, String simpleName) {
        for (Class<?> candidate : owner.getDeclaredClasses()) {
            if (candidate.getSimpleName().equals(simpleName)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("Nested class not found: " + simpleName);
    }
}
