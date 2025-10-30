package com.kr.knucampus.domain.student;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String studentNumber;
    private int age;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String name;

    @Enumerated(EnumType.STRING)
    private Mbti mbti;

    @Lob
    private String profile;

    private String department;

    @Builder
    public Student(String studentNumber, int age, Gender gender, String name, Mbti mbti, String profile,
                   String department) {
        this.studentNumber = studentNumber;
        this.age = age;
        this.gender = gender;
        this.name = name;
        this.mbti = mbti;
        this.profile = profile;
        this.department = department;
    }
}
