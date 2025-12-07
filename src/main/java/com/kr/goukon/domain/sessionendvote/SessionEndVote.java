package com.kr.goukon.domain.sessionendvote;

import com.kr.goukon.domain.matchingsession.MatchingSession;
import com.kr.goukon.domain.student.Student;
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
@Table(name = "session_end_vote")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class SessionEndVote {

    @EmbeddedId
    private SessionEndVoteId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("sessionId")
    @JoinColumn(name = "session_id")
    private MatchingSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("studentId")
    @JoinColumn(name = "student_id")
    private Student student;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    private SessionEndVote(MatchingSession session, Student student) {
        this.id = new SessionEndVoteId(session.getId(), student.getId());
        this.session = session;
        this.student = student;
    }

    public static SessionEndVote create(MatchingSession session, Student student) {
        return new SessionEndVote(session, student);
    }
}
