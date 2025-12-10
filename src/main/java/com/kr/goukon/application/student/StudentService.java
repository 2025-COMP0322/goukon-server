package com.kr.goukon.application.student;

import com.kr.goukon.domain.student.Mbti;
import com.kr.goukon.domain.student.Student;
import com.kr.goukon.domain.student.repository.StudentRepository;
import com.kr.goukon.global.exception.BusinessException;
import com.kr.goukon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;

    /**
     * 학생 조회
     */
    @Transactional(readOnly = true)
    public Student getStudent(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
    }

    /**
     * 이메일로 학생 조회
     */
    @Transactional(readOnly = true)
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
     */
    public Student updateProfile(Long studentId, String name, String mbtiStr, String profile,
                                 String department, Integer age) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        Mbti mbti = parseMbti(mbtiStr);

        student.updateProfile(
                name,
                mbti,
                profile,
                department,
                age
        );
        return student;
    }

    private Mbti parseMbti(String mbtiStr) {
        if (!StringUtils.hasText(mbtiStr)) {
            return null;
        }
        try {
            return Mbti.valueOf(mbtiStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.WRONG_MBTI);
        }
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
