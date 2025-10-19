# 🚀 Finsight API Documentation

> **금융 교육 플랫폼 백엔드 서버** - 포트폴리오용 API 명세서

---

## 📋 프로젝트 개요

### 🎯 서비스 소개
**Finsight**는 금융 교육을 위한 인터랙티브 퀴즈 플랫폼입니다. 사용자가 단계별로 금융 지식을 학습하고, 실시간 채점과 오답 노트를 통해 효과적인 학습을 지원합니다.

### 🛠️ 기술 스택
- **Backend**: Spring Boot 3.2, Java 17
- **Database**: MySQL 8.0 (AWS RDS)
- **Authentication**: JWT
- **Documentation**: Swagger UI
- **Deployment**: Docker, AWS EC2, Nginx

### 🌐 서버 정보
| 환경 | URL | 상태 |
|------|-----|------|
| **로컬 개발** | `http://localhost:8080/api` | ✅ 활성 |
| **프로덕션** | `https://finsight.o-r.kr/api` | ✅ 활성 |
| **Swagger UI** | `https://finsight.o-r.kr/api/swagger-ui/index.html` | ✅ 활성 |

---

## 🎯 핵심 기능

### 1. 📚 **계층적 학습 구조**
- **섹터** → **서브섹터** → **레벨** → **퀴즈** (4단계 구조)
- 7개 금융 섹터 (은행업, 증권업, 보험업 등)
- 각 레벨당 4개 퀴즈, 각 퀴즈당 4개 문제

### 2. 🧩 **인터랙티브 퀴즈 시스템**
- **실시간 채점**: 답안 제출 즉시 결과 확인
- **다양한 문제 유형**: 개념, 스토리, 가상기사 기반 문제
- **즉시 피드백**: 정답/오답 설명과 학습 포인트 제공

### 3. 📊 **진행률 추적 시스템**
- **징검다리 시스템**: 4문제 중 2문제 이상 통과 시 단계 완료
- **실시간 통계**: 완료율, 통과율, 소요시간 추적
- **다중 서브섹터 지원**: 여러 분야 동시 학습 가능

### 4. 📝 **오답 노트 시스템**
- **자동 생성**: 틀린 문제 자동으로 오답 노트에 추가
- **개인 메모**: 사용자별 학습 노트 작성
- **복습 시스템**: 틀린 문제 재학습 지원

### 5. 🏅 **게이미피케이션**
- **6단계 배지 시스템**: 브론즈 → 실버 → 골드 → 플레티넘 → 다이아 → 마스터
- **진행률 표시**: 각 배지별 달성 조건과 현재 진행도
- **동기부여**: 학습 성취감 극대화

### 6. 💬 **커뮤니티 기능**
- **게시글 작성/조회**: 학습 경험 공유
- **댓글 시스템**: 소통 및 질문/답변
- **좋아요 기능**: 인기 게시글 추천

---

## 🔗 API 엔드포인트

### 🔐 **인증 & 사용자 관리**

#### 회원가입
```http
POST /api/auth/signup
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123!",
  "nickname": "금융초보"
}
```

#### 로그인 (임시 구현)
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123!"
}
```

#### 게스트 로그인 (다중 사용자 지원)
```http
# 새 게스트 계정 생성
POST /api/auth/guest

# 기존 게스트 계정 재사용 (닉네임 유지)
POST /api/auth/guest?userId={userId}
```

<details>
<summary><strong>📋 응답 예시</strong></summary>

```json
{
  "accessToken": "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiIxNDk5IiwiaWF0IjoxNzYwOTA0NzU0LCJleHAiOjE3NjA5OTExNTR9.4kzd0yGdiUEPPi_4KhOUDCp3hupPKfvzvn4lgqJjN7NLcjQkSVlwjdy2kAfbnQpJ",
  "userId": 1499
}
```

</details>

**특징:**
- ✅ **다중 사용자 지원**: 각 브라우저별 독립적인 계정 관리
- ✅ **닉네임 일관성**: 다른 서버 갔다가 돌아와도 같은 닉네임 유지
- ✅ **자동 계정 재사용**: 기존 계정이 있으면 자동으로 재사용
- ✅ **만료 시간 연장**: 사용할 때마다 12시간씩 연장
- ✅ **에러 처리**: 재사용 실패 시 자동으로 새 계정 생성

---

### 🧩 **퀴즈 실행**

#### 퀴즈 문제 조회
```http
GET /api/quizzes/{quizId}
```

<details>
<summary><strong>📋 응답 예시</strong></summary>

```json
{
  "id": 1,
  "title": "금융기초 퀴즈",
  "levelId": 1,
  "questions": [
    {
      "id": 1,
      "stemMd": "1금융권에 해당하는 기관은?",
      "type": "CONCEPT",
      "difficulty": 1,
      "options": [
        {
          "id": 1,
          "label": "A",
          "contentMd": "은행",
          "isCorrect": true
        },
        {
          "id": 2,
          "label": "B", 
          "contentMd": "증권사",
          "isCorrect": false
        }
      ]
    }
  ]
}
```

</details>

#### 답안 제출 (실시간 채점)
```http
POST /api/quizzes/submit-answer
Content-Type: application/json

{
  "questionId": 1,
  "selectedOptionId": 3,
  "userId": 65
}
```

<details>
<summary><strong>📋 응답 예시</strong></summary>

```json
{
  "isCorrect": true,
  "correctOptionId": 1,
  "feedback": "정답입니다! 1금융권은 은행을 의미합니다."
}
```

</details>

#### 퀴즈 완료 처리
```http
POST /api/quizzes/{quizId}/complete?userId={userId}
```

<details>
<summary><strong>📋 응답 예시</strong></summary>

```json
{
  "quizId": 1,
  "isCompleted": true,
  "isPassed": true,
  "score": 4,
  "totalQuestions": 4,
  "correctAnswers": 4,
  "completedAt": "2025-01-20T19:58:30"
}
```

</details>

#### 퀴즈 재시도
```http
POST /api/quizzes/{quizId}/retry?userId={userId}
```

#### 사용자 총 점수 조회
```http
GET /api/quizzes/user/{userId}/total-score
```

<details>
<summary><strong>📋 응답 예시</strong></summary>

```json
{
  "userId": 1499,
  "totalScore": 1250,
  "totalQuizzesCompleted": 15,
  "averageScore": 83.3
}
```

</details>

---

### 📊 **진행률 관리**

#### 레벨 진행상황 조회 (징검다리 포함)
```http
GET /api/levels/{levelId}/progress?userId={userId}
```

<details>
<summary><strong>📋 응답 예시</strong></summary>

```json
{
  "levelId": 1,
  "levelNumber": 1,
  "levelTitle": "초급자",
  "subsectorId": 1,
  "subsectorName": "금융권",
  "learningGoal": "1금융권과 2금융권의 차이를 배워요.",
  "status": "NOT_STARTED",
  "totalQuizzes": 4,
  "completedQuizzes": 0,
  "passedQuizzes": 0,
  "failedQuizzes": 0,
  "correctAnswers": 0,
  "remainingToPass": 3,
  "startedAt": null,
  "completedAt": null,
  "timeSpent": 0,
  "timeLimit": 3600,
  "completionRate": 0.0,
  "passRate": 0.0,
  "quizProgress": [],
  "nextLevelId": null,
  "nextLevelTitle": null,
  "nextLevelUnlocked": false,
  "levelPassed": false,
  "steps": [],
  "isStepPassed": false,
  "currentStep": 1
}
```

</details>

#### 레벨 완료 처리
```http
POST /api/levels/{levelId}/complete?userId={userId}
```

#### 레벨 시작 처리
```http
POST /api/levels/{levelId}/start?userId={userId}
```

#### 나의 전체 진행률 조회
```http
GET /api/progress/user/me
```

#### 나의 서브섹터별 레벨 진행률 조회
```http
GET /api/progress/user/me/subsector/{subsectorId}/level/{levelId}
```

#### 나의 서브섹터별 진행률 조회
```http
GET /api/progress/user/me/subsector/{subsectorId}
```

#### 나의 진행률 요약
```http
GET /api/progress/user/me/summary
```

---

### 📝 **오답 노트 관리**

#### 오답 노트 목록 조회
```http
GET /api/wrong-notes?userId={userId}&page=0&size=20
```

<details>
<summary><strong>📋 응답 예시</strong></summary>

```json
{
  "wrongNotes": [],
  "statistics": {
    "totalCount": 0,
    "unresolvedCount": 0,
    "resolvedCount": 0,
    "needReviewCount": 0
  },
  "subsectorStatistics": [],
  "levelStatistics": [],
  "totalPages": 0,
  "currentPage": 0,
  "pageSize": 20
}
```

</details>

#### 특정 오답 노트 조회
```http
GET /api/wrong-notes/{noteId}?userId={userId}
```

#### 개인 메모 작성
```http
PUT /api/wrong-notes/{noteId}/personal-note?userId={userId}
Content-Type: text/plain

복리는 이자에도 이자가 붙는다는 것을 기억하자!
```

#### 해결 상태 토글
```http
PUT /api/wrong-notes/{noteId}/toggle-resolved?userId={userId}
```

#### 오답 노트 통계 조회
```http
GET /api/wrong-notes/statistics?userId={userId}
```

<details>
<summary><strong>📋 응답 예시</strong></summary>

```json
{
  "totalCount": 0,
  "unresolvedCount": 0,
  "resolvedCount": 0,
  "needReviewCount": 0
}
```

</details>

#### 오답 노트 삭제
```http
DELETE /api/wrong-notes/{noteId}?userId={userId}
```

---

### 🏅 **배지 시스템**

#### 배지 시스템 초기화 (관리자용)
```http
POST /api/badges/init
```

#### 사용자 배지 진행률 업데이트
```http
POST /api/badges/update/{userId}
```

#### 사용자 배지 요약 조회
```http
GET /api/badges/user/{userId}/summary
```

#### 사용자 현재 배지 조회
```http
GET /api/badges/user/{userId}/current
```

<details>
<summary><strong>📋 응답 예시</strong></summary>

```json
{
  "id": 1,
  "code": "BRONZE",
  "name": "브론즈",
  "description": "첫 번째 벳지 - 3개 퀴즈 완료",
  "iconUrl": "https://s3.ap-northeast-2.amazonaws.com/fin.img99/badges/bronze.png",
  "levelNumber": 1,
  "requiredQuizzes": 3,
  "requiredCorrectAnswers": 5,
  "color": "#CD7F32",
  "createdAt": "2025-10-02T05:25:21",
  "updatedAt": "2025-10-02T05:25:21"
}
```

</details>

#### 사용자 획득 배지 목록 조회
```http
GET /api/badges/user/{userId}/achieved
```

#### 사용자 배지 요약 조회
```http
GET /api/badges/user/{userId}/summary
```

<details>
<summary><strong>📋 응답 예시</strong></summary>

```json
{
  "currentBadge": {
    "id": 1,
    "code": "BRONZE",
    "name": "브론즈",
    "description": "첫 번째 벳지 - 3개 퀴즈 완료",
    "iconUrl": "https://s3.ap-northeast-2.amazonaws.com/fin.img99/badges/bronze.png",
    "levelNumber": 1,
    "requiredQuizzes": 3,
    "requiredCorrectAnswers": 5,
    "color": "#CD7F32",
    "createdAt": "2025-10-02T05:25:21",
    "updatedAt": "2025-10-02T05:25:21"
  },
  "nextBadge": {
    "id": 1,
    "code": "BRONZE",
    "name": "브론즈",
    "description": "첫 번째 벳지 - 3개 퀴즈 완료",
    "iconUrl": "https://s3.ap-northeast-2.amazonaws.com/fin.img99/badges/bronze.png",
    "levelNumber": 1,
    "requiredQuizzes": 3,
    "requiredCorrectAnswers": 5,
    "color": "#CD7F32",
    "createdAt": "2025-10-02T05:25:21",
    "updatedAt": "2025-10-02T05:25:21"
  },
  "allBadges": [
    {
      "id": 3710,
      "badge": {
        "id": 1,
        "code": "BRONZE",
        "name": "브론즈",
        "description": "첫 번째 벳지 - 3개 퀴즈 완료",
        "iconUrl": "https://s3.ap-northeast-2.amazonaws.com/fin.img99/badges/bronze.png",
        "levelNumber": 1,
        "requiredQuizzes": 3,
        "requiredCorrectAnswers": 5,
        "color": "#CD7F32",
        "createdAt": "2025-10-02T05:25:21",
        "updatedAt": "2025-10-02T05:25:21"
      },
      "progress": 0,
      "isAchieved": false,
      "earnedAt": "2025-10-20T05:12:40",
      "awardedAt": null,
      "source": null
    }
  ],
  "achievedBadges": [],
  "totalBadges": 6,
  "achievedBadgesCount": 0,
  "progressPercentage": 0
}
```

</details>

#### 사용자 모든 배지 목록 (진행률 포함)
```http
GET /api/badges/user/{userId}/all
```

<details>
<summary><strong>📋 응답 예시</strong></summary>

```json
[
  {
    "id": 3710,
    "badge": {
      "id": 1,
      "code": "BRONZE",
      "name": "브론즈",
      "description": "첫 번째 벳지 - 3개 퀴즈 완료",
      "iconUrl": "https://s3.ap-northeast-2.amazonaws.com/fin.img99/badges/bronze.png",
      "levelNumber": 1,
      "requiredQuizzes": 3,
      "requiredCorrectAnswers": 5,
      "color": "#CD7F32",
      "createdAt": "2025-10-02T05:25:21",
      "updatedAt": "2025-10-02T05:25:21"
    },
    "progress": 0,
    "isAchieved": false,
    "earnedAt": "2025-10-20T05:12:40",
    "awardedAt": null,
    "source": null
  },
  {
    "id": 3711,
    "badge": {
      "id": 2,
      "code": "SILVER",
      "name": "실버",
      "description": "두 번째 벳지 - 6개 퀴즈 완료",
      "iconUrl": "https://s3.ap-northeast-2.amazonaws.com/fin.img99/badges/silver.png",
      "levelNumber": 2,
      "requiredQuizzes": 6,
      "requiredCorrectAnswers": 10,
      "color": "#C0C0C0",
      "createdAt": "2025-10-02T05:25:21",
      "updatedAt": "2025-10-02T05:25:21"
    },
    "progress": 0,
    "isAchieved": false,
    "earnedAt": "2025-10-20T05:12:40",
    "awardedAt": null,
    "source": null
  }
]
```

</details>

#### 모든 배지 목록 조회
```http
GET /api/badges
```

#### 사용자 진행률 요약
```http
GET /api/badges/user/{userId}/progress/summary
```

---

### 💬 **커뮤니티**

#### 게시글 작성
```http
POST /api/community/posts
Content-Type: application/json

{
  "body": "오늘 예금과 적금의 차이를 확실히 이해했어요! 💡",
  "tags": ["예금", "적금", "저축"]
}
```

#### 게시글 목록 조회
```http
GET /api/community/posts?page=0&size=20
```

<details>
<summary><strong>📋 응답 예시</strong></summary>

```json
[
  {
    "id": 23,
    "author": {
      "id": 1295,
      "nickname": "오리",
      "badge": {
        "name": "브론즈",
        "iconUrl": "https://s3.ap-northeast-2.amazonaws.com/fin.img99/badges/bronze.png"
      }
    },
    "body": "안녕",
    "likeCount": 9,
    "liked": false,
    "commentCount": 1,
    "tags": [],
    "createdAt": "2025-10-18T16:28:09"
  },
  {
    "id": 25,
    "author": {
      "id": 1349,
      "nickname": "사자",
      "badge": {
        "name": "브론즈",
        "iconUrl": "https://s3.ap-northeast-2.amazonaws.com/fin.img99/badges/bronze.png"
      }
    },
    "body": "안녕\n",
    "likeCount": 17,
    "liked": false,
    "commentCount": 2,
    "tags": [],
    "createdAt": "2025-10-19T17:02:49"
  }
]
```

</details>

#### 특정 게시글 조회
```http
GET /api/community/posts/{postId}
```

<details>
<summary><strong>📋 응답 예시</strong></summary>

```json
{
  "id": 23,
  "author": {
    "id": 1295,
    "nickname": "오리",
    "badge": {
      "name": "브론즈",
      "iconUrl": "https://s3.ap-northeast-2.amazonaws.com/fin.img99/badges/bronze.png"
    }
  },
  "body": "안녕",
  "likeCount": 9,
  "liked": false,
  "commentCount": 1,
  "tags": [],
  "createdAt": "2025-10-18T16:28:09"
}
```

</details>

#### 게시글 수정
```http
PUT /api/community/posts/{postId}
Content-Type: application/json

{
  "body": "수정된 내용",
  "tags": ["수정된", "태그"]
}
```

#### 게시글 삭제
```http
DELETE /api/community/posts/{postId}
```

#### 게시글 좋아요 토글
```http
POST /api/community/posts/{postId}/like?userId={userId}
```

<details>
<summary><strong>📋 응답 예시</strong></summary>

```json
{
  "liked": true,
  "likeCount": 10,
  "message": "좋아요를 눌렀습니다."
}
```

</details>

#### 게시글 좋아요 상태 조회
```http
GET /api/community/posts/{postId}/like?userId={userId}
```

#### 댓글 작성
```http
POST /api/community/posts/{postId}/comments
Content-Type: application/json

{
  "body": "좋은 정보 감사합니다!"
}
```

#### 댓글 목록 조회
```http
GET /api/community/posts/{postId}/comments
```

<details>
<summary><strong>📋 응답 예시</strong></summary>

```json
[
  {
    "id": 1,
    "author": {
      "id": 1330,
      "nickname": "말",
      "badge": {
        "name": "브론즈",
        "iconUrl": "https://s3.ap-northeast-2.amazonaws.com/fin.img99/badges/bronze.png"
      }
    },
    "body": "댓글을 수정했습니다! 댓글 수정 기능 테스트입니다.",
    "parentCommentId": null,
    "replies": [
      {
        "id": 2,
        "author": {
          "id": 1330,
          "nickname": "말",
          "badge": {
            "name": "브론즈",
            "iconUrl": "https://s3.ap-northeast-2.amazonaws.com/fin.img99/badges/bronze.png"
          }
        },
        "body": "답글입니다! 대댓글 기능 테스트입니다.",
        "parentCommentId": 1,
        "replies": [],
        "createdAt": "2025-10-18T20:40:49"
      }
    ],
    "createdAt": "2025-10-18T20:40:03"
  }
]
```

</details>

#### 댓글 수정
```http
PUT /api/community/posts/comments/{commentId}
Content-Type: application/json

{
  "body": "수정된 댓글 내용"
}
```

#### 댓글 삭제
```http
DELETE /api/community/posts/comments/{commentId}
```

#### 사용자 댓글 목록 조회
```http
GET /api/community/posts/comments/user/{userId}
```

<details>
<summary><strong>📋 응답 예시</strong></summary>

```json
[]
```

</details>

---

### 📈 **대시보드 & 통계**

#### 사용자 대시보드
```http
GET /api/dashboard?userId={userId}
```

<details>
<summary><strong>📋 응답 예시</strong></summary>

```json
{
  "userInfo": {
    "userId": 1499,
    "nickname": "하마",
    "currentLevelTitle": "브론즈",
    "currentLevelNumber": 1,
    "streak": 0,
    "totalScore": 0
  },
  "learningStats": {
    "totalLevelsCompleted": 0,
    "totalQuizzesCompleted": 0,
    "totalQuestionsAnswered": 0,
    "totalMinutesSpent": 0,
    "averageScore": 0.0
  },
  "weeklyProgress": [
    {
      "dayOfMonth": 20,
      "completed": false,
      "minutesSpent": 0,
      "quizzesCompleted": 0
    }
  ],
  "recentActivities": [],
  "nextLevelRecommendation": {
    "levelId": 1,
    "levelTitle": "초급자",
    "subsectorName": "금융권",
    "reason": "현재 레벨 진행 중",
    "progressPercentage": 50,
    "remainingQuizzes": 2,
    "difficulty": "EASY",
    "estimatedTime": 30,
    "learningGoal": "1금융권과 2금융권의 차이를 배워요."
  },
  "currentLevelSession": {
    "sessionId": "level_1_1499",
    "levelId": 1,
    "levelTitle": "초급자",
    "subsectorName": "금융권",
    "startedAt": null,
    "timeLimit": 3600,
    "timeRemaining": 3600,
    "currentQuizIndex": 0,
    "completedQuizzes": 0,
    "correctAnswers": 0,
    "remainingToPass": 3,
    "status": "NOT_STARTED"
  }
}
```

</details>

#### 오답 노트 통계
```http
GET /api/wrong-notes/statistics?userId={userId}
```

---

### 📈 **관리자 통계 (Admin)**

#### 전체 오답 노트 통계
```http
GET /api/admin/wrong-notes/statistics/overall
```

<details>
<summary><strong>📋 응답 예시</strong></summary>

```json
{
  "totalWrongNotesCount": 204,
  "totalUniqueUsersCount": 71,
  "totalQuestionsCount": null,
  "overallWrongAnswerRate": null,
  "topWrongQuestions": [
    {
      "questionId": 4,
      "questionText": "아래 기사 내용에 따라 C와 D 금융기관의 금융권 구분과 소상공인이 안정성과 비용을 중시할 때 유리한 선택은 무엇인가?",
      "quizTitle": "금융권 초급자 퀴즈",
      "sectorName": "은행",
      "subsectorName": "금융권",
      "wrongCount": 19,
      "wrongAnswerRate": null
    },
    {
      "questionId": 1,
      "questionText": "은행의 기본적인 역할 중 '여·수신'과 '지급결제'를 주로 수행하는 금융기관은 무엇인가?",
      "quizTitle": "금융권 초급자 퀴즈",
      "sectorName": "은행",
      "subsectorName": "금융권",
      "wrongCount": 17,
      "wrongAnswerRate": null
    }
  ],
  "sectorStatistics": [
    {
      "sectorId": 1,
      "sectorName": "은행",
      "sectorSlug": "banking",
      "totalWrongCount": 134,
      "uniqueUsersCount": 48,
      "totalQuestionsCount": null,
      "wrongAnswerRate": null,
      "subsectors": null
    },
    {
      "sectorId": 2,
      "sectorName": "카드",
      "sectorSlug": "card",
      "totalWrongCount": 46,
      "uniqueUsersCount": 17,
      "totalQuestionsCount": null,
      "wrongAnswerRate": null,
      "subsectors": null
    }
  ]
}
```

</details>

#### 섹터별 오답 노트 통계
```http
GET /api/admin/wrong-notes/statistics/sector/{sectorId}
```

#### 서브섹터별 오답 노트 통계
```http
GET /api/admin/wrong-notes/statistics/subsector/{subsectorId}
```

#### 퀴즈별 오답 노트 통계
```http
GET /api/admin/wrong-notes/statistics/quiz/{quizId}
```

#### 관리자 대시보드
```http
GET /api/admin/wrong-notes/dashboard
```

---

## 🎨 **핵심 기능 상세**

### 🧩 **징검다리 시스템**
```json
{
  "steps": [
    {
      "stepNumber": 1,
      "stepTitle": "1단계",
      "completedQuizzes": 4,
      "totalQuizzes": 4,
      "passedQuizzes": 4,
      "isCompleted": true,
      "isPassed": true,
      "passRate": 1.0
    }
  ],
  "isStepPassed": true,
  "currentStep": 1
}
```

**통과 조건**: 4문제 중 2문제 이상 정답 (50% 이상)

### 🏅 **배지 시스템**
| 레벨 | 배지명 | 필요 퀴즈 | 필요 정답 | 색상 |
|------|--------|-----------|-----------|------|
| 1 | 브론즈 | 3개 | 5개 | #CD7F32 |
| 2 | 실버 | 6개 | 10개 | #C0C0C0 |
| 3 | 골드 | 10개 | 20개 | #FFD700 |
| 4 | 플레티넘 | 15개 | 30개 | #E5E4E2 |
| 5 | 다이아 | 25개 | 50개 | #B9F2FF |
| 6 | 마스터 | 50개 | 100개 | #800080 |

### 📊 **실시간 채점 응답**
```json
{
  "isCorrect": true,
  "correctOptionId": 3,
  "feedback": "정답입니다! 예금은 목돈을 한 번에 맡기는 방식입니다."
}
```

---

## 🔧 **개발자 가이드**

### 인증 방식
- **JWT 토큰**: `Authorization: Bearer {token}`
- **자동 사용자 식별**: JWT 토큰에서 자동으로 사용자 ID 추출
- **개발 편의**: 대부분 API에서 `userId` 파라미터로 접근 가능

<details>
<summary><strong>📋 JWT 토큰 사용 예시</strong></summary>

```javascript
// 1. 게스트 로그인으로 토큰 획득
const loginResponse = await fetch('https://finsight.o-r.kr/api/auth/guest', {
    method: 'POST'
});
const { accessToken, userId } = await loginResponse.json();

// 2. 모든 API 호출에 토큰 포함
const response = await fetch('https://finsight.o-r.kr/api/dashboard?userId=1499', {
    headers: {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
    }
});
```

</details>

### CORS 설정
- **허용 오리진**: 모든 도메인
- **허용 메서드**: GET, POST, PUT, DELETE, OPTIONS
- **자격 증명**: true

### 페이지네이션
- **기본 페이지 크기**: 20개
- **사용법**: `?page=0&size=20`

### 에러 처리
```json
{
  "error": "Resource not found",
  "message": "Quiz with id 999 does not exist",
  "timestamp": "2024-01-08T15:00:00",
  "path": "/api/quizzes/999",
  "status": 404
}
```

---

## 📊 **성능 및 확장성**

### 데이터베이스 최적화
- **인덱싱**: 사용자 ID, 퀴즈 ID, 레벨 ID에 복합 인덱스
- **쿼리 최적화**: JOIN FETCH를 통한 N+1 문제 해결
- **트랜잭션**: 모든 쓰기 작업에 @Transactional 적용

### 캐싱 전략 (계획)
- **Redis 도입 예정**: 퀴즈 콘텐츠, 섹터 정보 캐싱
- **TTL**: 콘텐츠 1시간, 섹터 정보 24시간

### 모니터링
- **헬스 체크**: `/api/actuator/health`
- **Spring Actuator**: `/api/actuator/health`
- **로그**: 구조화된 로깅으로 디버깅 지원

---

## 🚀 **배포 및 운영**

### Docker 컨테이너화
```dockerfile
FROM openjdk:17-jdk-slim
COPY build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 환경별 설정
- **로컬**: H2 인메모리 DB
- **개발**: MySQL 로컬
- **프로덕션**: AWS RDS MySQL

### 자동화
- **게스트 계정 정리**: 24시간마다 만료된 계정 자동 삭제
- **배지 업데이트**: 퀴즈 완료 시 자동 진행률 업데이트

---

## 📈 **프로젝트 성과**

### 구현된 기능
- ✅ **8개 주요 API 그룹** 구현
- ✅ **40+ 엔드포인트** 개발
- ✅ **실시간 채점 시스템** 구축
- ✅ **징검다리 진행률 시스템** 구현
- ✅ **6단계 배지 시스템** 완성
- ✅ **오답 노트 관리 시스템** 구축
- ✅ **커뮤니티 기능** 추가
- ✅ **다중 사용자 지원 게스트 로그인** 구현
- ✅ **닉네임 일관성 보장** 시스템
- ✅ **관리자 통계 대시보드** 구축

### 기술적 성과
- 🏗️ **계층적 아키텍처**: Controller → Service → Repository 패턴
- 🔒 **보안**: JWT 인증, CORS 설정, 입력값 검증
- 📊 **성능**: 쿼리 최적화, 트랜잭션 관리
- 🧪 **테스트**: 단위 테스트, 통합 테스트 구현
- 📚 **문서화**: Swagger UI, 상세 API 문서

---

## 📞 **연락처**

- **프로젝트**: [GitHub Repository](https://github.com/DennyAhn/Finsight_main_server)
- **API 테스트**: [Swagger UI](https://finsight.o-r.kr/api/swagger-ui/index.html)
- **이메일**: support@finsight.com

---

**API 버전**: 1.5.0  
**최종 업데이트**: 2025-01-20  
**개발 기간**: 2024.12 ~ 2025.01  
**개발자**: Finsight Development Team