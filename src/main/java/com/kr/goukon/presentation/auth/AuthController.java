package com.kr.goukon.presentation.auth;

import com.kr.goukon.application.auth.AuthService;
import com.kr.goukon.global.annotation.AuthUser;
import com.kr.goukon.presentation.auth.dto.request.LoginRequest;
import com.kr.goukon.presentation.auth.dto.request.PasswordResetRequest;
import com.kr.goukon.presentation.auth.dto.request.SignUpRequest;
import com.kr.goukon.presentation.auth.dto.response.Login200Response;
import com.kr.goukon.presentation.auth.dto.response.PasswordReset200Response;
import com.kr.goukon.presentation.auth.dto.response.SignUp201Response;
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
}
