package com.kr.knucampus.presentation.chat;

import com.kr.knucampus.presentation.chat.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    @MessageMapping("/sendMessage")
    @SendTo("/topic/chatroom/{roomId}")
    public String sendMessage(ChatMessage message) {
        // 메세지 가공, 저장 로직 필요(Redis, DB 등)
        log.info("보낸 사람: {}, 내용: {}", message.senderName(), message.content());
        return message.content();
    }

    @MessageMapping("/enter")
    @SendTo("/topic/chatroom/{roomId}")
    public ChatMessage enter(ChatMessage message) {
        // 입장 처리
        return message;
    }

    @MessageMapping("/leave")
    @SendTo("/topic/chatroom/{roomId}")
    public ChatMessage leave(ChatMessage message) {
        // 퇴장 처리
        return message;
    }
}
