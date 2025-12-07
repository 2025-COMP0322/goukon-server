package com.kr.goukon.application.invite;

import com.kr.goukon.domain.group.Group;
import com.kr.goukon.domain.group.repository.GroupRepository;
import com.kr.goukon.domain.studentgroup.repository.StudentGroupRepository;
import com.kr.goukon.global.exception.BusinessException;
import com.kr.goukon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class InviteCodeService {

    private final RedisTemplate<String, String> redisTemplate;
    private final GroupRepository groupRepository;
    private final StudentGroupRepository studentGroupRepository;

    private static final String INVITE_CODE_PREFIX = "invite:code:";
    private static final String INVITE_GROUP_PREFIX = "invite:group:";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Value("${invite.code.ttl:3600}")
    private long inviteCodeTtl;

    /**
     * 그룹 초대 코드 생성
     * - 이미 존재하는 코드가 있으면 TTL만 갱신하고 반환
     * - 없으면 새로운 6자리 코드 생성
     */
    public String generateInviteCode(Long groupId, Long requesterId) {
        // 요청자가 그룹 멤버인지 확인
        if (!studentGroupRepository.existsByStudentIdAndGroupId(requesterId, groupId)) {
            throw new BusinessException(ErrorCode.NOT_GROUP_MEMBER);
        }

        // 그룹 존재 및 상태 확인
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

        if (!group.isAvailable()) {
            throw new BusinessException(ErrorCode.GROUP_NOT_AVAILABLE);
        }

        // 기존 코드가 있는지 확인
        String existingCode = redisTemplate.opsForValue().get(INVITE_GROUP_PREFIX + groupId);
        if (existingCode != null) {
            // 기존 코드의 TTL 갱신
            redisTemplate.expire(INVITE_CODE_PREFIX + existingCode, inviteCodeTtl, TimeUnit.SECONDS);
            redisTemplate.expire(INVITE_GROUP_PREFIX + groupId, inviteCodeTtl, TimeUnit.SECONDS);
            log.info("Renewed invite code {} for group {}", existingCode, groupId);
            return existingCode;
        }

        // 새 코드 생성
        String code = generateUniqueCode();

        // Redis에 코드 저장 (code -> groupId)
        redisTemplate.opsForValue().set(
                INVITE_CODE_PREFIX + code,
                String.valueOf(groupId),
                inviteCodeTtl,
                TimeUnit.SECONDS
        );

        // Redis에 그룹 -> 코드 매핑 저장 (중복 방지용)
        redisTemplate.opsForValue().set(
                INVITE_GROUP_PREFIX + groupId,
                code,
                inviteCodeTtl,
                TimeUnit.SECONDS
        );

        log.info("Generated new invite code {} for group {}", code, groupId);
        return code;
    }

    /**
     * 초대 코드 유효성 검증 및 그룹 ID 반환
     */
    public Long validateInviteCode(String code) {
        String groupIdStr = redisTemplate.opsForValue().get(INVITE_CODE_PREFIX + code);
        if (groupIdStr == null) {
            throw new BusinessException(ErrorCode.INVALID_INVITE_CODE);
        }
        return Long.parseLong(groupIdStr);
    }

    /**
     * 초대 코드 정보 조회 (그룹 ID + 남은 시간)
     */
    public InviteCodeInfo getInviteCodeInfo(String code) {
        String groupIdStr = redisTemplate.opsForValue().get(INVITE_CODE_PREFIX + code);
        if (groupIdStr == null) {
            throw new BusinessException(ErrorCode.INVALID_INVITE_CODE);
        }
        Long ttl = redisTemplate.getExpire(INVITE_CODE_PREFIX + code, TimeUnit.SECONDS);
        return new InviteCodeInfo(Long.parseLong(groupIdStr), ttl != null ? ttl : 0);
    }

    /**
     * 그룹의 초대 코드 무효화
     */
    public void invalidateInviteCode(Long groupId) {
        String code = redisTemplate.opsForValue().get(INVITE_GROUP_PREFIX + groupId);
        if (code != null) {
            redisTemplate.delete(INVITE_CODE_PREFIX + code);
            redisTemplate.delete(INVITE_GROUP_PREFIX + groupId);
            log.info("Invalidated invite code for group {}", groupId);
        }
    }

    /**
     * 고유한 6자리 숫자 코드 생성
     */
    private String generateUniqueCode() {
        String code;
        int attempts = 0;
        do {
            code = String.format("%06d", RANDOM.nextInt(1000000));
            attempts++;
            if (attempts > 100) {
                throw new BusinessException(ErrorCode.INVITE_CODE_GENERATION_FAILED);
            }
        } while (Boolean.TRUE.equals(redisTemplate.hasKey(INVITE_CODE_PREFIX + code)));
        return code;
    }

    /**
     * 초대 코드 정보 DTO
     */
    public record InviteCodeInfo(Long groupId, Long remainingSeconds) {}
}
