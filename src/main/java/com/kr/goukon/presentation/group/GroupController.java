package com.kr.goukon.presentation.group;

import com.kr.goukon.application.group.GroupService;
import com.kr.goukon.domain.group.Group;
import com.kr.goukon.domain.student.Student;
import com.kr.goukon.global.annotation.AuthUser;
import com.kr.goukon.presentation.group.dto.request.AddMemberRequest;
import com.kr.goukon.presentation.group.dto.response.GroupDetailResponse;
import com.kr.goukon.presentation.group.dto.response.GroupResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /**
     * 그룹 생성
     */
    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@AuthUser Long studentId) {
        Group group = groupService.createGroup(studentId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GroupResponse.from(group));
    }

    /**
     * 그룹 조회
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDetailResponse> getGroup(@PathVariable Long groupId) {
        Group group = groupService.getGroup(groupId);
        List<Student> members = groupService.getGroupMembers(groupId);
        return ResponseEntity.ok(GroupDetailResponse.from(group, members));
    }

    /**
     * 내 그룹 목록 조회
     */
    @GetMapping("/me")
    public ResponseEntity<List<GroupResponse>> getMyGroups(@AuthUser Long studentId) {
        List<Group> groups = groupService.getStudentGroups(studentId);
        List<GroupResponse> responses = groups.stream()
                .map(GroupResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * 그룹에 멤버 추가
     */
    @PostMapping("/{groupId}/members")
    public ResponseEntity<Void> addMember(
            @AuthUser Long requesterId,
            @PathVariable Long groupId,
            @Valid @RequestBody AddMemberRequest request) {
        groupService.addMember(groupId, request.studentId(), requesterId);
        return ResponseEntity.ok().build();
    }

    /**
     * 그룹에서 멤버 제거
     */
    @DeleteMapping("/{groupId}/members/{studentId}")
    public ResponseEntity<Void> removeMember(
            @AuthUser Long requesterId,
            @PathVariable Long groupId,
            @PathVariable Long studentId) {
        groupService.removeMember(groupId, studentId, requesterId);
        return ResponseEntity.ok().build();
    }

    /**
     * 그룹 나가기
     */
    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<Void> leaveGroup(
            @AuthUser Long studentId,
            @PathVariable Long groupId) {
        groupService.removeMember(groupId, studentId);
        return ResponseEntity.ok().build();
    }

    /**
     * 그룹 삭제
     */
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(
            @AuthUser Long studentId,
            @PathVariable Long groupId) {
        groupService.deleteGroup(groupId, studentId);
        return ResponseEntity.noContent().build();
    }
}
