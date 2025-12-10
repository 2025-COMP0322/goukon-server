package com.kr.goukon.presentation.student.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ProfileUpdateRequest(
        String name,
        String mbti,
        String profile,
        String department,
        @Min(18) @Max(30) Integer age
) {}
