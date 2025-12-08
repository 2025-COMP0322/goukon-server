package com.kr.goukon.presentation.group.dto.response;

public record InviteCodeResponse(
        String code,
        Long groupId,
        Long expiresInSeconds
) {}
