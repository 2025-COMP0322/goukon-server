package com.kr.goukon.domain.matchingsession.repository;

import com.kr.goukon.domain.matchingsession.MatchingSession;
import com.kr.goukon.domain.matchingsession.SessionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchingSessionRepository extends JpaRepository<MatchingSession, Long> {

    List<MatchingSession> findByStatus(SessionStatus status);

    List<MatchingSession> findByStatusOrderByCreatedAtDesc(SessionStatus status);

    // 비관적 락으로 세션 조회 (종료 투표 시 동시성 제어용)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM MatchingSession s WHERE s.id = :sessionId")
    Optional<MatchingSession> findByIdWithLock(@Param("sessionId") Long sessionId);
}
