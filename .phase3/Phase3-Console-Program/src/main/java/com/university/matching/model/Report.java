package com.university.matching.model;

import java.sql.Timestamp;

/**
 * Report 모델 클래스
 * REPORT 테이블을 나타냅니다.
 */
public class Report {
    private Long reportId;
    private Long studentId;
    private String title;
    private String content;
    private String status;        // PENDING, REVIEWING, RESOLVED, REJECTED
    private Timestamp createdAt;

    // Constructors
    public Report() {
    }

    public Report(Long reportId, Long studentId, String title, String content, String status, Timestamp createdAt) {
        this.reportId = reportId;
        this.studentId = studentId;
        this.title = title;
        this.content = content;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Report{" +
                "reportId=" + reportId +
                ", studentId=" + studentId +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
