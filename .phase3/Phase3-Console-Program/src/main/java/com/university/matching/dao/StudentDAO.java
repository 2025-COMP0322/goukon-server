package com.university.matching.dao;

import com.university.matching.config.DatabaseConfig;
import com.university.matching.model.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Student 테이블에 대한 데이터 액세스 객체
 * CRUD 및 다양한 동적 쿼리 기능 제공
 */
public class StudentDAO {
    private static final Logger logger = LoggerFactory.getLogger(StudentDAO.class);

    /**
     * 새로운 학생을 데이터베이스에 추가합니다
     */
    public Long insert(Student student) throws SQLException {
        String sql = "INSERT INTO STUDENT (student_id, student_number, age, gender, name, mbti, profile, department) " +
                "VALUES (student_seq.NEXTVAL, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, new String[]{"student_id"})) {

            pstmt.setString(1, student.getStudentNumber());
            pstmt.setInt(2, student.getAge());
            pstmt.setString(3, student.getGender());
            pstmt.setString(4, student.getName());
            pstmt.setString(5, student.getMbti());
            pstmt.setString(6, student.getProfile());
            pstmt.setString(7, student.getDepartment());

            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        Long generatedId = rs.getLong(1);
                        logger.info("Student inserted with ID: {}", generatedId);
                        return generatedId;
                    }
                }
            }
            return null;
        }
    }

    /**
     * 학생 정보를 업데이트합니다
     */
    public boolean update(Student student) throws SQLException {
        String sql = "UPDATE STUDENT SET age = ?, name = ?, mbti = ?, profile = ?, department = ? " +
                "WHERE student_id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, student.getAge());
            pstmt.setString(2, student.getName());
            pstmt.setString(3, student.getMbti());
            pstmt.setString(4, student.getProfile());
            pstmt.setString(5, student.getDepartment());
            pstmt.setLong(6, student.getStudentId());

            int affected = pstmt.executeUpdate();
            logger.info("Student updated: {} rows affected", affected);
            return affected > 0;
        }
    }

    /**
     * 학생을 삭제합니다
     */
    public boolean delete(Long studentId) throws SQLException {
        String sql = "DELETE FROM STUDENT WHERE student_id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, studentId);
            int affected = pstmt.executeUpdate();
            logger.info("Student deleted: {} rows affected", affected);
            return affected > 0;
        }
    }

    /**
     * ID로 학생을 조회합니다
     */
    public Student findById(Long studentId) throws SQLException {
        String sql = "SELECT * FROM STUDENT WHERE student_id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, studentId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStudent(rs);
                }
            }
        }
        return null;
    }

    /**
     * 모든 학생을 조회합니다
     */
    public List<Student> findAll() throws SQLException {
        String sql = "SELECT * FROM STUDENT ORDER BY created_at DESC";
        List<Student> students = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
        }
        return students;
    }

    // ==================== 동적 쿼리 메서드 ====================

    /**
     * Query 1-1: 남성 학생 중 20세 이상인 학생 조회
     * Team2-Phase2-3.sql 참조
     */
    public List<Student> findStudentsByGenderAndAge(String gender, int minAge) throws SQLException {
        String sql = "SELECT name, age, department FROM STUDENT WHERE gender = ? AND age >= ? ORDER BY age DESC";
        List<Student> students = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, gender);
            pstmt.setInt(2, minAge);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Student student = new Student();
                    student.setName(rs.getString("name"));
                    student.setAge(rs.getInt("age"));
                    student.setDepartment(rs.getString("department"));
                    students.add(student);
                }
            }
        }
        logger.info("Found {} students with gender={} and age>={}", students.size(), gender, minAge);
        return students;
    }

    /**
     * Query 3-1: 각 학과별 학생 수와 평균 나이 (HAVING COUNT(*) >= 5)
     * Team2-Phase2-3.sql 참조
     */
    public List<DepartmentStats> getDepartmentStatistics() throws SQLException {
        String sql = "SELECT department, COUNT(*) as student_count, ROUND(AVG(age), 2) as avg_age " +
                "FROM STUDENT " +
                "GROUP BY department " +
                "HAVING COUNT(*) >= 5 " +
                "ORDER BY student_count DESC";

        List<DepartmentStats> stats = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                DepartmentStats stat = new DepartmentStats(
                        rs.getString("department"),
                        rs.getInt("student_count"),
                        rs.getDouble("avg_age")
                );
                stats.add(stat);
            }
        }
        logger.info("Retrieved statistics for {} departments", stats.size());
        return stats;
    }

    /**
     * Query 4-1: 평균 나이보다 어린 학생들 조회
     * Team2-Phase2-3.sql 참조
     */
    public List<Student> findStudentsBelowAverageAge() throws SQLException {
        String sql = "SELECT student_id, name, age, department " +
                "FROM STUDENT " +
                "WHERE age < (SELECT AVG(age) FROM STUDENT) " +
                "ORDER BY age";
        List<Student> students = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
        }
        logger.info("Found {} students below average age", students.size());
        return students;
    }

    /**
     * Query 4-2: 가장 많은 그룹이 속한 학과의 학생들 조회
     * Team2-Phase2-3.sql 참조
     */
    public List<Student> findStudentsFromTopGroupDepartment() throws SQLException {
        String sql = "SELECT s.student_id, s.name, s.department, s.age " +
                "FROM STUDENT s " +
                "WHERE s.department = ( " +
                "    SELECT s2.department " +
                "    FROM STUDENT s2, STUDENT_GROUP sg " +
                "    WHERE s2.student_id = sg.student_id " +
                "    GROUP BY s2.department " +
                "    ORDER BY COUNT(DISTINCT sg.group_id) DESC " +
                "    FETCH FIRST 1 ROW ONLY " +
                ") " +
                "ORDER BY s.name";
        List<Student> students = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
        }
        logger.info("Found {} students from top group department", students.size());
        return students;
    }

    /**
     * Query 5-1: 메시지를 한 번이라도 보낸 학생들 조회
     * Team2-Phase2-3.sql 참조
     */
    public List<Student> findActiveStudents() throws SQLException {
        String sql = "SELECT s.student_id, s.name, s.department, s.age " +
                "FROM STUDENT s " +
                "WHERE EXISTS ( " +
                "    SELECT 1 FROM MESSAGE m WHERE m.student_id = s.student_id " +
                ") " +
                "ORDER BY s.name";
        List<Student> students = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
        }
        logger.info("Found {} active students", students.size());
        return students;
    }

    /**
     * Query 5-2: 신고를 한 적이 있는 학생들 조회
     * Team2-Phase2-3.sql 참조
     */
    public List<Student> findReportingStudents() throws SQLException {
        String sql = "SELECT s.student_id, s.name, s.department, s.age " +
                "FROM STUDENT s " +
                "WHERE EXISTS ( " +
                "    SELECT 1 FROM REPORT r WHERE r.student_id = s.student_id " +
                ") " +
                "ORDER BY s.name";
        List<Student> students = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
        }
        logger.info("Found {} reporting students", students.size());
        return students;
    }

    /**
     * Query 6-1: 특정 MBTI 유형 학생 조회 (동적 IN 절)
     * Team2-Phase2-3.sql 참조 - 예: ENFJ, INFJ, INTJ
     */
    public List<Student> findStudentsByMBTI(List<String> mbtiList) throws SQLException {
        if (mbtiList == null || mbtiList.isEmpty()) {
            return new ArrayList<>();
        }

        StringBuilder sql = new StringBuilder("SELECT student_id, name, department, mbti, age FROM STUDENT WHERE mbti IN (");
        for (int i = 0; i < mbtiList.size(); i++) {
            sql.append("?");
            if (i < mbtiList.size() - 1) {
                sql.append(", ");
            }
        }
        sql.append(") ORDER BY mbti, name");

        List<Student> students = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < mbtiList.size(); i++) {
                pstmt.setString(i + 1, mbtiList.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    students.add(mapResultSetToStudent(rs));
                }
            }
        }
        logger.info("Found {} students with MBTI in: {}", students.size(), mbtiList);
        return students;
    }

    /**
     * Query 6-2: 특정 학과 학생 조회 (동적 IN 절)
     * Team2-Phase2-3.sql 참조 - 예: 컴퓨터공학과, 소프트웨어학과, 전자공학과
     */
    public List<Student> findStudentsByDepartments(List<String> departments) throws SQLException {
        if (departments == null || departments.isEmpty()) {
            return new ArrayList<>();
        }

        // IN 절을 위한 동적 SQL 생성
        StringBuilder sql = new StringBuilder("SELECT student_id, name, department, age, mbti FROM STUDENT WHERE department IN (");
        for (int i = 0; i < departments.size(); i++) {
            sql.append("?");
            if (i < departments.size() - 1) {
                sql.append(", ");
            }
        }
        sql.append(") ORDER BY department, name");

        List<Student> students = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < departments.size(); i++) {
                pstmt.setString(i + 1, departments.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    students.add(mapResultSetToStudent(rs));
                }
            }
        }
        logger.info("Found {} students in departments: {}", students.size(), departments);
        return students;
    }

    /**
     * ResultSet을 Student 객체로 매핑합니다
     */
    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setStudentId(rs.getLong("student_id"));

        // student_number는 선택적 필드로 처리
        try {
            student.setStudentNumber(rs.getString("student_number"));
        } catch (SQLException e) {
            // 컬럼이 없는 경우 무시
        }

        student.setAge(rs.getInt("age"));

        // gender는 선택적 필드로 처리
        try {
            student.setGender(rs.getString("gender"));
        } catch (SQLException e) {
            // 컬럼이 없는 경우 무시
        }
        student.setName(rs.getString("name"));

        // mbti는 선택적 필드로 처리
        try {
            student.setMbti(rs.getString("mbti"));
        } catch (SQLException e) {
            // 컬럼이 없는 경우 무시
        }

        // profile은 선택적 필드로 처리
        try {
            student.setProfile(rs.getString("profile"));
        } catch (SQLException e) {
            // 컬럼이 없는 경우 무시
        }

        student.setDepartment(rs.getString("department"));

        // created_at은 선택적 필드로 처리
        try {
            student.setCreatedAt(rs.getTimestamp("created_at"));
        } catch (SQLException e) {
            // 컬럼이 없는 경우 무시
        }

        return student;
    }

    /**
     * Query 7-1: 각 학과의 평균 나이보다 많은 학생들 조회
     * Team2-Phase2-3.sql 참조
     */
    public List<StudentWithDeptAvg> findStudentsAboveDepartmentAverage() throws SQLException {
        List<StudentWithDeptAvg> results = new ArrayList<>();
        String sql = "SELECT s.student_id, s.name, s.department, s.age, dept_avg.avg_age " +
                "FROM STUDENT s, " +
                "     (SELECT department, AVG(age) as avg_age " +
                "      FROM STUDENT " +
                "      GROUP BY department) dept_avg " +
                "WHERE s.department = dept_avg.department " +
                "  AND s.age > dept_avg.avg_age " +
                "ORDER BY s.department, s.age DESC";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                StudentWithDeptAvg result = new StudentWithDeptAvg();
                result.studentId = rs.getLong("student_id");
                result.name = rs.getString("name");
                result.age = rs.getInt("age");
                result.department = rs.getString("department");
                result.deptAvgAge = rs.getDouble("avg_age");
                results.add(result);
            }
        }
        logger.info("Found {} students above department average age", results.size());
        return results;
    }

    /**
     * Query 10-1: 메시지를 보낸 학생과 신고를 한 학생의 합집합 (UNION)
     * Team2-Phase2-3.sql 참조
     */
    public List<StudentActivity> findActiveOrReportingStudents() throws SQLException {
        List<StudentActivity> results = new ArrayList<>();
        String sql = "SELECT DISTINCT s.student_id, s.name, s.department, 'Message Sender' as activity_type " +
                "FROM STUDENT s, MESSAGE m " +
                "WHERE s.student_id = m.student_id " +
                "UNION " +
                "SELECT DISTINCT s.student_id, s.name, s.department, 'Reporter' as activity_type " +
                "FROM STUDENT s, REPORT r " +
                "WHERE s.student_id = r.student_id " +
                "ORDER BY student_id";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                StudentActivity result = new StudentActivity();
                result.studentId = rs.getLong("student_id");
                result.name = rs.getString("name");
                result.department = rs.getString("department");
                result.activityType = rs.getString("activity_type");
                results.add(result);
            }
        }
        logger.info("Found {} active or reporting students", results.size());
        return results;
    }

    /**
     * Query 10-2: 메시지를 보낸 학생 중 신고를 한 적이 없는 학생 (MINUS)
     * Team2-Phase2-3.sql 참조
     */
    public List<Student> findMessageSendersWithoutReports() throws SQLException {
        List<Student> results = new ArrayList<>();
        String sql = "SELECT DISTINCT s.student_id, s.name, s.department " +
                "FROM STUDENT s, MESSAGE m " +
                "WHERE s.student_id = m.student_id " +
                "MINUS " +
                "SELECT DISTINCT s.student_id, s.name, s.department " +
                "FROM STUDENT s, REPORT r " +
                "WHERE s.student_id = r.student_id " +
                "ORDER BY student_id";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Student student = new Student();
                student.setStudentId(rs.getLong("student_id"));
                student.setName(rs.getString("name"));
                student.setDepartment(rs.getString("department"));
                results.add(student);
            }
        }
        logger.info("Found {} message senders without reports", results.size());
        return results;
    }

    // ==================== 통계 및 결과 데이터 클래스 ====================

    /**
         * 학과별 통계 정보
         */
        public record DepartmentStats(String department, int studentCount, double avgAge) {
    }

    /**
     * Query 7-1 결과: 학생과 학과 평균 나이
     */
    public static class StudentWithDeptAvg {
        public Long studentId;
        public String name;
        public int age;
        public String department;
        public double deptAvgAge;
    }

    /**
     * Query 10-1 결과: 학생 활동 정보
     */
    public static class StudentActivity {
        public Long studentId;
        public String name;
        public String department;
        public String activityType;
    }
}
