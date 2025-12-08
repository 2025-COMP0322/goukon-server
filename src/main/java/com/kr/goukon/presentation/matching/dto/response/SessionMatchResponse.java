package com.kr.goukon.presentation.matching.dto.response;

import com.kr.goukon.domain.sessionmatches.SessionMatches;

import java.time.LocalDateTime;

public record SessionMatchResponse(
        Long sessionId,
        Long groupId,
        Long queueId,
        LocalDateTime createdAt
) {
    public static SessionMatchResponse from(SessionMatches match) {
        return new SessionMatchResponse(
                match.getSession().getId(),
                match.getGroup().getId(),
                match.getQueue().getId(),
                match.getCreatedAt()
        );
    }
}
