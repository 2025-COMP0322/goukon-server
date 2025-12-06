package com.kr.knucampus.presentation.report.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateReportRequest(
        @NotBlank String title,
        @NotBlank String content
) {}
