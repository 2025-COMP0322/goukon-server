package com.kr.goukon.domain.group.repository;

import com.kr.goukon.domain.group.Group;
import com.kr.goukon.domain.group.GroupStatus;
import com.kr.goukon.domain.student.Gender;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    List<Group> findByGender(Gender gender);

    List<Group> findByStatus(GroupStatus status);

    List<Group> findByGenderAndStatus(Gender gender, GroupStatus status);

    // 비관적 락 - 매칭 시 동시성 제어
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM Group g WHERE g.id = :id")
    Optional<Group> findByIdWithLock(@Param("id") Long id);
}
