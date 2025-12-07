package com.kr.goukon.domain.token;

import javax.crypto.SecretKey;
import java.util.Date;

public sealed interface Token permits AccessToken, RefreshToken {
    TokenType getType();
    SecretKey secretKey();
    Date getExpiredTime();
}
