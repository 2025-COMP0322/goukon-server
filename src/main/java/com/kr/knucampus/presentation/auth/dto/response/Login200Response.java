package com.kr.knucampus.presentation.auth.dto.response;

public record Login200Response(
        String accessToken,
        String refreshToken
) {
}
