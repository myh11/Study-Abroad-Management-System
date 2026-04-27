package com.example.admissionsystem.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @NotBlank(message = "oldPassword must not be blank")
    private String oldPassword;

    @NotBlank(message = "newPassword must not be blank")
    private String newPassword;
}
