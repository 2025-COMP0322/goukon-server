package com.kr.goukon.presentation.group.dto.request;

import jakarta.validation.constraints.NotNull;

public record AddMemberRequest(
        @NotNull Long studentId
) {}
