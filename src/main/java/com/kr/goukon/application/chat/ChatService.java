package com.kr.goukon.application.chat;

import com.kr.goukon.domain.chatroom.ChatRoom;
import com.kr.goukon.domain.chatroom.repository.ChatRoomRepository;
import com.kr.goukon.domain.message.Message;
import com.kr.goukon.domain.message.repository.MessageRepository;
import com.kr.goukon.domain.sessionmatches.SessionMatches;
import com.kr.goukon.domain.sessionmatches.repository.SessionMatchesRepository;
import com.kr.goukon.domain.student.Student;
import com.kr.goukon.domain.student.repository.StudentRepository;
import com.kr.goukon.domain.studentgroup.repository.StudentGroupRepository;
import com.kr.goukon.global.exception.BusinessException;
import com.kr.goukon.global.exception.ErrorCode;
import com.kr.goukon.presentation.chat.dto.ChatMessage;
import com.kr.goukon.presentation.chat.dto.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private static final int BUFFER_FLUSH_THRESHOLD = 10;

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final StudentRepository studentRepository;
    private final SessionMatchesRepository sessionMatchesRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final ChatMessageBuffer chatMessageBuffer;
    private final PlatformTransactionManager transactionManager;

    /**
     * 실시간 메시지 전송 (WebSocket)
     */
    public void sendMessage(ChatMessage message) {
        chatMessageBuffer.save(message);
        String destination = "/topic/chatroom/" + message.roomId();
        messagingTemplate.convertAndSend(destination, message);
        log.info("Sent message to {}: {}", destination, message.content());
        flushBufferedMessagesIfNeeded(message.roomId());
    }

    /**
     * 채팅방 입장 처리 (WebSocket)
     */
    public void handleEnter(ChatMessage message) {
        chatMessageBuffer.save(message);
        ChatMessage enterMessage = ChatMessage.enter(message.roomId(), message.senderId(), message.senderName());
        messagingTemplate.convertAndSend("/topic/chatroom/" + message.roomId(), enterMessage);
    }

    /**
     * 채팅방 퇴장 처리 (WebSocket)
     */
    public void handleLeave(ChatMessage message) {
        chatMessageBuffer.save(message);
        ChatMessage leaveMessage = ChatMessage.leave(message.roomId(), message.senderId(), message.senderName());
        messagingTemplate.convertAndSend("/topic/chatroom/" + message.roomId(), leaveMessage);
    }

    @Transactional
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
        flushBufferedMessages(sessionId);
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
        flushBufferedMessages(sessionId);
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
        flushBufferedMessages(sessionId);
        return messageRepository.findLastMessageByChatRoomId(sessionId).orElse(null);
    }

    private void flushBufferedMessagesIfNeeded(Long sessionId) {
        if (sessionId == null) {
            return;
        }
        long bufferSize = chatMessageBuffer.size(sessionId);
        if (bufferSize >= BUFFER_FLUSH_THRESHOLD) {
            flushBufferedMessages(sessionId);
        }
    }

    public void flushBufferedMessages(Long sessionId) {
        if (sessionId == null) {
            return;
        }
        List<ChatMessage> bufferedMessages = chatMessageBuffer.popAll(sessionId);
        if (bufferedMessages.isEmpty()) {
            return;
        }

        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        template.setReadOnly(false);
        template.executeWithoutResult(status -> persistBufferedMessages(bufferedMessages));
    }

    private void persistBufferedMessages(List<ChatMessage> bufferedMessages) {
        for (ChatMessage bufferedMessage : bufferedMessages) {
            if (bufferedMessage.type() != MessageType.CHAT) {
                continue;
            }

            Long senderId = bufferedMessage.senderId();
            if (senderId == null) {
                log.warn("Skip buffered chat message without senderId: session={}", bufferedMessage.roomId());
                continue;
            }

            try {
                saveMessage(bufferedMessage.roomId(), senderId, bufferedMessage.content());
            } catch (BusinessException e) {
                log.warn("Failed to persist buffered message: session={}, sender={}, reason={}",
                        bufferedMessage.roomId(), senderId, e.getErrorMessage());
            }
        }
    }
}
