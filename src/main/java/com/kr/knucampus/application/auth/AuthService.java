package com.kr.knucampus.application.auth;

import com.kr.knucampus.application.token.TokenService;
import com.kr.knucampus.domain.member.Member;
import com.kr.knucampus.domain.member.repository.MemberRepository;
import com.kr.knucampus.domain.token.TokenType;
import com.kr.knucampus.global.exception.BusinessException;
import com.kr.knucampus.presentation.auth.dto.request.LoginRequest;
import com.kr.knucampus.presentation.auth.dto.request.PasswordResetRequest;
import com.kr.knucampus.presentation.auth.dto.request.SignUpRequest;
import com.kr.knucampus.presentation.auth.dto.response.Login200Response;
import com.kr.knucampus.presentation.auth.dto.response.PasswordReset200Response;
import com.kr.knucampus.presentation.auth.dto.response.SignUp201Response;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.kr.knucampus.global.exception.ErrorCode.*;
import static com.kr.knucampus.global.status.SuccessCode.*;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public SignUp201Response signUp(SignUpRequest request){
        Member member = request.toEntity();
        member.encryptPassword(passwordEncoder);
        memberRepository.save(member);
        return SignUp201Response.of(member);
    }

    public Login200Response login(LoginRequest request){
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(NO_USER));
        if(!member.matchPassword(request.password(), passwordEncoder)){
            throw new BusinessException(WRONG_PASSWORD);
        }
        String accessToken = tokenService.getToken(TokenType.ACCESS, member.getId());
        String refreshToken = tokenService.getToken(TokenType.REFRESH, member.getId());
        return new Login200Response(accessToken, refreshToken);
    }

    public PasswordReset200Response passwordReset(Long memberId, PasswordResetRequest request){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(NO_USER));
        if(!member.matchPassword(request.password(), passwordEncoder)){
            throw new BusinessException(WRONG_PASSWORD);
        }
        member.modifyPassword(request.newPassword(), passwordEncoder);
        return new PasswordReset200Response(PASSWORD_CHANGED.getMessage());
    }
}
