package com.kr.knucampus.presentation.chatroom.dto.response;

import com.kr.knucampus.domain.message.Message;

import java.time.LocalDateTime;

public record MessageResponse(
        Long sessionId,
        Long senderId,
        String senderName,
        Long messageId,
        String content,
        LocalDateTime createdAt
) {
    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getId().getSessionId(),
                message.getSender().getId(),
                message.getSender().getName(),
                message.getId().getMessageId(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
