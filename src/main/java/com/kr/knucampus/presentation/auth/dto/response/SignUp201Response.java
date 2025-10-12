package com.kr.knucampus.presentation.auth.dto.response;

import com.kr.knucampus.domain.member.Member;

public record SignUp201Response(
        String nickname,
        String mbti,
        String gender
) {
    public static SignUp201Response of(Member member){
        String gender = member.isMale() ? "MALE": "FEMALE";
        return new SignUp201Response(
                member.getNickname(),
                member.getMbti().name(),
                gender
        );
    }
}
