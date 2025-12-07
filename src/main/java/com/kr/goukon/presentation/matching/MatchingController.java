package com.kr.goukon.presentation.matching;

import com.kr.goukon.application.matching.MatchingService;
import com.kr.goukon.domain.matchingqueue.MatchingQueue;
import com.kr.goukon.domain.matchingqueue.MatchingType;
import com.kr.goukon.domain.sessionmatches.SessionMatches;
import com.kr.goukon.global.annotation.AuthUser;
import com.kr.goukon.global.exception.BusinessException;
import com.kr.goukon.global.exception.ErrorCode;
import com.kr.goukon.presentation.matching.dto.request.RegisterQueueRequest;
import com.kr.goukon.presentation.matching.dto.response.MatchingQueueResponse;
import com.kr.goukon.presentation.matching.dto.response.SessionMatchResponse;
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
