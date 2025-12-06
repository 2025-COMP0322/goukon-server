package com.kr.knucampus.presentation.chatroom.dto.response;

import com.kr.knucampus.domain.chatroom.ChatRoom;

import java.time.LocalDateTime;

public record ChatRoomResponse(
        Long sessionId,
        LocalDateTime createdAt
) {
    public static ChatRoomResponse from(ChatRoom chatRoom) {
        return new ChatRoomResponse(
                chatRoom.getId(),
                chatRoom.getCreatedAt()
        );
    }
}
