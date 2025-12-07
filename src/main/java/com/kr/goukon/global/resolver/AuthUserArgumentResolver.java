package com.kr.goukon.global.resolver;

import com.kr.goukon.application.token.TokenService;
import com.kr.goukon.domain.token.TokenType;
import com.kr.goukon.global.annotation.AuthUser;
import com.kr.goukon.global.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static com.kr.goukon.global.exception.ErrorCode.*;

@Component
@RequiredArgsConstructor
public class AuthUserArgumentResolver implements HandlerMethodArgumentResolver {
    private final TokenService tokenService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest httpServletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        if(httpServletRequest == null){
            return null;
        }
        try{
            String authorizationHeader = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
            String token = extractToken(authorizationHeader);
            return tokenService.getId(TokenType.ACCESS, token);
        }catch(NullPointerException e){
            throw new BusinessException(NO_TOKEN);
        }
    }

    private String extractToken(String authorizationHeader) {
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        throw new BusinessException(INVALID_HEADER);
    }
}
