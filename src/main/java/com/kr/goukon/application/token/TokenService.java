package com.kr.goukon.application.token;

import com.kr.goukon.domain.token.TokenType;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public interface TokenService {
    String getToken(TokenType type, Long id);
    Long getId(TokenType type, String token);
    SecretKey getSecretKey(TokenType type);
}
