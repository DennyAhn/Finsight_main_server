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

### 1. 인증 헤더
JWT 토큰이 구현되면 다음 헤더를 포함해야 합니다:
```http
Authorization: Bearer {access_token}
```

### 2. 요청 제한
- API 호출 빈도 제한: 분당 100회
- 파일 업로드 크기 제한: 10MB

### 3. 응답 시간
- 일반 API: 200ms 이하
- 복잡한 쿼리: 1초 이하

### 4. 로깅
모든 API 호출은 로그에 기록되며, 민감한 정보는 마스킹 처리됩니다.

---

**API 문서 버전**: 1.0.0  
**최종 업데이트**: 2024-01-15  
**문서 작성자**: FinTech 교육 플랫폼 개발팀
