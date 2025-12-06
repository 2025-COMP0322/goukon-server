package com.kr.knucampus.domain.matchingsession.repository;

import com.kr.knucampus.domain.matchingsession.MatchingSession;
import com.kr.knucampus.domain.matchingsession.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchingSessionRepository extends JpaRepository<MatchingSession, Long> {

    List<MatchingSession> findByStatus(SessionStatus status);

    List<MatchingSession> findByStatusOrderByCreatedAtDesc(SessionStatus status);
}
