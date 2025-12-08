package com.kr.goukon.application.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kr.goukon.presentation.chat.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Override
    public long size(Long roomId) {
        if (roomId == null) {
            return 0L;
        }
        Long size = redisTemplate.opsForList().size(buildKey(roomId));
        return size != null ? size : 0L;
    }

    @Override
    public List<ChatMessage> popAll(Long roomId) {
        if (roomId == null) {
            return Collections.emptyList();
        }
        String key = buildKey(roomId);
        List<String> serializedMessages = redisTemplate.opsForList().range(key, 0, -1);
        if (serializedMessages == null || serializedMessages.isEmpty()) {
            return Collections.emptyList();
        }
        redisTemplate.delete(key);
        List<ChatMessage> messages = serializedMessages.stream()
                .map(this::deserialize)
                .collect(Collectors.toList());
        Collections.reverse(messages);
        return messages;
    }

    @Override
    public Set<Long> roomsWithBufferedMessages() {
        Set<String> keys = redisTemplate.keys(CHAT_SESSION_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Long> roomIds = new HashSet<>();
        for (String key : keys) {
            Long sessionId = parseSessionId(key);
            if (sessionId != null) {
                roomIds.add(sessionId);
            }
        }
        return roomIds;
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

    private ChatMessage deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, ChatMessage.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize ChatMessage", e);
        }
    }

    private Long parseSessionId(String key) {
        if (key == null || !key.startsWith(CHAT_SESSION_KEY_PREFIX)) {
            return null;
        }
        try {
            return Long.parseLong(key.substring(CHAT_SESSION_KEY_PREFIX.length()));
        } catch (NumberFormatException e) {
            log.warn("Invalid chat session key detected: {}", key);
            return null;
        }
    }
}
