package com.example.admissionsystem.auth.vo;

import com.example.admissionsystem.auth.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserVO {

    private String token;
    private Long userId;
    private String username;
    private UserRole role;
    private String schoolCode;
    private Boolean mustChangePassword;
}
