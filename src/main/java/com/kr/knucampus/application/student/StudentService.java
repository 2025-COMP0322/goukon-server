package com.kr.knucampus.application.student;

import com.kr.knucampus.domain.student.Mbti;
import com.kr.knucampus.domain.student.Student;
import com.kr.knucampus.domain.student.repository.StudentRepository;
import com.kr.knucampus.global.exception.BusinessException;
import com.kr.knucampus.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;

    /**
     * 학생 조회
     */
    public Student getStudent(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
    }

    /**
     * 이메일로 학생 조회
     */
    public Student getStudentByEmail(String email) {
        return studentRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
    }

    /**
     * 학번으로 학생 조회
     */
    public Student getStudentByStudentNumber(String studentNumber) {
        return studentRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
    }

    /**
     * 프로필 정보 업데이트
     * 트랜잭션 격리 수준: REPEATABLE_READ - 동시 수정 방지
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Student updateProfile(Long studentId, String name, String mbtiStr, String profile, String department) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        Mbti mbti = null;
        if (mbtiStr != null && !mbtiStr.isBlank()) {
            try {
                mbti = Mbti.valueOf(mbtiStr);
            } catch (IllegalArgumentException e) {
                throw new BusinessException(ErrorCode.WRONG_MBTI);
            }
        }

        student.updateProfile(name, mbti, profile, department);
        return student;
    }

    /**
     * 학생 검색 (이름 또는 학번)
     */
    public List<Student> searchStudents(String keyword) {
        return studentRepository.searchByNameOrStudentNumber(keyword);
    }

    /**
     * 학과별 학생 조회
     */
    public List<Student> getStudentsByDepartment(String department) {
        return studentRepository.findByDepartment(department);
    }
}
