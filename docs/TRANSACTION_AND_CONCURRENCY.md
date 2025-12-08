# Goukon 백엔드 트랜잭션 및 동시성 제어 문서

## 1. Oracle 트랜잭션 처리

### 1.1 트랜잭션 격리수준 (Isolation Level)

| 서비스 | 메서드 | 격리수준 | 선택 이유 |
|--------|--------|----------|-----------|
| AuthService | signUp | READ_COMMITTED (기본) | DB UNIQUE 제약조건으로 중복 방지 |
| AuthService | passwordReset | READ_COMMITTED (기본) | 단일 레코드 업데이트 |
| GroupService | createGroup | READ_COMMITTED (기본) | 단순 INSERT, 동시성 이슈 없음 |
| GroupService | addMember | **SERIALIZABLE** | 그룹 인원 제한(3명) 검증 필요 |
| GroupService | removeMember | READ_COMMITTED (기본) | 단순 DELETE |
| GroupService | deleteGroup | READ_COMMITTED (기본) | 비관적 락으로 충분 |
| MatchingService | registerQueue | READ_COMMITTED (기본) | 비관적 락으로 중복 등록 방지 + ORA-08177 회피 |
| MatchingService | processMatchingRequest | **SERIALIZABLE** | 동시 매칭 시 race condition 방지 |
| MatchingService | voteEndSession | **SERIALIZABLE** | 만장일치 투표 카운팅 정확성 |
| ChatService | saveMessage | READ_COMMITTED (기본) | 메시지 순서는 createdAt으로 관리 |

### 1.2 SERIALIZABLE 격리수준 사용 케이스

```java
// 1. 그룹 멤버 추가 - 인원 제한 검증
@Transactional(isolation = Isolation.SERIALIZABLE)
public void addMember(Long groupId, Long studentId, Long requesterId) {
    Group group = groupRepository.findByIdWithLock(groupId);  // 비관적 락
    long currentCount = studentGroupRepository.countByGroupId(groupId);
    if (currentCount >= 3) {
        throw new BusinessException(ErrorCode.GROUP_FULL);
    }
    // ... 멤버 추가
}
```

**이유**: 두 사용자가 동시에 그룹 참가 시 인원 초과 가능. Phantom Read 방지 필요.

```java
// 2. 매칭 대기열 등록
@Transactional
public MatchingQueue registerQueue(Long groupId, MatchingType matchingType) {
    Group group = groupRepository.findByIdWithLock(groupId); // 비관적 락으로 행 단위 보호

    MatchingQueue existingQueue = matchingQueueRepository.findByGroupId(groupId)
            .orElse(null);
    if (existingQueue != null) {
        if (existingQueue.isWaiting()) {
            throw new BusinessException(ErrorCode.ALREADY_IN_QUEUE);
        }
        existingQueue.requeue(matchingType);
        group.startQueuing();
        return existingQueue;
    }

    if (!group.isAvailable()) {
        throw new BusinessException(ErrorCode.GROUP_NOT_AVAILABLE);
    }

    // ... 신규 큐 생성 및 상태 변경
}
```

**이유**: 비관적 락과 재큐잉 로직으로 중복 등록을 막으면서, SERIALIZABLE 격리에서 발생한 ORA-08177(직렬화 충돌)을 제거.

```java
// 3. 매칭 처리
@Transactional(isolation = Isolation.SERIALIZABLE)
public void processMatchingRequest(MatchingQueueMessage message) {
    MatchingQueue currentQueue = matchingQueueRepository.findByIdWithLock(message.getQueueId());
    List<MatchingQueue> oppositeQueues = matchingQueueRepository.findWaitingQueuesWithLock(...);
    // ... 매칭 실행
}
```

**이유**: 동일한 대기열이 중복 매칭되는 것 방지.

```java
// 4. 세션 종료 투표
@Transactional(isolation = Isolation.SERIALIZABLE)
public EndVoteResult voteEndSession(Long sessionId, Long studentId) {
    MatchingSession session = matchingSessionRepository.findByIdWithLock(sessionId);
    // 투표 등록 및 만장일치 체크
}
```

**이유**: 동시 투표 시 정확한 카운팅 및 종료 판정.

### 1.3 비관적 락 (Pessimistic Lock) 사용

```java
// GroupRepository
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT g FROM Group g WHERE g.id = :groupId")
Optional<Group> findByIdWithLock(@Param("groupId") Long groupId);

// MatchingQueueRepository
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT mq FROM MatchingQueue mq WHERE mq.id = :queueId")
Optional<MatchingQueue> findByIdWithLock(@Param("queueId") Long queueId);

// MatchingSessionRepository
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT ms FROM MatchingSession ms WHERE ms.id = :sessionId")
Optional<MatchingSession> findByIdWithLock(@Param("sessionId") Long sessionId);
```

**Oracle에서의 동작**: `SELECT ... FOR UPDATE` 구문으로 변환되어 해당 행에 배타적 락 획득.

### 1.4 DB 제약조건 활용

```java
// Student 엔티티
@Column(name = "student_number", nullable = false, unique = true)
private String studentNumber;

@Column(nullable = false, unique = true)
private String email;
```

회원가입 시 SERIALIZABLE 대신 DB UNIQUE 제약조건 활용:
- 성능 향상 (락 범위 최소화)
- DataIntegrityViolationException으로 중복 감지

```java
@Transactional
public SignUp201Response signUp(SignUpRequest request) {
    // 사전 체크 (UX용)
    if (studentRepository.existsByEmail(request.email())) {
        throw new BusinessException(DUPLICATE_EMAIL);
    }

    try {
        studentRepository.save(student);
        studentRepository.flush();
    } catch (DataIntegrityViolationException e) {
        // Race condition 발생 시 DB 제약조건이 최종 방어
        throw new BusinessException(DUPLICATE_EMAIL);
    }
}
```

---

## 2. Redis 사용

### 2.1 초대 코드 관리 (InviteCodeService)

```java
@Service
public class InviteCodeService {
    private final RedisTemplate<String, String> redisTemplate;

    private static final String INVITE_CODE_PREFIX = "invite:code:";
    private static final String INVITE_GROUP_PREFIX = "invite:group:";
}
```

#### 데이터 구조
| Key | Value | TTL | 용도 |
|-----|-------|-----|------|
| `invite:code:{code}` | groupId | 1시간 | 코드 → 그룹 매핑 |
| `invite:group:{groupId}` | code | 1시간 | 그룹 → 코드 매핑 (중복 방지) |

#### 주요 기능

```java
// 1. 초대 코드 생성
public String generateInviteCode(Long groupId, Long requesterId) {
    // 기존 코드 있으면 TTL 갱신 후 반환
    String existingCode = redisTemplate.opsForValue().get(INVITE_GROUP_PREFIX + groupId);
    if (existingCode != null) {
        redisTemplate.expire(INVITE_CODE_PREFIX + existingCode, inviteCodeTtl, TimeUnit.SECONDS);
        redisTemplate.expire(INVITE_GROUP_PREFIX + groupId, inviteCodeTtl, TimeUnit.SECONDS);
        return existingCode;
    }

    // 새 코드 생성 (6자리 숫자)
    String code = generateUniqueCode();
    redisTemplate.opsForValue().set(INVITE_CODE_PREFIX + code, groupId, inviteCodeTtl, TimeUnit.SECONDS);
    redisTemplate.opsForValue().set(INVITE_GROUP_PREFIX + groupId, code, inviteCodeTtl, TimeUnit.SECONDS);
    return code;
}

// 2. 초대 코드 검증
public Long validateInviteCode(String code) {
    String groupIdStr = redisTemplate.opsForValue().get(INVITE_CODE_PREFIX + code);
    if (groupIdStr == null) {
        throw new BusinessException(ErrorCode.INVALID_INVITE_CODE);
    }
    return Long.parseLong(groupIdStr);
}
```

#### Redis 선택 이유
- **TTL 자동 만료**: 1시간 후 자동 삭제
- **빠른 조회**: O(1) 시간복잡도
- **원자적 연산**: 동시 요청 시에도 안전
- **휘발성 데이터**: 영구 저장 불필요

### 2.2 Redis 동시성 제어

초대 코드 생성 시 중복 방지:
```java
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
```

### 2.3 Redis 기반 채팅 버퍼

| 요소 | 구현 | 동시성/무결성 포인트 |
|------|------|---------------------|
| 버퍼 저장 | WebSocket 수신 시 `ChatMessageBuffer.save()` → `chat:session:{sessionId}` 리스트에 push | 방 단위 키로 분리해 경합 최소화 |
| Flush 트리거 | (1) 방별 10개 이상 적재, (2) REST 조회 진입 직전, (3) `@Scheduled` 주기(기본 5초) | 조회 시점에 DB와 Redis 상태 일치 보장, 일정 주기로 미조회 방도 동기화 |
| Flush 트랜잭션 | `TransactionTemplate` + `PROPAGATION_REQUIRES_NEW` | Redis→DB 반영이 기존 트랜잭션과 분리되어 메시지 손실 방지 |
| 무결성 검사 | flush 시 `MessageType.CHAT` & `senderId` 유효성 검증 후 `saveMessage` 재사용 | 비정상 메시지 필터링, DB 제약 오류는 로그 후 건너뜀 |

**효과**: 실시간 채팅에서 다수 사용자가 동시에 메시지를 전송해도 Redis가 완충하고, 임계치·조회·주기 flush가 곧바로 DB에 반영해 데이터 일관성을 유지한다.

---

## 3. RabbitMQ 사용

### 3.1 매칭 시스템 비동기 처리

#### 구성
```java
@Configuration
public class RabbitMQConfig {
    public static final String MATCHING_QUEUE = "matching.queue";
    public static final String MATCHING_EXCHANGE = "matching.exchange";
    public static final String MATCHING_ROUTING_KEY = "matching.request";
}
```

#### 메시지 구조
```java
public class MatchingQueueMessage {
    private Long queueId;
    private Long groupId;
    private Gender gender;
    private MatchingType matchingType;
}
```

#### 흐름

```
[Client] → registerQueue() → [DB에 큐 저장] → [RabbitMQ로 메시지 전송]
                                                        ↓
                              [매칭 성공] ← processMatchingRequest() ← [Consumer]
```

### 3.2 Producer (매칭 등록)

```java
@Transactional
public MatchingQueue registerQueue(Long groupId, MatchingType matchingType) {
    // 1. DB에 큐 등록
    MatchingQueue queue = MatchingQueue.create(group, matchingType);
    matchingQueueRepository.save(queue);

    // 2. 그룹 상태 변경
    group.startQueuing();

    // 3. RabbitMQ로 매칭 요청 전송
    sendMatchingRequest(queue, group.getGender());

    return queue;
}

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
}
```

### 3.3 Consumer (매칭 처리)

```java
@RabbitListener(queues = RabbitMQConfig.MATCHING_QUEUE)
@Transactional(isolation = Isolation.SERIALIZABLE)
public void processMatchingRequest(MatchingQueueMessage message) {
    // 1. 현재 큐 확인 (비관적 락)
    MatchingQueue currentQueue = matchingQueueRepository.findByIdWithLock(message.getQueueId());
    if (currentQueue == null || !currentQueue.isWaiting()) {
        return;  // 이미 처리됨
    }

    // 2. 반대 성별의 대기열 찾기
    Gender oppositeGender = message.getGender() == Gender.M ? Gender.F : Gender.M;
    List<MatchingQueue> oppositeQueues = matchingQueueRepository.findWaitingQueuesWithLock(
        oppositeGender,
        message.getMatchingType()
    );

    if (oppositeQueues.isEmpty()) {
        return;  // 매칭 대상 없음
    }

    // 3. 매칭 실행
    executeMatching(currentQueue, oppositeQueues.get(0));
}
```

### 3.4 RabbitMQ 선택 이유

1. **비동기 처리**: 매칭 등록 API 응답 지연 방지
2. **순차 처리**: 큐에 들어온 순서대로 매칭 (선입선출 공정성)
3. **실패 복구**: 메시지 영속성으로 서버 재시작 시에도 처리 보장
4. **부하 분산**: 매칭 처리를 별도로 분리하여 API 서버 부하 감소

### 3.5 트랜잭션과 메시지 전송

```java
@Transactional(isolation = Isolation.SERIALIZABLE)
public MatchingQueue registerQueue(...) {
    // DB 작업
    matchingQueueRepository.save(queue);
    group.startQueuing();

    // 트랜잭션 커밋 전 메시지 전송
    sendMatchingRequest(queue, group.getGender());

    return queue;
}
```

**주의사항**:
- 트랜잭션 커밋 전 메시지가 전송됨
- 트랜잭션 롤백 시 메시지는 이미 전송된 상태
- Consumer에서 DB 상태 재확인으로 보완 (`if (!currentQueue.isWaiting()) return;`)

---

## 4. 성능 최적화 요약

| 최적화 항목 | 변경 전 | 변경 후 | 효과 |
|-------------|---------|---------|------|
| 회원가입 | SERIALIZABLE | READ_COMMITTED + DB 제약조건 | 락 범위 축소 |
| 비밀번호 변경 | REPEATABLE_READ | READ_COMMITTED | 불필요한 락 제거 |
| 그룹 생성 | SERIALIZABLE | READ_COMMITTED | 락 범위 축소 |
| 그룹 삭제 | SERIALIZABLE | READ_COMMITTED + 비관적 락 | 행 단위 락으로 축소 |
| 메시지 저장 | SERIALIZABLE | READ_COMMITTED | 불필요한 락 제거 |
| 초대 코드 | DB 저장 | Redis TTL | 자동 만료 + 빠른 조회 |
| 매칭 처리 | 동기 처리 | RabbitMQ 비동기 | API 응답 속도 향상 |

---

## 5. 동시성 제어 전략 정리

### 5.1 낙관적 동시성 제어
- 사용하지 않음 (충돌 빈도가 낮지 않은 도메인)

### 5.2 비관적 동시성 제어
- `@Lock(LockModeType.PESSIMISTIC_WRITE)` 사용
- 그룹, 매칭큐, 매칭세션 등 상태 변경 시

### 5.3 DB 제약조건 활용
- UNIQUE 제약조건: email, studentNumber
- NOT NULL 제약조건: 필수 필드
- Foreign Key: 참조 무결성

### 5.4 애플리케이션 레벨 제어
- SERIALIZABLE 격리수준: 인원 제한, 만장일치 투표 등 집계 연산
- Redis 원자적 연산: 초대 코드 생성
- RabbitMQ 순차 처리: 매칭 요청 순서 보장

---

## 6. DB 수업 프로젝트 동시성 요구사항 대응

| 요구사항 | 구현 위치 | 제어 기법 | 설명 |
|----------|-----------|-----------|------|
| 여러 사용자의 동시 그룹 참여 | `GroupService.addMember` | SERIALIZABLE + 비관적 락 | 동시에 참여해도 인원 제한(3명)을 넘지 않도록 트랜잭션으로 보호 |
| 매칭 대기열 중복/경합 방지 | `MatchingService.registerQueue`, `processMatchingRequest` | 비관적 락 + (필요 구간) SERIALIZABLE | 동일 그룹·큐가 두 번 등록/매칭되는 race condition 차단 |
| 세션 종료 투표 일관성 | `MatchingService.voteEndSession` | SERIALIZABLE + 카운트 검증 | 만장일치 여부를 정확히 계산, 동시 투표 시 중복 허용 안함 |
| 채팅 실시간 쓰기/조회 | `ChatService` + Redis 버퍼 | 방별 임계치 + 조회/주기 flush | WebSocket 폭주 상황에서도 메시지가 순서/무결성을 유지하며 DB에 적재 |
| 초대 코드 중복 생성 방지 | `InviteCodeService` | Redis 원자연산 + TTL | 다수 사용자가 동시에 코드 생성해도 중복 없음, 자동 만료로 무결성 유지 |

이와 같이 서비스 전반에서 트랜잭션 격리 수준, 비관적 락, Redis 버퍼링, 메시지 큐 등을 적절히 조합해 동시성 실패로 인한 데이터 일관성·무결성 오류를 방지하고 있으며, DB 수업 평가 항목(동시 접속, concurrency control 지원)에 부합하도록 설계/구현했다.
