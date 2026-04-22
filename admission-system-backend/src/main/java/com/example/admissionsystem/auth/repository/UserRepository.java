package com.example.admissionsystem.auth.repository;

import com.example.admissionsystem.auth.model.AuthUser;
import com.example.admissionsystem.auth.model.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private static final RowMapper<AuthUser> USER_ROW_MAPPER = (rs, rowNum) -> AuthUser.builder()
            .id(rs.getLong("id"))
            .username(rs.getString("username"))
            .passwordHash(rs.getString("password_hash"))
            .role(UserRole.valueOf(rs.getString("role_type")))
            .schoolCode(rs.getString("school_code"))
            .enabled(rs.getBoolean("is_enabled"))
            .mustChangePassword(rs.getBoolean("must_change_password"))
            .loginFailCount(rs.getInt("login_fail_count"))
            .lockedUntil(rs.getTimestamp("locked_until") == null ? null : rs.getTimestamp("locked_until").toLocalDateTime())
            .build();

    private final JdbcTemplate jdbcTemplate;

    public Optional<AuthUser> findByUsername(String username) {
        List<AuthUser> users = jdbcTemplate.query("""
                SELECT id, username, password_hash, role_type, school_code, is_enabled, must_change_password,
                       login_fail_count, locked_until
                FROM users
                WHERE username = ?
                """, USER_ROW_MAPPER, username);
        return users.stream().findFirst();
    }

    public void recordLoginSuccess(Long userId) {
        jdbcTemplate.update("""
                UPDATE users
                SET login_fail_count = 0,
                    locked_until = NULL,
                    last_login_at = CURRENT_TIMESTAMP,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """, userId);
    }

    public void recordLoginFailure(Long userId, int loginFailCount, LocalDateTime lockedUntil) {
        jdbcTemplate.update("""
                UPDATE users
                SET login_fail_count = ?,
                    locked_until = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                loginFailCount,
                lockedUntil == null ? null : Timestamp.valueOf(lockedUntil),
                userId);
    }

    public void changePassword(Long userId, String passwordHash) {
        jdbcTemplate.update("""
                UPDATE users
                SET password_hash = ?,
                    must_change_password = 0,
                    login_fail_count = 0,
                    locked_until = NULL,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """, passwordHash, userId);
    }
}
