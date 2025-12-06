package com.kr.knucampus.application.chat;

import com.kr.knucampus.domain.chatroom.ChatRoom;
import com.kr.knucampus.domain.chatroom.repository.ChatRoomRepository;
import com.kr.knucampus.domain.message.Message;
import com.kr.knucampus.domain.message.repository.MessageRepository;
import com.kr.knucampus.domain.sessionmatches.SessionMatches;
import com.kr.knucampus.domain.sessionmatches.repository.SessionMatchesRepository;
import com.kr.knucampus.domain.student.Student;
import com.kr.knucampus.domain.student.repository.StudentRepository;
import com.kr.knucampus.domain.studentgroup.repository.StudentGroupRepository;
import com.kr.knucampus.global.exception.BusinessException;
import com.kr.knucampus.global.exception.ErrorCode;
import com.kr.knucampus.presentation.chat.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final StudentRepository studentRepository;
    private final SessionMatchesRepository sessionMatchesRepository;
    private final StudentGroupRepository studentGroupRepository;

    /**
     * 실시간 메시지 전송 (WebSocket)
     */
    public void sendMessage(ChatMessage message) {
        String destination = "/topic/chatroom/" + message.roomId();
        messagingTemplate.convertAndSend(destination, message);
        log.info("Sent message to {}: {}", destination, message.content());
    }

    /**
     * 채팅방 입장 처리 (WebSocket)
     */
    public void handleEnter(ChatMessage message) {
        ChatMessage enterMessage = ChatMessage.enter(message.roomId(), message.senderName());
        messagingTemplate.convertAndSend("/topic/chatroom/" + message.roomId(), enterMessage);
    }

    /**
     * 채팅방 퇴장 처리 (WebSocket)
     */
    public void handleLeave(ChatMessage message) {
        ChatMessage leaveMessage = ChatMessage.leave(message.roomId(), message.senderName());
        messagingTemplate.convertAndSend("/topic/chatroom/" + message.roomId(), leaveMessage);
    }

    /**
     * 메시지 저장
     * 트랜잭션 격리 수준: SERIALIZABLE - 메시지 ID 생성 시 동시성 제어
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Message saveMessage(Long sessionId, Long senderId, String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException(ErrorCode.EMPTY_MESSAGE);
        }

        ChatRoom chatRoom = chatRoomRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHATROOM_NOT_FOUND));

        Student sender = studentRepository.findById(senderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        // 채팅방 멤버인지 확인
        if (!isChatRoomMember(sessionId, senderId)) {
            throw new BusinessException(ErrorCode.NOT_CHATROOM_MEMBER);
        }

        // 새 메시지 ID 생성
        Long newMessageId = messageRepository.findMaxMessageIdBySessionAndStudent(sessionId, senderId)
                .orElse(0L) + 1;

        Message message = Message.create(chatRoom, sender, newMessageId, content);
        messageRepository.save(message);

        log.info("Message saved: session={}, sender={}, messageId={}", sessionId, senderId, newMessageId);
        return message;
    }

    /**
     * 학생이 참여한 채팅방 목록 조회
     */
    public List<ChatRoom> getMyChatRooms(Long studentId) {
        return chatRoomRepository.findByStudentId(studentId);
    }

    /**
     * 채팅방 조회
     */
    public ChatRoom getChatRoom(Long sessionId) {
        return chatRoomRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHATROOM_NOT_FOUND));
    }

    /**
     * 채팅방 메시지 목록 조회
     */
    public List<Message> getChatMessages(Long sessionId, Long requesterId) {
        // 채팅방 존재 확인
        if (!chatRoomRepository.existsById(sessionId)) {
            throw new BusinessException(ErrorCode.CHATROOM_NOT_FOUND);
        }

        // 멤버 확인
        if (!isChatRoomMember(sessionId, requesterId)) {
            throw new BusinessException(ErrorCode.NOT_CHATROOM_MEMBER);
        }

        return messageRepository.findByChatRoomId(sessionId);
    }

    /**
     * 채팅방 메시지 목록 조회 (페이징)
     */
    public Page<Message> getChatMessagesWithPaging(Long sessionId, Long requesterId, Pageable pageable) {
        // 채팅방 존재 확인
        if (!chatRoomRepository.existsById(sessionId)) {
            throw new BusinessException(ErrorCode.CHATROOM_NOT_FOUND);
        }

        // 멤버 확인
        if (!isChatRoomMember(sessionId, requesterId)) {
            throw new BusinessException(ErrorCode.NOT_CHATROOM_MEMBER);
        }

        return messageRepository.findByChatRoomIdWithPaging(sessionId, pageable);
    }

    /**
     * 채팅방 멤버 확인
     */
    public boolean isChatRoomMember(Long sessionId, Long studentId) {
        // 학생이 속한 그룹들 중 해당 세션에 참여한 그룹이 있는지 확인
        List<SessionMatches> matches = sessionMatchesRepository.findBySessionId(sessionId);
        for (SessionMatches match : matches) {
            if (studentGroupRepository.existsByStudentIdAndGroupId(studentId, match.getGroup().getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 채팅방의 마지막 메시지 조회
     */
    public Message getLastMessage(Long sessionId) {
        return messageRepository.findLastMessageByChatRoomId(sessionId).orElse(null);
    }
}
