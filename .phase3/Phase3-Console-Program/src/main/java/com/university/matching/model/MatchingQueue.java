package com.university.matching.model;

import java.sql.Timestamp;

/**
 * MatchingQueue 모델 클래스
 * MATCHING_QUEUE 테이블을 나타냅니다.
 */
public class MatchingQueue {
    private Long queueId;
    private Long groupId;
    private String matchingStatus;    // WAITING, MATCHED, CANCELED
    private String matchingType;      // ONE_TO_ONE, THREE_TO_THREE
    private Timestamp createdAt;

    // Constructors
    public MatchingQueue() {
    }

    public MatchingQueue(Long queueId, Long groupId, String matchingStatus, String matchingType, Timestamp createdAt) {
        this.queueId = queueId;
        this.groupId = groupId;
        this.matchingStatus = matchingStatus;
        this.matchingType = matchingType;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getQueueId() {
        return queueId;
    }

    public void setQueueId(Long queueId) {
        this.queueId = queueId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getMatchingStatus() {
        return matchingStatus;
    }

    public void setMatchingStatus(String matchingStatus) {
        this.matchingStatus = matchingStatus;
    }

    public String getMatchingType() {
        return matchingType;
    }

    public void setMatchingType(String matchingType) {
        this.matchingType = matchingType;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "MatchingQueue{" +
                "queueId=" + queueId +
                ", groupId=" + groupId +
                ", matchingStatus='" + matchingStatus + '\'' +
                ", matchingType='" + matchingType + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
