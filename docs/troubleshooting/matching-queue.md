# Matching Queue 직렬화 충돌(ORA-08177) 트러블슈팅

## 상황 요약
- 2025-12-08에 `매칭 시작` 버튼을 동시에 여러 그룹이 누르면 `registerQueue` 트랜잭션이 `ORA-08177: 이 트랜잭션에 대한 직렬화 액세스를 할 수 없습니다` 예외를 발생시켰다.
- 스택트레이스 최상단은 `org.springframework.orm.jpa.JpaSystemException` 으로 랩핑되어 있었으며, 실제 원인은 Oracle에서 SERIALIZABLE 격리 수준 간 충돌이다.

## 원인 분석
- `MatchingService.registerQueue` 메서드가 `@Transactional(isolation = Isolation.SERIALIZABLE)`로 선언되어 있어, 트랜잭션 시작 이후 다른 세션이 `matching_queue` 혹은 `groups` 테이블을 수정하면 Oracle이 직렬화 보장을 위해 ORA-08177을 던졌다.
- 같은 시점에 여러 그룹이 큐에 진입하면서 서로의 트랜잭션이 충돌했고, 재시도 로직이 없어서 요청이 실패했다.

## 해결 방법
1. `registerQueue`에서 SERIALIZABLE 격리 수준을 제거하고 기본값(READ_COMMITTED)으로 되돌렸다 (`src/main/java/com/kr/goukon/application/matching/MatchingService.java`).
2. 동시성 보장은 기존처럼 비관적 락(`groupRepository.findByIdWithLock`, `matchingQueueRepository.findByIdWithLock`)으로 처리하므로, 격리 수준을 낮춰도 "동일 그룹 중복 등록"은 발생하지 않는다.
3. 이 변경으로 Oracle은 더 이상 직렬화 충돌을 감지하지 않으며, 실제 매칭을 성사시키는 `processMatchingRequest` 구간만 직렬화 트랜잭션을 유지한다.

## 검증
- 동일 그룹/서로 다른 그룹이 동시에 큐 등록 요청을 보내는 상황을 시뮬레이션했을 때 ORA-08177 예외 없이 정상적으로 큐 상태가 갱신되는 것을 확인했다.

## 후속 조치
- 향후 SERIALIZABLE을 사용해야 하는 다른 트랜잭션에서는 ORA-08177 재시도 로직을 반드시 넣는다.
- 운영 DB에서도 `matching_queue` 테이블에 새 `queued_at` 컬럼이 정상 적용되었는지 확인한다.
