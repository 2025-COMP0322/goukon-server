package com.kr.goukon.presentation.matching.dto.response;

import com.kr.goukon.application.matching.MatchingService;

public record EndVoteResponse(
        boolean sessionEnded,
        int currentVotes,
        int requiredVotes
) {
    public static EndVoteResponse from(MatchingService.EndVoteResult result) {
        return new EndVoteResponse(
                result.sessionEnded(),
                result.currentVotes(),
                result.requiredVotes()
        );
    }
}
