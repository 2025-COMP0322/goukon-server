package com.kr.goukon.domain.student.repository;

import com.kr.goukon.domain.student.Gender;
import com.kr.goukon.domain.student.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByEmail(String email);

    Optional<Student> findByStudentNumber(String studentNumber);

    boolean existsByEmail(String email);

    boolean existsByStudentNumber(String studentNumber);

    // 이름 또는 학번으로 학생 검색
    @Query("SELECT s FROM Student s WHERE s.name LIKE %:keyword% OR s.studentNumber LIKE %:keyword%")
    List<Student> searchByNameOrStudentNumber(@Param("keyword") String keyword);

    // 학과별 학생 조회
    List<Student> findByDepartment(String department);

    // 성별로 학생 조회
    List<Student> findByGender(Gender gender);
}
