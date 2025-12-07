package com.university.matching.model;

import java.sql.Timestamp;

/**
 * MatchingSession 모델 클래스
 * MATCHING_SESSION 테이블을 나타냅니다.
 */
public class MatchingSession {
    private Long sessionId;
    private String status;        // ACTIVE, COMPLETED, CANCELED
    private Timestamp createdAt;

    // Constructors
    public MatchingSession() {
    }

    public MatchingSession(Long sessionId, String status, Timestamp createdAt) {
        this.sessionId = sessionId;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "MatchingSession{" +
                "sessionId=" + sessionId +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
