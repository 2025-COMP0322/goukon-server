package com.kr.goukon.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // * Token
    NO_TOKEN(HttpStatus.NOT_FOUND, "TE_001", "토큰이 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "TE_002", "토큰이 유효하지않습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TE_003", "토큰이 만료되었습니다"),
    INVALID_HEADER(HttpStatus.UNAUTHORIZED, "TE_004", "유효하지 않은 Authorization Header 입니다."),
    INVALID_TOKEN_PROVIDER(HttpStatus.INTERNAL_SERVER_ERROR, "TE_005", "유효하지 않는 Token Provider 를 적용시켰습니다."),

    // * Auth
    WRONG_MBTI(HttpStatus.BAD_REQUEST, "AE_001", "MBTI 값이 올바르지 않습니다"),
    NO_USER(HttpStatus.NOT_FOUND, "AE_002", "해당 유저를 찾을 수 없습니다."),
    WRONG_PASSWORD(HttpStatus.BAD_REQUEST, "AE_003", "비밀번호가 일치하지 않습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "AE_004", "이미 사용중인 이메일입니다."),
    DUPLICATE_STUDENT_NUMBER(HttpStatus.CONFLICT, "AE_005", "이미 등록된 학번입니다."),

    // * Student
    STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND, "SE_001", "학생을 찾을 수 없습니다."),
    INVALID_AGE(HttpStatus.BAD_REQUEST, "SE_002", "나이는 18세 이상 30세 이하여야 합니다."),

    // * Group
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "GE_001", "그룹을 찾을 수 없습니다."),
    ALREADY_IN_GROUP(HttpStatus.CONFLICT, "GE_002", "이미 그룹에 속해있습니다."),
    NOT_GROUP_MEMBER(HttpStatus.FORBIDDEN, "GE_003", "그룹의 멤버가 아닙니다."),
    GROUP_FULL(HttpStatus.BAD_REQUEST, "GE_004", "그룹 인원이 가득 찼습니다."),
    GROUP_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "GE_005", "그룹이 매칭 가능한 상태가 아닙니다."),
    GENDER_MISMATCH(HttpStatus.BAD_REQUEST, "GE_006", "그룹의 성별과 일치하지 않습니다."),
    CANNOT_LEAVE_WHILE_MATCHING(HttpStatus.BAD_REQUEST, "GE_007", "매칭 중에는 그룹을 떠날 수 없습니다."),
    EMPTY_GROUP(HttpStatus.BAD_REQUEST, "GE_008", "그룹에 멤버가 없습니다."),

    // * Matching
    MATCHING_QUEUE_NOT_FOUND(HttpStatus.NOT_FOUND, "ME_001", "매칭 대기열을 찾을 수 없습니다."),
    ALREADY_IN_QUEUE(HttpStatus.CONFLICT, "ME_002", "이미 매칭 대기열에 있습니다."),
    NOT_IN_QUEUE(HttpStatus.BAD_REQUEST, "ME_003", "매칭 대기열에 없습니다."),
    MATCHING_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "ME_004", "매칭 세션을 찾을 수 없습니다."),
    SESSION_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "ME_005", "활성화된 세션이 아닙니다."),
    INVALID_MATCHING_TYPE(HttpStatus.BAD_REQUEST, "ME_006", "유효하지 않은 매칭 타입입니다."),
    MEMBER_COUNT_MISMATCH(HttpStatus.BAD_REQUEST, "ME_007", "매칭 타입에 맞는 인원이 아닙니다. (1:1은 1명, 3:3은 3명)"),
    MATCHING_IN_PROGRESS(HttpStatus.CONFLICT, "ME_008", "이미 매칭이 진행중입니다."),
    INVALID_SESSION_STATE(HttpStatus.INTERNAL_SERVER_ERROR, "ME_009", "세션 상태가 올바르지 않습니다."),
    NOT_SESSION_MEMBER(HttpStatus.FORBIDDEN, "ME_010", "해당 세션의 멤버가 아닙니다."),

    // * ChatRoom
    CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CE_001", "채팅방을 찾을 수 없습니다."),
    NOT_CHATROOM_MEMBER(HttpStatus.FORBIDDEN, "CE_002", "채팅방의 멤버가 아닙니다."),

    // * Message
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "MSE_001", "메시지를 찾을 수 없습니다."),
    EMPTY_MESSAGE(HttpStatus.BAD_REQUEST, "MSE_002", "메시지 내용이 비어있습니다."),

    // * Report
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "RE_001", "신고를 찾을 수 없습니다."),
    EMPTY_REPORT_CONTENT(HttpStatus.BAD_REQUEST, "RE_002", "신고 내용이 비어있습니다."),
    INVALID_REPORT_STATUS(HttpStatus.BAD_REQUEST, "RE_003", "유효하지 않은 신고 상태입니다."),

    // * Invite Code
    INVALID_INVITE_CODE(HttpStatus.BAD_REQUEST, "IC_001", "유효하지 않거나 만료된 초대 코드입니다."),
    INVITE_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "IC_002", "초대 코드가 만료되었습니다."),
    INVITE_CODE_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "IC_003", "초대 코드 생성에 실패했습니다."),

    // * Common
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CE_001", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "CE_002", "입력값이 올바르지 않습니다."),
    CONCURRENT_MODIFICATION(HttpStatus.CONFLICT, "CE_003", "동시 수정이 감지되었습니다. 다시 시도해주세요."),
    ADMIN_ONLY(HttpStatus.FORBIDDEN, "CE_004", "관리자 권한이 필요합니다."),
    NOT_GROUP_LEADER(HttpStatus.FORBIDDEN, "CE_005", "그룹장만 수행할 수 있습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
