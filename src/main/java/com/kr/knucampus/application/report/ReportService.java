package com.kr.knucampus.application.report;

import com.kr.knucampus.domain.report.Report;
import com.kr.knucampus.domain.report.ReportStatus;
import com.kr.knucampus.domain.report.repository.ReportRepository;
import com.kr.knucampus.domain.student.Student;
import com.kr.knucampus.domain.student.repository.StudentRepository;
import com.kr.knucampus.global.exception.BusinessException;
import com.kr.knucampus.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final StudentRepository studentRepository;

    /**
     * 신고 접수
     * 트랜잭션 격리 수준: READ_COMMITTED
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Report createReport(Long reporterId, String title, String content) {
        if (title == null || title.isBlank()) {
            throw new BusinessException(ErrorCode.EMPTY_REPORT_CONTENT);
        }
        if (content == null || content.isBlank()) {
            throw new BusinessException(ErrorCode.EMPTY_REPORT_CONTENT);
        }

        Student reporter = studentRepository.findById(reporterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        Report report = Report.builder()
                .reporter(reporter)
                .title(title)
                .content(content)
                .build();

        reportRepository.save(report);
        log.info("Report {} created by student {}", report.getId(), reporterId);

        return report;
    }

    /**
     * 신고 조회
     */
    public Report getReport(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND));
    }

    /**
     * 신고자의 신고 목록 조회
     */
    public List<Report> getMyReports(Long studentId) {
        return reportRepository.findByReporterId(studentId);
    }

    /**
     * 상태별 신고 목록 조회 (관리자용)
     */
    public List<Report> getReportsByStatus(Long adminId, ReportStatus status) {
        verifyAdmin(adminId);
        return reportRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    /**
     * 전체 신고 목록 조회 (관리자용)
     */
    public List<Report> getAllReports(Long adminId) {
        verifyAdmin(adminId);
        return reportRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * 신고 상태 변경 (관리자용)
     * 트랜잭션 격리 수준: REPEATABLE_READ
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Report changeReportStatus(Long adminId, Long reportId, ReportStatus newStatus) {
        verifyAdmin(adminId);

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND));

        report.changeStatus(newStatus);
        log.info("Report {} status changed to {} by admin {}", reportId, newStatus, adminId);

        return report;
    }

    /**
     * 관리자 권한 확인
     */
    private void verifyAdmin(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        if (!student.isAdmin()) {
            throw new BusinessException(ErrorCode.ADMIN_ONLY);
        }
    }

    /**
     * 신고 검토 시작
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Report startReview(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND));

        report.startReview();
        log.info("Report {} review started", reportId);

        return report;
    }

    /**
     * 신고 처리 완료
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Report resolveReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND));

        report.resolve();
        log.info("Report {} resolved", reportId);

        return report;
    }

    /**
     * 신고 반려
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Report rejectReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND));

        report.reject();
        log.info("Report {} rejected", reportId);

        return report;
    }
}
