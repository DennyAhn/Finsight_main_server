# API 문서

## 📋 개요

FinTech 교육 플랫폼 서버의 모든 API 엔드포인트에 대한 상세 문서입니다.

- **Base URL**: `http://localhost:8081/api`
- **Content-Type**: `application/json`
- **인코딩**: UTF-8

## 🔐 인증 API

### 1. 회원가입
```http
POST /api/auth/signup
```

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "nickname": "사용자닉네임"
}
```

**Response (200 OK):**
```
회원가입이 성공적으로 완료되었습니다.
```

**Error Response (400 Bad Request):**
- 이메일 중복: `이미 가입된 이메일입니다: user@example.com`
- 닉네임 중복: `이미 사용 중인 닉네임입니다: 사용자닉네임`

### 2. 로그인
```http
POST /api/auth/login
```

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "임시_액세스_토큰_입니다._나중에_JWT로_교체하세요."
}
```

### 3. 게스트 로그인
```http
POST /api/auth/guest
```

**Request Body:** 없음

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "userId": 123
}
```

## 🏥 헬스 체크 API

### 1. 서버 상태 확인
```http
GET /api/health
```

**Response (200 OK):**
```json
{
  "status": "UP",
  "timestamp": "2024-01-15T14:30:00",
  "service": "fin-main-server",
  "version": "1.0.0"
}
```

### 2. 핑 테스트
```http
GET /api/health/ping
```

**Response (200 OK):**
```
pong
```

## 📝 오답 노트 API

### 1. 오답 노트 목록 조회
```http
GET /api/wrong-notes?userId={userId}&page={page}&size={size}&filter={filter}
```

**Query Parameters:**
- `userId`: 사용자 ID (필수)
- `page`: 페이지 번호 (기본값: 0)
- `size`: 페이지 크기 (기본값: 20)
- `filter`: 필터 타입 (`all`, `unresolved`, `resolved`, `needreview`)

**Response (200 OK):**
```json
{
  "wrongNotes": [
    {
      "id": 1,
      "userId": 52,
      "questionId": 1,
      "questionText": "'예금'과 '적금'의 사전적 뜻으로 가장 옳은 것은?",
      "lastAnswerOptionId": 2,
      "lastAnswerText": "예금은 매달 돈을 내는 것이고, 적금은 한 번에 맡기는 것이다.",
      "correctOptionId": 1,
      "correctAnswerText": "예금은 은행에 돈을 한 번에 맡기는 것이고, 적금은 일정 기간 동안 나눠서 돈을 넣는 것이다.",
      "timesWrong": 2,
      "firstWrongAt": "2024-01-15T10:30:00",
      "lastWrongAt": "2024-01-16T14:20:00",
      "reviewedAt": null,
      "resolved": false,
      "personalNoteMd": "예금과 적금의 차이점을 정확히 기억하자",
      "snapshotTeachingSummaryMd": "예금은 목돈을 한번에, 적금은 매달 일정 금액을",
      "snapshotTeachingExplainerMd": "예금과 적금의 핵심 차이점 설명...",
      "snapshotKeypointsMd": "핵심 포인트: 돈을 넣는 방식의 차이",
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-16T14:20:00",
      "quizTitle": "예금과 적금 개념 퀴즈",
      "allOptions": [
        {
          "id": 1,
          "text": "예금은 은행에 돈을 한 번에 맡기는 것이고, 적금은 일정 기간 동안 나눠서 돈을 넣는 것이다.",
          "isCorrect": true
        }
      ]
    }
  ],
  "statistics": {
    "totalCount": 5,
    "unresolvedCount": 3,
    "resolvedCount": 2,
    "needReviewCount": 3
  },
  "totalPages": 1,
  "currentPage": 0,
  "pageSize": 20
}
```

### 2. 특정 오답 노트 상세 조회
```http
GET /api/wrong-notes/{noteId}?userId={userId}
```

**Path Parameters:**
- `noteId`: 오답 노트 ID

**Query Parameters:**
- `userId`: 사용자 ID (필수)

**Response (200 OK):**
```json
{
  "id": 1,
  "userId": 52,
  "questionId": 1,
  "questionText": "'예금'과 '적금'의 사전적 뜻으로 가장 옳은 것은?",
  "lastAnswerOptionId": 2,
  "lastAnswerText": "예금은 매달 돈을 내는 것이고, 적금은 한 번에 맡기는 것이다.",
  "correctOptionId": 1,
  "correctAnswerText": "예금은 은행에 돈을 한 번에 맡기는 것이고, 적금은 일정 기간 동안 나눠서 돈을 넣는 것이다.",
  "timesWrong": 2,
  "firstWrongAt": "2024-01-15T10:30:00",
  "lastWrongAt": "2024-01-16T14:20:00",
  "reviewedAt": null,
  "resolved": false,
  "personalNoteMd": "예금과 적금의 차이점을 정확히 기억하자",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-16T14:20:00"
}
```

### 3. 개인 메모 작성/수정
```http
PUT /api/wrong-notes/{noteId}/personal-note?userId={userId}
```

**Path Parameters:**
- `noteId`: 오답 노트 ID

**Query Parameters:**
- `userId`: 사용자 ID (필수)

**Request Body:**
```
예금과 적금의 차이점을 정확히 기억하자. 예금은 목돈을 한번에, 적금은 매달 일정 금액을 넣는 방식이다.
```

**Response (200 OK):**
오답 노트 상세 정보와 동일한 형태

### 4. 해결 상태 토글
```http
PUT /api/wrong-notes/{noteId}/toggle-resolved?userId={userId}
```

**Path Parameters:**
- `noteId`: 오답 노트 ID

**Query Parameters:**
- `userId`: 사용자 ID (필수)

**Response (200 OK):**
오답 노트 상세 정보와 동일한 형태

### 5. 복습 완료 처리
```http
PUT /api/wrong-notes/{noteId}/mark-reviewed?userId={userId}
```

**Path Parameters:**
- `noteId`: 오답 노트 ID

**Query Parameters:**
- `userId`: 사용자 ID (필수)

**Response (200 OK):**
오답 노트 상세 정보와 동일한 형태

### 6. 오답 노트 삭제
```http
DELETE /api/wrong-notes/{noteId}?userId={userId}
```

**Path Parameters:**
- `noteId`: 오답 노트 ID

**Query Parameters:**
- `userId`: 사용자 ID (필수)

**Response (204 No Content)**

### 7. 오답 노트 통계 조회
```http
GET /api/wrong-notes/statistics?userId={userId}
```

**Query Parameters:**
- `userId`: 사용자 ID (필수)

**Response (200 OK):**
```json
{
  "totalCount": 5,
  "unresolvedCount": 3,
  "resolvedCount": 2,
  "needReviewCount": 3
}
```

## 🔧 관리자 오답 노트 통계 API

### 1. 전체 오답 노트 통계
```http
GET /api/admin/wrong-notes/statistics/overall
```

**Response (200 OK):**
```json
{
  "totalWrongNotesCount": 150,
  "totalUniqueUsersCount": 45,
  "sectorStatistics": [
    {
      "sectorId": 1,
      "sectorName": "예적금",
      "sectorSlug": "savings",
      "totalWrongCount": 80,
      "uniqueUsersCount": 25,
      "subsectors": [
        {
          "subsectorId": 1,
          "subsectorName": "예금 상품",
          "subsectorSlug": "deposit-products",
          "totalWrongCount": 40,
          "uniqueUsersCount": 20,
          "levels": [
            {
              "levelId": 1,
              "levelNumber": 1,
              "levelTitle": "기본 개념",
              "totalWrongCount": 25,
              "uniqueUsersCount": 15,
              "quizzes": [
                {
                  "quizId": 1,
                  "quizTitle": "예금과 적금 개념 퀴즈",
                  "totalWrongCount": 15,
                  "uniqueUsersCount": 10,
                  "questions": [
                    {
                      "questionId": 1,
                      "questionText": "'예금'과 '적금'의 사전적 뜻으로 가장 옳은 것은?",
                      "wrongCount": 8,
                      "uniqueUsersCount": 6,
                      "recentWrongUsers": [
                        {
                          "userId": 52,
                          "userNickname": "학습자1",
                          "userEmail": "user1@example.com",
                          "timesWrong": 2,
                          "lastWrongAt": "2024-01-16T14:20:00",
                          "resolved": false
                        }
                      ]
                    }
                  ]
                }
              ]
            }
          ]
        }
      ]
    }
  ]
}
```

### 2. 섹터별 오답 노트 통계
```http
GET /api/admin/wrong-notes/statistics/sector/{sectorId}
```

**Path Parameters:**
- `sectorId`: 섹터 ID

### 3. 서브섹터별 오답 노트 통계
```http
GET /api/admin/wrong-notes/statistics/subsector/{subsectorId}
```

**Path Parameters:**
- `subsectorId`: 서브섹터 ID

### 4. 퀴즈별 오답 노트 통계
```http
GET /api/admin/wrong-notes/statistics/quiz/{quizId}
```

**Path Parameters:**
- `quizId`: 퀴즈 ID

### 5. 관리자 대시보드
```http
GET /api/admin/wrong-notes/dashboard
```

**Response (200 OK):**
전체 통계와 동일한 형태

## 📊 대시보드 API

### 1. 사용자 대시보드 조회
```http
GET /api/dashboard?userId={userId}
```

**Parameters:**
- `userId` (Long, required): 사용자 ID

**Response (200 OK):**
```json
{
  "userInfo": {
    "nickname": "익명의 사용자",
    "currentLevel": "금융기초 1단계",
    "streak": 5,
    "totalScore": 1250,
    "level": 1
  },
  "learningStats": {
    "totalLevelsCompleted": 3,
    "totalQuizzesCompleted": 12,
    "totalQuestionsAnswered": 48,
    "totalMinutesSpent": 240,
    "averageScore": 85.5
  },
  "weeklyProgress": [...],
  "recentActivities": [...],
  "nextLevelRecommendation": {
    "levelId": 4,
    "levelTitle": "금융기초 4단계",
    "subsectorName": "은행업",
    "reason": "현재 레벨 완료",
    "progressPercentage": 100,
    "remainingQuizzes": 0,
    "difficulty": "MEDIUM",
    "estimatedTime": 60,
    "learningGoal": "은행 업무의 기본 원리 이해"
  },
  "currentLevelSession": {
    "sessionId": "level_1_123",
    "levelId": 1,
    "levelTitle": "금융기초 1단계",
    "subsectorName": "은행업",
    "startedAt": "2024-01-15T14:30:00",
    "timeLimit": 3600,
    "timeRemaining": 1800,
    "currentQuizIndex": 2,
    "completedQuizzes": 2,
    "correctAnswers": 2,
    "remainingToPass": 1,
    "status": "IN_PROGRESS"
  }
}
```

## 🎯 퀴즈 콘텐츠 API

### 1. 모든 섹터 조회
```http
GET /api/sectors
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "은행업",
    "description": "금융기관의 핵심 업무",
    "subsectors": [
      {
        "id": 1,
        "name": "상업은행",
        "description": "일반 고객 대상 금융 서비스"
      }
    ]
  }
]
```

### 2. 서브섹터 상세 정보 조회
```http
GET /api/subsectors/{id}
```

**Parameters:**
- `id` (Long, required): 서브섹터 ID

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "상업은행",
  "description": "일반 고객 대상 금융 서비스",
  "sectorName": "은행업",
  "levels": [
    {
      "id": 1,
      "title": "금융기초 1단계",
      "levelNumber": 1,
      "difficulty": "EASY",
      "estimatedTime": 30,
      "learningGoal": "은행 업무의 기본 원리 이해"
    }
  ]
}
```

### 3. 레벨별 퀴즈 상태 조회
```http
GET /api/levels/{levelId}/quizzes?userId={userId}
```

**Parameters:**
- `levelId` (Long, required): 레벨 ID
- `userId` (Long, required): 사용자 ID

**Response (200 OK):**
```json
{
  "levelId": 1,
  "levelTitle": "금융기초 1단계",
  "subsectorName": "은행업",
  "levelNumber": 1,
  "learningGoal": "은행 업무의 기본 원리 이해",
  "difficulty": "EASY",
  "estimatedTime": 30,
  "quizzes": [
    {
      "id": 1,
      "title": "은행의 기본 기능",
      "questionCount": 5,
      "timeLimit": 300,
      "status": "NOT_STARTED",
      "completedAt": null,
      "score": null
    }
  ],
  "totalQuizzes": 4,
  "completedQuizzes": 0,
  "passedQuizzes": 0,
  "overallStatus": "NOT_STARTED"
}
```

## 🧩 퀴즈 API

### 1. 퀴즈 정보 조회
```http
GET /api/quizzes/{id}
```

**Parameters:**
- `id` (Long, required): 퀴즈 ID

**Response (200 OK):**
```json
{
  "id": 1,
  "title": "은행의 기본 기능",
  "description": "은행의 핵심 업무에 대한 이해를 확인합니다",
  "levelId": 1,
  "levelTitle": "금융기초 1단계",
  "subsectorName": "은행업",
  "timeLimit": 300,
  "questions": [
    {
      "id": 1,
      "questionText": "은행의 가장 기본적인 기능은 무엇인가요?",
      "options": [
        {
          "id": 1,
          "optionText": "예금 수취",
          "isCorrect": true
        },
        {
          "id": 2,
          "optionText": "투자 상담",
          "isCorrect": false
        }
      ]
    }
  ],
  "totalQuestions": 5,
  "passingScore": 3
}
```

### 2. 답안 제출
```http
POST /api/quizzes/submit-answer
```

**Request Body:**
```json
{
  "quizId": 1,
  "userId": 123,
  "answers": [
    {
      "questionId": 1,
      "selectedOptionId": 1
    },
    {
      "questionId": 2,
      "selectedOptionId": 3
    }
  ]
}
```

**Response (200 OK):**
```json
{
  "quizId": 1,
  "userId": 123,
  "correctAnswers": 3,
  "totalQuestions": 5,
  "score": 3,
  "isPassed": true,
  "timeSpent": 180,
  "submittedAt": "2024-01-15T15:00:00",
  "answers": [
    {
      "questionId": 1,
      "selectedOptionId": 1,
      "isCorrect": true,
      "correctOptionId": 1
    }
  ]
}
```

### 3. 퀴즈 결과 조회
```http
GET /api/quizzes/{id}/result?userId={userId}
```

**Parameters:**
- `id` (Long, required): 퀴즈 ID
- `userId` (Long, required): 사용자 ID

**Response (200 OK):**
```json
{
  "quizId": 1,
  "userId": 123,
  "correctAnswers": 3,
  "totalQuestions": 5,
  "score": 3,
  "isPassed": true,
  "timeSpent": 180,
  "completedAt": "2024-01-15T15:00:00"
}
```

### 4. 퀴즈 완료 처리
```http
POST /api/quizzes/{id}/complete?userId={userId}
```

**Parameters:**
- `id` (Long, required): 퀴즈 ID
- `userId` (Long, required): 사용자 ID

**Response (200 OK):**
```json
{
  "quizId": 1,
  "userId": 123,
  "correctAnswers": 3,
  "totalQuestions": 5,
  "score": 3,
  "isPassed": true,
  "timeSpent": 180,
  "completedAt": "2024-01-15T15:00:00"
}
```

## 📈 레벨 관리 API

### 1. 레벨 진행 상황 조회
```http
GET /api/levels/{id}/progress?userId={userId}
```

**Parameters:**
- `id` (Long, required): 레벨 ID
- `userId` (Long, required): 사용자 ID

**Response (200 OK):**
```json
{
  "levelId": 1,
  "levelTitle": "금융기초 1단계",
  "subsectorName": "은행업",
  "levelNumber": 1,
  "learningGoal": "은행 업무의 기본 원리 이해",
  "status": "IN_PROGRESS",
  "completedQuizzes": 2,
  "totalQuizzes": 4,
  "correctAnswers": 2,
  "remainingToPass": 1,
  "startedAt": "2024-01-15T14:30:00",
  "completedAt": null,
  "timeSpent": 600,
  "timeLimit": 3600,
  "quizProgress": [
    {
      "quizId": 1,
      "quizTitle": "은행의 기본 기능",
      "status": "COMPLETED",
      "score": 3,
      "completedAt": "2024-01-15T14:45:00"
    }
  ],
  "nextLevelId": 2,
  "nextLevelTitle": "금융기초 2단계",
  "nextLevelUnlocked": false
}
```

### 2. 레벨 완료 처리
```http
POST /api/levels/{id}/complete?userId={userId}
```

**Parameters:**
- `id` (Long, required): 레벨 ID
- `userId` (Long, required): 사용자 ID

**Response (200 OK):**
```json
{
  "levelId": 1,
  "levelTitle": "금융기초 1단계",
  "isCompleted": true,
  "isPassed": true,
  "correctAnswers": 3,
  "totalQuestions": 4,
  "score": 3,
  "timeSpent": 1200,
  "completedAt": "2024-01-15T15:00:00",
  "nextLevelId": 2,
  "nextLevelTitle": "금융기초 2단계",
  "nextLevelUnlocked": true,
  "pointsEarned": 350,
  "badgeEarned": "Level 1 Master",
  "achievementMessage": "축하합니다! 레벨 1을 완료했습니다!"
}
```

### 3. 레벨 시작
```http
POST /api/levels/{id}/start?userId={userId}
```

**Parameters:**
- `id` (Long, required): 레벨 ID
- `userId` (Long, required): 사용자 ID

**Response (200 OK):**
```json
{
  "message": "Level started successfully",
  "levelId": 1,
  "levelTitle": "금융기초 1단계"
}
```

## ❌ 에러 응답

### 공통 에러 코드

| HTTP 상태 코드 | 설명 | 예시 메시지 |
|---------------|------|-------------|
| 400 | 잘못된 요청 | `Invalid request: {error message}` |
| 401 | 인증 실패 | `Authentication failed` |
| 403 | 권한 없음 | `Access denied` |
| 404 | 리소스 없음 | `Resource not found` |
| 500 | 서버 오류 | `Internal server error: {error message}` |

### 에러 응답 형식
```json
{
  "error": "Error message",
  "timestamp": "2024-01-15T15:00:00",
  "path": "/api/quizzes/1"
}
```

## 🔧 개발자 참고사항

### 1. 인증 헤더 (✅ 구현 완료)
JWT 토큰을 사용한 인증이 구현되었습니다. 다음 헤더를 포함하여 API를 호출하세요:
```http
Authorization: Bearer {access_token}
```

**인증 동작 방식:**
- Authorization 헤더가 있으면 JWT 토큰에서 사용자 ID 자동 추출
- 토큰이 없거나 유효하지 않으면 요청 본문의 `userId` 필드 사용 (Fallback)
- 모든 API는 여전히 토큰 없이도 접근 가능 (테스트 편의성)

### 2. 요청 제한
- API 호출 빈도 제한: 분당 100회
- 파일 업로드 크기 제한: 10MB

### 3. 응답 시간
- 일반 API: 200ms 이하
- 복잡한 쿼리: 1초 이하

### 4. 로깅
모든 API 호출은 로그에 기록되며, 민감한 정보는 마스킹 처리됩니다.

---

**API 문서 버전**: 1.1.0  
**최종 업데이트**: 2025-09-25  
**문서 작성자**: FinTech 안현진

### 📝 v1.1.0 업데이트 내용
- JWT 토큰 인증 시스템 구현 완료
- 사용자 ID 일관성 문제 해결
- 인증 헤더 사용 방법 업데이트