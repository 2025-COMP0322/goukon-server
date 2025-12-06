package com.kr.knucampus.domain.studentgroup.repository;

import com.kr.knucampus.domain.group.Group;
import com.kr.knucampus.domain.student.Student;
import com.kr.knucampus.domain.studentgroup.StudentGroup;
import com.kr.knucampus.domain.studentgroup.StudentGroupId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentGroupRepository extends JpaRepository<StudentGroup, StudentGroupId> {

    // 학생이 속한 모든 그룹 조회
    @Query("SELECT sg FROM StudentGroup sg JOIN FETCH sg.group WHERE sg.student = :student")
    List<StudentGroup> findByStudent(@Param("student") Student student);

    // 학생 ID로 조회
    @Query("SELECT sg FROM StudentGroup sg JOIN FETCH sg.group WHERE sg.student.id = :studentId")
    List<StudentGroup> findByStudentId(@Param("studentId") Long studentId);

    // 그룹의 모든 멤버 조회
    @Query("SELECT sg FROM StudentGroup sg JOIN FETCH sg.student WHERE sg.group = :group")
    List<StudentGroup> findByGroup(@Param("group") Group group);

    // 그룹 ID로 멤버 조회
    @Query("SELECT sg FROM StudentGroup sg JOIN FETCH sg.student WHERE sg.group.id = :groupId")
    List<StudentGroup> findByGroupId(@Param("groupId") Long groupId);

    // 그룹의 멤버 수
    @Query("SELECT COUNT(sg) FROM StudentGroup sg WHERE sg.group.id = :groupId")
    long countByGroupId(@Param("groupId") Long groupId);

    // 학생이 특정 그룹에 속해있는지 확인
    boolean existsByStudentIdAndGroupId(Long studentId, Long groupId);

    // 학생이 특정 그룹에서 나가기
    void deleteByStudentIdAndGroupId(Long studentId, Long groupId);
}
