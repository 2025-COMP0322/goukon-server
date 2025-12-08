package com.kr.goukon.presentation.report.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChangeStatusRequest(
        @NotBlank String status  // "PENDING", "REVIEWING", "RESOLVED", "REJECTED"
) {}
