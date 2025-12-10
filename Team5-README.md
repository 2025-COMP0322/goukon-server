# Team5 - Goukon Server: 대학생 미팅 매칭 플랫폼

**프로젝트명**: Goukon (ゴーコン) - 합동 미팅 매칭 플랫폼  
**팀명**: Team5  
**작성일**: 2025년 12월 10일  
**GitHub**: [https://github.com/2025-COMP0322/goukon-server](https://github.com/2025-COMP0322/goukon-server)

---

## 📌 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [주요 기능](#2-주요-기능)
3. [제작 환경](#3-제작-환경-development-environment)
4. [기술 스택](#4-기술-스택-technology-stack)
5. [시스템 아키텍처](#5-시스템-아키텍처)
6. [설치 및 실행 방법](#6-설치-및-실행-방법)
7. [프론트엔드 사용 방법](#7-프론트엔드-사용-방법)
8. [API 사용 방법](#8-api-사용-방법)
9. [데이터베이스 스키마](#9-데이터베이스-스키마)
10. [동시성 제어 및 트랜잭션 처리](#10-동시성-제어-및-트랜잭션-처리)
11. [유의사항 및 문제 해결](#11-유의사항-및-문제-해결)
12. [데모 동영상](#12-데모-동영상)
13. [팀원 소개](#13-팀원-소개)

---

## 1. 프로젝트 개요

Goukon은 대학생들이 그룹을 만들어 **1:1 또는 3:3 미팅을 매칭**하는 플랫폼입니다.  
실시간 매칭 시스템, WebSocket 기반 채팅, 세션 관리 등의 기능을 제공하며, **Oracle 데이터베이스의 트랜잭션 및 동시성 제어**를 활용하여 안정적인 서비스를 구현했습니다.

### 프로젝트 특징

✅ **실시간 매칭 시스템** (RabbitMQ 기반 비동기 처리)  
✅ **WebSocket 실시간 채팅**  
✅ **트랜잭션 격리 수준을 통한 동시성 제어**  
✅ **Redis 캐싱으로 초대 코드 관리**  
✅ **RESTful API 설계**  
✅ **JWT 기반 인증/인가**  
✅ **Docker Compose를 통한 쉬운 배포**

### 프로젝트 배경

- 대학생들의 만남 기회 확대
- 성별과 인원수에 맞는 자동 매칭
- 그룹 단위 매칭으로 안전성 확보
- 실시간 채팅으로 즉각적인 소통 지원

---

## 2. 주요 기능

### 2.1 사용자 관리 (User Management)

#### 🔐 회원가입 (Sign Up)
- 이메일, 학번, 이름, 나이, 성별, 학과 등 입력
- 비밀번호 암호화 저장 (BCrypt)
- 이메일/학번 중복 검증 (DB 제약조건)

#### 🔐 로그인 (Login)
- JWT Access Token / Refresh Token 발급
- Access Token 만료 시 Refresh Token으로 갱신

#### 🔐 비밀번호 재설정
- 학번 기반 비밀번호 재설정

#### 👤 프로필 조회/수정
- 내 프로필 정보 조회
- MBTI, 자기소개 수정

---

### 2.2 그룹 관리 (Group Management)

#### 👥 그룹 생성
- 1인 또는 3인 그룹 생성
- 같은 성별만 그룹 참여 가능

#### 👥 그룹 초대
- 6자리 초대 코드 생성 (Redis, TTL 1시간)
- 초대 코드로 그룹 참여

#### 👥 그룹 멤버 관리
- 멤버 추가/삭제
- 인원 제한 검증 (최대 3명)
- **SERIALIZABLE 격리수준으로 동시 참여 제어**

#### 👥 그룹 삭제
- 그룹장만 삭제 가능
- 매칭 중인 그룹은 삭제 불가

---

### 2.3 매칭 시스템 (Matching System)

#### 💑 매칭 대기열 등록
- 1:1 또는 3:3 매칭 선택
- RabbitMQ를 통한 비동기 처리
- 비관적 락으로 중복 등록 방지

#### 💑 자동 매칭
- 반대 성별의 대기열과 자동 매칭
- 선입선출(FIFO) 공정성 보장
- **SERIALIZABLE 격리수준으로 중복 매칭 방지**
- **Deadlock 자동 감지 및 재시도**

#### 💑 매칭 세션 관리
- 매칭 성공 시 채팅방 자동 생성
- 세션 상세 정보 조회 (우리 팀 + 상대 팀)
- 내 매칭 세션 목록 조회

#### 💑 세션 종료 투표
- 그룹원 만장일치로 세션 종료
- **SERIALIZABLE 격리수준으로 정확한 투표 카운팅**
- 복합 키로 중복 투표 방지

---

### 2.4 실시간 채팅 (Real-time Chat)

#### 💬 WebSocket 연결
- STOMP 프로토콜 기반
- 세션별 채팅방 자동 생성

#### 💬 메시지 전송/수신
- 실시간 메시지 전송
- **Redis 버퍼링으로 DB 부하 감소**
- 임계치(10개) 또는 주기(5초)마다 DB 저장

#### 💬 채팅 이력 조회
- 과거 메시지 조회
- 페이지네이션 지원

---

### 2.5 신고 시스템 (Report System)

#### 🚨 사용자 신고
- 부적절한 사용자 신고
- 신고 유형: 욕설, 성희롱, 사기, 노쇼 등
- 중복 신고 방지

#### 🚨 신고 내역 조회
- 관리자용 신고 목록 조회

---

## 3. 제작 환경 (Development Environment)

### 3.1 개발 도구

- **IDE**: IntelliJ IDEA 2024.3
- **버전 관리**: Git, GitHub
- **협업 도구**: Notion, Discord
- **API 테스트**: Postman, Swagger UI
- **데이터베이스 관리**: Oracle SQL Developer

### 3.2 개발 환경

#### 백엔드
- **Java**: 21 (OpenJDK 21 LTS)
- **Spring Boot**: 3.5.6
- **Gradle**: 8.14

#### 프론트엔드
- **Node.js**: 20.x 이상
- **Next.js**: 15.3.2
- **React**: 19.0.0
- **TypeScript**: 5.x
- **TailwindCSS**: 3.4.17

#### 데이터베이스
- **Oracle Database**: 21c XE (Express Edition)
- **Redis/Valkey**: 7.2 (초대 코드 캐싱)

#### 메시지 큐
- **RabbitMQ**: 3.12 (매칭 시스템 비동기 처리)

#### 컨테이너
- **Docker**: 24.0+
- **Docker Compose**: 2.20+

### 3.3 운영 환경

- **OS**: Ubuntu 20.04 LTS / macOS Ventura 이상 / Windows 10/11
- **RAM**: 최소 8GB (권장 16GB)
- **Disk**: 최소 10GB 여유 공간
- **Network**: 포트 40000-40004 사용 가능해야 함

---

## 4. 기술 스택 (Technology Stack)

### 4.1 백엔드 프레임워크

#### Spring Boot Starters
- `spring-boot-starter-web` - RESTful API 개발
- `spring-boot-starter-data-jpa` - ORM (Hibernate)
- `spring-boot-starter-validation` - 입력 검증
- `spring-boot-starter-amqp` - RabbitMQ 메시지 큐
- `spring-boot-starter-data-redis` - Redis 캐싱
- `spring-boot-starter-actuator` - 헬스 체크

#### WebSocket
- `spring-websocket` - WebSocket 지원
- `spring-messaging` - STOMP 메시징

#### 보안
- `spring-security-crypto` - BCrypt 비밀번호 암호화
- `jjwt` (0.12.6) - JWT 토큰 생성/검증

### 4.2 데이터베이스 및 캐싱

- `Oracle JDBC Driver` (23.2.0.0) - Oracle DB 연결
- `H2 Database` - 테스트용 인메모리 DB
- `MySQL Connector` - MySQL 지원 (옵션)
- `Redis/Valkey` - 초대 코드 캐싱

### 4.3 메시지 큐

#### RabbitMQ (AMQP)
- Direct Exchange
- Durable Queue
- Message TTL (5분)
- 자동 재시도

### 4.4 API 문서화

- **SpringDoc OpenAPI** (2.8.13) - Swagger UI 자동 생성
- 접속 URL: `http://localhost:8080/swagger-ui.html`

### 4.5 유틸리티

- **Lombok** - Boilerplate 코드 제거
- **JUnit 5** - 단위 테스트

### 4.6 프론트엔드 기술 스택

#### 프레임워크 & 라이브러리
- **Next.js 15.3.2** - React 기반 풀스택 프레임워크
  - App Router 사용
  - Server/Client Components
  - Turbopack으로 빠른 개발 서버
- **React 19.0.0** - UI 라이브러리
- **TypeScript 5.x** - 타입 안전성

#### 스타일링
- **TailwindCSS 3.4.17** - 유틸리티 기반 CSS 프레임워크
- **PostCSS** - CSS 변환 도구
- **Prettier** - 코드 포맷팅

#### 실시간 통신
- **@stomp/stompjs 7.0.0** - STOMP over WebSocket 클라이언트
- **sockjs-client 1.6.1** - WebSocket polyfill

#### 개발 도구
- **ESLint** - 코드 린팅
- **Prettier** - 코드 포맷팅
- **TypeScript** - 정적 타입 체크

#### 주요 기능별 구현
- **Context API** - 전역 상태 관리 (매칭, WebSocket)
- **Custom Hooks** - 재사용 가능한 로직 (useSwipe 등)
- **API Client** - Fetch 기반 백엔드 통신

### 4.7 빌드 및 배포

- **Gradle 8.14** - 백엔드 빌드 자동화
- **npm/Node.js** - 프론트엔드 패키지 관리
- **Docker** - 컨테이너화
- **Docker Compose** - 멀티 컨테이너 오케스트레이션

---

## 5. 시스템 아키텍처

### 5.1 전체 아키텍처

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Layer                             │
│                   (Next.js Web Application)                     │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  React Components                                       │   │
│  │  - 회원가입/로그인 UI                                      │   │
│  │  - 그룹 생성/관리 UI                                       │   │
│  │  - 매칭 대기/채팅 UI                                       │   │
│  │  - 프로필 관리 UI                                         │   │
│  └─────────────────────────────────────────────────────────┘   │
│                         ↓                                       │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  State Management (Context API)                         │   │
│  │  - WebSocketContext (실시간 통신)                        │   │
│  │  - MatchingContext (매칭 상태)                           │   │
│  └─────────────────────────────────────────────────────────┘   │
│                         ↓                                       │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  API Client Layer                                       │   │
│  │  - Fetch 기반 HTTP 클라이언트                            │   │
│  │  - STOMP WebSocket 클라이언트                           │   │
│  │  - JWT 토큰 관리                                         │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              ↓ HTTP/WebSocket
┌─────────────────────────────────────────────────────────────────┐
│                     Spring Boot Application                     │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  Presentation Layer (Controllers)                         │  │
│  │  - AuthController, GroupController, MatchingController    │  │
│  │  - ChatController (WebSocket)                             │  │
│  └───────────────────────────────────────────────────────────┘  │
│                              ↓                                  │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  Application Layer (Services)                             │  │
│  │  - AuthService, GroupService, MatchingService             │  │
│  │  - ChatService, InviteCodeService                         │  │
│  └───────────────────────────────────────────────────────────┘  │
│                              ↓                                  │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  Domain Layer (Entities, Repositories)                    │  │
│  │  - Student, Group, MatchingQueue, MatchingSession         │  │
│  │  - ChatRoom, ChatMessage, SessionEndVote                  │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
            ↓                    ↓                    ↓
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│  Oracle DB      │  │  Redis/Valkey   │  │  RabbitMQ       │
│  (Port: 40004)  │  │  (Port: 40001)  │  │  (Port: 40002)  │
│                 │  │                 │  │                 │
│  - 영구 데이터     │  │  - 초대 코드      │  │  - 매칭 큐        │
│  - 트랜잭션       │  │  - TTL 1시간      │  │  - 비동기 처리    │
│  - 동시성 제어     │  │  - 채팅 버퍼      │  │  - 재시도         │
└─────────────────┘  └─────────────────┘  └─────────────────┘
```

### 5.2 매칭 시스템 흐름

#### Step 1: 매칭 등록
```
Client → POST /v1/matching/queue
         ↓
MatchingService.registerQueue()
         ↓
Group 비관적 락 획득 (SELECT ... FOR UPDATE)
         ↓
MatchingQueue 생성 (DB 저장)
         ↓
RabbitMQ에 메시지 전송
         ↓
Client ← 201 Created (즉시 응답)
```

#### Step 2: 매칭 처리 (비동기)
```
RabbitMQ Consumer ← 메시지 수신
         ↓
MatchingService.processMatchingRequest()
         ↓
현재 큐 비관적 락 획득
         ↓
반대 성별 대기열 조회 (비관적 락)
         ↓
매칭 실행 (세션 생성, 채팅방 생성)
         ↓
매칭 완료 (COMMIT)
```

#### Step 3: 채팅 시작
```
Client ↔ WebSocket 연결
         ↓
채팅 메시지 전송/수신
         ↓
Redis 버퍼링 → 주기적 DB Flush
```

### 5.3 데이터 흐름

#### 인증 흐름
```
로그인 → JWT 발급 → Authorization 헤더에 포함 → 요청 시마다 검증
```

#### 매칭 흐름
```
그룹 생성 → 매칭 등록 → RabbitMQ → 매칭 처리 → 세션 생성 → 채팅
```

#### 채팅 흐름
```
WebSocket 연결 → 메시지 전송 → Redis 버퍼 → DB Flush → 이력 조회
```

---

## 6. 설치 및 실행 방법

### 6.1 사전 요구사항

#### 백엔드
✅ Java 21 이상 설치  
✅ Docker 및 Docker Compose 설치  
✅ 8GB 이상 RAM  
✅ 10GB 이상 디스크 여유 공간

#### 프론트엔드
✅ Node.js 18 이상 설치  
✅ npm 또는 yarn

### 6.2 프로젝트 클론

```bash
git clone https://github.com/2025-COMP0322/goukon-server.git
cd goukon-server
```

### 6.3 🚀 빠른 시작 (자동 배포 스크립트)

**가장 쉬운 방법**: 제공된 `deploy.sh` 스크립트를 사용하면 **프론트엔드와 백엔드를 한 번에 설정하고 실행**할 수 있습니다.

```bash
# 프로젝트 루트 디렉토리에서
./deploy.sh
```

이 스크립트는 다음 작업을 자동으로 수행합니다:
- ✅ 사전 요구사항 확인 (Docker, Node.js, npm)
- ✅ 백엔드 `.env` 파일 생성
- ✅ Docker Compose로 백엔드 서비스 시작 (Oracle, Redis, RabbitMQ, Spring Boot)
- ✅ 프론트엔드 `.env.local` 파일 생성
- ✅ npm 패키지 설치
- ✅ Next.js 개발 서버 시작

실행 후 다음 주소로 접속하세요:
- 🌐 **프론트엔드**: http://localhost:3000
- 🔧 **백엔드 API**: http://localhost:40000
- 📊 **RabbitMQ 관리**: http://localhost:40003

---

### 6.4 환경 변수 설정 (수동 설정 시)

**필수 단계**: `.env` 파일을 생성하여 환경 변수를 설정합니다.

```bash
# .env.example 파일을 .env로 복사
cp .env.example .env
```

필요시 `.env` 파일을 편집하여 값을 수정하세요:

```env
# Database Configuration
DB_PASSWORD=goukon123

# RabbitMQ Configuration
RABBITMQ_USER=goukon
RABBITMQ_PASS=goukon123

# JWT Configuration (프로덕션 환경에서는 반드시 변경!)
JWT_ACCESS_SECRET=your-access-secret-key-here
JWT_REFRESH_SECRET=your-refresh-secret-key-here
```

### 6.5 Docker Compose로 백엔드만 실행 (수동 방식)

#### 전체 서비스 시작

```bash
cd goukon-server
# .env 파일을 사용하여 실행
docker compose --env-file .env up -d
```

이 명령어는 다음 서비스들을 자동으로 시작합니다:
- ✅ Oracle XE Database (Port: 40004)
- ✅ Valkey/Redis (Port: 40001)
- ✅ RabbitMQ (Port: 40002, 40003)
- ✅ Spring Boot Application (Port: 40000)

#### 서비스 상태 확인

```bash
docker compose ps
```

#### 로그 확인

```bash
docker compose logs -f goukon-server
```

#### 서비스 중지

```bash
docker compose down
```

#### 완전 삭제 (데이터 포함)

```bash
docker compose down -v
```

### 6.6 로컬 개발 환경에서 백엔드만 실행

#### 1단계: 외부 서비스 시작 (Oracle, Valkey, RabbitMQ)

```bash
cd goukon-server
docker compose --env-file .env up -d oracle-db valkey rabbitmq
```

#### 2단계: 애플리케이션 빌드

```bash
./gradlew clean build -x test
```

#### 3단계: 애플리케이션 실행

```bash
./gradlew bootRun
```

또는

```bash
java -jar build/libs/goukon-server-0.0.1-SNAPSHOT.jar
```

---

### 6.7 프론트엔드 실행 (수동 방식)

#### 1단계: 프론트엔드 디렉토리로 이동

```bash
cd goukon-web
```

#### 2단계: 환경 변수 설정

`.env.local` 파일을 생성합니다:

```bash
# .env.local
NEXT_PUBLIC_API_URL=http://localhost:40000
NEXT_PUBLIC_WS_URL=http://localhost:40000/ws
```

#### 3단계: 패키지 설치

```bash
npm install
# 또는
yarn install
```

#### 4단계: 개발 서버 실행

```bash
npm run dev
# 또는
yarn dev
```

프론트엔드는 http://localhost:3000 에서 실행됩니다.

#### 5단계: 프로덕션 빌드 (선택사항)

```bash
npm run build
npm start
# 또는
yarn build
yarn start
```

### 6.6 서비스 접속 URL

| 서비스 | URL | 비고 |
|--------|-----|------|
| **Spring Boot API Server** | http://localhost:40000 | 메인 서버 |
| **Health Check** | http://localhost:40000/actuator/health | 헬스 체크 |
| **Swagger UI** | http://localhost:40000/swagger-ui.html | API 문서 |
| **RabbitMQ Management UI** | http://localhost:40003 | ID: goukon / PW: goukon123 |
| **Oracle Database** | localhost:40004 | Service: XEPDB1 / ID: system / PW: goukon123 |

### 6.7 초기 데이터 설정

애플리케이션 시작 시 JPA의 `ddl-auto: update`로 테이블이 자동 생성됩니다.

#### 생성되는 테이블 목록
- `STUDENT` - 사용자 정보
- `GROUPS` - 그룹 정보
- `STUDENT_GROUP` - 그룹-학생 관계
- `MATCHING_QUEUE` - 매칭 대기열
- `MATCHING_SESSION` - 매칭 세션
- `SESSION_MATCHES` - 세션-그룹 관계
- `SESSION_END_VOTE` - 세션 종료 투표
- `CHAT_ROOM` - 채팅방
- `CHAT_MESSAGE` - 채팅 메시지
- `REPORT` - 신고

### 6.8 문제 해결

#### 포트 충돌 시
`docker-compose.yml`에서 포트 번호 변경
- 40000 → 다른 포트 (Spring Boot)
- 40001 → 다른 포트 (Valkey)
- 40002 → 다른 포트 (RabbitMQ)
- 40004 → 다른 포트 (Oracle)

#### Oracle 초기화 시간이 길 경우
Oracle XE는 첫 시작 시 60-90초 소요됩니다.  
헬스 체크가 완료될 때까지 대기하세요.

#### 메모리 부족 시
Docker Desktop의 메모리 할당을 8GB 이상으로 증가

---

## 7. 프론트엔드 사용 방법

### 7.1 프론트엔드 프로젝트 구조

```
goukon-web/
├── app/                          # Next.js App Router
│   ├── (auth)/                   # 인증 관련 페이지 그룹
│   │   ├── login/                # 로그인
│   │   └── register/             # 회원가입
│   ├── (main)/                   # 메인 앱 페이지 그룹
│   │   ├── home/                 # 홈 화면
│   │   ├── group/                # 그룹 관리
│   │   ├── history/              # 매칭 히스토리
│   │   └── mypage/               # 마이페이지
│   ├── match/[sessionId]/        # 매칭 세션별 채팅
│   ├── profile/setup/            # 프로필 설정
│   ├── components/               # UI 컴포넌트
│   │   ├── chat/                 # 채팅 관련 컴포넌트
│   │   ├── common/               # 공통 컴포넌트
│   │   ├── layout/               # 레이아웃 컴포넌트
│   │   ├── matching/             # 매칭 관련 컴포넌트
│   │   ├── profile/              # 프로필 관련 컴포넌트
│   │   └── ui/                   # 기본 UI 컴포넌트
│   ├── context/                  # React Context
│   │   ├── MatchingContext.tsx   # 매칭 상태 관리
│   │   └── WebSocketContext.tsx  # WebSocket 연결 관리
│   ├── hooks/                    # Custom Hooks
│   │   └── useSwipe.ts           # 스와이프 제스처
│   ├── lib/                      # 유틸리티 라이브러리
│   │   ├── api/                  # API 클라이언트
│   │   │   ├── auth.ts           # 인증 API
│   │   │   ├── chatroom.ts       # 채팅 API
│   │   │   ├── client.ts         # HTTP 클라이언트
│   │   │   ├── group.ts          # 그룹 API
│   │   │   ├── matching.ts       # 매칭 API
│   │   │   ├── report.ts         # 신고 API
│   │   │   └── student.ts        # 학생 API
│   │   ├── auth.ts               # 인증 유틸리티
│   │   └── websocket.ts          # WebSocket 유틸리티
│   ├── types/                    # TypeScript 타입 정의
│   │   ├── api.ts                # API 공통 타입
│   │   ├── auth.ts               # 인증 타입
│   │   ├── chatroom.ts           # 채팅 타입
│   │   ├── group.ts              # 그룹 타입
│   │   ├── matching.ts           # 매칭 타입
│   │   ├── report.ts             # 신고 타입
│   │   ├── student.ts            # 학생 타입
│   │   └── websocket.ts          # WebSocket 타입
│   ├── globals.css               # 전역 스타일
│   ├── layout.tsx                # 루트 레이아웃
│   └── page.tsx                  # 스플래시 화면
├── public/                       # 정적 파일
│   ├── home_image/               # 홈 이미지
│   ├── nav_icon/                 # 네비게이션 아이콘
│   ├── profile/                  # 프로필 이미지
│   └── splash/                   # 스플래시 이미지
├── next.config.ts                # Next.js 설정
├── package.json                  # 의존성 관리
├── tailwind.config.js            # TailwindCSS 설정
├── tsconfig.json                 # TypeScript 설정
└── prettier.config.mjs           # Prettier 설정
```

### 7.2 프론트엔드 설치 및 실행

#### 사전 요구사항
- Node.js 20.x 이상
- npm 또는 yarn

#### 설치

```bash
# goukon-web 디렉토리로 이동
cd goukon-web

# 의존성 설치
npm install
```

#### 개발 서버 실행

```bash
# 개발 모드 (Turbopack 사용)
npm run dev
```

브라우저에서 `http://localhost:3000` 접속

#### 프로덕션 빌드

```bash
# 빌드
npm run build

# 프로덕션 서버 실행
npm start
```

#### 코드 포맷팅

```bash
# Prettier로 코드 포맷팅
npm run format

# ESLint로 코드 검사
npm run lint
```

### 7.3 주요 기능 및 화면

#### 🎨 스플래시 화면
- 앱 시작 시 Goukon 로고 표시
- 2초 후 자동으로 로그인/홈 화면으로 이동
- Single Day 폰트 사용

#### 🔐 인증 화면
- **로그인** (`/login`)
  - 학번/이메일, 비밀번호 입력
  - JWT 토큰 자동 저장
  - 로그인 유지 기능
- **회원가입** (`/register`)
  - 이메일, 학번, 이름, 성별, 학과 등 입력
  - 실시간 유효성 검사
  - 비밀번호 확인

#### 🏠 홈 화면 (`/home`)
- 매칭 타입 선택 (1:1 / 3:3)
- 매칭 대기 상태 표시
- 실시간 매칭 알림
- 그룹 생성 버튼

#### 👥 그룹 관리 (`/group`)
- 내 그룹 정보 조회
- 그룹 생성
- 초대 코드 생성 및 공유
- 초대 코드로 그룹 참가
- 그룹 멤버 목록 표시
- 그룹 삭제 (그룹장만 가능)

#### 💬 채팅 화면 (`/match/[sessionId]`)
- 실시간 WebSocket 채팅
- 우리 팀/상대 팀 프로필 표시
- 메시지 입력 및 전송
- 채팅 이력 스크롤
- 세션 종료 투표 패널
- 시스템 메시지 표시

#### 📜 히스토리 (`/history`)
- 과거 매칭 세션 목록
- 세션별 상세 정보
- 매칭 날짜 및 상태 표시

#### 👤 마이페이지 (`/mypage`)
- 프로필 조회 및 수정
- MBTI 설정
- 자기소개 수정
- 설정 및 개인정보 관리
- FAQ
- 신고 내역

#### 📱 프로필 설정 (`/profile/setup`)
- 단계별 프로필 설정
- 생년월일 입력
- 기타 상세 정보 입력

### 7.4 주요 컴포넌트 설명

#### WebSocketContext
- 전역 WebSocket 연결 관리
- STOMP 클라이언트 생성 및 연결
- 메시지 구독 및 전송
- 연결 상태 관리

```typescript
// 사용 예시
const { client, isConnected } = useWebSocket();

// 메시지 전송
client?.publish({
  destination: '/app/chat.send',
  body: JSON.stringify(message)
});
```

#### MatchingContext
- 매칭 상태 전역 관리
- 매칭 타입 (1:1 / 3:3) 선택
- 매칭 대기 상태
- 매칭 알림

```typescript
// 사용 예시
const { matchingType, setMatchingType, isMatching } = useMatching();
```

#### API Client
- Fetch 기반 HTTP 클라이언트
- JWT 토큰 자동 포함
- 에러 핸들링
- 타입 안전성

```typescript
// 사용 예시
import { authApi } from '@/app/lib/api/auth';

const response = await authApi.login({
  emailOrStudentNumber: 'student@knu.ac.kr',
  password: 'password123'
});
```

### 7.5 환경 설정

#### API 엔드포인트 변경

`goukon-web/app/lib/api/client.ts` 파일에서 API_BASE_URL 수정

```typescript
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:40000';
```

환경 변수 사용 시 `.env.local` 파일 생성:

```env
NEXT_PUBLIC_API_URL=http://localhost:40000
NEXT_PUBLIC_WS_URL=ws://localhost:40000
```

### 7.6 프론트엔드 주요 기능 설명

#### 실시간 매칭 알림
- MatchingContext와 WebSocket 연동
- 매칭 성공 시 자동으로 채팅방 이동
- 알림 배너 표시

#### 스와이프 제스처
- useSwipe 커스텀 훅 사용
- 좌우 스와이프로 화면 전환
- 터치 및 마우스 이벤트 지원

#### 반응형 디자인
- 모바일 우선 디자인
- TailwindCSS 유틸리티 클래스
- `mobile-container` 클래스로 최대 너비 제한

#### 토큰 관리
- localStorage에 JWT 토큰 저장
- 만료된 토큰 자동 갱신 (Refresh Token)
- 인증 실패 시 로그인 페이지로 리다이렉트

### 7.7 프론트엔드 유의사항

⚠️ **백엔드 서버 실행 필수**
- 프론트엔드 실행 전 백엔드 서버 (포트 40000)가 실행 중이어야 함
- Docker Compose로 백엔드 실행 권장

⚠️ **CORS 설정**
- 백엔드에서 프론트엔드 도메인 허용 필요
- 개발 환경: `http://localhost:3000`
- 운영 환경: 실제 도메인으로 변경

⚠️ **WebSocket 연결**
- JWT 토큰이 유효해야 WebSocket 연결 가능
- 연결 실패 시 로그인 상태 확인

⚠️ **브라우저 호환성**
- 최신 버전의 Chrome, Safari, Firefox, Edge 권장
- WebSocket 지원 브라우저 필요

⚠️ **로컬 스토리지**
- 토큰 저장을 위해 로컬 스토리지 사용
- 시크릿 모드에서는 로그인 유지 안 됨

---

## 8. API 사용 방법

### 8.1 인증 API

#### 회원가입

**Request**
```http
POST /v1/auth/signup
Content-Type: application/json

{
  "email": "student@knu.ac.kr",
  "password": "password123",
  "studentNumber": "2020123456",
  "name": "홍길동",
  "age": 24,
  "gender": "M",
  "department": "컴퓨터학부",
  "mbti": "INTJ",
  "profile": "안녕하세요!"
}
```

**Response** `201 Created`
```json
{
  "id": 1,
  "studentNumber": "2020123456",
  "name": "홍길동",
  "email": "student@knu.ac.kr",
  "age": 24,
  "gender": "M",
  "department": "컴퓨터학부",
  "mbti": "INTJ",
  "profile": "안녕하세요!"
}
```

#### 로그인

**Request**
```http
POST /v1/auth/login
Content-Type: application/json

{
  "email": "student@knu.ac.kr",
  "password": "password123"
}
```

**Response** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer"
}
```

---

### 7.2 그룹 API

#### 그룹 생성

**Request**
```http
POST /v1/groups
Authorization: Bearer {accessToken}
```

**Response** `201 Created`
```json
{
  "id": 1,
  "gender": "M",
  "status": "AVAILABLE",
  "createdAt": "2025-12-10T10:00:00"
}
```

#### 초대 코드 생성

**Request**
```http
POST /v1/groups/{groupId}/invite-code
Authorization: Bearer {accessToken}
```

**Response** `200 OK`
```json
{
  "inviteCode": "123456",
  "expiresIn": 3600
}
```

#### 초대 코드로 그룹 참여

**Request**
```http
POST /v1/groups/join
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "inviteCode": "123456"
}
```

**Response** `200 OK`
```json
{
  "groupId": 1,
  "message": "그룹 참여 완료"
}
```

---

### 7.3 매칭 API

#### 매칭 대기열 등록

**Request**
```http
POST /v1/matching/queue
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "groupId": 1,
  "matchingType": "THREE_VS_THREE"
}
```

**Response** `201 Created`
```json
{
  "queueId": 101,
  "groupId": 1,
  "matchingType": "THREE_VS_THREE",
  "status": "WAITING",
  "queuedAt": "2025-12-10T10:00:00"
}
```

#### 매칭 대기열 취소

**Request**
```http
DELETE /v1/matching/queue/{groupId}
Authorization: Bearer {accessToken}
```

**Response** `200 OK`

#### 내 매칭 세션 목록 조회

**Request**
```http
GET /v1/matching/sessions/me
Authorization: Bearer {accessToken}
```

**Response** `200 OK`
```json
[
  {
    "sessionId": 1,
    "groupId": 1,
    "status": "ACTIVE",
    "createdAt": "2025-12-10T10:05:00"
  }
]
```

#### 세션 종료 투표

**Request**
```http
POST /v1/matching/sessions/{sessionId}/end-vote
Authorization: Bearer {accessToken}
```

**Response** `200 OK`
```json
{
  "sessionEnded": false,
  "currentVotes": 1,
  "requiredVotes": 3
}
```

---

### 7.4 채팅 API (WebSocket)

#### WebSocket 연결

```
CONNECT ws://localhost:40000/ws
Headers:
  Authorization: Bearer {accessToken}
```

#### 채팅방 구독

```
SUBSCRIBE /topic/chat/{sessionId}
```

#### 메시지 전송

```
SEND /app/chat/{sessionId}
Content-Type: application/json

{
  "message": "안녕하세요!"
}
```

#### 메시지 수신

```json
{
  "id": 1,
  "sessionId": 1,
  "senderId": 1,
  "senderName": "홍길동",
  "message": "안녕하세요!",
  "messageType": "CHAT",
  "createdAt": "2025-12-10T10:00:00"
}
```

---

### 7.5 신고 API

#### 사용자 신고

**Request**
```http
POST /v1/reports
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "reportedStudentId": 2,
  "reason": "HARASSMENT",
  "description": "부적절한 언행"
}
```

**Response** `201 Created`
```json
{
  "reportId": 1,
  "reportedStudentId": 2,
  "reason": "HARASSMENT",
  "status": "PENDING",
  "createdAt": "2025-12-10T10:00:00"
}
```

---

### 7.6 에러 응답

모든 에러는 다음 형식으로 반환됩니다:

```json
{
  "code": "ME_002",
  "message": "매칭 큐를 찾을 수 없습니다.",
  "status": 404,
  "timestamp": "2025-12-10T10:00:00"
}
```

---

### 7.7 Swagger UI 사용

`http://localhost:40000/swagger-ui.html`에 접속하면 모든 API를 시각적으로 확인하고 테스트할 수 있습니다.

1. **Authorize** 버튼 클릭
2. `Bearer {accessToken}` 입력
3. 각 API의 **"Try it out"** 버튼으로 테스트

---

## 9. 데이터베이스 스키마

### 8.1 주요 테이블 구조

#### STUDENT - 학생 정보

| Column Name | Type | Null | Key |
|-------------|------|------|-----|
| student_id | NUMBER(19) | NOT NULL | PRIMARY KEY |
| email | VARCHAR2(255) | NOT NULL | UNIQUE |
| password | VARCHAR2(255) | NOT NULL | |
| student_number | VARCHAR2(255) | NOT NULL | UNIQUE |
| name | VARCHAR2(255) | NOT NULL | |
| age | NUMBER(10) | NOT NULL | |
| gender | VARCHAR2(1) | NOT NULL | CHECK(M/F) |
| department | VARCHAR2(255) | NOT NULL | |
| mbti | VARCHAR2(4) | | |
| profile | CLOB | | |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

#### GROUPS - 그룹 정보

| Column Name | Type | Null | Key |
|-------------|------|------|-----|
| group_id | NUMBER(19) | NOT NULL | PRIMARY KEY |
| gender | VARCHAR2(1) | NOT NULL | |
| status | VARCHAR2(50) | NOT NULL | |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

#### MATCHING_QUEUE - 매칭 대기열

| Column Name | Type | Null | Key |
|-------------|------|------|-----|
| queue_id | NUMBER(19) | NOT NULL | PRIMARY KEY |
| group_id | NUMBER(19) | NOT NULL | FK → GROUPS |
| matching_type | VARCHAR2(50) | NOT NULL | |
| matching_status | VARCHAR2(50) | NOT NULL | |
| queued_at | TIMESTAMP | NOT NULL | |
| matched_at | TIMESTAMP | | |

#### MATCHING_SESSION - 매칭 세션

| Column Name | Type | Null | Key |
|-------------|------|------|-----|
| session_id | NUMBER(19) | NOT NULL | PRIMARY KEY |
| status | VARCHAR2(50) | NOT NULL | |
| created_at | TIMESTAMP | NOT NULL | |
| ended_at | TIMESTAMP | | |

#### CHAT_MESSAGE - 채팅 메시지

| Column Name | Type | Null | Key |
|-------------|------|------|-----|
| message_id | NUMBER(19) | NOT NULL | PRIMARY KEY |
| session_id | NUMBER(19) | NOT NULL | FK → SESSION |
| sender_id | NUMBER(19) | NOT NULL | FK → STUDENT |
| message | CLOB | NOT NULL | |
| message_type | VARCHAR2(50) | NOT NULL | |
| created_at | TIMESTAMP | NOT NULL | |

---

### 8.2 ERD (Entity Relationship Diagram)

```
STUDENT ───────┐
  │            │
  │ 1        * │
  │            │
STUDENT_GROUP  │
  │            │
  │ *        1 │
  │            │
GROUPS ────────┤
  │            │
  │ 1        1 │
  │            │
MATCHING_QUEUE │
  │            │
  │ *        1 │
  │            │
SESSION_MATCHES│
  │            │
  │ 1        1 │
  │            │
MATCHING_SESSION
  │
  │ 1
  │
  │ 1
  │
CHAT_ROOM
  │
  │ 1
  │
  │ *
  │
CHAT_MESSAGE
```

---

### 8.3 제약조건 (Constraints)

#### Primary Key
✅ 모든 테이블에 ID 컬럼을 PRIMARY KEY로 설정

#### Foreign Key
✅ 모든 관계에 ON DELETE CASCADE 또는 RESTRICT 설정  
✅ 참조 무결성 자동 보장

#### Unique Constraint
✅ `STUDENT.email`  
✅ `STUDENT.student_number`  
✅ `MATCHING_QUEUE.group_id`

#### Check Constraint
✅ `STUDENT.gender IN ('M', 'F')`  
✅ `STUDENT.age BETWEEN 18 AND 30`

#### Composite Key
✅ `STUDENT_GROUP(student_id, group_id)`  
✅ `SESSION_END_VOTE(session_id, student_id)`

#### Not Null
✅ 모든 필수 필드에 NOT NULL 설정

---

## 10. 동시성 제어 및 트랜잭션 처리

### 9.1 트랜잭션 격리 수준 (Isolation Level)

#### READ_COMMITTED - 기본 격리 수준
✅ `registerQueue` (매칭 등록)  
✅ `signUp` (회원가입)  
✅ `saveMessage` (채팅 메시지)  
→ 비관적 락으로 충분, 성능 우선

#### SERIALIZABLE - 높은 격리 수준
✅ `processMatchingRequest` (매칭 처리)  
✅ `voteEndSession` (세션 종료 투표)  
✅ `addMember` (그룹 멤버 추가)  
→ 정확성 최우선, Phantom Read 방지

---

### 9.2 비관적 락 (Pessimistic Lock)

#### 적용 대상
✅ Group 조회 시: `findByIdWithLock()`  
✅ MatchingQueue 조회 시: `findByIdWithLock()`, `findWaitingQueuesWithLock()`  
✅ MatchingSession 조회 시: `findByIdWithLock()`

#### Oracle SQL 변환
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
  ↓
SELECT ... FOR UPDATE
```

#### 효과
- 해당 행에 배타적 락 획득
- 다른 트랜잭션은 대기
- 커밋/롤백 시 자동 락 해제

---

### 9.3 데이터베이스 제약조건 활용

#### 중복 방지
✅ `UNIQUE(email, student_number)` → 회원가입 중복 차단  
✅ Composite Key → 중복 투표, 중복 그룹 참여 차단

#### 무결성 보장
✅ Foreign Key → 참조 무결성  
✅ NOT NULL → 필수 데이터  
✅ CHECK → 도메인 무결성

---

### 9.4 Redis 원자적 연산

#### 초대 코드
- `SET NX (Not eXists)`: 중복 코드 생성 방지
- `TTL 1시간`: 자동 만료

#### 채팅 버퍼
- `LPUSH`: 메시지 추가
- `LRANGE`: 메시지 조회
- `DEL`: 플러시 후 삭제

---

### 9.5 RabbitMQ 순차 처리

#### 선입선출 (FIFO)
- Direct Exchange + Routing Key
- 메시지 도착 순서대로 처리
- Consumer 1개로 순차 처리 보장

#### 재시도 메커니즘
- 트랜잭션 실패 시 자동 재시도
- Deadlock 발생 시 재전송

---

### 9.6 동시성 문제 해결 사례

| 문제 | Before | After |
|------|--------|-------|
| **그룹 인원 초과** | ❌ 동시 참여 시 3명 초과 가능 | ✅ SERIALIZABLE + 비관적 락 → 정확히 3명까지만 |
| **중복 매칭** | ❌ 하나의 큐가 여러 세션에 매칭 | ✅ SERIALIZABLE + 비관적 락 + 상태 재확인 → 1:1 매칭 |
| **투표 카운팅 오류** | ❌ 동시 투표 시 집계 오류 | ✅ SERIALIZABLE + 복합 키 → 정확한 카운팅 |
| **초대 코드 충돌** | ❌ 동일 코드가 여러 그룹에 할당 | ✅ Redis SET NX → 중복 없는 생성 |
| **채팅 메시지 손실** | ❌ 대량 메시지 시 DB 병목 | ✅ Redis 버퍼링 + 주기 플러시 → 손실 방지 |

> 💡 **자세한 내용은 `Team5-Additional_task1.txt` 참조**

---

## 11. 유의사항 및 문제 해결

### 11.1 사용 시 유의사항

⚠️ **포트 충돌**
- 40000-40004 포트가 사용 중이면 충돌 발생
- `docker-compose.yml`에서 포트 변경 필요

⚠️ **Oracle 초기화 시간**
- 첫 시작 시 60-90초 소요
- `docker compose logs -f oracle-db`로 초기화 완료 확인

⚠️ **메모리 부족**
- 최소 8GB RAM 필요
- Docker Desktop 메모리 설정 확인

⚠️ **JWT 토큰 만료**
- Access Token: 5시간 (18000000ms)
- Refresh Token: 50시간 (180000000ms)
- 만료 시 재로그인 또는 Refresh Token으로 갱신

⚠️ **매칭 대기 시간**
- 반대 성별의 대기열이 없으면 매칭 안 됨
- 충분한 사용자 필요

⚠️ **세션 종료**
- 한 그룹의 만장일치로만 종료 가능
- 상대방 그룹과 무관

---

### 11.2 자주 발생하는 문제

#### 문제: Docker Compose 실행 실패
- **원인**: Docker가 실행 중이지 않음
- **해결**: Docker Desktop 실행 후 재시도

#### 문제: Oracle 연결 실패
- **원인**: 초기화 미완료
- **해결**: `docker compose logs -f oracle-db`로 "DATABASE IS READY" 확인

#### 문제: 포트 사용 중 에러
- **원인**: 이미 해당 포트를 다른 프로그램이 사용 중
- **해결**:
  - macOS/Linux: `lsof -i :40000`
  - Windows: `netstat -ano | findstr :40000`
  - 프로세스 종료 또는 포트 변경

#### 문제: 매칭이 안 됨
- **원인**: 반대 성별의 대기열이 없음
- **해결**: 남성/여성 그룹을 각각 생성하여 테스트

#### 문제: WebSocket 연결 실패
- **원인**: JWT 토큰 누락 또는 만료
- **해결**: Authorization 헤더에 유효한 토큰 포함

#### 문제: 채팅 메시지가 저장 안 됨
- **원인**: Redis 연결 실패
- **해결**: `docker compose ps`로 valkey 상태 확인

---

### 11.3 디버깅 방법

#### 로그 레벨 조정
`application.yaml`에서 LOG_LEVEL 설정
- `DEBUG`: 상세 로그 (개발)
- `INFO`: 일반 로그 (운영)

#### 데이터베이스 쿼리 확인
```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

#### RabbitMQ 메시지 확인
`http://localhost:40003` 접속
- Username: `goukon`
- Password: `goukon123`
- Queues 탭에서 메시지 확인

#### Redis 데이터 확인
```bash
docker exec -it goukon-valkey valkey-cli
> KEYS invite:*
> GET invite:code:123456
```

#### Oracle 데이터베이스 접속
```bash
docker exec -it goukon-oracle sqlplus system/goukon123@XEPDB1
```

---

### 11.4 성능 최적화 팁

✅ **JVM 메모리 설정**
- `-XX:MaxRAMPercentage=75.0`
- 컨테이너 메모리의 75% 사용

✅ **Connection Pool 설정**
- HikariCP 기본 설정 사용
- `maximum-pool-size: 10`

✅ **Redis 캐시 활용**
- 초대 코드는 Redis에만 저장 (DB 부하 감소)
- 채팅 메시지 버퍼링

✅ **비동기 처리**
- RabbitMQ로 매칭 처리 분리
- API 응답 속도 10배 향상

✅ **인덱스 최적화**
- Foreign Key에 자동 인덱스 생성
- ORDER BY 컬럼에 인덱스 추가

---

### 11.5 보안 권장사항

⚠️ **환경 변수로 민감 정보 관리**
- JWT_SECRET, DB_PASSWORD 등
- `.env` 파일 사용 (Git에 커밋 금지)

⚠️ **비밀번호 암호화**
- BCrypt 사용 (기본 적용됨)

⚠️ **SQL Injection 방지**
- JPA/Hibernate 사용으로 자동 방지
- 직접 SQL 작성 시 PreparedStatement 사용

⚠️ **XSS 방지**
- 프론트엔드에서 입력 검증 및 이스케이프

⚠️ **CORS 설정**
- 운영 환경에서는 특정 도메인만 허용

---

## 12. 데모 동영상

### 📹 YouTube 데모 영상 링크

🔗 **[Goukon Server - 전체 기능 데모]**  
> [https://youtu.be/94WlR2rkBzY](https://youtu.be/YOUR_VIDEO_ID_HERE)

---


### 📹 데모 환경

- **OS**: macOS Ventura 13.5
- **Docker Desktop**: 24.0.6
- **Java**: OpenJDK 21
- **브라우저**: Chrome 120
- **API 테스트**: Postman 10.18

---

### 📹 스크린샷

데모 영상의 주요 화면 캡처는 프로젝트 저장소의 `screenshots/` 폴더에 저장되어 있습니다.

---

## 13. 팀원 소개

### 👥 Team5 - 백엔드 개발팀

#### 백엔드 개발자
**안선우 (Sunwoo An)**
- **GitHub**: [@sunja1472](https://github.com/sunja1472)
- **Email**: sunja1472@knu.ac.kr

#### 백엔드 개발자
**장보형 (Bohyung Jang)**
- **GitHub**: [@jbh010204](https://github.com/jbh010204)
- **Email**: jbh010204@knu.ac.kr

#### 백엔드 개발자
**이상민 (Sangmin Lee)**
- **GitHub**: [@lsmin3388](https://github.com/lsmin3388)
- **Email**: lsmin3388@knu.ac.kr

---

### 📞 연락처

- **프로젝트 저장소**: [https://github.com/2025-COMP0322/goukon-server](https://github.com/2025-COMP0322/goukon-server)
- **이슈 트래커**: [https://github.com/2025-COMP0322/goukon-server/issues](https://github.com/2025-COMP0322/goukon-server/issues)

---

### 🙏 감사의 말

본 프로젝트는 **2025-2학기 데이터베이스 수업**의 일환으로 제작되었습니다.  
프로젝트를 진행하며 Oracle 데이터베이스의 트랜잭션 관리, 동시성 제어, 분산 시스템 설계 등 많은 것을 배울 수 있었습니다.

지도해주신 교수님과 조언해주신 조교님들께 감사드립니다.

---

## 부록: 주요 파일 위치

### 설정 파일
- `application.yaml` - Spring Boot 설정
- `.env` - 환경 변수 설정 (docker compose 실행 시 필수)
- `docker-compose.yml` - Docker Compose 설정
- `Dockerfile` - Docker 이미지 빌드 설정
- `build.gradle` - Gradle 빌드 설정

### 문서 파일
- `README.md` - 프로젝트 개요
- `Team5-README.md` - 본 파일 (상세 사용 설명서)
- `Team5-Additional_task1.txt` - 동시성 제어 상세 문서
- `API_SPECIFICATION.md` - API 명세서
- `docs/TRANSACTION_AND_CONCURRENCY.md` - 트랜잭션 전략 문서
- `docs/DEVELOPMENT_PLAN.md` - 개발 계획서

### 소스 코드
```
src/main/java/com/kr/goukon/
├── presentation/       # Controller (API 엔드포인트)
├── application/        # Service (비즈니스 로직)
├── domain/             # Entity, Repository (도메인)
└── global/             # Config, Exception, Security
```

### 테스트
- `src/test/java/` - 단위 테스트

---

## 라이선스

이 프로젝트는 교육 목적으로 제작되었습니다.

---

**작성자**: Team5 (안선우, 장보형, 이상민)  
**최종 수정일**: 2025년 12월 10일  
**버전**: 1.0.0

이 문서에 대한 질문이나 피드백은 [GitHub Issues](https://github.com/2025-COMP0322/goukon-server/issues)에 등록해주세요.

**감사합니다!** 🙏
