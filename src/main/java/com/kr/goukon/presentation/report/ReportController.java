package com.kr.goukon.presentation.report;

import com.kr.goukon.application.report.ReportService;
import com.kr.goukon.domain.report.Report;
import com.kr.goukon.domain.report.ReportStatus;
import com.kr.goukon.global.annotation.AuthUser;
import com.kr.goukon.global.exception.BusinessException;
import com.kr.goukon.global.exception.ErrorCode;
import com.kr.goukon.presentation.report.dto.request.ChangeStatusRequest;
import com.kr.goukon.presentation.report.dto.request.CreateReportRequest;
import com.kr.goukon.presentation.report.dto.response.ReportResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * 신고 접수
     */
    @PostMapping
    public ResponseEntity<ReportResponse> createReport(
            @AuthUser Long studentId,
            @Valid @RequestBody CreateReportRequest request) {
        Report report = reportService.createReport(studentId, request.title(), request.content());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReportResponse.from(report));
    }

    /**
     * 신고 조회
     */
    @GetMapping("/{reportId}")
    public ResponseEntity<ReportResponse> getReport(@PathVariable Long reportId) {
        Report report = reportService.getReport(reportId);
        return ResponseEntity.ok(ReportResponse.from(report));
    }

    /**
     * 내 신고 목록 조회
     */
    @GetMapping("/me")
    public ResponseEntity<List<ReportResponse>> getMyReports(@AuthUser Long studentId) {
        List<Report> reports = reportService.getMyReports(studentId);
        List<ReportResponse> responses = reports.stream()
                .map(ReportResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * 전체 신고 목록 조회 (관리자용)
     */
    @GetMapping
    public ResponseEntity<List<ReportResponse>> getAllReports(
            @AuthUser Long adminId,
            @RequestParam(required = false) String status) {
        List<Report> reports;
        if (status != null && !status.isBlank()) {
            try {
                ReportStatus reportStatus = ReportStatus.valueOf(status);
                reports = reportService.getReportsByStatus(adminId, reportStatus);
            } catch (IllegalArgumentException e) {
                throw new BusinessException(ErrorCode.INVALID_REPORT_STATUS);
            }
        } else {
            reports = reportService.getAllReports(adminId);
        }
        List<ReportResponse> responses = reports.stream()
                .map(ReportResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * 신고 상태 변경 (관리자용)
     */
    @PatchMapping("/{reportId}/status")
    public ResponseEntity<ReportResponse> changeStatus(
            @AuthUser Long adminId,
            @PathVariable Long reportId,
            @Valid @RequestBody ChangeStatusRequest request) {
        ReportStatus newStatus;
        try {
            newStatus = ReportStatus.valueOf(request.status());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_REPORT_STATUS);
        }

        Report report = reportService.changeReportStatus(adminId, reportId, newStatus);
        return ResponseEntity.ok(ReportResponse.from(report));
    }
}
