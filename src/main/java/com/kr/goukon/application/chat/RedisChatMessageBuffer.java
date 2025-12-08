package com.kr.goukon.application.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kr.goukon.presentation.chat.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisChatMessageBuffer implements ChatMessageBuffer {

    private static final String CHAT_SESSION_KEY_PREFIX = "chat:session:";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void save(ChatMessage message) {
        if (message == null || message.roomId() == null) {
            log.warn("Skip saving chat message because roomId is null: {}", message);
            return;
        }

        String key = buildKey(message.roomId());
        String payload = serialize(message);
        redisTemplate.opsForList().leftPush(key, payload);
    }

    private String buildKey(Long sessionId) {
        return CHAT_SESSION_KEY_PREFIX + sessionId;
    }

    private String serialize(ChatMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize ChatMessage", e);
        }
    }
}
