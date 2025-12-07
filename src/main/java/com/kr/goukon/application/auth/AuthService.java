package com.kr.goukon.application.auth;

import com.kr.goukon.application.token.TokenService;
import com.kr.goukon.domain.student.Student;
import com.kr.goukon.domain.student.repository.StudentRepository;
import com.kr.goukon.domain.token.TokenType;
import com.kr.goukon.global.exception.BusinessException;
import com.kr.goukon.presentation.auth.dto.request.LoginRequest;
import com.kr.goukon.presentation.auth.dto.request.PasswordResetRequest;
import com.kr.goukon.presentation.auth.dto.request.SignUpRequest;
import com.kr.goukon.presentation.auth.dto.response.Login200Response;
import com.kr.goukon.presentation.auth.dto.response.PasswordReset200Response;
import com.kr.goukon.presentation.auth.dto.response.SignUp201Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.kr.goukon.global.exception.ErrorCode.*;
import static com.kr.goukon.global.status.SuccessCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthService {
    private final StudentRepository studentRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignUp201Response signUp(SignUpRequest request) {
        // 사전 중복 체크 (사용자 친화적 에러 메시지용)
        if (studentRepository.existsByEmail(request.email())) {
            throw new BusinessException(DUPLICATE_EMAIL);
        }
        if (studentRepository.existsByStudentNumber(request.studentNumber())) {
            throw new BusinessException(DUPLICATE_STUDENT_NUMBER);
        }

        Student student = request.toEntity();
        student.encryptPassword(passwordEncoder);

        try {
            studentRepository.save(student);
            studentRepository.flush(); // 즉시 DB 반영하여 제약조건 검증
        } catch (DataIntegrityViolationException e) {
            // Race condition 발생 시 DB 제약조건에 의해 예외 발생
            log.warn("Duplicate key violation during signup: {}", e.getMessage());
            throw new BusinessException(DUPLICATE_EMAIL);
        }

        return SignUp201Response.of(student);
    }

    public Login200Response login(LoginRequest request) {
        // 이메일 또는 학번으로 사용자 조회
        Student student;
        if (request.isEmail()) {
            student = studentRepository.findByEmail(request.identifier())
                    .orElseThrow(() -> new BusinessException(NO_USER));
        } else {
            student = studentRepository.findByStudentNumber(request.identifier())
                    .orElseThrow(() -> new BusinessException(NO_USER));
        }

        if (!student.matchPassword(request.password(), passwordEncoder)) {
            throw new BusinessException(WRONG_PASSWORD);
        }

        String accessToken = tokenService.getToken(TokenType.ACCESS, student.getId());
        String refreshToken = tokenService.getToken(TokenType.REFRESH, student.getId());

        return new Login200Response(accessToken, refreshToken);
    }

    @Transactional
    public PasswordReset200Response passwordReset(Long studentId, PasswordResetRequest request) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(NO_USER));

        if (!student.matchPassword(request.password(), passwordEncoder)) {
            throw new BusinessException(WRONG_PASSWORD);
        }

        student.modifyPassword(request.newPassword(), passwordEncoder);

        return new PasswordReset200Response(PASSWORD_CHANGED.getMessage());
    }

    // 이메일 중복 확인
    public boolean checkEmailExists(String email) {
        return studentRepository.existsByEmail(email);
    }

    // 학번 중복 확인
    public boolean checkStudentNumberExists(String studentNumber) {
        return studentRepository.existsByStudentNumber(studentNumber);
    }
}
