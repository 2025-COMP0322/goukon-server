package com.kr.knucampus.domain.message;

import com.kr.knucampus.domain.chatroom.ChatRoom;
import com.kr.knucampus.domain.student.Student;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
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
@Table(name = "message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Message {

    @EmbeddedId
    private MessageId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("sessionId")
    @JoinColumn(name = "session_id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("studentId")
    @JoinColumn(name = "student_id")
    private Student sender;

    @Lob
    @Column(nullable = false)
    private String content;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    private Message(ChatRoom chatRoom, Student sender, Long messageId, String content) {
        this.id = new MessageId(chatRoom.getId(), sender.getId(), messageId);
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.content = content;
    }

    public static Message create(ChatRoom chatRoom, Student sender, Long messageId, String content) {
        return new Message(chatRoom, sender, messageId, content);
    }
}
