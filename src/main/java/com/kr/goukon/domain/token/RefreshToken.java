package com.kr.goukon.domain.token;

import com.kr.goukon.global.utils.DateUtils;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public final class RefreshToken implements Token {
    private final SecretKey secretKey;
    private final Long expiredTime;

    public RefreshToken(
            @Value("${jwt.refresh.secret}") String secretKey,
            @Value("${jwt.refresh.expired}") Long expiredTime
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.expiredTime = expiredTime;
    }

    @Override
    public TokenType getType() {
        return TokenType.REFRESH;
    }

    @Override
    public SecretKey secretKey() {
        return this.secretKey;
    }

    @Override
    public Date getExpiredTime() {
        return DateUtils.convertToDate(this.expiredTime);
    }
}
