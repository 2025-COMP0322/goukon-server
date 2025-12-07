package com.kr.goukon.presentation.student.dto.request;

public record ProfileUpdateRequest(
        String name,
        String mbti,
        String profile,
        String department
) {}
