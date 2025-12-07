package com.kr.goukon.domain.matchingsession.repository;

import com.kr.goukon.domain.matchingsession.MatchingSession;
import com.kr.goukon.domain.matchingsession.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchingSessionRepository extends JpaRepository<MatchingSession, Long> {

    List<MatchingSession> findByStatus(SessionStatus status);

    List<MatchingSession> findByStatusOrderByCreatedAtDesc(SessionStatus status);
}
