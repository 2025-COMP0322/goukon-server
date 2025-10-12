package com.kr.knucampus.application.token;

import com.kr.knucampus.domain.token.TokenType;
import org.springframework.stereotype.Service;

@Service
public interface TokenService {
    String getToken(TokenType type, Long id);
    Long getId(TokenType type, String token);
}
