package com.kr.goukon.domain.studentgroup;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class StudentGroupId implements Serializable {

    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "group_id")
    private Long groupId;
}
