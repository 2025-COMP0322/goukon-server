package com.kr.knucampus.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<String> handleCustomException(BusinessException e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(e.getErrorMessage());
    }
}
