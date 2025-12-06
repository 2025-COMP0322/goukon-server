package com.kr.knucampus.presentation.matching.dto.request;

import jakarta.validation.constraints.NotNull;

public record RegisterQueueRequest(
        @NotNull Long groupId,
        @NotNull String matchingType  // "ONE_TO_ONE" or "THREE_TO_THREE"
) {}
