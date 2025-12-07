package com.university.matching.model;

import java.sql.Timestamp;

/**
 * 학생 정보를 나타내는 도메인 모델
 */
public class Student {
    private Long studentId;
    private String studentNumber;
    private Integer age;
    private String gender;
    private String name;
    private String mbti;
    private String profile;
    private String department;
    private Timestamp createdAt;

    // 기본 생성자
    public Student() {
    }

    // 전체 필드 생성자
    public Student(Long studentId, String studentNumber, Integer age, String gender,
                   String name, String mbti, String profile, String department, Timestamp createdAt) {
        this.studentId = studentId;
        this.studentNumber = studentNumber;
        this.age = age;
        this.gender = gender;
        this.name = name;
        this.mbti = mbti;
        this.profile = profile;
        this.department = department;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMbti() {
        return mbti;
    }

    public void setMbti(String mbti) {
        this.mbti = mbti;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentId=" + studentId +
                ", studentNumber='" + studentNumber + '\'' +
                ", age=" + age +
                ", gender='" + gender + '\'' +
                ", name='" + name + '\'' +
                ", mbti='" + mbti + '\'' +
                ", department='" + department + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
