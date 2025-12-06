package com.kr.knucampus.presentation.chat;

import com.kr.knucampus.application.chat.ChatService;
import com.kr.knucampus.presentation.chat.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @MessageMapping("/sendMessage")
    public void sendMessage(ChatMessage message) {
        log.info("Received message: {}", message.content());
        chatService.sendMessage(message);
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
