package com.kr.knucampus.presentation.auth.dto.request;

import com.kr.knucampus.domain.student.Gender;
import com.kr.knucampus.domain.student.Mbti;
import com.kr.knucampus.domain.student.Student;
import com.kr.knucampus.global.exception.BusinessException;
import com.kr.knucampus.global.exception.ErrorCode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SignUpRequest(
        @Email @NotBlank String email,
        @NotBlank String password,
        @NotBlank String studentNumber,
        @NotBlank String name,
        @NotNull @Min(18) @Max(30) Integer age,
        @NotNull String gender,  // "M" or "F"
        @NotBlank String department,
        String mbti,  // optional
        String profile  // optional
) {
    public Student toEntity() {
        try {
            return Student.builder()
                    .email(email)
                    .password(password)
                    .studentNumber(studentNumber)
                    .name(name)
                    .age(age)
                    .gender(Gender.valueOf(gender))
                    .department(department)
                    .mbti(mbti != null && !mbti.isBlank() ? Mbti.valueOf(mbti) : null)
                    .profile(profile)
                    .build();
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.WRONG_MBTI, e);
        }
    }
}
