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
import com.kr.goukon.presentation.auth.dto.response.Logout200Response;
import com.kr.goukon.presentation.auth.dto.response.PasswordReset200Response;
import com.kr.goukon.presentation.auth.dto.response.SignUp201Response;
import com.kr.goukon.presentation.auth.dto.response.TokenRefresh200Response;
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
    private final RefreshTokenService refreshTokenService;
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

        // Refresh token을 Redis에 저장
        refreshTokenService.saveRefreshToken(student.getId(), refreshToken);

        log.info("User {} logged in successfully", student.getId());

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

    /**
     * Refresh token을 사용하여 새로운 access token 발급
     */
    public TokenRefresh200Response refreshAccessToken(String refreshToken) {
        // 1. JWT 형식 검증 및 만료 확인
        Long studentId;
        try {
            studentId = tokenService.getId(TokenType.REFRESH, refreshToken);
        } catch (BusinessException e) {
            // JWT 자체가 만료되었거나 유효하지 않음
            throw new BusinessException(INVALID_REFRESH_TOKEN);
        }

        // 2. Redis에 저장된 refresh token과 비교 (로그아웃 여부 확인)
        Long validatedStudentId = refreshTokenService.validateRefreshToken(refreshToken);

        // 3. studentId 일치 확인
        if (!studentId.equals(validatedStudentId)) {
            throw new BusinessException(INVALID_REFRESH_TOKEN);
        }

        // 4. 사용자 존재 여부 확인
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(NO_USER));

        // 5. 새로운 access token 발급
        String newAccessToken = tokenService.getToken(TokenType.ACCESS, student.getId());

        // 6. 새로운 refresh token도 발급 (Refresh Token Rotation 전략)
        String newRefreshToken = tokenService.getToken(TokenType.REFRESH, student.getId());

        // 7. 새로운 refresh token을 Redis에 저장 (기존 토큰은 자동으로 대체됨)
        refreshTokenService.saveRefreshToken(student.getId(), newRefreshToken);

        log.info("Access token refreshed for student {}", student.getId());

        return new TokenRefresh200Response(newAccessToken, newRefreshToken);
    }

    /**
     * 로그아웃 - Refresh token 무효화
     */
    public Logout200Response logout(Long studentId) {
        // Redis에서 refresh token 삭제
        refreshTokenService.invalidateRefreshToken(studentId);

        log.info("User {} logged out successfully", studentId);

        return new Logout200Response("로그아웃 되었습니다.");
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
