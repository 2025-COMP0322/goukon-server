package com.university.matching.model;

import java.sql.Timestamp;

/**
 * Message 모델 클래스
 * MESSAGE 테이블을 나타냅니다.
 */
public class Message {
    private Long sessionId;
    private Long studentId;
    private Long messageId;
    private String content;
    private Timestamp createdAt;

    // Constructors
    public Message() {
    }

    public Message(Long sessionId, Long studentId, Long messageId, String content, Timestamp createdAt) {
        this.sessionId = sessionId;
        this.studentId = studentId;
        this.messageId = messageId;
        this.content = content;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Message{" +
                "sessionId=" + sessionId +
                ", studentId=" + studentId +
                ", messageId=" + messageId +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
