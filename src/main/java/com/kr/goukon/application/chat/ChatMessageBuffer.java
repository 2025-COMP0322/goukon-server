package com.kr.goukon.application.chat;

import com.kr.goukon.presentation.chat.dto.ChatMessage;

public interface ChatMessageBuffer {

    void save(ChatMessage message);
}
