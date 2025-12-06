package com.kr.knucampus.presentation.matching.dto.response;

import com.kr.knucampus.domain.matchingqueue.MatchingQueue;

import java.time.LocalDateTime;

public record MatchingQueueResponse(
        Long queueId,
        Long groupId,
        String matchingStatus,
        String matchingType,
        LocalDateTime createdAt
) {
    public static MatchingQueueResponse from(MatchingQueue queue) {
        return new MatchingQueueResponse(
                queue.getId(),
                queue.getGroup().getId(),
                queue.getMatchingStatus().name(),
                queue.getMatchingType().name(),
                queue.getCreatedAt()
        );
    }
}
