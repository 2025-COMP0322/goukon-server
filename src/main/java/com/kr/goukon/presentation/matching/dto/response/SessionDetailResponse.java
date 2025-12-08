package com.kr.goukon.presentation.matching.dto.response;

import com.kr.goukon.presentation.group.dto.response.GroupDetailResponse;

public record SessionDetailResponse(
        Long sessionId,
        GroupDetailResponse myGroup,
        GroupDetailResponse opponentGroup
) {
    public static SessionDetailResponse of(Long sessionId, GroupDetailResponse myGroup, GroupDetailResponse opponentGroup) {
        return new SessionDetailResponse(sessionId, myGroup, opponentGroup);
    }
}
