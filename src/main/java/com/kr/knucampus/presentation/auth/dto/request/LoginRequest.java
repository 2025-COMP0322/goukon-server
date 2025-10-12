package com.kr.knucampus.presentation.auth.dto.request;

public record LoginRequest(
        String email,
        String password
) {
}
