package com.kr.knucampus.domain.chatroom;

import com.kr.knucampus.domain.baseentity.BaseEntity;
import com.kr.knucampus.domain.matchingsession.MatchingSession;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", unique = true)
    private MatchingSession session;

    private ChatRoom(MatchingSession session) {
        this.session = session;
    }

    public static ChatRoom create(MatchingSession session) {
        return new ChatRoom(session);
    }
}

