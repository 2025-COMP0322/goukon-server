package com.kr.knucampus.domain.matchingsession;

import com.kr.knucampus.domain.baseentity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "matching_session")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchingSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status;

    private MatchingSession(SessionStatus status) {
        this.status = status;
    }

    public static MatchingSession create() {
        return new MatchingSession(SessionStatus.ACTIVE);
    }

    public void complete() {
        this.status = SessionStatus.COMPLETED;
    }

    public void cancel() {
        this.status = SessionStatus.CANCELED;
    }

    public boolean isActive() {
        return this.status == SessionStatus.ACTIVE;
    }
}
