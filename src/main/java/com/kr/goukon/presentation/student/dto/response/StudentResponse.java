package com.kr.goukon.presentation.student.dto.response;

import com.kr.goukon.domain.student.Student;

import java.time.LocalDateTime;

public record StudentResponse(
        Long id,
        String studentNumber,
        String name,
        String email,
        int age,
        String gender,
        String department,
        String mbti,
        String profile,
        LocalDateTime createdAt
) {
    public static StudentResponse from(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getStudentNumber(),
                student.getName(),
                student.getEmail(),
                student.getAge(),
                student.getGender().name(),
                student.getDepartment(),
                student.getMbti() != null ? student.getMbti().name() : null,
                student.getProfile(),
                student.getCreatedAt()
        );
    }
}
