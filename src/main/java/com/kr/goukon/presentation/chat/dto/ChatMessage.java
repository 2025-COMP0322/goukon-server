package com.kr.goukon.presentation.chat.dto;

public record ChatMessage(
        Long roomId,
        String senderName,
        String content,
        MessageType type
) {
    public static ChatMessage of(Long roomId, String senderName, String content, MessageType type) {
        return new ChatMessage(roomId, senderName, content, type);
    }

    public static ChatMessage enter(Long roomId, String senderName) {
        return new ChatMessage(roomId, senderName, senderName + "님이 입장했습니다.", MessageType.ENTER);
    }

    public static ChatMessage leave(Long roomId, String senderName) {
        return new ChatMessage(roomId, senderName, senderName + "님이 퇴장했습니다.", MessageType.LEAVE);
    }
}
