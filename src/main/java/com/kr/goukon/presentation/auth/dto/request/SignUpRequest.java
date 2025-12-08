package com.kr.goukon.presentation.auth.dto.request;

import com.kr.goukon.domain.student.Student;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignUpRequest(
        @Email @NotBlank String email,
        @NotBlank String password,
        @NotBlank String studentNumber,
        @NotBlank String name
) {
    public Student toEntity() {
        return Student.builder()
                .email(email)
                .password(password)
                .studentNumber(studentNumber)
                .name(name)
                .build();
    }
}
