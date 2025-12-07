package com.university.matching.model;

import java.sql.Timestamp;

/**
 * Group 모델 클래스
 * GROUPS 테이블을 나타냅니다.
 */
public class Group {
    private Long groupId;
    private String gender;        // M, F
    private String status;        // AVAILABLE, QUEUING, MATCHED
    private Timestamp createdAt;

    // Constructors
    public Group() {
    }

    public Group(Long groupId, String gender, String status, Timestamp createdAt) {
        this.groupId = groupId;
        this.gender = gender;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
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
        return "Group{" +
                "groupId=" + groupId +
                ", gender='" + gender + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
