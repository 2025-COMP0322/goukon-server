package com.kr.goukon.presentation.matching;

import com.kr.goukon.application.matching.MatchingService;
import com.kr.goukon.domain.matchingqueue.MatchingQueue;
import com.kr.goukon.domain.matchingqueue.MatchingType;
import com.kr.goukon.domain.sessionmatches.SessionMatches;
import com.kr.goukon.global.annotation.AuthUser;
import com.kr.goukon.global.exception.BusinessException;
import com.kr.goukon.global.exception.ErrorCode;
import com.kr.goukon.presentation.matching.dto.request.RegisterQueueRequest;
import com.kr.goukon.presentation.matching.dto.response.EndVoteResponse;
import com.kr.goukon.presentation.matching.dto.response.EndVoteStatusResponse;
import com.kr.goukon.presentation.matching.dto.response.MatchingQueueResponse;
import com.kr.goukon.presentation.matching.dto.response.SessionDetailResponse;
import com.kr.goukon.presentation.matching.dto.response.SessionMatchResponse;
import com.kr.goukon.presentation.group.dto.response.GroupDetailResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;

    /**
     * 매칭 대기열 등록
     */
    @PostMapping("/queue")
    public ResponseEntity<MatchingQueueResponse> registerQueue(@Valid @RequestBody RegisterQueueRequest request) {
        MatchingType type;
        try {
            type = MatchingType.valueOf(request.matchingType());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_MATCHING_TYPE);
        }

        MatchingQueue queue = matchingService.registerQueue(request.groupId(), type);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MatchingQueueResponse.from(queue));
    }

    /**
     * 매칭 대기열 취소
     */
    @DeleteMapping("/queue/{groupId}")
    public ResponseEntity<Void> cancelQueue(@PathVariable Long groupId) {
        matchingService.cancelQueue(groupId);
        return ResponseEntity.ok().build();
    }

    /**
     * 매칭 대기열 상태 조회
     */
    @GetMapping("/queue/{groupId}")
    public ResponseEntity<MatchingQueueResponse> getQueueStatus(@PathVariable Long groupId) {
        MatchingQueue queue = matchingService.getQueueStatus(groupId);
        return ResponseEntity.ok(MatchingQueueResponse.from(queue));
    }

    /**
     * 내 매칭 세션 목록 조회
     */
    @GetMapping("/sessions/me")
    public ResponseEntity<List<SessionMatchResponse>> getMySessions(@AuthUser Long studentId) {
        List<SessionMatches> matches = matchingService.getStudentSessions(studentId);
        List<SessionMatchResponse> responses = matches.stream()
                .map(SessionMatchResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * 매칭 세션 상세 조회 (우리 팀 + 상대 팀 정보)
     */
    @GetMapping("/sessions/{sessionId}/details")
    public ResponseEntity<SessionDetailResponse> getSessionDetails(
            @PathVariable Long sessionId,
            @AuthUser Long studentId) {
        MatchingService.SessionDetailData data = matchingService.getSessionDetails(sessionId, studentId);

        GroupDetailResponse myGroupResponse = GroupDetailResponse.from(data.myGroup(), data.myMembers());
        GroupDetailResponse opponentGroupResponse = GroupDetailResponse.from(data.opponentGroup(), data.opponentMembers());

        return ResponseEntity.ok(SessionDetailResponse.of(sessionId, myGroupResponse, opponentGroupResponse));
    }

    /**
     * 세션 종료 투표
     */
    @PostMapping("/sessions/{sessionId}/end-vote")
    public ResponseEntity<EndVoteResponse> voteEndSession(
            @PathVariable Long sessionId,
            @AuthUser Long studentId) {
        MatchingService.EndVoteResult result = matchingService.voteEndSession(sessionId, studentId);
        return ResponseEntity.ok(EndVoteResponse.from(result));
    }

    /**
     * 세션 종료 투표 상태 조회
     */
    @GetMapping("/sessions/{sessionId}/end-vote")
    public ResponseEntity<EndVoteStatusResponse> getEndVoteStatus(
            @PathVariable Long sessionId,
            @AuthUser Long studentId) {
        MatchingService.EndVoteStatus status = matchingService.getEndVoteStatus(sessionId, studentId);
        return ResponseEntity.ok(EndVoteStatusResponse.from(status));
    }
}
