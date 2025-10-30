package com.kr.knucampus.application.chat;

import com.kr.knucampus.presentation.chat.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
    private final SimpMessagingTemplate messagingTemplate;

    public void sendMessage(ChatMessage message) {
        String destination = "/topic/chatroom/" + message.roomId();
        messagingTemplate.convertAndSend(destination, message);
        log.info("Sent message to {}: {}", destination, message.content());
    }

    public void handleEnter(ChatMessage message) {
        ChatMessage enterMessage = ChatMessage.enter(message.roomId(), message.senderName());
        messagingTemplate.convertAndSend("/topic/chatroom/" + message.roomId(), enterMessage);
    }

    public void handleLeave(ChatMessage message) {
        ChatMessage leaveMessage = ChatMessage.leave(message.roomId(), message.senderName());
        messagingTemplate.convertAndSend("/topic/chatroom/" + message.roomId(), leaveMessage);
    }
}
