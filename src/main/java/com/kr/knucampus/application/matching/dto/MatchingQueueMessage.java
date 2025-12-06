package com.kr.knucampus.application.matching.dto;

import com.kr.knucampus.domain.matchingqueue.MatchingType;
import com.kr.knucampus.domain.student.Gender;
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
