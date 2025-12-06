package com.kr.knucampus.domain.message;

import com.kr.knucampus.domain.baseentity.BaseEntity;
import com.kr.knucampus.domain.chatroom.ChatRoom;
import com.kr.knucampus.domain.student.Student;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message extends BaseEntity {

    @Id
    @Column(name = "message_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student sender;

    @Column(nullable = false, columnDefinition = "CLOB")
    private String content;

    @Builder
    public Message(ChatRoom chatRoom, Student sender, String content, LocalDateTime createdAt) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.content = content;
    }
}
