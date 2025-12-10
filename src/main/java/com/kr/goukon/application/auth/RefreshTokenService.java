package com.kr.goukon.application.auth;

import com.kr.goukon.global.exception.BusinessException;
import com.kr.goukon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh:token:";
    private static final String STUDENT_TOKEN_PREFIX = "refresh:student:";

    @Value("${jwt.refresh.expired}")
    private Long refreshTokenTtl;

    /**
     * Refresh token을 Redis에 저장
     * - key: refresh:token:{token} -> value: studentId
     * - key: refresh:student:{studentId} -> value: token (중복 로그인 방지용)
     */
    public void saveRefreshToken(Long studentId, String refreshToken) {
        // 기존 토큰이 있으면 삭제 (중복 로그인 방지)
        String existingToken = redisTemplate.opsForValue().get(STUDENT_TOKEN_PREFIX + studentId);
        if (existingToken != null) {
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + existingToken);
            log.info("Deleted existing refresh token for student {}", studentId);
        }

        // 새 토큰 저장
        // TTL은 밀리초를 초로 변환 (RefreshToken의 expiredTime이 밀리초 단위)
        long ttlInSeconds = refreshTokenTtl / 1000;

        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + refreshToken,
                String.valueOf(studentId),
                ttlInSeconds,
                TimeUnit.SECONDS
        );

        redisTemplate.opsForValue().set(
                STUDENT_TOKEN_PREFIX + studentId,
                refreshToken,
                ttlInSeconds,
                TimeUnit.SECONDS
        );

        log.info("Saved refresh token for student {} with TTL {} seconds", studentId, ttlInSeconds);
    }

    /**
     * Refresh token 검증 및 학생 ID 반환
     */
    public Long validateRefreshToken(String refreshToken) {
        String studentIdStr = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + refreshToken);
        if (studentIdStr == null) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        return Long.parseLong(studentIdStr);
    }

    /**
     * Refresh token 무효화 (로그아웃)
     */
    public void invalidateRefreshToken(Long studentId) {
        String refreshToken = redisTemplate.opsForValue().get(STUDENT_TOKEN_PREFIX + studentId);
        if (refreshToken != null) {
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + refreshToken);
            redisTemplate.delete(STUDENT_TOKEN_PREFIX + studentId);
            log.info("Invalidated refresh token for student {}", studentId);
        }
    }

    /**
     * 특정 refresh token 삭제
     */
    public void deleteRefreshToken(String refreshToken) {
        String studentIdStr = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + refreshToken);
        if (studentIdStr != null) {
            Long studentId = Long.parseLong(studentIdStr);
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + refreshToken);
            redisTemplate.delete(STUDENT_TOKEN_PREFIX + studentId);
            log.info("Deleted refresh token for student {}", studentId);
        }
    }

    /**
     * Refresh token이 Redis에 존재하는지 확인
     */
    public boolean existsRefreshToken(String refreshToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(REFRESH_TOKEN_PREFIX + refreshToken));
    }

    /**
     * 학생의 refresh token 조회 (테스트 또는 디버깅용)
     */
    public String getRefreshToken(Long studentId) {
        return redisTemplate.opsForValue().get(STUDENT_TOKEN_PREFIX + studentId);
    }
}
