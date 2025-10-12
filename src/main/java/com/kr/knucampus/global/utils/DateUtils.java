package com.kr.knucampus.global.utils;

import jakarta.validation.constraints.NotNull;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

@UtilityClass
public class DateUtils {
    public static Date now(){
        ZonedDateTime zoneTime = ZonedDateTime.now(ZoneId.of("Asia/Tokyo"));
        return Date.from(zoneTime.toInstant());
    }

    public static Date convertToDate(@NotNull Long ttl){
        Instant nowUtc = Instant.now().plusMillis(ttl);
        ZonedDateTime zoneTime = nowUtc.atZone(ZoneId.of("Asia/Tokyo"));
        return Date.from(zoneTime.toInstant());
    }
}
