package com.kr.knucampus.presentation.auth.dto.request;

public record PasswordResetRequest(String password, String newPassword) {
}
