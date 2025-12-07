package com.kr.goukon.presentation.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "이메일 또는 학번은 필수입니다")
        String identifier,

        @NotBlank(message = "비밀번호는 필수입니다")
        String password
) {
    /**
     * identifier가 이메일 형식인지 확인
     */
    public boolean isEmail() {
        return identifier != null && identifier.contains("@");
    }
}
