package com.kr.knucampus.presentation.auth.dto.request;

import com.kr.knucampus.domain.member.MBTI;
import com.kr.knucampus.domain.member.Member;
import com.kr.knucampus.global.exception.BusinessException;
import com.kr.knucampus.global.exception.ErrorCode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record SignUpRequest(
        @Email String email,
        String nickname,
        @NotNull String password,
        String mbti,
        Boolean gender
) {
    public Member toEntity(){
        try{
            return new Member(
                    email,
                    nickname,
                    password,
                    MBTI.valueOf(mbti),
                    gender
            );
        }catch(IllegalArgumentException e){
            throw new BusinessException(ErrorCode.WRONG_MBTI,e);
        }
    }
}
