package com.kr.knucampus.presentation.chat.dto;

public record ChatMessage(
        Long roomId,
        String senderName,
        String content,
        MessageType type
) {
}
