package com.kr.goukon.application.matching;

import com.kr.goukon.application.matching.dto.MatchingQueueMessage;
import com.kr.goukon.domain.chatroom.ChatRoom;
import com.kr.goukon.domain.chatroom.repository.ChatRoomRepository;
import com.kr.goukon.domain.group.Group;
import com.kr.goukon.domain.group.GroupStatus;
import com.kr.goukon.domain.group.repository.GroupRepository;
import com.kr.goukon.domain.matchingqueue.MatchingQueue;
import com.kr.goukon.domain.matchingqueue.MatchingStatus;
import com.kr.goukon.domain.matchingqueue.MatchingType;
import com.kr.goukon.domain.matchingqueue.repository.MatchingQueueRepository;
import com.kr.goukon.domain.matchingsession.MatchingSession;
import com.kr.goukon.domain.matchingsession.repository.MatchingSessionRepository;
import com.kr.goukon.domain.sessionmatches.SessionMatches;
import com.kr.goukon.domain.sessionmatches.repository.SessionMatchesRepository;
import com.kr.goukon.domain.student.Gender;
import com.kr.goukon.domain.student.Student;
import com.kr.goukon.domain.studentgroup.repository.StudentGroupRepository;
import com.kr.goukon.global.exception.BusinessException;
import com.kr.goukon.global.exception.ErrorCode;
import com.kr.goukon.global.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MatchingService {

    private final MatchingQueueRepository matchingQueueRepository;
    private final MatchingSessionRepository matchingSessionRepository;
    private final SessionMatchesRepository sessionMatchesRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final GroupRepository groupRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final RabbitTemplate rabbitTemplate;

    /**
     * 매칭 대기열에 등록
     * 트랜잭션 격리 수준: SERIALIZABLE - 동시 등록 방지
     */
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED)
    public MatchingQueue registerQueue(Long groupId, MatchingType matchingType) {
        // 그룹 존재 확인 및 비관적 락
        Group group = groupRepository.findByIdWithLock(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

        // 그룹 상태 확인
        if (!group.isAvailable()) {
            throw new BusinessException(ErrorCode.GROUP_NOT_AVAILABLE);
        }

        // 이미 대기열에 있는지 확인
        if (matchingQueueRepository.existsByGroupId(groupId)) {
            throw new BusinessException(ErrorCode.ALREADY_IN_QUEUE);
        }

        // 그룹 인원 확인
        long memberCount = studentGroupRepository.countByGroupId(groupId);
        validateMemberCount(memberCount, matchingType);

        // 대기열 등록
        MatchingQueue queue = MatchingQueue.create(group, matchingType);
        matchingQueueRepository.save(queue);

        // 그룹 상태 변경
        group.startQueuing();

        // RabbitMQ에 매칭 요청 전송
        sendMatchingRequest(queue, group.getGender());

        log.info("Group {} registered to matching queue with type {}", groupId, matchingType);
        return queue;
    }

    /**
     * 매칭 대기열 취소
     */
    @Transactional
    public void cancelQueue(Long groupId) {
        MatchingQueue queue = matchingQueueRepository.findByGroupId(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCHING_QUEUE_NOT_FOUND));

        if (!queue.isWaiting()) {
            throw new BusinessException(ErrorCode.MATCHING_IN_PROGRESS);
        }

        // 큐 상태 변경
        queue.cancel();

        // 그룹 상태 복원
        Group group = queue.getGroup();
        group.resetToAvailable();

        log.info("Group {} cancelled matching queue", groupId);
    }

    /**
     * RabbitMQ로 매칭 요청 전송
     */
    private void sendMatchingRequest(MatchingQueue queue, Gender gender) {
        MatchingQueueMessage message = new MatchingQueueMessage(
                queue.getId(),
                queue.getGroup().getId(),
                gender,
                queue.getMatchingType()
        );
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.MATCHING_EXCHANGE,
                RabbitMQConfig.MATCHING_ROUTING_KEY,
                message
        );
        log.debug("Sent matching request to RabbitMQ: {}", message);
    }

    /**
     * RabbitMQ 리스너 - 매칭 처리
     * 트랜잭션 격리 수준: SERIALIZABLE - 동시 매칭 방지
     */
    @RabbitListener(queues = RabbitMQConfig.MATCHING_QUEUE)
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED)
    public void processMatchingRequest(MatchingQueueMessage message) {
        log.info("Processing matching request for queue {}", message.getQueueId());

        try {
            // 현재 큐 확인 (비관적 락)
            MatchingQueue currentQueue = matchingQueueRepository.findByIdWithLock(message.getQueueId())
                    .orElse(null);

            if (currentQueue == null || !currentQueue.isWaiting()) {
                log.info("Queue {} is no longer waiting, skipping", message.getQueueId());
                return;
            }

            // 반대 성별의 대기중인 큐 찾기
            Gender oppositeGender = message.getGender() == Gender.M ? Gender.F : Gender.M;

            List<MatchingQueue> oppositeQueues = matchingQueueRepository.findWaitingQueuesWithLock(
                    oppositeGender,
                    message.getMatchingType()
            );

            if (oppositeQueues.isEmpty()) {
                log.info("No opposite gender queue found for type {}", message.getMatchingType());
                return;
            }

            // 첫 번째 대기열과 매칭
            MatchingQueue oppositeQueue = oppositeQueues.get(0);

            // 매칭 실행
            executeMatching(currentQueue, oppositeQueue);

        } catch (Exception e) {
            log.error("Error processing matching request: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 매칭 실행 - 세션 생성 및 채팅방 생성
     */
    private void executeMatching(MatchingQueue queue1, MatchingQueue queue2) {
        // 매칭 세션 생성
        MatchingSession session = MatchingSession.create();
        matchingSessionRepository.save(session);

        // 채팅방 생성
        ChatRoom chatRoom = ChatRoom.create(session);
        chatRoomRepository.save(chatRoom);

        // 큐 상태 변경
        queue1.matched();
        queue2.matched();

        // 그룹 상태 변경
        queue1.getGroup().matched();
        queue2.getGroup().matched();

        // 세션 매칭 정보 저장
        SessionMatches match1 = SessionMatches.create(session, queue1.getGroup(), queue1);
        SessionMatches match2 = SessionMatches.create(session, queue2.getGroup(), queue2);
        sessionMatchesRepository.save(match1);
        sessionMatchesRepository.save(match2);

        log.info("Matching completed: Session {} created for groups {} and {}",
                session.getId(), queue1.getGroup().getId(), queue2.getGroup().getId());
    }

    /**
     * 매칭 인원 검증
     */
    private void validateMemberCount(long memberCount, MatchingType matchingType) {
        int requiredCount = matchingType == MatchingType.ONE_TO_ONE ? 1 : 3;
        if (memberCount != requiredCount) {
            throw new BusinessException(ErrorCode.MEMBER_COUNT_MISMATCH);
        }
    }

    /**
     * 그룹의 매칭 큐 상태 조회
     */
    public MatchingQueue getQueueStatus(Long groupId) {
        return matchingQueueRepository.findByGroupId(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCHING_QUEUE_NOT_FOUND));
    }

    /**
     * 학생이 참여한 매칭 세션 목록 조회
     */
    public List<SessionMatches> getStudentSessions(Long studentId) {
        return sessionMatchesRepository.findByStudentId(studentId);
    }

    /**
     * 매칭 세션 상세 정보 조회
     * 요청자의 그룹과 상대 그룹 정보를 반환
     */
    public SessionDetailData getSessionDetails(Long sessionId, Long studentId) {
        // 세션의 두 그룹 조회
        List<SessionMatches> matches = sessionMatchesRepository.findBySessionId(sessionId);

        if (matches.isEmpty()) {
            throw new BusinessException(ErrorCode.MATCHING_SESSION_NOT_FOUND);
        }

        if (matches.size() != 2) {
            throw new BusinessException(ErrorCode.INVALID_SESSION_STATE);
        }

        // 요청자가 속한 그룹 찾기
        Group myGroup = null;
        Group opponentGroup = null;

        for (SessionMatches match : matches) {
            Long groupId = match.getGroup().getId();
            boolean isMember = studentGroupRepository.existsByStudentIdAndGroupId(studentId, groupId);

            if (isMember) {
                myGroup = match.getGroup();
            } else {
                opponentGroup = match.getGroup();
            }
        }

        if (myGroup == null) {
            throw new BusinessException(ErrorCode.NOT_SESSION_MEMBER);
        }

        // 각 그룹의 멤버 조회
        List<Student> myMembers = studentGroupRepository.findByGroupId(myGroup.getId())
                .stream()
                .map(sg -> sg.getStudent())
                .collect(Collectors.toList());

        List<Student> opponentMembers = studentGroupRepository.findByGroupId(opponentGroup.getId())
                .stream()
                .map(sg -> sg.getStudent())
                .collect(Collectors.toList());

        return new SessionDetailData(sessionId, myGroup, myMembers, opponentGroup, opponentMembers);
    }

    /**
     * 세션 상세 데이터 DTO
     */
    public record SessionDetailData(
            Long sessionId,
            Group myGroup,
            List<Student> myMembers,
            Group opponentGroup,
            List<Student> opponentMembers
    ) {}
}
