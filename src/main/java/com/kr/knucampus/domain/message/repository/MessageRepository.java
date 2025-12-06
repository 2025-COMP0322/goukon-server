package com.kr.knucampus.domain.message.repository;

import com.kr.knucampus.domain.message.Message;
import com.kr.knucampus.domain.message.MessageId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, MessageId> {

    // 채팅방의 메시지 목록 (최신순)
    @Query("SELECT m FROM Message m JOIN FETCH m.sender WHERE m.chatRoom.id = :sessionId ORDER BY m.createdAt DESC")
    List<Message> findByChatRoomId(@Param("sessionId") Long sessionId);

    // 채팅방의 메시지 목록 (페이징)
    @Query("SELECT m FROM Message m JOIN FETCH m.sender WHERE m.chatRoom.id = :sessionId ORDER BY m.createdAt DESC")
    Page<Message> findByChatRoomIdWithPaging(@Param("sessionId") Long sessionId, Pageable pageable);

    // 특정 학생이 보낸 메시지
    @Query("SELECT m FROM Message m WHERE m.sender.id = :studentId ORDER BY m.createdAt DESC")
    List<Message> findBySenderId(@Param("studentId") Long studentId);

    // 세션 내에서 학생의 마지막 메시지 ID 조회 (새 메시지 ID 생성용)
    @Query("SELECT MAX(m.id.messageId) FROM Message m WHERE m.chatRoom.id = :sessionId AND m.sender.id = :studentId")
    Optional<Long> findMaxMessageIdBySessionAndStudent(
            @Param("sessionId") Long sessionId,
            @Param("studentId") Long studentId);

    // 채팅방의 마지막 메시지 조회
    @Query("SELECT m FROM Message m JOIN FETCH m.sender WHERE m.chatRoom.id = :sessionId ORDER BY m.createdAt DESC LIMIT 1")
    Optional<Message> findLastMessageByChatRoomId(@Param("sessionId") Long sessionId);
}
