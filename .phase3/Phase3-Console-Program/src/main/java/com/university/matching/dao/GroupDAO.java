package com.university.matching.dao;

import com.university.matching.config.DatabaseConfig;
import com.university.matching.model.Group;
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
 * Group 테이블에 대한 데이터 액세스 객체
 * CRUD 및 5개의 동적 쿼리 기능 제공
 */
public class GroupDAO {
    private static final Logger logger = LoggerFactory.getLogger(GroupDAO.class);

    /**
     * 새로운 그룹을 데이터베이스에 추가합니다
     */
    public Long insert(Group group) throws SQLException {
        String sql = "INSERT INTO GROUPS (group_id, gender, status) " +
                "VALUES (group_seq.NEXTVAL, ?, ?)";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, new String[]{"group_id"})) {

            pstmt.setString(1, group.getGender());
            pstmt.setString(2, group.getStatus());

            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        Long generatedId = rs.getLong(1);
                        logger.info("Group inserted with ID: {}", generatedId);
                        return generatedId;
                    }
                }
            }
            return null;
        }
    }

    /**
     * 그룹 정보를 업데이트합니다
     */
    public boolean update(Group group) throws SQLException {
        String sql = "UPDATE GROUPS SET gender = ?, status = ? WHERE group_id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, group.getGender());
            pstmt.setString(2, group.getStatus());
            pstmt.setLong(3, group.getGroupId());

            int affected = pstmt.executeUpdate();
            logger.info("Group updated: {} rows affected", affected);
            return affected > 0;
        }
    }

    /**
     * 그룹을 삭제합니다
     */
    public boolean delete(Long groupId) throws SQLException {
        String sql = "DELETE FROM GROUPS WHERE group_id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, groupId);
            int affected = pstmt.executeUpdate();
            logger.info("Group deleted: {} rows affected", affected);
            return affected > 0;
        }
    }

    /**
     * ID로 그룹을 조회합니다
     */
    public Group findById(Long groupId) throws SQLException {
        String sql = "SELECT * FROM GROUPS WHERE group_id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, groupId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToGroup(rs);
                }
            }
        }
        return null;
    }

    /**
     * 모든 그룹을 조회합니다
     */
    public List<Group> findAll() throws SQLException {
        String sql = "SELECT * FROM GROUPS ORDER BY created_at DESC";
        List<Group> groups = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                groups.add(mapResultSetToGroup(rs));
            }
        }
        return groups;
    }

    /**
     * 학생을 그룹에 추가합니다
     */
    public boolean addStudentToGroup(Long groupId, Long studentId) throws SQLException {
        String sql = "INSERT INTO STUDENT_GROUP (student_id, group_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, studentId);
            pstmt.setLong(2, groupId);

            int affected = pstmt.executeUpdate();
            logger.info("Added student {} to group {}", studentId, groupId);
            return affected > 0;
        }
    }

    /**
     * 학생을 그룹에서 제거합니다
     */
    public boolean removeStudentFromGroup(Long groupId, Long studentId) throws SQLException {
        String sql = "DELETE FROM STUDENT_GROUP WHERE group_id = ? AND student_id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, groupId);
            pstmt.setLong(2, studentId);

            int affected = pstmt.executeUpdate();
            logger.info("Removed student {} from group {}", studentId, groupId);
            return affected > 0;
        }
    }

    // ==================== 동적 쿼리 메서드 (정확히 5개) ====================

    /**
     * Query 1-2: Single-table query (Selection + Projection)
     * 매칭 완료 상태인 큐의 정보 조회
     * 동적 파라미터: matching_status
     */
    public List<MatchedQueueInfo> findMatchedQueues(String matchingStatus) throws SQLException {
        String sql = "SELECT queue_id, matching_type, matching_status, created_at " +
                "FROM MATCHING_QUEUE " +
                "WHERE matching_status = ?";

        List<MatchedQueueInfo> queues = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, matchingStatus);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    MatchedQueueInfo info = new MatchedQueueInfo(
                            rs.getLong("queue_id"),
                            rs.getString("matching_type"),
                            rs.getString("matching_status"),
                            rs.getTimestamp("created_at")
                    );
                    queues.add(info);
                }
            }
        }
        logger.info("Found {} queues with matching status: {}", queues.size(), matchingStatus);
        return queues;
    }

    /**
     * Query 2-2: Multi-way join (4-way join)
     * 매칭 큐에 등록된 그룹과 해당 그룹의 학생 정보 조회
     * 동적 파라미터: queue_id (옵션)
     */
    public List<GroupMemberFromQueue> findGroupMembersFromQueue(Long queueId) throws SQLException {
        String sql = "SELECT mq.queue_id, mq.matching_type, g.group_id, s.student_id, s.name, s.department " +
                "FROM MATCHING_QUEUE mq, GROUPS g, STUDENT_GROUP sg, STUDENT s " +
                "WHERE mq.group_id = g.group_id " +
                "  AND g.group_id = sg.group_id " +
                "  AND sg.student_id = s.student_id";

        if (queueId != null) {
            sql += " AND mq.queue_id = ?";
        }

        List<GroupMemberFromQueue> members = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (queueId != null) {
                pstmt.setLong(1, queueId);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    GroupMemberFromQueue member = new GroupMemberFromQueue(
                            rs.getLong("queue_id"),
                            rs.getString("matching_type"),
                            rs.getLong("group_id"),
                            rs.getLong("student_id"),
                            rs.getString("name"),
                            rs.getString("department")
                    );
                    members.add(member);
                }
            }
        }
        logger.info("Found {} group members from queue: {}", members.size(), queueId);
        return members;
    }

    /**
     * Query 8-1: Multi-way join with WHERE + ORDER BY
     * 매칭된 세션의 상세 정보를 시간순으로 조회
     * 동적 파라미터: matching_status (옵션)
     */
    public List<MatchedSessionDetail> findMatchedSessionDetails(String matchingStatus) throws SQLException {
        String sql = "SELECT ms.session_id, ms.status as session_status, g.group_id, g.gender, " +
                "       mq.matching_type, mq.matching_status, ms.created_at " +
                "FROM MATCHING_SESSION ms, SESSION_MATCHES sm, GROUPS g, MATCHING_QUEUE mq " +
                "WHERE ms.session_id = sm.session_id " +
                "  AND sm.group_id = g.group_id " +
                "  AND sm.queue_id = mq.queue_id";

        if (matchingStatus != null) {
            sql += " AND mq.matching_status = ?";
        }

        sql += " ORDER BY ms.created_at DESC";

        List<MatchedSessionDetail> details = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (matchingStatus != null) {
                pstmt.setString(1, matchingStatus);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    MatchedSessionDetail detail = new MatchedSessionDetail(
                            rs.getLong("session_id"),
                            rs.getString("session_status"),
                            rs.getLong("group_id"),
                            rs.getString("gender"),
                            rs.getString("matching_type"),
                            rs.getString("matching_status"),
                            rs.getTimestamp("created_at")
                    );
                    details.add(detail);
                }
            }
        }
        logger.info("Found {} matched session details", details.size());
        return details;
    }

    /**
     * Query 9-1: Aggregation + multi-way join + GROUP BY + ORDER BY
     * 각 그룹의 멤버 수와 평균 나이를 멤버 수 순으로 조회
     */
    public List<GroupStatistics> getGroupStatistics() throws SQLException {
        String sql = "SELECT g.group_id, g.gender, COUNT(sg.student_id) as member_count, " +
                "       ROUND(AVG(s.age), 2) as avg_age, g.created_at " +
                "FROM GROUPS g, STUDENT_GROUP sg, STUDENT s " +
                "WHERE g.group_id = sg.group_id " +
                "  AND sg.student_id = s.student_id " +
                "GROUP BY g.group_id, g.gender, g.created_at " +
                "ORDER BY member_count DESC, g.created_at DESC";

        List<GroupStatistics> stats = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                GroupStatistics stat = new GroupStatistics(
                        rs.getLong("group_id"),
                        rs.getString("gender"),
                        rs.getInt("member_count"),
                        rs.getDouble("avg_age"),
                        rs.getTimestamp("created_at")
                );
                stats.add(stat);
            }
        }
        logger.info("Retrieved statistics for {} groups", stats.size());
        return stats;
    }

    /**
     * Query 9-2: Aggregation + multi-way join + GROUP BY + ORDER BY
     * 학과별 그룹 참여 학생 수와 매칭 세션 수를 참여 학생 순으로 조회
     */
    public List<DepartmentGroupParticipation> getDepartmentGroupParticipation() throws SQLException {
        String sql = "SELECT s.department, " +
                "       COUNT(DISTINCT sg.student_id) as participating_students, " +
                "       COUNT(DISTINCT sm.session_id) as total_sessions, " +
                "       COUNT(DISTINCT CASE WHEN mq.matching_status = 'MATCHED' THEN sm.session_id END) as matched_sessions " +
                "FROM STUDENT s, STUDENT_GROUP sg, GROUPS g, MATCHING_QUEUE mq, SESSION_MATCHES sm " +
                "WHERE s.student_id = sg.student_id " +
                "  AND sg.group_id = g.group_id " +
                "  AND g.group_id = mq.group_id " +
                "  AND mq.queue_id = sm.queue_id " +
                "  AND g.group_id = sm.group_id " +
                "GROUP BY s.department " +
                "ORDER BY participating_students DESC, matched_sessions DESC";

        List<DepartmentGroupParticipation> participations = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                DepartmentGroupParticipation participation = new DepartmentGroupParticipation(
                        rs.getString("department"),
                        rs.getInt("participating_students"),
                        rs.getInt("total_sessions"),
                        rs.getInt("matched_sessions")
                );
                participations.add(participation);
            }
        }
        logger.info("Retrieved department participation statistics for {} departments", participations.size());
        return participations;
    }

    /**
     * ResultSet을 Group 객체로 매핑합니다
     */
    private Group mapResultSetToGroup(ResultSet rs) throws SQLException {
        Group group = new Group();
        group.setGroupId(rs.getLong("group_id"));
        group.setGender(rs.getString("gender"));
        group.setStatus(rs.getString("status"));
        group.setCreatedAt(rs.getTimestamp("created_at"));
        return group;
    }

    // ==================== 내부 클래스 (Result DTOs) ====================

    /**
         * Query 1-2: 매칭 큐 정보
         */
        public record MatchedQueueInfo(Long queueId, String matchingType, String matchingStatus, Timestamp createdAt) {
    }

    /**
         * Query 2-2: 매칭 큐의 그룹 멤버 정보
         */
        public record GroupMemberFromQueue(Long queueId, String matchingType, Long groupId, Long studentId,
                                           String studentName, String department) {
    }

    /**
         * Query 8-1: 매칭된 세션 상세 정보
         */
        public record MatchedSessionDetail(Long sessionId, String sessionStatus, Long groupId, String groupGender,
                                           String matchingType, String matchingStatus, Timestamp createdAt) {
    }

    /**
         * Query 9-1: 그룹 통계 정보
         */
        public record GroupStatistics(Long groupId, String gender, int memberCount, double avgAge, Timestamp createdAt) {
    }

    /**
         * Query 9-2: 학과별 그룹 참여 통계
         */
        public record DepartmentGroupParticipation(String department, int participatingStudents, int totalSessions,
                                                   int matchedSessions) {
    }
}
