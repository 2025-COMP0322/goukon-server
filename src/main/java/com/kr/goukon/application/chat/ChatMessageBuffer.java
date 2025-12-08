package com.kr.goukon.application.chat;

import com.kr.goukon.presentation.chat.dto.ChatMessage;

import java.util.List;
import java.util.Set;

public interface ChatMessageBuffer {

    void save(ChatMessage message);

    long size(Long roomId);

    List<ChatMessage> popAll(Long roomId);

    Set<Long> roomsWithBufferedMessages();
}
