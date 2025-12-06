package com.kr.knucampus.presentation.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(
        @NotBlank(message = "현재 비밀번호는 필수입니다")
        String password,

        @NotBlank(message = "새 비밀번호는 필수입니다")
        String newPassword
) {
}
