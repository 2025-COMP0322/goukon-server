package com.kr.knucampus.domain.group;

import com.kr.knucampus.domain.baseentity.BaseEntity;
import com.kr.knucampus.domain.student.Gender;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "groups")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Group extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupStatus status;

    @Builder
    public Group(Gender gender) {
        this.gender = gender;
        this.status = GroupStatus.AVAILABLE;
    }

    // 상태 변경 메서드
    public void changeStatus(GroupStatus status) {
        this.status = status;
    }

    public void startQueuing() {
        this.status = GroupStatus.QUEUING;
    }

    public void matched() {
        this.status = GroupStatus.MATCHED;
    }

    public void resetToAvailable() {
        this.status = GroupStatus.AVAILABLE;
    }

    public boolean isAvailable() {
        return this.status == GroupStatus.AVAILABLE;
    }

    public boolean isQueuing() {
        return this.status == GroupStatus.QUEUING;
    }
}
