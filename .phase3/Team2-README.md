# Phase 3 - 과팅 매칭 시스템 콘솔 애플리케이션

## 📋 프로젝트 개요

대학교 과팅 매칭 시스템을 위한 **JDBC 기반 콘솔 애플리케이션**입니다. Oracle Database와 연동하여 학생, 그룹, 메시지, 신고 등의 데이터를 관리하고, 다양한 통계 및 분석 기능을 제공합니다.

---

## 🚀 실행 방법

### 1️⃣ 사전 요구사항

- **Java 8 이상** 설치 필수 (권장: Java 17)
- **Oracle Database** 실행 중
- **데이터베이스 계정** 및 권한

### 2️⃣ 데이터베이스 설정

`src/main/resources/application.yml` 파일을 편집하여 데이터베이스 연결 정보를 입력합니다:

```yaml
db:
  url: jdbc:oracle:thin:@localhost:1521/XE
  username: YOUR_USERNAME
  password: YOUR_PASSWORD
  driver: oracle.jdbc.OracleDriver
```

### 3️⃣ 애플리케이션 빌드

```bash
./gradlew build
```

### 4️⃣ 애플리케이션 실행

```bash
./gradlew run --console=plain 
# 또는 ./gradlew run
```

또는 배포판을 생성한 후 실행:

```bash
./gradlew installDist
./build/install/Phase3-Console-Program/bin/Phase3-Console-Program
```

### 5️⃣ 초기 실행 시

애플리케이션은 자동으로 다음 작업을 수행합니다:

1. **데이터베이스 연결 테스트** - Oracle DB 연결 확인
2. **Flyway 마이그레이션** - 스키마 및 샘플 데이터 자동 생성
   - `V1__create_schema.sql`: 테이블, 시퀀스, 인덱스 생성
   - `V2__insert_sample_data.sql`: 샘플 데이터 삽입
   - `V3__create_queries.sql`: 추가 쿼리 정의
3. **콘솔 UI 시작** - 대화형 메뉴 표시

---

## 💡 주요 기능

### 📌 도메인별 주요 기능

이 시스템은 4개 도메인(Student, Group, Message, Report)을 중심으로 구성되며, 각 도메인별로 데이터 관리 및 분석 기능을 제공합니다.

---

#### 1. **학생 (Student) 관련 기능**

학생 테이블에 새로운 학생 데이터를 삽입하거나 기존 학생의 정보를 수정, 삭제하거나 다양한 조건으로 학생 정보를 검색할 수 있습니다.

##### 기본 CRUD 기능
- **학생 등록**: 학번, 이름, 나이, 성별, MBTI, 학과, 프로필을 입력하여 새로운 학생 추가
  - 학번: 정확히 10자리 숫자 (예: 2024123456)
  - 나이: 18세 이상 30세 이하
  - 성별: M (남성) 또는 F (여성)
  - MBTI: 4자리 형식 [E/I][N/S][T/F][J/P] (예: ENFP, ISTJ) 또는 비어있음
  - 학과: 필수 입력 (예: 컴퓨터공학과, 경영학과)

- **학생 정보 조회**: 전체 학생 목록 조회 또는 ID로 특정 학생 검색

- **학생 정보 수정**: 기존 학생의 나이, 학과 정보 수정 가능
  - 단, 학번(studentNumber)과 성별(gender)은 수정 불가능

- **학생 삭제**: ID로 학생 삭제 (CASCADE 설정으로 관련 데이터 자동 삭제)
  - 삭제 시 해당 학생의 그룹 멤버십, 메시지, 신고 내역도 함께 삭제됨

##### 고급 검색 및 분석 기능 (11개 동적 쿼리)
1. **성별 및 나이로 검색**: 특정 성별과 최소 나이 조건으로 학생 필터링
2. **학과별 통계**: 학과별 학생 수와 평균 나이 (5명 이상 학과만)
3. **평균 나이 미만 학생**: 전체 학생 평균 나이보다 어린 학생들 조회
4. **최다 그룹 학과 학생**: 가장 많은 그룹이 속한 학과의 학생들 조회
5. **활동 학생 조회**: 메시지를 보낸 적이 있는 학생들 (EXISTS 서브쿼리)
6. **신고 학생 조회**: 신고를 한 적이 있는 학생들 (EXISTS 서브쿼리)
7. **MBTI로 검색**: 여러 MBTI 유형으로 학생 검색 (동적 IN 절)
8. **학과로 검색**: 여러 학과로 학생 검색 (동적 IN 절)
9. **학과 평균 이상**: 자신의 학과 평균보다 나이가 많은 학생들
10. **활동 또는 신고 학생**: 메시지 발신자와 신고자의 합집합 (UNION)
11. **미신고 발신자**: 메시지를 보냈지만 신고는 하지 않은 학생들 (MINUS)

##### 제약사항
- 학번은 UNIQUE 제약조건으로 중복 불가
- 외래키 CASCADE DELETE로 학생 삭제 시 관련 데이터 자동 삭제
- 나이, 성별, MBTI는 CHECK 제약조건으로 유효성 검증

---

#### 2. **그룹 (Group) 관련 기능**

그룹 테이블에서 과팅 그룹을 생성, 수정, 삭제하고, 학생을 그룹에 추가/제거하며, 그룹의 매칭 상태를 관리할 수 있습니다.

##### 기본 CRUD 기능
- **그룹 생성**: 성별과 상태를 지정하여 새로운 그룹 생성
  - 성별: M (남성 그룹) 또는 F (여성 그룹)
  - 상태: AVAILABLE (매칭 가능), QUEUING (매칭 대기 중), MATCHED (매칭 완료)

- **그룹 조회**: 전체 그룹 목록 조회 또는 ID로 특정 그룹 검색

- **그룹 상태 수정**: 그룹의 매칭 상태 업데이트 (AVAILABLE ↔ QUEUING ↔ MATCHED)

- **그룹 삭제**: ID로 그룹 삭제 (CASCADE로 관련 멤버십, 매칭 큐 자동 삭제)

##### 그룹 멤버 관리
- **학생 추가**: 특정 그룹에 학생 추가 (STUDENT_GROUP 테이블에 관계 생성)
- **학생 제거**: 그룹에서 학생 제거 (STUDENT_GROUP 테이블에서 관계 삭제)

##### 고급 검색 및 분석 기능 (5개 동적 쿼리)
1. **매칭 상태별 큐 조회**: 특정 매칭 상태(WAITING, MATCHED, CANCELED)의 큐 목록
2. **큐의 그룹 멤버 조회**: 매칭 큐에 속한 그룹의 멤버 상세 정보 (4-way JOIN)
3. **매칭 세션 상세**: 매칭된 세션의 그룹 및 큐 정보 (4-way JOIN)
4. **그룹 통계**: 각 그룹의 멤버 수와 평균 나이
5. **학과별 그룹 참여**: 학과별 참여 그룹 수와 고유 학생 수 (5-way JOIN)

##### 제약사항
- 그룹 상태는 CHECK 제약조건으로 AVAILABLE, QUEUING, MATCHED만 허용
- 그룹당 하나의 매칭 큐만 가능 (UNIQUE 제약)
- 외래키 CASCADE로 그룹 삭제 시 관련 멤버십, 매칭 큐 자동 삭제

---

#### 3. **메시지 (Message) 관련 기능**

매칭 세션에서 학생들이 주고받은 메시지를 관리하고, 메시지 통계 및 발신자 정보를 분석할 수 있습니다.

##### 기본 CRUD 기능
- **메시지 전송**: 매칭 세션 ID, 발신자 학생 ID, 메시지 내용을 입력하여 메시지 추가
  - sessionId: 필수 (매칭 세션 ID)
  - studentId: 필수 (발신자 학생 ID)
  - content: 필수 (메시지 내용, 비어있을 수 없음)
  - messageId: 자동 생성 (시퀀스)

- **메시지 조회**: 전체 메시지 목록 조회 (생성일 역순)

- **메시지 삭제**: sessionId, studentId, messageId를 모두 입력하여 특정 메시지 삭제
  - 복합 키(Composite Key) 구조로 3개 ID 모두 필요

##### 고급 검색 및 분석 기능 (3개 동적 쿼리)
1. **발신자 정보 포함 조회**: 메시지와 발신자의 이름, 학과 정보 함께 조회 (2-way JOIN)
   - 선택적 파라미터: sessionId (특정 세션만 조회하거나 전체 조회)
2. **세션별 메시지 통계**: 각 세션의 총 메시지 수, 고유 발신자 수 (GROUP BY + HAVING)
   - 최소 메시지 수 이상인 세션만 필터링
3. **활발한 학생 조회**: 평균 이상의 메시지를 보낸 학생들 (Inline View)

##### 제약사항
- sessionId와 studentId는 외래키로 각각 MATCHING_SESSION, STUDENT 테이블 참조
- 복합 기본 키(sessionId, studentId, messageId)로 메시지 고유성 보장
- 외래키 CASCADE로 학생 또는 세션 삭제 시 메시지 자동 삭제

---

#### 4. **신고 (Report) 관련 기능**

학생이 작성한 신고 내역을 관리하고, 신고 상태를 추적하며, 신고자 정보와 함께 조회할 수 있습니다.

##### 기본 CRUD 기능
- **신고 생성**: 신고자 ID, 제목, 내용, 상태를 입력하여 새로운 신고 추가
  - studentId: 필수 (신고자 학생 ID)
  - title: 필수 (신고 제목)
  - content: 필수 (신고 상세 내용)
  - status: PENDING (대기), REVIEWING (검토 중), RESOLVED (해결), REJECTED (거부)
  - 기본 상태: PENDING

- **신고 조회**: 전체 신고 목록 조회 또는 ID로 특정 신고 검색

- **신고 상태 수정**: 신고 ID로 검색하여 상태만 업데이트
  - 관리 워크플로우: PENDING → REVIEWING → RESOLVED/REJECTED

- **신고 전체 수정**: 제목, 내용, 상태 모두 수정 가능

- **신고 삭제**: ID로 신고 삭제 (확인 프롬프트 표시)

##### 고급 검색 및 분석 기능 (1개 동적 쿼리)
1. **신고자 정보 포함 조회**: 신고 내역과 신고자의 이름, 학과 정보 함께 조회 (2-way JOIN)
   - 생성일 역순 정렬

##### 제약사항
- 신고 상태는 CHECK 제약조건으로 4가지 값만 허용
- studentId는 외래키로 STUDENT 테이블 참조
- 외래키 CASCADE로 학생 삭제 시 해당 학생의 신고 내역 자동 삭제

---

### 📌 구현된 20개 쿼리 (Team2-Phase2-3.sql 기반)

모든 쿼리는 **Team2-Phase2-3.sql**의 쿼리를 기반으로 구현되었으며, 4개 DAO에 분산 배치되어 있습니다.

#### 1. **StudentDAO - 11개 쿼리**

1. **Query 1-1: 성별 및 나이로 검색**
   - 동적 파라미터: 성별(M/F), 최소 나이

2. **Query 3-1: 학과별 학생 수와 평균 나이**
   - 동적 파라미터: 최소 학생 수 (HAVING COUNT(*) >= 5)

3. **Query 4-1: 평균 나이보다 어린 학생 조회**
   - Subquery 사용

4. **Query 4-2: 가장 많은 그룹이 속한 학과의 학생 조회**
   - Subquery 사용

5. **Query 5-1: 메시지를 보낸 학생 조회**
   - EXISTS 서브쿼리 사용

6. **Query 5-2: 신고를 한 학생 조회**
   - EXISTS 서브쿼리 사용

7. **Query 6-1: 특정 MBTI 유형 학생 조회**
   - 동적 IN 절: MBTI 리스트

8. **Query 6-2: 특정 학과 학생 조회**
   - 동적 IN 절: 학과 리스트

9. **Query 7-1: 학과 평균보다 나이 많은 학생 조회**
   - Inline View 사용

10. **Query 10-1: 메시지 발신자와 신고자 합집합**
    - UNION 연산 사용

11. **Query 10-2: 메시지 발신자 중 신고 안 한 학생**
    - MINUS 연산 사용

#### 2. **GroupDAO - 5개 쿼리**

1. **Query 1-2: 매칭 상태별 큐 조회**
   - 동적 파라미터: 매칭 상태

2. **Query 2-2: 매칭 큐의 그룹 및 학생 정보 조회**
   - 4-way JOIN 사용

3. **Query 8-1: 매칭된 세션 상세 정보 조회**
   - Multi-way JOIN + ORDER BY

4. **Query 9-1: 그룹별 멤버 수와 평균 나이**
   - GROUP BY + ORDER BY

5. **Query 9-2: 학과별 그룹 참여 통계**
   - GROUP BY + ORDER BY

#### 3. **MessageDAO - 3개 쿼리**

1. **Query 2-1: 메시지와 발신자 정보 조회**
   - 동적 파라미터: 세션 ID (Optional)
   - JOIN 사용

2. **Query 3-2: 세션별 메시지 통계**
   - 동적 파라미터: 최소 메시지 수
   - GROUP BY + HAVING

3. **Query 7-2: 평균 이상 메시지를 보낸 학생 조회**
   - Inline View 사용

#### 4. **ReportDAO - 1개 쿼리**

1. **Query 8-2: 신고 내역과 신고자 정보 조회**
   - JOIN + ORDER BY

### 🔍 쿼리 타입 요약 (총 20개)

| 타입 | 설명 | 개수 | Phase2 참조 쿼리 | 구현 위치 |
|------|------|------|------------------|----------|
| **Type 1** | 단일 테이블 WHERE 절 | 2개 | Query 1-1, 1-2 | StudentDAO (1), GroupDAO (1) |
| **Type 2** | 다중 테이블 JOIN | 2개 | Query 2-1, 2-2 | GroupDAO (1), MessageDAO (1) |
| **Type 3** | GROUP BY + 집계함수 + HAVING | 2개 | Query 3-1, 3-2 | StudentDAO (1), MessageDAO (1) |
| **Type 4** | Subquery | 2개 | Query 4-1, 4-2 | StudentDAO (2) |
| **Type 5** | EXISTS 서브쿼리 | 2개 | Query 5-1, 5-2 | StudentDAO (2) |
| **Type 6** | IN 절 (동적 생성) | 2개 | Query 6-1, 6-2 | StudentDAO (2) |
| **Type 7** | Inline View | 2개 | Query 7-1, 7-2 | StudentDAO (1), MessageDAO (1) |
| **Type 8** | JOIN + ORDER BY | 2개 | Query 8-1, 8-2 | GroupDAO (1), ReportDAO (1) |
| **Type 9** | GROUP BY + ORDER BY | 2개 | Query 9-1, 9-2 | GroupDAO (2) |
| **Type 10** | SET 연산 (UNION, MINUS) | 2개 | Query 10-1, 10-2 | StudentDAO (2) |

**DAO별 쿼리 분포:**
- **StudentDAO**: 11개 (Query 1-1, 3-1, 4-1, 4-2, 5-1, 5-2, 6-1, 6-2, 7-1, 10-1, 10-2)
- **GroupDAO**: 5개 (Query 1-2, 2-2, 8-1, 9-1, 9-2)
- **MessageDAO**: 3개 (Query 2-1, 3-2, 7-2)
- **ReportDAO**: 1개 (Query 8-2)

### 📊 콘솔 메뉴 구조

애플리케이션 실행 시 표시되는 대화형 메뉴 구조는 다음과 같습니다:

#### 메인 메뉴
```
========================================
     University Matching System
========================================
1. Student Management
2. Group Management
3. Message Management
4. Report Management
5. 📖 View Dynamic Queries Guide
0. Exit
========================================
```

#### 1. Student Management (17개 옵션)
- CRUD: 전체 조회, ID 검색, 추가, 수정, 삭제
- 동적 쿼리 11개: Query 1-1, 3-1, 4-1, 4-2, 5-1, 5-2, 6-1, 6-2, 7-1, 10-1, 10-2

#### 2. Group Management (13개 옵션)
- CRUD: 전체 조회, ID 검색, 추가, 상태 수정, 삭제
- 멤버 관리: 학생 추가, 학생 제거
- 동적 쿼리 5개: Query 1-2, 2-2, 8-1, 9-1, 9-2

#### 3. Message Management (7개 옵션)
- CRUD: 전체 조회, 추가, 삭제
- 동적 쿼리 3개: Query 2-1, 3-2, 7-2

#### 4. Report Management (7개 옵션)
- CRUD: 전체 조회, ID 검색, 추가, 상태 수정, 삭제
- 동적 쿼리 1개: Query 8-2

#### 사용 방법
- 숫자 키로 메뉴 옵션 선택 (메인 메뉴: 1-5, 하위 메뉴: 각각 다름)
- 각 하위 메뉴에서 "Back to Main Menu" 옵션으로 복귀
- 삭제 작업 시 확인 프롬프트 표시
- 선택 사항 필드는 Enter로 건너뛰기 가능
- Ctrl+C 또는 메인 메뉴에서 0번 선택으로 종료

---

### 📊 출력 기능

- **테이블 형식 출력**: 자동 컬럼 너비 조정, 경계선 포함
- **통계 정보**: 개수, 평균, 최소, 최대값 표시
- **타임스탬프**: `yyyy-MM-dd HH:mm:ss` 형식
- **상태 메시지**: SUCCESS, ERROR, WARNING, INFO 구분
- **페이지 단위 표시**: 긴 결과의 경우 확인 프롬프트

---

## ⚙️ 제작 환경

### 개발 환경
- **언어**: Java 8 이상 (권장: Java 17)
- **빌드 도구**: Gradle 8.5
- **IDE**: IntelliJ IDEA / Eclipse (선택)

### 데이터베이스
- **DBMS**: Oracle Database XE (Express Edition)
- **JDBC 드라이버**: ojdbc11 (23.3.0.23.09)
- **형상 관리 도구**: Flyway 10.4.1

### 주요 라이브러리

| 라이브러리 | 버전 | 용도 |
|-----------|------|------|
| ojdbc11 | 23.3.0.23.09 | Oracle JDBC 드라이버 |
| flyway-core | 10.4.1 | 데이터베이스 마이그레이션 |
| flyway-database-oracle | 10.4.1 | Oracle 마이그레이션 지원 |
| snakeyaml | 2.2 | YAML 설정 파일 파싱 |
| logback-classic | 1.4.14 | 로깅 구현체 |
| slf4j-api | 2.0.9 | 로깅 인터페이스 |

### 프로젝트 구조

```
Phase3-Console-Program/
├── src/main/java/com/university/matching/
│   ├── Main.java                          # 애플리케이션 진입점
│   ├── config/
│   │   └── DatabaseConfig.java            # DB 연결 관리 (Singleton)
│   ├── dao/                               # 데이터 접근 계층 (4개 클래스, 총 20개 쿼리)
│   │   ├── StudentDAO.java                # 학생 CRUD + 11개 쿼리
│   │   ├── GroupDAO.java                  # 그룹 CRUD + 5개 쿼리
│   │   ├── MessageDAO.java                # 메시지 CRUD + 3개 쿼리
│   │   └── ReportDAO.java                 # 신고 CRUD + 1개 쿼리
│   ├── model/                             # 도메인 모델 (6개 엔티티)
│   │   ├── Student.java
│   │   ├── Group.java
│   │   ├── Message.java
│   │   ├── Report.java
│   │   ├── MatchingSession.java
│   │   └── MatchingQueue.java
│   ├── ui/                                # 사용자 인터페이스 (리팩토링됨)
│   │   ├── ConsoleUI.java                 # 메인 콘솔 UI (메뉴 라우터)
│   │   ├── StudentMenuUI.java             # 학생 관리 전용 UI
│   │   ├── GroupMenuUI.java               # 그룹 관리 전용 UI
│   │   ├── MessageMenuUI.java             # 메시지 관리 전용 UI
│   │   ├── ReportMenuUI.java              # 신고 관리 전용 UI
│   │   ├── InputValidator.java            # 입력 검증
│   │   └── OutputFormatter.java           # 출력 포맷팅
│   └── util/
│       └── FlywayMigration.java           # DB 마이그레이션
└── src/main/resources/
    ├── application.yml                    # 설정 파일 (YAML)
    ├── logback.xml                        # 로깅 설정
    └── db/migration/                      # Flyway 마이그레이션
        ├── V1__create_schema.sql          # 스키마 생성
        ├── V2__insert_sample_data.sql     # 샘플 데이터
        └── V3__create_queries.sql         # 쿼리 정의
```

### 아키텍처 패턴
- **DAO 패턴**: 데이터 접근 로직 분리 및 캡슐화
- **Singleton 패턴**: DatabaseConfig 단일 인스턴스 관리
- **Delegation 패턴**: ConsoleUI가 각 도메인별 MenuUI에 책임 위임
- **MVC 패턴**: Model(엔티티) - View(UI) - Controller(DAO) 계층 분리
- **Try-with-resources**: 자동 리소스 관리 및 메모리 누수 방지

---

## ⚠️ 유의 사항

### 입력 검증 규칙

애플리케이션은 모든 입력값에 대해 클라이언트 측 검증을 수행합니다. 각 도메인별 상세한 입력 규칙은 위의 [도메인별 주요 기능](#-도메인별-주요-기능) 섹션을 참조하세요.

**주요 검증 규칙 요약:**

| 항목 | 규칙 | 상세 참조 |
|------|------|----------|
| **학번** | 정확히 10자리 숫자 | [학생 관련 기능](#1-학생-student-관련-기능) |
| **나이** | 18세 이상 30세 이하 | [학생 관련 기능](#1-학생-student-관련-기능) |
| **성별** | M (남성) 또는 F (여성) | [학생 관련 기능](#1-학생-student-관련-기능) / [그룹 관련 기능](#2-그룹-group-관련-기능) |
| **MBTI** | 4자리: [E/I][N/S][T/F][J/P] 형식 (예: ENFP, ISTJ) | [학생 관련 기능](#1-학생-student-관련-기능) |
| **그룹 상태** | AVAILABLE, QUEUING, MATCHED 중 하나 | [그룹 관련 기능](#2-그룹-group-관련-기능) |
| **신고 상태** | PENDING, REVIEWING, RESOLVED, REJECTED 중 하나 | [신고 관련 기능](#4-신고-report-관련-기능) |
| **세션 상태** | ACTIVE, COMPLETED, CANCELED 중 하나 | - |
| **매칭 큐 상태** | WAITING, MATCHED, CANCELED 중 하나 | [그룹 관련 기능](#2-그룹-group-관련-기능) |

### 데이터베이스 제약사항

1. **외래키 제약**: CASCADE DELETE 설정으로 참조 데이터 자동 삭제
   - 학생 삭제 시 관련 그룹 멤버십, 메시지, 신고 자동 삭제
   - 그룹 삭제 시 관련 멤버십, 매칭 큐 자동 삭제

2. **CHECK 제약**: 잘못된 값 입력 방지
   - 나이 범위, 성별, MBTI 형식, 상태값 검증

3. **UNIQUE 제약**: 중복 방지
   - 학번 중복 불가
   - 그룹당 하나의 매칭 큐만 가능

### 보안

- **SQL Injection 방지**: 모든 쿼리에 PreparedStatement 사용
- **자동 리소스 관리**: Try-with-resources로 연결 누수 방지
- **입력 검증**: 클라이언트 측 + 데이터베이스 측 이중 검증

### 오류 처리

**일반적인 오류 메시지:**
- `Database connection failed`: DB 연결 실패 → `application.yml` 확인
- `Invalid input`: 입력값 검증 실패 → 오류 메시지의 형식 참고
- `Record not found`: 존재하지 않는 ID → ID 재확인
- `Constraint violation`: 제약조건 위반 → 외래키 또는 CHECK 제약 확인

**문제 해결:**
1. 데이터베이스가 실행 중인지 확인
2. `application.yml`의 연결 정보 검증
3. 사용자 계정에 필요한 권한이 있는지 확인
4. 콘솔에 출력되는 상세 오류 메시지 확인

### 사용 팁

- **메뉴 탐색**: 숫자 키로 옵션 선택 (메인: 0-5, 각 하위 메뉴마다 옵션 수가 다름)
- **되돌아가기**: 각 하위 메뉴에서 "Back to Main Menu" 옵션 사용
- **삭제 확인**: 삭제 작업 시 확인 프롬프트 표시 (실수 방지)
- **빈 입력**: 선택 사항 필드는 Enter로 건너뛰기 가능
- **MBTI 입력**: 비어있거나 정확히 4자리 형식 (예: ENFP)
- **리스트 입력**: 쉼표로 구분하여 여러 값 입력 (예: ENFP,ISTJ,ENTJ)
- **종료**: 메인 메뉴에서 0번 선택 또는 Ctrl+C
- **에러 발생 시**: 에러 메시지를 확인하고 메뉴로 자동 복귀 (프로그램이 종료되지 않음)


---

## 프로젝트 수정사항
Phase2 -> 3로 넘어오면서 JOIN 흐름 관리와 DB 관리 편의성을 위해, 전반적인 수정을 하였습니다. 

### ER 다이어그램 수정사항
- MATCHING_QUEUE와 MATCHING_SESSION 사이의 관계 삭제
- GROUP과 MATCHING_SESSION 간의 관계 생성 (MATCHES)
  ![image](https://media.discordapp.net/attachments/1426845224256147466/1439547960181653524/SQL.png?ex=691aeb03&is=69199983&hm=5eea60cc8c979cd08017f286df9977f9d3e5bfa0d47300eb6d1aa4072d379295&=&format=webp&quality=lossless&width=1608&height=1144)
  - 하나의 SESSION은 여러 개의 GROUP를 가질 수 있다. (1:N)
  - 모든 SESSION은 MATCHES 관계를 모두 참여해야한다 (Full Participate)
  - GROUP 들은 MATCHES를 관계를 참여할 수 있다. (Partial Participate)
- MATCHING_SESSION에 status 필드 추가 (active, completed, canceled)
- GROUP에 status 추가 (available, queuing, matched)


### SQL 파일 수정사항

#### 1. 파일 구조 안내

**Phase 2 제출 파일 구조 (Team2-Phase2-*.sql):**
- `Team2-Phase2-1.sql`: DDL (스키마 생성)
  - **Flyway 매핑:** `V1__create_schema.sql` 전체 내용
  - 테이블 정의, 시퀀스, 인덱스 포함

- `Team2-Phase2-2.sql`: 샘플 데이터
  - **Flyway 매핑:** `V2__insert_sample_data.sql` 전체 내용
  - STUDENT, GROUPS, STUDENT_GROUP, MATCHING_SESSION, SESSION_MATCHES, MATCHING_QUEUE, CHAT_ROOM, MESSAGE, REPORT 데이터 포함

- `Team2-Phase2-3.sql`: 20개 쿼리 (SELECT 문)
  - **Flyway 매핑:** `V3__create_queries.sql`은 flyway 작동을 위해 VIEW로 생성

**Phase 3 구조 (Flyway 마이그레이션):**
- `V1__create_schema.sql`: DDL만 포함 (테이블, 시퀀스, 인덱스)
- `V2__insert_sample_data.sql`: 모든 샘플 데이터
- `V3__create_queries.sql`: 20개 쿼리를 VIEW로 변환
- 파일 위치: Phase3-Console-Program/src/main/resource/db/migration/

**변경 목적:**
- Flyway 마이그레이션 도구의 버전 관리 규칙 준수
- Phase 2에서 요구한 부족한 요구사항을 해결 (지난 감점 요인 해결)
- Flyway에서는 재사용성을 위해 VIEW로 구현, 제출용 SQL은 일반 SELECT 문 사용

---

#### 2. DDL (스키마) 주요 수정사항 (V1__create_schema.sql)

##### 2.1 테이블 구조 변경

**GROUPS 테이블:**
- `status` 컬럼 추가 (VARCHAR2(20), DEFAULT 'AVAILABLE')
- CHECK 제약조건: 'AVAILABLE', 'QUEUING', 'MATCHED'
- 그룹의 라이프사이클 관리 강화

**STUDENT_GROUP 테이블:**
- 컬럼명 통일: `joined_at` → `created_at`

**MATCHING_SESSION 테이블:**
- `status` 컬럼 추가 (VARCHAR2(20), DEFAULT 'ACTIVE')
- CHECK 제약조건: 'ACTIVE', 'COMPLETED', 'CANCELED'
- 세션 상태 추적 가능

**MATCHING_QUEUE 테이블 (중요):**
- `session_id` 컬럼 완전 제거
- `session_id` 외래키 및 인덱스 제거
- 큐는 대기 상태만 관리하도록 역할 명확화

**SESSION_MATCHES 테이블 (신규 추가):**
- MATCHING_SESSION과 GROUPS의 다대다 관계 관리
- 컬럼: `session_id`, `group_id`, `queue_id`, `created_at`
- MATCHING_QUEUE의 session_id를 별도 테이블로 분리하여 정규화
- 한 세션에 여러 그룹이 매칭되는 구조 명확화

**REPORT 테이블 (중요):**
- `reporter_student_id` + `reported_student_id` → `student_id` 단일 컬럼
- 피신고자 정보 제거 (개인정보 보호 강화)
- 신고자만 기록하는 간소화된 구조

##### 2.2 인덱스 최적화

**추가된 인덱스:**
- `idx_student_department`, `idx_student_mbti` (STUDENT)
- `idx_group_status` (GROUPS)
- `idx_matching_session_status` (MATCHING_SESSION)
- `idx_session_matches_*` 3개 (SESSION_MATCHES - 신규)

**제거된 인덱스:**
- `idx_matching_queue_session` (session_id 컬럼 제거로 인한 삭제)
- `idx_report_reported` (피신고자 정보 제거로 인한 삭제)

---

#### 3. 20개 쿼리 수정사항 (V3__create_queries.sql)

##### 3.1 전반적 변경
- **Team2:** 일반 SELECT 쿼리 (일회성 실행)
- **참고사항:** `CREATE OR REPLACE VIEW` (재사용 가능, 명명된 쿼리) 으로 변경
- 대부분의 쿼리에서 `ORDER BY`, `FETCH FIRST` 제거

##### 3.2 주요 쿼리별 수정사항

**Type 2 (Multi-way join):**
- Query 2-2: MATCHING_SESSION 조인 제거, SESSION_MATCHES 사용 안 함
- MATCHING_QUEUE 중심의 조회로 변경 (스키마 변경 반영)

**Type 3 (Aggregation):**
- Query 3-1: HAVING 조건 완화 (`>= 10` → `>= 5`)

**Type 4 (Subquery):**
- Query 4-2: 완전히 다른 쿼리로 변경 (채팅방 통계 → 학과별 그룹 참여도)

**Type 5 (EXISTS):**
- Query 5-2: "신고를 받은 학생" → "신고를 한 학생"으로 의미 변경
- `r.reported_student_id` → `r.student_id` (REPORT 테이블 구조 변경 반영)

**Type 6 (IN predicates):**
- Query 6-1: 서브쿼리   제거, MBTI 목록 변경, 그룹 소속 조건 제거

**Type 7 (In-line view):**
- Query 7-1, 7-2를 완전히 다른 쿼리로 변경
- Query 7-1: 각 학과의 평균 나이보다 많은 학생들 조회
- Query 7-2: 메시지를 평균 이상으로 보낸 학생들 조회

**Type 8 (Multi-way join + ORDER BY):**
- Query 8-1: SESSION_MATCHES 테이블 사용, 학생 정보 제거, 세션 중심 조회
- Query 8-2: 피신고자 정보 제거, 조인 단순화 (STUDENT 1번만 조인)

**Type 9 (Aggregation + GROUP BY + ORDER BY):**
- Query 9-1: 그룹별 멤버 통계 (Team2의 Query 17과는 완전히 다름)
- Query 9-2: SESSION_MATCHES 사용, 시간 계산 제거, department 기준 그룹화

**Type 10 (SET operation):**
- Query 10-1: "신고자+피신고자" → "메시지 발신자+신고자" (REPORT 구조 변경 반영)
- Query 10-2: `INNER JOIN` → 콤마 조인, `FETCH FIRST` 제거

---

###
