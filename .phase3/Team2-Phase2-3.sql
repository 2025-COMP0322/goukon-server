-- ================================================
-- Query Functions & Views
-- TPC-H 기반 10가지 Query Type (각 Type별 2개씩, 총 20개)
-- ================================================

-- ================================================
-- Type 1: Single-table query (Selection + Projection)
-- 단일 테이블에서 조건에 맞는 특정 컬럼들만 조회
-- ================================================

-- Query 1-1: 남성 학생 중 20세 이상인 학생의 이름, 나이, 학과 조회
SELECT name, age, department
FROM STUDENT
WHERE gender = 'M' AND age >= 20
ORDER BY age DESC;

-- Query 1-2: 매칭 완료 상태인 큐의 정보 조회
SELECT queue_id, matching_type, matching_status, created_at
FROM MATCHING_QUEUE
WHERE matching_status = 'MATCHED';

-- ================================================
-- Type 2: Multi-way join with join predicates in WHERE
-- WHERE 절에 조인 조건을 포함한 다중 테이블 조인
-- ================================================

-- Query 2-1: 각 메시지를 보낸 학생의 이름과 메시지 내용 조회
SELECT s.name, s.department, m.content, m.created_at
FROM MESSAGE m, STUDENT s
WHERE m.student_id = s.student_id;

-- Query 2-2: 매칭 큐에 등록된 그룹과 해당 그룹의 학생 정보 조회
SELECT mq.queue_id, mq.matching_type, g.group_id, s.student_id, s.name, s.department
FROM MATCHING_QUEUE mq, GROUPS g, STUDENT_GROUP sg, STUDENT s
WHERE mq.group_id = g.group_id
  AND g.group_id = sg.group_id
  AND sg.student_id = s.student_id;

-- ================================================
-- Type 3: Aggregation + multi-way join + GROUP BY
-- 집계 함수와 다중 조인, GROUP BY 사용
-- ================================================

-- Query 3-1: 각 학과별 학생 수와 평균 나이
SELECT department, COUNT(*) as student_count, ROUND(AVG(age), 2) as avg_age
FROM STUDENT
GROUP BY department
HAVING COUNT(*) >= 5;

-- Query 3-2: 각 채팅방에서 가장 많이 메시지를 보낸 학생 통계
SELECT m.session_id, s.name, s.department, COUNT(*) as message_count
FROM MESSAGE m, STUDENT s
WHERE m.student_id = s.student_id
GROUP BY m.session_id, s.student_id, s.name, s.department
HAVING COUNT(*) >= 2;

-- ================================================
-- Type 4: Subquery
-- 서브쿼리를 활용한 조회
-- ================================================

-- Query 4-1: 평균 나이보다 어린 학생들의 정보 조회
SELECT student_id, name, age, department
FROM STUDENT
WHERE age < (SELECT AVG(age) FROM STUDENT);

-- Query 4-2: 가장 많은 그룹이 속한 학과의 학생들 조회
SELECT s.student_id, s.name, s.department, s.age
FROM STUDENT s
WHERE s.department = (
    SELECT s2.department
    FROM STUDENT s2, STUDENT_GROUP sg
    WHERE s2.student_id = sg.student_id
    GROUP BY s2.department
    ORDER BY COUNT(DISTINCT sg.group_id) DESC
    FETCH FIRST 1 ROW ONLY
);

-- ================================================
-- Type 5: EXISTS를 포함하는 Subquery
-- EXISTS 연산자를 사용한 서브쿼리
-- ================================================

-- Query 5-1: 메시지를 한 번이라도 보낸 학생들의 정보
SELECT s.student_id, s.name, s.department, s.age
FROM STUDENT s
WHERE EXISTS (
    SELECT 1 FROM MESSAGE m WHERE m.student_id = s.student_id
);

-- Query 5-2: 신고를 한 적이 있는 학생들의 정보
SELECT s.student_id, s.name, s.department, s.age
FROM STUDENT s
WHERE EXISTS (
    SELECT 1 FROM REPORT r WHERE r.student_id = s.student_id
);

-- ================================================
-- Type 6: Selection + Projection + IN predicates
-- IN 연산자를 사용한 조건 검색
-- ================================================

-- Query 6-1: 특정 MBTI 유형(ENFJ, INFJ, INTJ)을 가진 학생 조회
SELECT student_id, name, department, mbti, age
FROM STUDENT
WHERE mbti IN ('ENFJ', 'INFJ', 'INTJ')
ORDER BY mbti, name;

-- Query 6-2: 특정 학과(컴퓨터공학과, 소프트웨어학과, 전자공학과)에 속한 학생 조회
SELECT student_id, name, department, age, mbti
FROM STUDENT
WHERE department IN ('컴퓨터공학과', '소프트웨어학과', '전자공학과')
ORDER BY department, name;

-- ================================================
-- Type 7: In-line view를 활용한 Query
-- FROM 절에 서브쿼리(인라인 뷰)를 사용한 조회
-- ================================================

-- Query 7-1: 각 학과의 평균 나이보다 많은 학생들 조회
SELECT s.student_id, s.name, s.department, s.age, dept_avg.avg_age
FROM STUDENT s,
     (SELECT department, AVG(age) as avg_age
      FROM STUDENT
      GROUP BY department) dept_avg
WHERE s.department = dept_avg.department
  AND s.age > dept_avg.avg_age;

-- Query 7-2: 메시지를 평균 이상으로 보낸 학생들 조회
SELECT s.student_id, s.name, s.department, msg_count.message_count
FROM STUDENT s,
     (SELECT student_id, COUNT(*) as message_count
      FROM MESSAGE
      GROUP BY student_id
      HAVING COUNT(*) >= (SELECT AVG(cnt) FROM (SELECT COUNT(*) as cnt FROM MESSAGE GROUP BY student_id))) msg_count
WHERE s.student_id = msg_count.student_id;

-- ================================================
-- Type 8: Multi-way join with join predicates in WHERE + ORDER BY
-- WHERE 절 조인과 ORDER BY를 함께 사용
-- ================================================

-- Query 8-1: 매칭된 세션의 상세 정보를 시간순으로 조회
SELECT ms.session_id, ms.status as session_status, g.group_id, g.gender,
       mq.matching_type, mq.matching_status, ms.created_at
FROM MATCHING_SESSION ms, SESSION_MATCHES sm, GROUPS g, MATCHING_QUEUE mq
WHERE ms.session_id = sm.session_id
  AND sm.group_id = g.group_id
  AND sm.queue_id = mq.queue_id
  AND mq.matching_status = 'MATCHED'
ORDER BY ms.created_at DESC;

-- Query 8-2: 신고 내역을 신고자 정보와 함께 시간순으로 조회
SELECT r.report_id, r.title, r.content, r.status, r.created_at,
       s.student_id, s.name as reporter_name, s.department as reporter_dept
FROM REPORT r, STUDENT s
WHERE r.student_id = s.student_id
ORDER BY r.created_at DESC;

-- ================================================
-- Type 9: Aggregation + multi-way join + GROUP BY + ORDER BY
-- 집계 함수, 다중 조인, GROUP BY, ORDER BY를 모두 사용
-- ================================================

-- Query 9-1: 각 그룹의 멤버 수와 평균 나이를 멤버 수 순으로 조회
SELECT g.group_id, g.gender, COUNT(sg.student_id) as member_count,
       ROUND(AVG(s.age), 2) as avg_age, g.created_at
FROM GROUPS g, STUDENT_GROUP sg, STUDENT s
WHERE g.group_id = sg.group_id
  AND sg.student_id = s.student_id
GROUP BY g.group_id, g.gender, g.created_at
ORDER BY member_count DESC, g.created_at DESC;

-- Query 9-2: 학과별 그룹 참여 학생 수와 매칭 세션 수를 참여 학생 순으로 조회
SELECT s.department,
       COUNT(DISTINCT sg.student_id) as participating_students,
       COUNT(DISTINCT sm.session_id) as total_sessions,
       COUNT(DISTINCT CASE WHEN mq.matching_status = 'MATCHED' THEN sm.session_id END) as matched_sessions
FROM STUDENT s, STUDENT_GROUP sg, GROUPS g, MATCHING_QUEUE mq, SESSION_MATCHES sm
WHERE s.student_id = sg.student_id
  AND sg.group_id = g.group_id
  AND g.group_id = mq.group_id
  AND mq.queue_id = sm.queue_id
  AND g.group_id = sm.group_id
GROUP BY s.department
ORDER BY participating_students DESC, matched_sessions DESC;

-- ================================================
-- Type 10: SET operation (UNION, SET DIFFERENCE, INTERSECT)
-- 집합 연산자를 활용한 쿼리
-- ================================================

-- Query 10-1: 메시지를 보낸 학생과 신고를 한 학생의 합집합 (UNION)
SELECT DISTINCT s.student_id, s.name, s.department, 'Message Sender' as activity_type
FROM STUDENT s, MESSAGE m
WHERE s.student_id = m.student_id
UNION
SELECT DISTINCT s.student_id, s.name, s.department, 'Reporter' as activity_type
FROM STUDENT s, REPORT r
WHERE s.student_id = r.student_id
ORDER BY student_id;

-- Query 10-2: 메시지를 보낸 학생 중 신고를 한 적이 없는 학생 (MINUS)
SELECT DISTINCT s.student_id, s.name, s.department
FROM STUDENT s, MESSAGE m
WHERE s.student_id = m.student_id
MINUS
SELECT DISTINCT s.student_id, s.name, s.department
FROM STUDENT s, REPORT r
WHERE s.student_id = r.student_id
ORDER BY student_id;

-- ================================================
-- Commit all changes
-- ================================================
COMMIT;
