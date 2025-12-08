package com.kr.goukon.presentation.matching.dto.response;

import com.kr.goukon.application.matching.MatchingService;

import java.util.List;

public record EndVoteStatusResponse(
        boolean isActive,
        boolean hasVoted,
        int currentVotes,
        int requiredVotes,
        List<Long> votedMemberIds
) {
    public static EndVoteStatusResponse from(MatchingService.EndVoteStatus status) {
        return new EndVoteStatusResponse(
                status.isActive(),
                status.hasVoted(),
                status.currentVotes(),
                status.requiredVotes(),
                status.votedMemberIds()
        );
    }
}
