package com.kr.knucampus.domain.report.repository;

import com.kr.knucampus.domain.report.Report;
import com.kr.knucampus.domain.report.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    // 신고자의 신고 목록
    @Query("SELECT r FROM Report r WHERE r.reporter.id = :studentId ORDER BY r.createdAt DESC")
    List<Report> findByReporterId(@Param("studentId") Long studentId);

    // 상태별 신고 목록
    List<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status);

    // 전체 신고 목록 (관리자용)
    List<Report> findAllByOrderByCreatedAtDesc();
}
