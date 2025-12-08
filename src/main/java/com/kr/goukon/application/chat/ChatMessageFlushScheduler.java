package com.kr.goukon.application.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageFlushScheduler {

    private final ChatMessageBuffer chatMessageBuffer;
    private final ChatService chatService;

    @Scheduled(fixedDelayString = "${chat.flush.interval-ms:30000}")
    public void flushBufferedMessagesPeriodically() {
        Set<Long> roomIds = chatMessageBuffer.roomsWithBufferedMessages();
        if (roomIds.isEmpty()) {
            return;
        }

        for (Long roomId : roomIds) {
            try {
                chatService.flushBufferedMessages(roomId);
            } catch (Exception e) {
                log.warn("Failed to flush buffered messages for room {}", roomId, e);
            }
        }
    }
}
