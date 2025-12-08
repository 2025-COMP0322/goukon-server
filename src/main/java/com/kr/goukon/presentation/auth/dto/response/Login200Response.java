package com.kr.goukon.presentation.auth.dto.response;

public record Login200Response(
        String accessToken,
        String refreshToken
) {
}
