package com.kr.goukon.application.matching.dto;

import com.kr.goukon.domain.matchingqueue.MatchingType;
import com.kr.goukon.domain.student.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MatchingQueueMessage implements Serializable {
    private Long queueId;
    private Long groupId;
    private Gender gender;
    private MatchingType matchingType;
}
