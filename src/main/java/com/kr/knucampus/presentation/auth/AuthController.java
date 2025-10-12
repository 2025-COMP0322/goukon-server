package com.kr.knucampus.presentation.auth;

import com.kr.knucampus.application.auth.AuthService;
import com.kr.knucampus.global.annotation.AuthUser;
import com.kr.knucampus.presentation.auth.dto.request.LoginRequest;
import com.kr.knucampus.presentation.auth.dto.request.PasswordResetRequest;
import com.kr.knucampus.presentation.auth.dto.request.SignUpRequest;
import com.kr.knucampus.presentation.auth.dto.response.Login200Response;
import com.kr.knucampus.presentation.auth.dto.response.PasswordReset200Response;
import com.kr.knucampus.presentation.auth.dto.response.SignUp201Response;
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
    public ResponseEntity<SignUp201Response> signUp(@RequestBody SignUpRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.signUp(request));
    }

    @PostMapping("/login")
    public ResponseEntity<Login200Response> login(@RequestBody LoginRequest request){
        return ResponseEntity.ok().body(authService.login(request));
    }

    @PatchMapping("/reset")
    public ResponseEntity<PasswordReset200Response> passwordReset(@AuthUser Long memberId, @RequestBody PasswordResetRequest request){
        return ResponseEntity.ok()
                .body(authService.passwordReset(memberId, request));
    }
}
