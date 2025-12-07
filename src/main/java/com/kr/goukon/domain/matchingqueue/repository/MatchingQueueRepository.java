package com.kr.goukon.domain.matchingqueue.repository;

import com.kr.goukon.domain.group.Group;
import com.kr.goukon.domain.matchingqueue.MatchingQueue;
import com.kr.goukon.domain.matchingqueue.MatchingStatus;
import com.kr.goukon.domain.matchingqueue.MatchingType;
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
public interface MatchingQueueRepository extends JpaRepository<MatchingQueue, Long> {

    Optional<MatchingQueue> findByGroup(Group group);

    Optional<MatchingQueue> findByGroupId(Long groupId);

    boolean existsByGroupId(Long groupId);

    List<MatchingQueue> findByMatchingStatus(MatchingStatus status);

    List<MatchingQueue> findByMatchingStatusAndMatchingType(MatchingStatus status, MatchingType type);

    // 대기중인 큐에서 특정 성별, 매칭 타입으로 조회 (매칭용)
    @Query("SELECT mq FROM MatchingQueue mq JOIN FETCH mq.group g " +
           "WHERE mq.matchingStatus = :status AND mq.matchingType = :type AND g.gender = :gender " +
           "ORDER BY mq.createdAt ASC")
    List<MatchingQueue> findWaitingQueuesByGenderAndType(
            @Param("status") MatchingStatus status,
            @Param("gender") Gender gender,
            @Param("type") MatchingType type);

    // 비관적 락 - 매칭 시 동시성 제어
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT mq FROM MatchingQueue mq WHERE mq.id = :id")
    Optional<MatchingQueue> findByIdWithLock(@Param("id") Long id);

    // 비관적 락으로 대기중인 큐 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT mq FROM MatchingQueue mq JOIN FETCH mq.group g " +
           "WHERE mq.matchingStatus = 'WAITING' AND mq.matchingType = :type AND g.gender = :gender " +
           "ORDER BY mq.createdAt ASC")
    List<MatchingQueue> findWaitingQueuesWithLock(
            @Param("gender") Gender gender,
            @Param("type") MatchingType type);
}
