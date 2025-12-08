package com.kr.goukon.presentation.group.dto.response;

import com.kr.goukon.domain.group.Group;

import java.time.LocalDateTime;

public record GroupResponse(
        Long id,
        String gender,
        String status,
        LocalDateTime createdAt
) {
    public static GroupResponse from(Group group) {
        return new GroupResponse(
                group.getId(),
                group.getGender().name(),
                group.getStatus().name(),
                group.getCreatedAt()
        );
    }
}
