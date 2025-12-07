package com.kr.goukon.domain.sessionmatches;

import com.kr.goukon.domain.group.Group;
import com.kr.goukon.domain.matchingqueue.MatchingQueue;
import com.kr.goukon.domain.matchingsession.MatchingSession;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "session_matches")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class SessionMatches {

    @EmbeddedId
    private SessionMatchesId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("sessionId")
    @JoinColumn(name = "session_id")
    private MatchingSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId")
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "queue_id", nullable = false)
    private MatchingQueue queue;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    private SessionMatches(MatchingSession session, Group group, MatchingQueue queue) {
        this.id = new SessionMatchesId(session.getId(), group.getId());
        this.session = session;
        this.group = group;
        this.queue = queue;
    }

    public static SessionMatches create(MatchingSession session, Group group, MatchingQueue queue) {
        return new SessionMatches(session, group, queue);
    }
}
