package com.kr.knucampus.presentation.student.dto.request;

public record ProfileUpdateRequest(
        String name,
        String mbti,
        String profile,
        String department
) {}
