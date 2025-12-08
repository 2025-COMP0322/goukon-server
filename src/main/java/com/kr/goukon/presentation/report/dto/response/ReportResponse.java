package com.kr.goukon.presentation.report.dto.response;

import com.kr.goukon.domain.report.Report;

import java.time.LocalDateTime;

public record ReportResponse(
        Long id,
        Long reporterId,
        String reporterName,
        String title,
        String content,
        String status,
        LocalDateTime createdAt
) {
    public static ReportResponse from(Report report) {
        return new ReportResponse(
                report.getId(),
                report.getReporter().getId(),
                report.getReporter().getName(),
                report.getTitle(),
                report.getContent(),
                report.getStatus().name(),
                report.getCreatedAt()
        );
    }
}
