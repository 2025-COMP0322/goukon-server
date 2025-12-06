package com.kr.knucampus.domain.sessionmatches;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class SessionMatchesId implements Serializable {

    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "group_id")
    private Long groupId;
}
