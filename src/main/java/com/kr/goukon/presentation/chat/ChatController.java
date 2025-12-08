package com.kr.goukon.presentation.chat;

import com.kr.goukon.application.chat.ChatService;
import com.kr.goukon.presentation.chat.dto.ChatMessage;
import com.kr.goukon.presentation.chat.dto.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    /**
     * 모든 채팅 메시지 처리 (타입에 따라 분기)
     */
    @MessageMapping("/sendMessage")
    public void sendMessage(ChatMessage message) {
        log.info("Received message: type={}, content={}", message.type(), message.content());

        if (message.type() == null) {
            log.warn("Message type is null, treating as CHAT");
            chatService.sendMessage(message);
            return;
        }

        switch (message.type()) {
            case ENTER:
                chatService.handleEnter(message);
                break;
            case LEAVE:
                chatService.handleLeave(message);
                break;
            case CHAT:
            default:
                chatService.sendMessage(message);
                break;
        }
    }

    @MessageMapping("/enter")
    public void enter(ChatMessage message) {
        chatService.handleEnter(message);
    }

    @MessageMapping("/leave")
    public void leave(ChatMessage message) {
        chatService.handleLeave(message);
    }
}
