package com.kr.goukon.application.group;

import com.kr.goukon.domain.group.Group;
import com.kr.goukon.domain.group.GroupStatus;
import com.kr.goukon.domain.group.repository.GroupRepository;
import com.kr.goukon.domain.matchingqueue.MatchingType;
import com.kr.goukon.domain.student.Gender;
import com.kr.goukon.domain.student.Student;
import com.kr.goukon.domain.student.repository.StudentRepository;
import com.kr.goukon.domain.studentgroup.StudentGroup;
import com.kr.goukon.domain.studentgroup.StudentGroupId;
import com.kr.goukon.domain.studentgroup.repository.StudentGroupRepository;
import com.kr.goukon.global.exception.BusinessException;
import com.kr.goukon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GroupService {

    private final GroupRepository groupRepository;
    private final StudentRepository studentRepository;
    private final StudentGroupRepository studentGroupRepository;

    /**
     * 그룹 생성
     * 트랜잭션 격리 수준: SERIALIZABLE - 동시 생성 방지
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Group createGroup(Long creatorId) {
        Student creator = studentRepository.findById(creatorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        // 그룹 생성 (생성자의 성별로)
        Group group = Group.builder()
                .gender(creator.getGender())
                .build();
        groupRepository.save(group);

        // 생성자를 그룹에 추가
        StudentGroup studentGroup = StudentGroup.create(creator, group);
        studentGroupRepository.save(studentGroup);

        log.info("Group {} created by student {}", group.getId(), creatorId);
        return group;
    }

    /**
     * 그룹 조회
     */
    public Group getGroup(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));
    }

    /**
     * 그룹 멤버 목록 조회
     */
    public List<Student> getGroupMembers(Long groupId) {
        List<StudentGroup> studentGroups = studentGroupRepository.findByGroupId(groupId);
        return studentGroups.stream()
                .map(StudentGroup::getStudent)
                .collect(Collectors.toList());
    }

    /**
     * 학생이 속한 그룹 목록 조회
     */
    public List<Group> getStudentGroups(Long studentId) {
        List<StudentGroup> studentGroups = studentGroupRepository.findByStudentId(studentId);
        return studentGroups.stream()
                .map(StudentGroup::getGroup)
                .collect(Collectors.toList());
    }

    /**
     * 그룹에 멤버 추가
     * 트랜잭션 격리 수준: SERIALIZABLE - 동시 추가 방지, 인원 제한 보장
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void addMember(Long groupId, Long studentId, Long requesterId) {
        // 요청자가 그룹의 멤버인지 확인 (그룹 멤버만 초대 가능)
        if (!studentGroupRepository.existsByStudentIdAndGroupId(requesterId, groupId)) {
            throw new BusinessException(ErrorCode.NOT_GROUP_MEMBER);
        }

        // 그룹 조회 (비관적 락)
        Group group = groupRepository.findByIdWithLock(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

        // 그룹 상태 확인
        if (!group.isAvailable()) {
            throw new BusinessException(ErrorCode.GROUP_NOT_AVAILABLE);
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        // 성별 확인
        if (student.getGender() != group.getGender()) {
            throw new BusinessException(ErrorCode.GENDER_MISMATCH);
        }

        // 이미 그룹에 있는지 확인
        if (studentGroupRepository.existsByStudentIdAndGroupId(studentId, groupId)) {
            throw new BusinessException(ErrorCode.ALREADY_IN_GROUP);
        }

        // 그룹 인원 확인 (최대 3명)
        long currentCount = studentGroupRepository.countByGroupId(groupId);
        if (currentCount >= 3) {
            throw new BusinessException(ErrorCode.GROUP_FULL);
        }

        // 멤버 추가
        StudentGroup studentGroup = StudentGroup.create(student, group);
        studentGroupRepository.save(studentGroup);

        log.info("Student {} added to group {} by requester {}", studentId, groupId, requesterId);
    }

    /**
     * 그룹에서 멤버 제거
     * 트랜잭션 격리 수준: REPEATABLE_READ
     * requesterId: 요청자 (자기 자신을 제거하거나, 그룹 멤버가 다른 멤버를 제거)
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void removeMember(Long groupId, Long studentId, Long requesterId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

        // 매칭 중인 경우 제거 불가
        if (group.isQueuing() || group.getStatus() == GroupStatus.MATCHED) {
            throw new BusinessException(ErrorCode.CANNOT_LEAVE_WHILE_MATCHING);
        }

        // 요청자가 그룹 멤버인지 확인 (본인이거나 그룹 멤버여야 함)
        boolean requesterIsMember = studentGroupRepository.existsByStudentIdAndGroupId(requesterId, groupId);
        boolean isSelf = requesterId.equals(studentId);

        if (!isSelf && !requesterIsMember) {
            throw new BusinessException(ErrorCode.NOT_GROUP_MEMBER);
        }

        // 대상이 그룹에 속해있는지 확인
        if (!studentGroupRepository.existsByStudentIdAndGroupId(studentId, groupId)) {
            throw new BusinessException(ErrorCode.NOT_GROUP_MEMBER);
        }

        studentGroupRepository.deleteByStudentIdAndGroupId(studentId, groupId);

        log.info("Student {} removed from group {} by requester {}", studentId, groupId, requesterId);

        // 그룹에 아무도 없으면 그룹 삭제
        long remainingCount = studentGroupRepository.countByGroupId(groupId);
        if (remainingCount == 0) {
            groupRepository.delete(group);
            log.info("Group {} deleted because it's empty", groupId);
        }
    }

    /**
     * 그룹에서 멤버 제거 (본인)
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void removeMember(Long groupId, Long studentId) {
        removeMember(groupId, studentId, studentId);
    }

    /**
     * 그룹 삭제
     * 트랜잭션 격리 수준: SERIALIZABLE
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deleteGroup(Long groupId, Long requesterId) {
        Group group = groupRepository.findByIdWithLock(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

        // 매칭 중인 경우 삭제 불가
        if (group.isQueuing() || group.getStatus() == GroupStatus.MATCHED) {
            throw new BusinessException(ErrorCode.CANNOT_LEAVE_WHILE_MATCHING);
        }

        // 요청자가 그룹에 속해있는지 확인
        if (!studentGroupRepository.existsByStudentIdAndGroupId(requesterId, groupId)) {
            throw new BusinessException(ErrorCode.NOT_GROUP_MEMBER);
        }

        // 그룹 멤버 관계 삭제 (CASCADE로 자동 삭제되지만 명시적으로)
        List<StudentGroup> members = studentGroupRepository.findByGroupId(groupId);
        studentGroupRepository.deleteAll(members);

        // 그룹 삭제
        groupRepository.delete(group);

        log.info("Group {} deleted by student {}", groupId, requesterId);
    }

    /**
     * 그룹 멤버 수 확인
     */
    public long getMemberCount(Long groupId) {
        return studentGroupRepository.countByGroupId(groupId);
    }

    /**
     * 학생이 그룹의 멤버인지 확인
     */
    public boolean isMember(Long groupId, Long studentId) {
        return studentGroupRepository.existsByStudentIdAndGroupId(studentId, groupId);
    }
}
