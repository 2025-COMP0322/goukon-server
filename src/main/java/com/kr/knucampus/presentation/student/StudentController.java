package com.kr.knucampus.presentation.student;

import com.kr.knucampus.application.student.StudentService;
import com.kr.knucampus.domain.student.Student;
import com.kr.knucampus.global.annotation.AuthUser;
import com.kr.knucampus.presentation.student.dto.request.ProfileUpdateRequest;
import com.kr.knucampus.presentation.student.dto.response.StudentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    /**
     * 내 프로필 조회
     */
    @GetMapping("/me")
    public ResponseEntity<StudentResponse> getMyProfile(@AuthUser Long studentId) {
        Student student = studentService.getStudent(studentId);
        return ResponseEntity.ok(StudentResponse.from(student));
    }

    /**
     * 특정 학생 조회
     */
    @GetMapping("/{studentId}")
    public ResponseEntity<StudentResponse> getStudent(@PathVariable Long studentId) {
        Student student = studentService.getStudent(studentId);
        return ResponseEntity.ok(StudentResponse.from(student));
    }

    /**
     * 프로필 수정
     */
    @PatchMapping("/me")
    public ResponseEntity<StudentResponse> updateProfile(
            @AuthUser Long studentId,
            @RequestBody ProfileUpdateRequest request) {
        Student student = studentService.updateProfile(
                studentId,
                request.name(),
                request.mbti(),
                request.profile(),
                request.department()
        );
        return ResponseEntity.ok(StudentResponse.from(student));
    }

    /**
     * 학생 검색 (이름 또는 학번)
     */
    @GetMapping("/search")
    public ResponseEntity<List<StudentResponse>> searchStudents(@RequestParam String keyword) {
        List<Student> students = studentService.searchStudents(keyword);
        List<StudentResponse> responses = students.stream()
                .map(StudentResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * 학과별 학생 조회
     */
    @GetMapping
    public ResponseEntity<List<StudentResponse>> getStudentsByDepartment(
            @RequestParam(required = false) String department) {
        List<Student> students;
        if (department != null && !department.isBlank()) {
            students = studentService.getStudentsByDepartment(department);
        } else {
            students = List.of();
        }
        List<StudentResponse> responses = students.stream()
                .map(StudentResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
