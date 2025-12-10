package com.kr.goukon.global.dto;

import com.kr.goukon.domain.student.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

public record AuthInfo(
        Long id,
        Role role
) {
    private final static String tokenRoleKey = "role";

    public static AuthInfo from(Jws<Claims> jws) {
        return new AuthInfo(
                Long.parseLong(jws.getPayload().getSubject()),
                Role.convert(jws.getPayload().get(tokenRoleKey).toString())
        );
    }
}
