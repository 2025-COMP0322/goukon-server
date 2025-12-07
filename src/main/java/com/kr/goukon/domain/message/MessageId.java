package com.kr.goukon.domain.message;

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
public class MessageId implements Serializable {

    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "message_id")
    private Long messageId;
}
