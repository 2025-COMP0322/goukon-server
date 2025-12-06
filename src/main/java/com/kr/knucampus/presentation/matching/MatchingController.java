package com.kr.knucampus.presentation.matching;

import com.kr.knucampus.application.matching.MatchingService;
import com.kr.knucampus.domain.matchingqueue.MatchingQueue;
import com.kr.knucampus.domain.matchingqueue.MatchingType;
import com.kr.knucampus.domain.sessionmatches.SessionMatches;
import com.kr.knucampus.global.annotation.AuthUser;
import com.kr.knucampus.global.exception.BusinessException;
import com.kr.knucampus.global.exception.ErrorCode;
import com.kr.knucampus.presentation.matching.dto.request.RegisterQueueRequest;
import com.kr.knucampus.presentation.matching.dto.response.MatchingQueueResponse;
import com.kr.knucampus.presentation.matching.dto.response.SessionMatchResponse;
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
}
