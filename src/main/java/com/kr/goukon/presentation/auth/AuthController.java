package com.kr.goukon.presentation.auth;

import com.kr.goukon.application.auth.AuthService;
import com.kr.goukon.global.annotation.AuthUser;
import com.kr.goukon.presentation.auth.dto.request.LoginRequest;
import com.kr.goukon.presentation.auth.dto.request.PasswordResetRequest;
import com.kr.goukon.presentation.auth.dto.request.RefreshTokenRequest;
import com.kr.goukon.presentation.auth.dto.request.SignUpRequest;
import com.kr.goukon.presentation.auth.dto.response.CheckDuplicate200Response;
import com.kr.goukon.presentation.auth.dto.response.Login200Response;
import com.kr.goukon.presentation.auth.dto.response.Logout200Response;
import com.kr.goukon.presentation.auth.dto.response.PasswordReset200Response;
import com.kr.goukon.presentation.auth.dto.response.SignUp201Response;
import com.kr.goukon.presentation.auth.dto.response.TokenRefresh200Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignUp201Response> signUp(@Valid @RequestBody SignUpRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.signUp(request));
    }

    @PostMapping("/login")
    public ResponseEntity<Login200Response> login(@Valid @RequestBody LoginRequest request){
        return ResponseEntity.ok().body(authService.login(request));
    }

    @PatchMapping("/reset")
    public ResponseEntity<PasswordReset200Response> passwordReset(@AuthUser Long studentId, @Valid @RequestBody PasswordResetRequest request){
        return ResponseEntity.ok()
                .body(authService.passwordReset(studentId, request));
    }

    @GetMapping("/check-email")
    public ResponseEntity<CheckDuplicate200Response> checkEmail(@RequestParam String email) {
        boolean exists = authService.checkEmailExists(email);
        return ResponseEntity.ok(new CheckDuplicate200Response(exists));
    }

    @GetMapping("/check-student-number")
    public ResponseEntity<CheckDuplicate200Response> checkStudentNumber(@RequestParam String studentNumber) {
        boolean exists = authService.checkStudentNumberExists(studentNumber);
        return ResponseEntity.ok(new CheckDuplicate200Response(exists));
    }

    /**
     * Refresh Token을 사용하여 새로운 Access Token 발급
     * @param request Refresh Token 요청 DTO
     * @return 새로운 Access Token + Refresh Token
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefresh200Response> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok()
                .body(authService.refreshAccessToken(request.refreshToken()));
    }

    /**
     * 로그아웃
     * @param studentId 인증된 사용자 ID
     * @return 로그아웃 성공 메시지
     */
    @PostMapping("/logout")
    public ResponseEntity<Logout200Response> logout(@AuthUser Long studentId) {
        return ResponseEntity.ok()
                .body(authService.logout(studentId));
    }
}
