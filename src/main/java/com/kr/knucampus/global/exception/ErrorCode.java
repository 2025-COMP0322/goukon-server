package com.kr.knucampus.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // * Token
    NO_TOKEN(HttpStatus.NOT_FOUND, "TE_001", "토큰이 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "TE_002","토큰이 유효하지않습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TE_003", "토큰이 만료되었습니다"),
    INVALID_HEADER(HttpStatus.UNAUTHORIZED, "TE_004", "유효하지 않은 Authorization Header 입니다."),
    INVALID_TOKEN_PROVIDER(HttpStatus.INTERNAL_SERVER_ERROR, "TE_005","유효하지 않는 Token Provider 를 적용시켰습니다."),

    // * Auth
    WRONG_MBTI(HttpStatus.BAD_REQUEST, "AE_001", "멤버의 MBTI 값이 올바르지 않습니다"),
    NO_USER(HttpStatus.NOT_FOUND, "AE_002","해당 유저를 찾을 수 없습니다."),
    WRONG_PASSWORD(HttpStatus.BAD_REQUEST, "AE_003", "비밀번호가 일치하지 않습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
