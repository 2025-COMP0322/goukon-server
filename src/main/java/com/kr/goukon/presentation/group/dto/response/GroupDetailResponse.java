package com.kr.goukon.presentation.group.dto.response;

import com.kr.goukon.domain.group.Group;
import com.kr.goukon.domain.student.Student;
import com.kr.goukon.presentation.student.dto.response.StudentResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record GroupDetailResponse(
        Long id,
        String gender,
        String status,
        LocalDateTime createdAt,
        List<StudentResponse> members
) {
    public static GroupDetailResponse from(Group group, List<Student> members) {
        return new GroupDetailResponse(
                group.getId(),
                group.getGender().name(),
                group.getStatus().name(),
                group.getCreatedAt(),
                members.stream().map(StudentResponse::from).collect(Collectors.toList())
        );
    }
}
