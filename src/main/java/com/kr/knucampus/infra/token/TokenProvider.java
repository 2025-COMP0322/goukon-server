package com.kr.knucampus.infra.token;

import com.kr.knucampus.application.token.TokenService;
import com.kr.knucampus.domain.token.Token;
import com.kr.knucampus.domain.token.TokenType;
import com.kr.knucampus.global.exception.BusinessException;
import com.kr.knucampus.global.utils.DateUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.kr.knucampus.global.exception.ErrorCode.*;

@Component
public class TokenProvider implements TokenService {
    private final Map<TokenType, Token> tokens;

    public TokenProvider(List<Token> tokens) {
        this.tokens = tokens.stream()
                .collect(Collectors.toMap(Token::getType, Function.identity()));
    }

    @Override
    public String getToken(TokenType type, Long id) {
        Token token = tokens.get(type);
        if(token == null){
            throw new BusinessException(INVALID_TOKEN_PROVIDER);
        }
        return Jwts.builder()
                .subject(id.toString())
                .claim("tokenType", type.name())
                .issuedAt(DateUtils.now())
                .expiration(token.getExpiredTime())
                .signWith(token.secretKey())
                .compact();
    }

    @Override
    public Long getId(TokenType type, String token) {
        Claims claims = getClaims(type, token);
        validateToken(claims, type.name());
        return Long.parseLong(claims.getSubject());
    }

    private void validateToken(Claims claims, String expectedType) throws BusinessException {
        String actualType = claims.get("tokenType").toString();
        if(!actualType.equals(expectedType)){
            throw new BusinessException(INVALID_TOKEN);
        }
        if(claims.getExpiration().before(DateUtils.now())){
            throw new BusinessException(TOKEN_EXPIRED);
        }
    }

    private Claims getClaims(TokenType type, String token){
        return Jwts.parser()
                .verifyWith(tokens.get(type).secretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
