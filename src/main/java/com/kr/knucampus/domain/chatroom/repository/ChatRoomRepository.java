package com.kr.knucampus.domain.chatroom.repository;

import com.kr.knucampus.domain.chatroom.ChatRoom;
import com.kr.knucampus.domain.matchingsession.MatchingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findBySession(MatchingSession session);

    // 학생이 참여한 채팅방 목록 (그룹 -> 세션매치 -> 채팅방)
    @Query("SELECT cr FROM ChatRoom cr " +
           "WHERE cr.session.id IN (" +
           "  SELECT sm.session.id FROM SessionMatches sm " +
           "  WHERE sm.group.id IN (" +
           "    SELECT sg.group.id FROM StudentGroup sg WHERE sg.student.id = :studentId" +
           "  )" +
           ") ORDER BY cr.createdAt DESC")
    List<ChatRoom> findByStudentId(@Param("studentId") Long studentId);
}
