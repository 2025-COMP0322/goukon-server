package com.kr.goukon.infra.token;

import com.kr.goukon.domain.token.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenValidator {

    private final TokenProvider tokenProvider;

    public Jws<Claims> validate(TokenType tokenType, String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(tokenProvider.getSecretKey(tokenType))
                .build()
                .parseSignedClaims(token);
    }
}
