package com.university.matching.dao;

import com.university.matching.config.DatabaseConfig;
import com.university.matching.model.Message;
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
 * Message 테이블에 대한 데이터 액세스 객체
 * CRUD 및 다양한 동적 쿼리 기능 제공
 */
public class MessageDAO {
    private static final Logger logger = LoggerFactory.getLogger(MessageDAO.class);

    /**
     * 새로운 메시지를 데이터베이스에 추가합니다
     */
    public boolean insert(Message message) throws SQLException {
        String sql = "INSERT INTO MESSAGE (session_id, student_id, message_id, content) " +
                "VALUES (?, ?, message_seq.NEXTVAL, ?)";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, message.getSessionId());
            pstmt.setLong(2, message.getStudentId());
            pstmt.setString(3, message.getContent());

            int affected = pstmt.executeUpdate();
            logger.info("Message inserted: {} rows affected", affected);
            return affected > 0;
        }
    }

    /**
     * 메시지를 삭제합니다
     */
    public boolean delete(Long sessionId, Long studentId, Long messageId) throws SQLException {
        String sql = "DELETE FROM MESSAGE WHERE session_id = ? AND student_id = ? AND message_id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, sessionId);
            pstmt.setLong(2, studentId);
            pstmt.setLong(3, messageId);

            int affected = pstmt.executeUpdate();
            logger.info("Message deleted: {} rows affected", affected);
            return affected > 0;
        }
    }

    /**
     * 모든 메시지를 조회합니다
     */
    public List<Message> findAll() throws SQLException {
        String sql = "SELECT * FROM MESSAGE ORDER BY created_at DESC";
        List<Message> messages = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
        }
        return messages;
    }

    // ==================== 동적 쿼리 메서드 ====================

    /**
     * Query 2-1: Multi-way join with join predicates in WHERE
     * 각 메시지를 보낸 학생의 이름과 메시지 내용 조회
     * 동적 파라미터: sessionId (Optional) - null이면 전체 조회
     */
    public List<MessageWithSender> findMessagesWithSenderInfo(Long sessionId) throws SQLException {
        String sql = "SELECT s.name, s.department, m.content, m.created_at " +
                "FROM MESSAGE m, STUDENT s " +
                "WHERE m.student_id = s.student_id";

        if (sessionId != null) {
            sql += " AND m.session_id = ?";
        }

        sql += " ORDER BY m.created_at";

        List<MessageWithSender> messages = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (sessionId != null) {
                pstmt.setLong(1, sessionId);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    MessageWithSender msg = new MessageWithSender(
                            rs.getString("content"),
                            rs.getTimestamp("created_at"),
                            rs.getString("name"),
                            rs.getString("department")
                    );
                    messages.add(msg);
                }
            }
        }
        logger.info("Found {} messages with sender info", messages.size());
        return messages;
    }

    /**
     * Query 3-2: Aggregation + multi-way join + GROUP BY
     * 각 채팅방에서 가장 많이 메시지를 보낸 학생 통계
     * 동적 파라미터: minMessageCount (HAVING COUNT(*) >= ?)
     */
    public List<SessionMessageStatistics> getMessageStatisticsBySession(int minMessageCount) throws SQLException {
        String sql = "SELECT m.session_id, s.name, s.department, COUNT(*) as message_count " +
                "FROM MESSAGE m, STUDENT s " +
                "WHERE m.student_id = s.student_id " +
                "GROUP BY m.session_id, s.student_id, s.name, s.department " +
                "HAVING COUNT(*) >= ? " +
                "ORDER BY m.session_id, message_count DESC";

        List<SessionMessageStatistics> stats = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, minMessageCount);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    SessionMessageStatistics stat = new SessionMessageStatistics(
                            rs.getLong("session_id"),
                            rs.getString("name"),
                            rs.getString("department"),
                            rs.getInt("message_count")
                    );
                    stats.add(stat);
                }
            }
        }
        logger.info("Retrieved message statistics for sessions (min count: {})", minMessageCount);
        return stats;
    }

    /**
     * Query 7-2: In-line view를 활용한 Query
     * 메시지를 평균 이상으로 보낸 학생들 조회
     */
    public List<StudentWithMessageCount> findStudentsWithAboveAverageMessages() throws SQLException {
        String sql = "SELECT s.student_id, s.name, s.department, msg_count.message_count " +
                "FROM STUDENT s, " +
                "     (SELECT student_id, COUNT(*) as message_count " +
                "      FROM MESSAGE " +
                "      GROUP BY student_id " +
                "      HAVING COUNT(*) >= (SELECT AVG(cnt) FROM (SELECT COUNT(*) as cnt FROM MESSAGE GROUP BY student_id))) msg_count " +
                "WHERE s.student_id = msg_count.student_id " +
                "ORDER BY msg_count.message_count DESC, s.student_id";

        List<StudentWithMessageCount> students = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                StudentWithMessageCount student = new StudentWithMessageCount(
                        rs.getLong("student_id"),
                        rs.getString("name"),
                        rs.getString("department"),
                        rs.getInt("message_count")
                );
                students.add(student);
            }
        }
        logger.info("Found {} students with above-average messages", students.size());
        return students;
    }

    /**
     * ResultSet을 Message 객체로 매핑합니다
     */
    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setSessionId(rs.getLong("session_id"));
        message.setStudentId(rs.getLong("student_id"));
        message.setMessageId(rs.getLong("message_id"));
        message.setContent(rs.getString("content"));
        message.setCreatedAt(rs.getTimestamp("created_at"));
        return message;
    }

    // ==================== 쿼리 결과를 위한 내부 클래스들 ====================

    /**
         * Query 2-1: 메시지와 발신자 정보를 담는 클래스
         */
        public record MessageWithSender(String content, Timestamp createdAt, String senderName, String senderDepartment) {
    }

    /**
         * Query 3-2: 세션별 메시지 통계를 담는 클래스
         */
        public record SessionMessageStatistics(Long sessionId, String studentName, String department, int messageCount) {
    }

    /**
         * Query 7-2: 학생과 메시지 수를 담는 클래스
         */
        public record StudentWithMessageCount(Long studentId, String name, String department, int messageCount) {
    }
}
