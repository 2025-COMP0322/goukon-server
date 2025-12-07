package com.kr.goukon.presentation.group.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record JoinByCodeRequest(
        @NotBlank(message = "초대 코드는 필수입니다")
        @Pattern(regexp = "^[0-9]{6}$", message = "초대 코드는 6자리 숫자입니다")
        String code
) {}
