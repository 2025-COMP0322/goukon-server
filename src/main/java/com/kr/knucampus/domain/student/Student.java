package com.kr.knucampus.domain.student;

import com.kr.knucampus.domain.baseentity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Table(name = "student")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Student extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Long id;

    @Column(name = "student_number", length = 20, nullable = false, unique = true)
    private String studentNumber;

    @Column(nullable = false)
    private int age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    private Gender gender;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 4)
    private Mbti mbti;

    @Lob
    private String profile;

    @Column(nullable = false, length = 100)
    private String department;

    // 인증용 필드 (Member에서 통합)
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role = Role.USER;

    @Builder
    public Student(String studentNumber, int age, Gender gender, String name, Mbti mbti,
                   String profile, String department, String email, String password, Role role) {
        this.studentNumber = studentNumber;
        this.age = age;
        this.gender = gender;
        this.name = name;
        this.mbti = mbti;
        this.profile = profile;
        this.department = department;
        this.email = email;
        this.password = password;
        this.role = role != null ? role : Role.USER;
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    // 비밀번호 암호화
    public void encryptPassword(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(this.password);
    }

    // 비밀번호 검증
    public boolean matchPassword(String password, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(password, this.password);
    }

    // 비밀번호 변경
    public void modifyPassword(String password, PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(password);
    }

    // 프로필 정보 업데이트
    public void updateProfile(String name, Mbti mbti, String profile, String department) {
        if (name != null) this.name = name;
        if (mbti != null) this.mbti = mbti;
        if (profile != null) this.profile = profile;
        if (department != null) this.department = department;
    }
}
