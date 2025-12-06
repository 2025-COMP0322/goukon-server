package com.kr.knucampus.presentation.auth.dto.response;

import com.kr.knucampus.domain.student.Student;

public record SignUp201Response(
        Long id,
        String studentNumber,
        String name,
        String email,
        String gender,
        String department,
        String mbti
) {
    public static SignUp201Response of(Student student) {
        return new SignUp201Response(
                student.getId(),
                student.getStudentNumber(),
                student.getName(),
                student.getEmail(),
                student.getGender().name(),
                student.getDepartment(),
                student.getMbti() != null ? student.getMbti().name() : null
        );
    }
}
