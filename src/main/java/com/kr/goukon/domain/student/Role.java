package com.kr.goukon.domain.student;

import com.kr.goukon.global.exception.BusinessException;
import com.kr.goukon.global.exception.ErrorCode;

public enum Role {
    USER,   // 일반 사용자
    ADMIN   // 관리자
    ;

    public static Role convert(String role) {
        if(USER.name().equalsIgnoreCase(role)) {
            return USER;
        }else if(ADMIN.name().equalsIgnoreCase(role)) {
            return ADMIN;
        }
        throw new BusinessException(ErrorCode.CANNOT_CONVERT_ROLE);
    }

    public static boolean verify(Role role, Role standardRole) {
        if(role.equals(Role.ADMIN)){
            return true;
        }
        return role.equals(Role.USER) && role.equals(standardRole);
    }
}
