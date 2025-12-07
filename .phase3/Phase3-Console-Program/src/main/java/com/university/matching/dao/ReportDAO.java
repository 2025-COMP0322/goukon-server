package com.university.matching.dao;

import com.university.matching.config.DatabaseConfig;
import com.university.matching.model.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Report 테이블에 대한 데이터 액세스 객체
 * CRUD 및 Query 8-2 구현
 */
public class ReportDAO {
    private static final Logger logger = LoggerFactory.getLogger(ReportDAO.class);

    // ==================== CRUD 메서드 ====================

    /**
     * 새로운 신고를 데이터베이스에 추가합니다
     */
    public Long insert(Report report) throws SQLException {
        String sql = "INSERT INTO REPORT (report_id, student_id, title, content, status) " +
                "VALUES (report_seq.NEXTVAL, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, new String[]{"report_id"})) {

            pstmt.setLong(1, report.getStudentId());
            pstmt.setString(2, report.getTitle());
            pstmt.setString(3, report.getContent());
            pstmt.setString(4, report.getStatus());

            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        Long generatedId = rs.getLong(1);
                        logger.info("Report inserted with ID: {}", generatedId);
                        return generatedId;
                    }
                }
            }
            return null;
        }
    }

    /**
     * 신고 정보를 업데이트합니다
     */
    public boolean update(Report report) throws SQLException {
        String sql = "UPDATE REPORT SET student_id = ?, title = ?, content = ?, status = ? " +
                "WHERE report_id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, report.getStudentId());
            pstmt.setString(2, report.getTitle());
            pstmt.setString(3, report.getContent());
            pstmt.setString(4, report.getStatus());
            pstmt.setLong(5, report.getReportId());

            int affected = pstmt.executeUpdate();
            logger.info("Report updated: {} rows affected", affected);
            return affected > 0;
        }
    }

    /**
     * 신고 상태를 업데이트합니다
     */
    public boolean updateStatus(Long reportId, String status) throws SQLException {
        String sql = "UPDATE REPORT SET status = ? WHERE report_id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setLong(2, reportId);

            int affected = pstmt.executeUpdate();
            logger.info("Report status updated: {} rows affected", affected);
            return affected > 0;
        }
    }

    /**
     * 신고를 삭제합니다
     */
    public boolean delete(Long reportId) throws SQLException {
        String sql = "DELETE FROM REPORT WHERE report_id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, reportId);
            int affected = pstmt.executeUpdate();
            logger.info("Report deleted: {} rows affected", affected);
            return affected > 0;
        }
    }

    /**
     * ID로 신고를 조회합니다
     */
    public Report findById(Long reportId) throws SQLException {
        String sql = "SELECT * FROM REPORT WHERE report_id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, reportId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReport(rs);
                }
            }
        }
        return null;
    }

    /**
     * 모든 신고를 조회합니다
     */
    public List<Report> findAll() throws SQLException {
        String sql = "SELECT * FROM REPORT ORDER BY created_at DESC";
        List<Report> reports = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                reports.add(mapResultSetToReport(rs));
            }
        }
        return reports;
    }

    // ==================== 동적 쿼리 메서드 ====================

    /**
     * Query 8-2: Multi-way join with join predicates in WHERE + ORDER BY
     * 신고 내역을 신고자 정보와 함께 시간순으로 조회
     */
    public List<ReportWithStudentInfo> findReportsWithStudentInfo() throws SQLException {
        String sql = "SELECT r.report_id, r.title, r.content, r.status, r.created_at, " +
                "s.student_id, s.name as reporter_name, s.department as reporter_dept " +
                "FROM REPORT r, STUDENT s " +
                "WHERE r.student_id = s.student_id " +
                "ORDER BY r.created_at DESC";

        List<ReportWithStudentInfo> reports = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                ReportWithStudentInfo report = new ReportWithStudentInfo(
                        rs.getLong("report_id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at"),
                        rs.getLong("student_id"),
                        rs.getString("reporter_name"),
                        rs.getString("reporter_dept")
                );
                reports.add(report);
            }
        }
        logger.info("Found {} reports with student info", reports.size());
        return reports;
    }

    // ==================== 유틸리티 메서드 ====================

    /**
     * ResultSet을 Report 객체로 매핑합니다
     */
    private Report mapResultSetToReport(ResultSet rs) throws SQLException {
        Report report = new Report();
        report.setReportId(rs.getLong("report_id"));
        report.setStudentId(rs.getLong("student_id"));
        report.setTitle(rs.getString("title"));
        report.setContent(rs.getString("content"));
        report.setStatus(rs.getString("status"));
        report.setCreatedAt(rs.getTimestamp("created_at"));
        return report;
    }

    // ==================== 내부 클래스 ====================

    /**
         * Query 8-2 결과를 위한 DTO 클래스
         */
        public record ReportWithStudentInfo(Long reportId, String title, String content, String status, Timestamp createdAt,
                                            Long studentId, String reporterName, String reporterDept) {
    }
}
