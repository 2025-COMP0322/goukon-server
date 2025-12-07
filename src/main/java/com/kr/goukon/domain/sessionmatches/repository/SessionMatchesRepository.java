package com.kr.goukon.domain.sessionmatches.repository;

import com.kr.goukon.domain.matchingsession.MatchingSession;
import com.kr.goukon.domain.sessionmatches.SessionMatches;
import com.kr.goukon.domain.sessionmatches.SessionMatchesId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionMatchesRepository extends JpaRepository<SessionMatches, SessionMatchesId> {

    // 세션의 모든 매칭 조회
    @Query("SELECT sm FROM SessionMatches sm JOIN FETCH sm.group JOIN FETCH sm.queue WHERE sm.session = :session")
    List<SessionMatches> findBySession(@Param("session") MatchingSession session);

    @Query("SELECT sm FROM SessionMatches sm JOIN FETCH sm.group JOIN FETCH sm.queue WHERE sm.session.id = :sessionId")
    List<SessionMatches> findBySessionId(@Param("sessionId") Long sessionId);

    // 그룹이 참여한 모든 세션 조회
    @Query("SELECT sm FROM SessionMatches sm JOIN FETCH sm.session WHERE sm.group.id = :groupId")
    List<SessionMatches> findByGroupId(@Param("groupId") Long groupId);

    // 학생이 참여한 모든 세션 조회 (그룹을 통해)
    @Query("SELECT sm FROM SessionMatches sm JOIN FETCH sm.session " +
           "WHERE sm.group.id IN (SELECT sg.group.id FROM StudentGroup sg WHERE sg.student.id = :studentId)")
    List<SessionMatches> findByStudentId(@Param("studentId") Long studentId);
}
