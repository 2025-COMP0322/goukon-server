package com.kr.goukon.global.status;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessCode {
    // * Auth
    PASSWORD_CHANGED(HttpStatus.OK, "SA_001", "비밀번호가 변경되었습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
