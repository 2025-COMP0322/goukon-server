package com.kr.goukon.domain.sessionendvote.repository;

import com.kr.goukon.domain.sessionendvote.SessionEndVote;
import com.kr.goukon.domain.sessionendvote.SessionEndVoteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionEndVoteRepository extends JpaRepository<SessionEndVote, SessionEndVoteId> {

    // 학생이 이미 투표했는지 확인
    boolean existsByIdSessionIdAndIdStudentId(Long sessionId, Long studentId);

    // 세션의 모든 투표 조회
    @Query("SELECT v FROM SessionEndVote v WHERE v.session.id = :sessionId")
    List<SessionEndVote> findBySessionId(@Param("sessionId") Long sessionId);

    // 세션에서 특정 그룹 멤버들의 투표 수 조회
    @Query("""
        SELECT COUNT(v) FROM SessionEndVote v
        WHERE v.session.id = :sessionId
        AND v.student.id IN (
            SELECT sg.student.id FROM StudentGroup sg WHERE sg.group.id = :groupId
        )
        """)
    long countBySessionIdAndGroupId(@Param("sessionId") Long sessionId, @Param("groupId") Long groupId);

    // 세션에서 투표한 학생 ID 목록
    @Query("SELECT v.student.id FROM SessionEndVote v WHERE v.session.id = :sessionId")
    List<Long> findStudentIdsBySessionId(@Param("sessionId") Long sessionId);
}
