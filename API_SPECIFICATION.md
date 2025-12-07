# goukon-server API 명세서

## 기본 정보

- **Base URL**: `http://localhost:8080`
- **API Version**: `v1`
- **Content-Type**: `application/json`

---

## 인증 (Authentication)

### 인증 방식
- **JWT (JSON Web Token)** 기반 인증
- Access Token과 Refresh Token 발급
- 인증이 필요한 API는 `Authorization` 헤더에 토큰 포함

### 인증 헤더 형식
```
Authorization: Bearer {accessToken}
```

---

## 에러 응답 형식

모든 에러는 JSON 형식으로 반환됩니다:

```json
{
  "code": "AE_002",
  "message": "해당 유저를 찾을 수 없습니다.",
  "status": 404,
  "timestamp": "2025-12-07T10:00:00"
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| code | string | 에러 코드 |
| message | string | 에러 메시지 |
| status | number | HTTP 상태 코드 |
| timestamp | string | 에러 발생 시간 (ISO 8601) |

### HTTP 상태 코드
| 상태 코드 | 설명 |
|-----------|------|
| 200 | 성공 |
| 201 | 생성 성공 |
| 204 | 삭제 성공 (응답 본문 없음) |
| 400 | 잘못된 요청 |
| 401 | 인증 실패 |
| 403 | 권한 없음 |
| 404 | 리소스 없음 |
| 409 | 충돌 (중복 등) |
| 500 | 서버 에러 |

---

# 1. 인증 API

## 1.1 회원가입

### `POST /v1/auth/signup`

회원가입을 진행합니다.

**Request Body**
```json
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

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| email | string | O | 이메일 (이메일 형식) |
| password | string | O | 비밀번호 |
| studentNumber | string | O | 학번 |
| name | string | O | 이름 |
| age | number | O | 나이 (18-30) |
| gender | string | O | 성별 (`M` 또는 `F`) |
| department | string | O | 학과 |
| mbti | string | X | MBTI (예: `INTJ`, `ENFP`) |
| profile | string | X | 자기소개 |

**Response** `201 Created`
```json
{
  "id": 1,
  "studentNumber": "2020123456",
  "name": "홍길동",
  "email": "student@knu.ac.kr",
  "gender": "M",
  "department": "컴퓨터학부",
  "mbti": "INTJ"
}
```

**에러 코드**
| 코드 | 상태 | 설명 |
|------|------|------|
| AE_001 | 400 | MBTI 값이 올바르지 않습니다 |
| AE_004 | 409 | 이미 사용중인 이메일입니다 |
| AE_005 | 409 | 이미 등록된 학번입니다 |

---

## 1.2 로그인

### `POST /v1/auth/login`

로그인하여 JWT 토큰을 발급받습니다.

**Request Body**
```json
{
  "email": "student@knu.ac.kr",
  "password": "password123"
}
```

**Response** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**에러 코드**
| 코드 | 상태 | 설명 |
|------|------|------|
| AE_002 | 404 | 해당 유저를 찾을 수 없습니다 |
| AE_003 | 400 | 비밀번호가 일치하지 않습니다 |

---

## 1.3 비밀번호 변경

### `PATCH /v1/auth/reset`

비밀번호를 변경합니다.

**Headers**: `Authorization: Bearer {accessToken}` (필수)

**Request Body**
```json
{
  "password": "currentPassword",
  "newPassword": "newPassword123"
}
```

**Response** `200 OK`
```json
{
  "message": "비밀번호가 성공적으로 변경되었습니다."
}
```

**에러 코드**
| 코드 | 상태 | 설명 |
|------|------|------|
| AE_002 | 404 | 해당 유저를 찾을 수 없습니다 |
| AE_003 | 400 | 비밀번호가 일치하지 않습니다 |
| TE_002 | 401 | 토큰이 유효하지 않습니다 |
| TE_003 | 401 | 토큰이 만료되었습니다 |

---

# 2. 학생 API

## 2.1 내 프로필 조회

### `GET /v1/students/me`

현재 로그인한 사용자의 프로필을 조회합니다.

**Headers**: `Authorization: Bearer {accessToken}` (필수)

**Response** `200 OK`
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
  "profile": "안녕하세요!",
  "createdAt": "2025-12-07T10:00:00"
}
```

---

## 2.2 프로필 수정

### `PATCH /v1/students/me`

현재 로그인한 사용자의 프로필을 수정합니다.

**Headers**: `Authorization: Bearer {accessToken}` (필수)

**Request Body** (변경하려는 필드만 포함)
```json
{
  "name": "김철수",
  "mbti": "ENFP",
  "profile": "새로운 자기소개",
  "department": "소프트웨어학과"
}
```

**Response** `200 OK`
```json
{
  "id": 1,
  "studentNumber": "2020123456",
  "name": "김철수",
  "email": "student@knu.ac.kr",
  "age": 24,
  "gender": "M",
  "department": "소프트웨어학과",
  "mbti": "ENFP",
  "profile": "새로운 자기소개",
  "createdAt": "2025-12-07T10:00:00"
}
```

---

## 2.3 학생 조회

### `GET /v1/students/{studentId}`

특정 학생의 프로필을 조회합니다.

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| studentId | number | 학생 ID |

**Response** `200 OK`
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
  "profile": "안녕하세요!",
  "createdAt": "2025-12-07T10:00:00"
}
```

**에러 코드**
| 코드 | 상태 | 설명 |
|------|------|------|
| SE_001 | 404 | 학생을 찾을 수 없습니다 |

---

## 2.4 학생 검색

### `GET /v1/students/search`

이름 또는 학번으로 학생을 검색합니다.

**Query Parameters**
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| keyword | string | O | 검색어 (이름 또는 학번) |

**예시**: `GET /v1/students/search?keyword=홍길동`

**Response** `200 OK`
```json
[
  {
    "id": 1,
    "studentNumber": "2020123456",
    "name": "홍길동",
    "email": "student@knu.ac.kr",
    "age": 24,
    "gender": "M",
    "department": "컴퓨터학부",
    "mbti": "INTJ",
    "profile": "안녕하세요!",
    "createdAt": "2025-12-07T10:00:00"
  }
]
```

---

# 3. 그룹 API

## 3.1 그룹 생성

### `POST /v1/groups`

새로운 그룹을 생성합니다. 생성자가 첫 번째 멤버가 됩니다.

**Headers**: `Authorization: Bearer {accessToken}` (필수)

**Request Body**: 없음

**Response** `201 Created`
```json
{
  "id": 1,
  "gender": "M",
  "status": "AVAILABLE",
  "createdAt": "2025-12-07T10:00:00"
}
```

---

## 3.2 그룹 상세 조회

### `GET /v1/groups/{groupId}`

그룹의 상세 정보와 멤버 목록을 조회합니다.

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| groupId | number | 그룹 ID |

**Response** `200 OK`
```json
{
  "id": 1,
  "gender": "M",
  "status": "AVAILABLE",
  "createdAt": "2025-12-07T10:00:00",
  "members": [
    {
      "id": 1,
      "studentNumber": "2020123456",
      "name": "홍길동",
      "email": "student@knu.ac.kr",
      "age": 24,
      "gender": "M",
      "department": "컴퓨터학부",
      "mbti": "INTJ",
      "profile": "안녕하세요!",
      "createdAt": "2025-12-07T10:00:00"
    }
  ]
}
```

**에러 코드**
| 코드 | 상태 | 설명 |
|------|------|------|
| GE_001 | 404 | 그룹을 찾을 수 없습니다 |

---

## 3.3 내 그룹 목록 조회

### `GET /v1/groups/me`

현재 로그인한 사용자가 속한 그룹 목록을 조회합니다.

**Headers**: `Authorization: Bearer {accessToken}` (필수)

**Response** `200 OK`
```json
[
  {
    "id": 1,
    "gender": "M",
    "status": "AVAILABLE",
    "createdAt": "2025-12-07T10:00:00"
  }
]
```

---

## 3.4 그룹에 멤버 추가

### `POST /v1/groups/{groupId}/members`

그룹에 새로운 멤버를 추가합니다. **그룹 멤버만 다른 학생을 초대할 수 있습니다.**

**Headers**: `Authorization: Bearer {accessToken}` (필수)

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| groupId | number | 그룹 ID |

**Request Body**
```json
{
  "studentId": 2
}
```

**Response** `200 OK` (응답 본문 없음)

**에러 코드**
| 코드 | 상태 | 설명 |
|------|------|------|
| GE_001 | 404 | 그룹을 찾을 수 없습니다 |
| GE_002 | 409 | 이미 그룹에 속해있습니다 |
| GE_003 | 403 | 그룹의 멤버가 아닙니다 (요청자) |
| GE_004 | 400 | 그룹 인원이 가득 찼습니다 (최대 3명) |
| GE_005 | 400 | 그룹이 매칭 가능한 상태가 아닙니다 |
| GE_006 | 400 | 그룹의 성별과 일치하지 않습니다 |
| SE_001 | 404 | 학생을 찾을 수 없습니다 |

---

## 3.5 그룹에서 멤버 제거

### `DELETE /v1/groups/{groupId}/members/{studentId}`

그룹에서 특정 멤버를 제거합니다. **그룹 멤버만 다른 멤버를 제거할 수 있습니다.**

**Headers**: `Authorization: Bearer {accessToken}` (필수)

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| groupId | number | 그룹 ID |
| studentId | number | 제거할 학생 ID |

**Response** `200 OK` (응답 본문 없음)

**에러 코드**
| 코드 | 상태 | 설명 |
|------|------|------|
| GE_001 | 404 | 그룹을 찾을 수 없습니다 |
| GE_003 | 403 | 그룹의 멤버가 아닙니다 |
| GE_007 | 400 | 매칭 중에는 그룹을 떠날 수 없습니다 |

---

## 3.6 그룹 나가기

### `DELETE /v1/groups/{groupId}/leave`

현재 로그인한 사용자가 그룹에서 나갑니다.

**Headers**: `Authorization: Bearer {accessToken}` (필수)

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| groupId | number | 그룹 ID |

**Response** `200 OK` (응답 본문 없음)

**에러 코드**
| 코드 | 상태 | 설명 |
|------|------|------|
| GE_001 | 404 | 그룹을 찾을 수 없습니다 |
| GE_003 | 403 | 그룹의 멤버가 아닙니다 |
| GE_007 | 400 | 매칭 중에는 그룹을 떠날 수 없습니다 |

---

## 3.7 그룹 삭제

### `DELETE /v1/groups/{groupId}`

그룹을 삭제합니다.

**Headers**: `Authorization: Bearer {accessToken}` (필수)

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| groupId | number | 그룹 ID |

**Response** `204 No Content`

**에러 코드**
| 코드 | 상태 | 설명 |
|------|------|------|
| GE_001 | 404 | 그룹을 찾을 수 없습니다 |
| GE_003 | 403 | 그룹의 멤버가 아닙니다 |
| GE_007 | 400 | 매칭 중에는 그룹을 떠날 수 없습니다 |

---

## 그룹 상태 (GroupStatus)

| 상태 | 설명 |
|------|------|
| AVAILABLE | 매칭 가능 (기본 상태) |
| QUEUING | 매칭 대기 중 |
| MATCHED | 매칭 완료 |

---

# 4. 매칭 API

## 4.1 매칭 대기열 등록

### `POST /v1/matching/queue`

그룹을 매칭 대기열에 등록합니다.

**Headers**: `Authorization: Bearer {accessToken}` (필수)

**Request Body**
```json
{
  "groupId": 1,
  "matchingType": "ONE_TO_ONE"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| groupId | number | O | 그룹 ID |
| matchingType | string | O | 매칭 타입 (`ONE_TO_ONE` 또는 `THREE_TO_THREE`) |

**매칭 타입 규칙**
| 타입 | 필요 인원 | 설명 |
|------|----------|------|
| ONE_TO_ONE | 1명 | 1:1 매칭 |
| THREE_TO_THREE | 3명 | 3:3 매칭 |

**Response** `201 Created`
```json
{
  "queueId": 1,
  "groupId": 1,
  "matchingStatus": "WAITING",
  "matchingType": "ONE_TO_ONE",
  "createdAt": "2025-12-07T10:00:00"
}
```

**에러 코드**
| 코드 | 상태 | 설명 |
|------|------|------|
| GE_001 | 404 | 그룹을 찾을 수 없습니다 |
| GE_005 | 400 | 그룹이 매칭 가능한 상태가 아닙니다 |
| ME_002 | 409 | 이미 매칭 대기열에 있습니다 |
| ME_006 | 400 | 유효하지 않은 매칭 타입입니다 |
| ME_007 | 400 | 매칭 타입에 맞는 인원이 아닙니다 |

---

## 4.2 매칭 취소

### `DELETE /v1/matching/queue/{groupId}`

매칭 대기열에서 그룹을 제거합니다.

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| groupId | number | 그룹 ID |

**Response** `200 OK` (응답 본문 없음)

**에러 코드**
| 코드 | 상태 | 설명 |
|------|------|------|
| ME_001 | 404 | 매칭 대기열을 찾을 수 없습니다 |
| ME_008 | 409 | 이미 매칭이 진행중입니다 |

---

## 4.3 대기열 상태 조회

### `GET /v1/matching/queue/{groupId}`

그룹의 매칭 대기열 상태를 조회합니다.

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| groupId | number | 그룹 ID |

**Response** `200 OK`
```json
{
  "queueId": 1,
  "groupId": 1,
  "matchingStatus": "WAITING",
  "matchingType": "ONE_TO_ONE",
  "createdAt": "2025-12-07T10:00:00"
}
```

**매칭 상태 (MatchingStatus)**
| 상태 | 설명 |
|------|------|
| WAITING | 대기 중 |
| MATCHED | 매칭 완료 |
| CANCELED | 취소됨 |

**에러 코드**
| 코드 | 상태 | 설명 |
|------|------|------|
| ME_001 | 404 | 매칭 대기열을 찾을 수 없습니다 |

---

## 4.4 내 매칭 세션 목록

### `GET /v1/matching/sessions/me`

현재 로그인한 사용자의 매칭 세션 목록을 조회합니다.

**Headers**: `Authorization: Bearer {accessToken}` (필수)

**Response** `200 OK`
```json
[
  {
    "sessionId": 1,
    "groupId": 1,
    "queueId": 1,
    "createdAt": "2025-12-07T10:00:00"
  }
]
```

---

# 5. 채팅 API

## 5.1 내 채팅방 목록

### `GET /v1/chatrooms/me`

현재 로그인한 사용자의 채팅방 목록을 조회합니다.

**Headers**: `Authorization: Bearer {accessToken}` (필수)

**Response** `200 OK`
```json
[
  {
    "sessionId": 1,
    "createdAt": "2025-12-07T10:00:00"
  }
]
```

---

## 5.2 채팅방 조회

### `GET /v1/chatrooms/{sessionId}`

특정 채팅방의 정보를 조회합니다.

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| sessionId | number | 세션 ID |

**Response** `200 OK`
```json
{
  "sessionId": 1,
  "createdAt": "2025-12-07T10:00:00"
}
```

**에러 코드**
| 코드 | 상태 | 설명 |
|------|------|------|
| CE_001 | 404 | 채팅방을 찾을 수 없습니다 |

---

## 5.3 메시지 목록 조회

### `GET /v1/chatrooms/{sessionId}/messages`

채팅방의 전체 메시지를 조회합니다.

**Headers**: `Authorization: Bearer {accessToken}` (필수)

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| sessionId | number | 세션 ID |

**Response** `200 OK`
```json
[
  {
    "sessionId": 1,
    "senderId": 1,
    "senderName": "홍길동",
    "messageId": 1,
    "content": "안녕하세요!",
    "createdAt": "2025-12-07T10:00:00"
  }
]
```

**에러 코드**
| 코드 | 상태 | 설명 |
|------|------|------|
| CE_001 | 404 | 채팅방을 찾을 수 없습니다 |
| CE_002 | 403 | 채팅방의 멤버가 아닙니다 |

---

## 5.4 메시지 목록 조회 (페이징)

### `GET /v1/chatrooms/{sessionId}/messages/paged`

채팅방의 메시지를 페이징하여 조회합니다.

**Headers**: `Authorization: Bearer {accessToken}` (필수)

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| sessionId | number | 세션 ID |

**Query Parameters**
| 파라미터 | 타입 | 기본값 | 설명 |
|----------|------|--------|------|
| page | number | 0 | 페이지 번호 (0부터 시작) |
| size | number | 20 | 페이지당 메시지 수 |
| sort | string | - | 정렬 기준 (예: `createdAt,desc`) |

**예시**: `GET /v1/chatrooms/1/messages/paged?page=0&size=20`

**Response** `200 OK`
```json
{
  "content": [
    {
      "sessionId": 1,
      "senderId": 1,
      "senderName": "홍길동",
      "messageId": 1,
      "content": "안녕하세요!",
      "createdAt": "2025-12-07T10:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 5,
  "totalElements": 100,
  "last": false,
  "first": true,
  "size": 20,
  "number": 0,
  "numberOfElements": 20,
  "empty": false
}
```

**에러 코드**
| 코드 | 상태 | 설명 |
|------|------|------|
| CE_001 | 404 | 채팅방을 찾을 수 없습니다 |
| CE_002 | 403 | 채팅방의 멤버가 아닙니다 |

---

## 5.5 메시지 전송

### `POST /v1/chatrooms/{sessionId}/messages`

채팅방에 메시지를 전송합니다.

**Headers**: `Authorization: Bearer {accessToken}` (필수)

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| sessionId | number | 세션 ID |

**Request Body** (plain text)
```
"메시지 내용"
```

**Response** `200 OK`
```json
{
  "sessionId": 1,
  "senderId": 1,
  "senderName": "홍길동",
  "messageId": 2,
  "content": "메시지 내용",
  "createdAt": "2025-12-07T10:05:00"
}
```

**에러 코드**
| 코드 | 상태 | 설명 |
|------|------|------|
| CE_001 | 404 | 채팅방을 찾을 수 없습니다 |
| CE_002 | 403 | 채팅방의 멤버가 아닙니다 |
| SE_001 | 404 | 학생을 찾을 수 없습니다 |
| MSE_002 | 400 | 메시지 내용이 비어있습니다 |

---

# 6. 신고 API

## 6.1 신고 접수

### `POST /v1/reports`

새로운 신고를 접수합니다.

**Headers**: `Authorization: Bearer {accessToken}` (필수)

**Request Body**
```json
{
  "title": "부적절한 메시지",
  "content": "상대방이 욕설을 사용했습니다..."
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| title | string | O | 신고 제목 |
| content | string | O | 신고 내용 |

**Response** `201 Created`
```json
{
  "id": 1,
  "reporterId": 1,
  "reporterName": "홍길동",
  "title": "부적절한 메시지",
  "content": "상대방이 욕설을 사용했습니다...",
  "status": "PENDING",
  "createdAt": "2025-12-07T10:00:00"
}
```

**에러 코드**
| 코드 | 상태 | 설명 |
|------|------|------|
| SE_001 | 404 | 학생을 찾을 수 없습니다 |
| RE_002 | 400 | 신고 내용이 비어있습니다 |

---

## 6.2 신고 조회

### `GET /v1/reports/{reportId}`

특정 신고의 상세 정보를 조회합니다.

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| reportId | number | 신고 ID |

**Response** `200 OK`
```json
{
  "id": 1,
  "reporterId": 1,
  "reporterName": "홍길동",
  "title": "부적절한 메시지",
  "content": "상대방이 욕설을 사용했습니다...",
  "status": "PENDING",
  "createdAt": "2025-12-07T10:00:00"
}
```

**에러 코드**
| 코드 | 상태 | 설명 |
|------|------|------|
| RE_001 | 404 | 신고를 찾을 수 없습니다 |

---

## 6.3 내 신고 목록

### `GET /v1/reports/me`

현재 로그인한 사용자가 접수한 신고 목록을 조회합니다.

**Headers**: `Authorization: Bearer {accessToken}` (필수)

**Response** `200 OK`
```json
[
  {
    "id": 1,
    "reporterId": 1,
    "reporterName": "홍길동",
    "title": "부적절한 메시지",
    "content": "상대방이 욕설을 사용했습니다...",
    "status": "PENDING",
    "createdAt": "2025-12-07T10:00:00"
  }
]
```

---

## 6.4 전체 신고 목록 (관리자)

### `GET /v1/reports`

모든 신고 목록을 조회합니다. 상태로 필터링 가능합니다. **관리자 권한이 필요합니다.**

**Headers**: `Authorization: Bearer {accessToken}` (필수, 관리자)

**Query Parameters**
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| status | string | X | 신고 상태 필터 |

**예시**: `GET /v1/reports?status=PENDING`

**Response** `200 OK`
```json
[
  {
    "id": 1,
    "reporterId": 1,
    "reporterName": "홍길동",
    "title": "부적절한 메시지",
    "content": "상대방이 욕설을 사용했습니다...",
    "status": "PENDING",
    "createdAt": "2025-12-07T10:00:00"
  }
]
```

**에러 코드**
| 코드 | 상태 | 설명 |
|------|------|------|
| CE_004 | 403 | 관리자 권한이 필요합니다 |
| RE_003 | 400 | 유효하지 않은 신고 상태입니다 |

---

## 6.5 신고 상태 변경 (관리자)

### `PATCH /v1/reports/{reportId}/status`

신고의 상태를 변경합니다. **관리자 권한이 필요합니다.**

**Headers**: `Authorization: Bearer {accessToken}` (필수, 관리자)

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| reportId | number | 신고 ID |

**Request Body**
```json
{
  "status": "REVIEWING"
}
```

**신고 상태 (ReportStatus)**
| 상태 | 설명 |
|------|------|
| PENDING | 대기중 |
| REVIEWING | 검토중 |
| RESOLVED | 처리완료 |
| REJECTED | 반려 |

**Response** `200 OK`
```json
{
  "id": 1,
  "reporterId": 1,
  "reporterName": "홍길동",
  "title": "부적절한 메시지",
  "content": "상대방이 욕설을 사용했습니다...",
  "status": "REVIEWING",
  "createdAt": "2025-12-07T10:00:00"
}
```

**에러 코드**
| 코드 | 상태 | 설명 |
|------|------|------|
| CE_004 | 403 | 관리자 권한이 필요합니다 |
| RE_001 | 404 | 신고를 찾을 수 없습니다 |
| RE_003 | 400 | 유효하지 않은 신고 상태입니다 |

---

# 부록: 전체 에러 코드 목록

## 토큰 에러 (TE)
| 코드 | 상태 | 메시지 |
|------|------|--------|
| TE_001 | 404 | 토큰이 없습니다 |
| TE_002 | 401 | 토큰이 유효하지않습니다 |
| TE_003 | 401 | 토큰이 만료되었습니다 |
| TE_004 | 401 | 유효하지 않은 Authorization Header 입니다 |

## 인증 에러 (AE)
| 코드 | 상태 | 메시지 |
|------|------|--------|
| AE_001 | 400 | MBTI 값이 올바르지 않습니다 |
| AE_002 | 404 | 해당 유저를 찾을 수 없습니다 |
| AE_003 | 400 | 비밀번호가 일치하지 않습니다 |
| AE_004 | 409 | 이미 사용중인 이메일입니다 |
| AE_005 | 409 | 이미 등록된 학번입니다 |

## 학생 에러 (SE)
| 코드 | 상태 | 메시지 |
|------|------|--------|
| SE_001 | 404 | 학생을 찾을 수 없습니다 |
| SE_002 | 400 | 나이는 18세 이상 30세 이하여야 합니다 |

## 그룹 에러 (GE)
| 코드 | 상태 | 메시지 |
|------|------|--------|
| GE_001 | 404 | 그룹을 찾을 수 없습니다 |
| GE_002 | 409 | 이미 그룹에 속해있습니다 |
| GE_003 | 403 | 그룹의 멤버가 아닙니다 |
| GE_004 | 400 | 그룹 인원이 가득 찼습니다 |
| GE_005 | 400 | 그룹이 매칭 가능한 상태가 아닙니다 |
| GE_006 | 400 | 그룹의 성별과 일치하지 않습니다 |
| GE_007 | 400 | 매칭 중에는 그룹을 떠날 수 없습니다 |

## 매칭 에러 (ME)
| 코드 | 상태 | 메시지 |
|------|------|--------|
| ME_001 | 404 | 매칭 대기열을 찾을 수 없습니다 |
| ME_002 | 409 | 이미 매칭 대기열에 있습니다 |
| ME_003 | 400 | 매칭 대기열에 없습니다 |
| ME_004 | 404 | 매칭 세션을 찾을 수 없습니다 |
| ME_005 | 400 | 활성화된 세션이 아닙니다 |
| ME_006 | 400 | 유효하지 않은 매칭 타입입니다 |
| ME_007 | 400 | 매칭 타입에 맞는 인원이 아닙니다 |
| ME_008 | 409 | 이미 매칭이 진행중입니다 |

## 채팅 에러 (CE)
| 코드 | 상태 | 메시지 |
|------|------|--------|
| CE_001 | 404 | 채팅방을 찾을 수 없습니다 |
| CE_002 | 403 | 채팅방의 멤버가 아닙니다 |

## 메시지 에러 (MSE)
| 코드 | 상태 | 메시지 |
|------|------|--------|
| MSE_001 | 404 | 메시지를 찾을 수 없습니다 |
| MSE_002 | 400 | 메시지 내용이 비어있습니다 |

## 신고 에러 (RE)
| 코드 | 상태 | 메시지 |
|------|------|--------|
| RE_001 | 404 | 신고를 찾을 수 없습니다 |
| RE_002 | 400 | 신고 내용이 비어있습니다 |
| RE_003 | 400 | 유효하지 않은 신고 상태입니다 |

## 공통 에러 (CE)
| 코드 | 상태 | 메시지 |
|------|------|--------|
| CE_001 | 500 | 서버 내부 오류가 발생했습니다 |
| CE_002 | 400 | 입력값이 올바르지 않습니다 |
| CE_003 | 409 | 동시 수정이 감지되었습니다. 다시 시도해주세요 |
| CE_004 | 403 | 관리자 권한이 필요합니다 |
| CE_005 | 403 | 그룹장만 수행할 수 있습니다 |

## 검증 에러 (VE)
| 코드 | 상태 | 메시지 |
|------|------|--------|
| VE_001 | 400 | 필드 검증 오류 (상세 메시지 참조) |

---

## 작성일
2025-12-07
