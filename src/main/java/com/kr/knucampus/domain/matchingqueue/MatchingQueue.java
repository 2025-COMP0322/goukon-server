package com.kr.knucampus.domain.matchingqueue;

import com.kr.knucampus.domain.group.Group;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "matching_queue")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class MatchingQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "queue_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false, unique = true)
    private Group group;

    @Enumerated(EnumType.STRING)
    @Column(name = "matching_status", nullable = false, length = 20)
    private MatchingStatus matchingStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "matching_type", nullable = false, length = 15)
    private MatchingType matchingType;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    private MatchingQueue(Group group, MatchingType matchingType) {
        this.group = group;
        this.matchingStatus = MatchingStatus.WAITING;
        this.matchingType = matchingType;
    }

    public static MatchingQueue create(Group group, MatchingType matchingType) {
        return new MatchingQueue(group, matchingType);
    }

    public void matched() {
        this.matchingStatus = MatchingStatus.MATCHED;
    }

    public void cancel() {
        this.matchingStatus = MatchingStatus.CANCELED;
    }

    public boolean isWaiting() {
        return this.matchingStatus == MatchingStatus.WAITING;
    }
}
