package com.kr.goukon.presentation.auth.dto.response;

public record TokenRefresh200Response(
        String accessToken,
        String refreshToken
) {
}
