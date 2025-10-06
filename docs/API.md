# 📚 Finsight API 명세서

> 금융 교육 플랫폼 백엔드 서버의 전체 REST API 문서

---

## 📋 개요

### 기본 정보

| 항목 | 내용 |
|------|------|
| **Base URL (로컬)** | `http://localhost:8080/api` |
| **Base URL (프로덕션)** | `http://54.180.103.186:8080/api` |
| **Content-Type** | `application/json` |
| **Character Encoding** | UTF-8 |
| **인증 방식** | JWT Bearer Token (선택사항) |
| **API 버전** | v1.2.1 |

### 인증 헤더

JWT 토큰을 사용하는 경우 다음 헤더를 포함합니다:

```http
Authorization: Bearer {access_token}
```

**참고**: 대부분의 API는 토큰 없이도 `userId` 파라미터로 접근 가능합니다 (개발 편의성).

---

## 📑 목차

1. [인증 API](#1--인증-api) - 회원가입, 로그인, 게스트
2. [사용자 대시보드 API](#2--사용자-대시보드-api) - 학습 현황, 통계
3. [퀴즈 콘텐츠 API](#3--퀴즈-콘텐츠-api) - 섹터, 레벨 조회
4. [퀴즈 실행 API](#4--퀴즈-실행-api) - 문제 조회, 답안 제출
5. [레벨 관리 API](#5--레벨-관리-api) - 레벨 진행, 완료
6. [오답 노트 API](#6--오답-노트-api) - 오답 조회, 메모, 복습
7. [관리자 API](#7--관리자-api) - 통계, 대시보드
8. [커뮤니티 API](#8--커뮤니티-api) - 게시글 작성, 조회
9. [배지 API](#9--배지-api) - 배지 초기화, 업데이트
10. [헬스 체크 API](#10--헬스-체크-api) - 서버 상태 확인

---

## 1. 🔐 인증 API

사용자 인증 및 계정 관리를 위한 API입니다.

### 1.1. 회원가입

새로운 사용자 계정을 생성합니다.

```http
POST /api/auth/signup
```

#### Request Body

```json
{
  "email": "user@example.com",
  "password": "password123!",
  "nickname": "금융초보"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| email | String | ✅ | 이메일 주소 (로그인 ID) |
| password | String | ✅ | 비밀번호 (최소 8자) |
| nickname | String | ✅ | 닉네임 (고유값, 최대 255자) |

#### Response (200 OK)

```
회원가입이 성공적으로 완료되었습니다.
```

#### Error Responses

| 상태 코드 | 메시지 | 설명 |
|----------|--------|------|
| 400 | `이미 가입된 이메일입니다: user@example.com` | 이메일 중복 |
| 400 | `이미 사용 중인 닉네임입니다: 금융초보` | 닉네임 중복 |
| 400 | `Invalid request: {error message}` | 입력값 검증 실패 |

---

### 1.2. 로그인

기존 사용자 계정으로 로그인합니다.

```http
POST /api/auth/login
```

#### Request Body

```json
{
  "email": "user@example.com",
  "password": "password123!"
}
```

#### Response (200 OK)

```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI2NSIsImlhdCI6MTcwNTMxMjgwMCwiZXhwIjoxNzA1Mzk5MjAwfQ.K5xYz...",
  "userId": 65
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| token | String | JWT 액세스 토큰 (24시간 유효) |
| userId | Long | 사용자 ID |

#### Error Responses

| 상태 코드 | 메시지 | 설명 |
|----------|--------|------|
| 401 | `Authentication failed` | 이메일 또는 비밀번호 오류 |
| 403 | `Account is disabled` | 비활성화된 계정 |

---

### 1.3. 게스트 로그인

회원가입 없이 임시 계정을 생성합니다. 게스트 계정은 24시간 후 자동 삭제됩니다.

```http
POST /api/auth/guest
```

#### Request Body

없음

#### Response (200 OK)

```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "userId": 123
}
```

#### 특징

- 24시간 동안 유효한 임시 계정 생성
- 학습 데이터는 24시간 동안 보존
- 만료 후 자동 정리 (스케줄러)

---

## 2. 📊 사용자 대시보드 API

사용자의 학습 현황과 통계를 조회합니다.

### 2.1. 대시보드 조회

사용자의 전체 학습 현황을 조회합니다.

```http
GET /api/dashboard?userId={userId}
```

#### Query Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| userId | Long | ✅ | 사용자 ID |

#### Response (200 OK)

```json
{
  "userInfo": {
    "nickname": "금융초보",
    "currentLevel": "금융기초 1단계",
    "streak": 7,
    "totalScore": 2450,
    "level": 3
  },
  "learningStats": {
    "totalLevelsCompleted": 5,
    "totalQuizzesCompleted": 18,
    "totalQuestionsAnswered": 90,
    "totalMinutesSpent": 360,
    "averageScore": 88.5
  },
  "weeklyProgress": [
    {
      "dayOfMonth": 1,
      "completed": true
    },
    {
      "dayOfMonth": 2,
      "completed": true
    },
    {
      "dayOfMonth": 3,
      "completed": false
    },
    {
      "dayOfMonth": 4,
      "completed": true
    },
    {
      "dayOfMonth": 5,
      "completed": true
    },
    {
      "dayOfMonth": 6,
      "completed": true
    },
    {
      "dayOfMonth": 7,
      "completed": true
    }
  ],
  "recentActivities": [
    {
      "activityType": "QUIZ_COMPLETED",
      "title": "예금과 적금의 이해",
      "timestamp": "2024-01-07T15:30:00",
      "score": 5,
      "totalQuestions": 5
    },
    {
      "activityType": "LEVEL_COMPLETED",
      "title": "금융기초 3단계",
      "timestamp": "2024-01-07T14:00:00",
      "badgeEarned": "Gold Level 3"
    }
  ],
  "nextLevelRecommendation": {
    "levelId": 6,
    "levelTitle": "금융기초 6단계",
    "subsectorName": "은행업",
    "reason": "현재 레벨 완료",
    "progressPercentage": 100,
    "remainingQuizzes": 0,
    "difficulty": "MEDIUM",
    "estimatedTime": 45,
    "learningGoal": "은행 상품의 종류와 특징 이해"
  },
  "currentLevelSession": {
    "sessionId": "level_3_65",
    "levelId": 3,
    "startedAt": "2024-01-07T13:00:00",
    "status": "COMPLETED"
  }
}
```

#### Response 필드 설명

**userInfo** - 사용자 기본 정보
| 필드 | 타입 | 설명 |
|------|------|------|
| nickname | String | 사용자 닉네임 |
| currentLevel | String | 현재 학습 중인 레벨 |
| streak | Integer | 연속 학습 일수 |
| totalScore | Integer | 총 획득 점수 |
| level | Integer | 사용자 레벨 |

**learningStats** - 학습 통계
| 필드 | 타입 | 설명 |
|------|------|------|
| totalLevelsCompleted | Integer | 완료한 레벨 수 |
| totalQuizzesCompleted | Integer | 완료한 퀴즈 수 |
| totalQuestionsAnswered | Integer | 답변한 총 문제 수 |
| totalMinutesSpent | Integer | 총 학습 시간(분) |
| averageScore | Double | 평균 점수(%) |

**weeklyProgress** - 주간 학습 진행도 (7일간)
| 필드 | 타입 | 설명 |
|------|------|------|
| dayOfMonth | Integer | 날짜 (일) |
| completed | Boolean | 학습 완료 여부 |

---

## 3. 🎯 퀴즈 콘텐츠 API

학습 콘텐츠 계층 구조를 조회합니다.

### 3.1. 모든 섹터 조회

최상위 금융 섹터 목록을 조회합니다.

```http
GET /api/sectors
```

#### Response (200 OK)

```json
[
  {
    "id": 1,
    "name": "은행업",
    "slug": "banking",
    "description": "예금, 적금, 대출 등 은행의 기본 업무를 다룹니다.",
    "subsectors": [
      {
        "id": 1,
        "name": "상업은행",
        "slug": "commercial-banking",
        "description": "일반 고객 대상 금융 서비스",
        "sortOrder": 1
      },
      {
        "id": 2,
        "name": "투자은행",
        "slug": "investment-banking",
        "description": "기업 금융 및 증권 업무",
        "sortOrder": 2
      }
    ]
  },
  {
    "id": 2,
    "name": "증권업",
    "slug": "securities",
    "description": "주식, 채권 등 증권 투자를 다룹니다.",
    "subsectors": [...]
  }
]
```

---

### 3.2. 서브섹터 상세 정보 조회

특정 서브섹터의 상세 정보와 하위 레벨을 조회합니다.

```http
GET /api/subsectors/{id}
```

#### Path Parameters

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| id | Long | 서브섹터 ID |

#### Response (200 OK)

```json
{
  "id": 1,
  "name": "상업은행",
  "slug": "commercial-banking",
  "description": "일반 고객을 대상으로 예금, 대출, 송금 등의 금융 서비스를 제공하는 은행입니다.",
  "sectorName": "은행업",
  "levels": [
    {
      "id": 1,
      "levelNumber": 1,
      "title": "금융기초 1단계",
      "learningGoal": "예금과 적금의 기본 개념 이해",
      "totalQuizzes": 4,
      "estimatedTime": 30
    },
    {
      "id": 2,
      "levelNumber": 2,
      "title": "금융기초 2단계",
      "learningGoal": "금리와 이자 계산 방법 이해",
      "totalQuizzes": 5,
      "estimatedTime": 40
    }
  ]
}
```

---

### 3.3. 레벨별 퀴즈 상태 조회

특정 레벨의 퀴즈 목록과 사용자별 완료 상태를 조회합니다.

```http
GET /api/levels/{levelId}/quizzes?userId={userId}
```

#### Path Parameters

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| levelId | Long | 레벨 ID |

#### Query Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| userId | Long | ✅ | 사용자 ID |

#### Response (200 OK)

```json
{
  "levelId": 1,
  "levelTitle": "금융기초 1단계",
  "subsectorName": "상업은행",
  "quizzes": [
    {
      "id": 1,
      "title": "예금과 적금 개념 퀴즈",
      "sortOrder": 1,
      "status": "COMPLETED",
      "totalQuestions": 5,
      "userScore": 5,
      "maxScore": 5,
      "completedAt": "2024-01-05T14:30:00"
    },
    {
      "id": 2,
      "title": "금리 계산 퀴즈",
      "sortOrder": 2,
      "status": "IN_PROGRESS",
      "totalQuestions": 4,
      "userScore": 2,
      "maxScore": 4,
      "completedAt": null
    },
    {
      "id": 3,
      "title": "대출 상품 비교 퀴즈",
      "sortOrder": 3,
      "status": "LOCKED",
      "totalQuestions": 5,
      "userScore": null,
      "maxScore": 5,
      "completedAt": null
    }
  ],
  "totalQuizzes": 4,
  "completedQuizzes": 1,
  "overallStatus": "IN_PROGRESS"
}
```

#### 퀴즈 상태 (status)

| 상태 | 설명 |
|------|------|
| `LOCKED` | 잠김 (이전 퀴즈 미완료) |
| `IN_PROGRESS` | 진행 중 |
| `COMPLETED` | 완료 |

---

## 4. 🧩 퀴즈 실행 API

퀴즈 문제 조회 및 답안 제출을 처리합니다.

### 4.1. 퀴즈 정보 조회 (문제 포함)

특정 퀴즈의 전체 문제를 조회합니다.

```http
GET /api/quizzes/{id}
```

#### Path Parameters

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| id | Long | 퀴즈 ID |

#### Response (200 OK)

```json
{
  "id": 1,
  "title": "예금과 적금 개념 퀴즈",
  "questions": [
    {
      "id": 1,
      "type": "CONCEPT",
      "stemMd": "'예금'과 '적금'의 사전적 뜻으로 가장 옳은 것은?",
      "options": [
        {
          "id": 1,
          "label": "A",
          "contentMd": "예금은 은행에 돈을 한 번에 맡기는 것이고, 적금은 일정 기간 동안 나눠서 돈을 넣는 것이다."
        },
        {
          "id": 2,
          "label": "B",
          "contentMd": "예금은 매달 돈을 내는 것이고, 적금은 한 번에 맡기는 것이다."
        },
        {
          "id": 3,
          "label": "C",
          "contentMd": "예금과 적금은 같은 뜻으로, 아무 차이가 없다."
        },
        {
          "id": 4,
          "label": "D",
          "contentMd": "예금은 돈을 빌리는 것이고, 적금은 돈을 저축하는 것이다."
        }
      ],
      "answerExplanationMd": "예금은 보통 목돈을 한번에 맡기는 방식이고, 적금은 정해진 기간 동안 정기적으로 일정 금액을 납입하는 방식입니다.",
      "hintMd": "예금의 '예(預)'는 '미리 맡긴다'는 뜻이고, 적금의 '적(積)'은 '쌓는다'는 뜻이에요.",
      "teachingExplainerMd": "**예금**이라는 단어에서 '예(預)'는 '미리 맡긴다'는 뜻이에요. 이미 목돈이 있는 상태에서 한 번에 맡기고, 일정 기간 후에 이자와 함께 돌려받는 방식이에요.\n\n**적금**의 '적(積)'은 '쌓는다'는 뜻이에요. 매달 꾸준히 돈을 부어 목돈을 쌓는 방식이고, 마찬가지로 만기 시 이자와 함께 돌려받아요.",
      "solvingKeypointsMd": "💡 **핵심 포인트**\n\n* **예금** = 목돈을 한 번에 넣고, 전액에 이자가 붙는 구조\n* **적금** = 매달 일정 금액을 넣으며, 쌓인 금액에 점점 이자가 붙는 구조\n\n✓ 둘 다 저축 상품이고, 돈을 어떻게 넣느냐가 가장 중요한 차이점!",
      "article": null
    },
    {
      "id": 5,
      "type": "ARTICLE",
      "stemMd": "다음 기사를 읽고 물음에 답하세요. 이 기사에서 설명하는 금융 사건의 주요 원인은 무엇인가요?",
      "options": [
        {
          "id": 17,
          "label": "A",
          "contentMd": "디지털 전환 가속화"
        },
        {
          "id": 18,
          "label": "B",
          "contentMd": "금리 인상"
        },
        {
          "id": 19,
          "label": "C",
          "contentMd": "규제 완화"
        },
        {
          "id": 20,
          "label": "D",
          "contentMd": "인수합병"
        }
      ],
      "answerExplanationMd": "기사에서는 ○○은행의 디지털 전환이 주요 주제입니다.",
      "hintMd": "기사의 제목을 주목하세요.",
      "teachingExplainerMd": "최근 금융권에서는 디지털 전환이 중요한 트렌드로 자리잡고 있습니다...",
      "solvingKeypointsMd": "💡 기사를 읽을 때는 제목과 첫 문단에 주목하세요.",
      "article": {
        "id": 1,
        "title": "○○은행, 디지털 전환 가속화로 고객 편의성 대폭 향상",
        "bodyMd": "# 금융권 디지털 혁신의 선두주자\n\n○○은행이 최근 모바일 뱅킹 앱을 전면 개편하며 디지털 전환에 박차를 가하고 있다.\n\n## 주요 개편 내용\n\n1. **AI 기반 상담 서비스** - 24시간 AI 챗봇을 통한 실시간 상담\n2. **간편 송금** - QR 코드를 통한 즉시 송금 기능\n3. **맞춤형 상품 추천** - 고객 데이터 분석을 통한 개인화 서비스\n\n업계 관계자는 \"디지털 전환은 이제 선택이 아닌 필수\"라며 \"고객 경험 개선이 최우선 과제\"라고 밝혔다.",
        "imageUrl": "https://example.com/banking-digital.jpg",
        "sourceNote": "※ 이 기사는 교육 목적으로 제작된 가상의 기사입니다."
      }
    }
  ]
}
```

#### 문제 타입 (type)

| 타입 | 설명 | article 필드 |
|------|------|--------------|
| `CONCEPT` | 일반 개념 문제 | null |
| `STORY` | 스토리 기반 문제 | null |
| `ARTICLE` | 가상 기사 문제 | Article 객체 포함 |

#### Article 필드 (type="ARTICLE"인 경우)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 기사 ID |
| title | String | 기사 제목 |
| bodyMd | String | 기사 본문 (Markdown 형식) |
| imageUrl | String | 기사 대표 이미지 URL |
| sourceNote | String | 출처 표기 |

---

### 4.2. 답안 제출

퀴즈 답안을 제출하고 채점 결과를 받습니다.

```http
POST /api/quizzes/submit-answer
```

#### Request Body

```json
{
  "quizId": 1,
  "userId": 65,
  "answers": [
    {
      "questionId": 1,
      "selectedOptionId": 1
    },
    {
      "questionId": 2,
      "selectedOptionId": 6
    },
    {
      "questionId": 3,
      "selectedOptionId": 9
    },
    {
      "questionId": 4,
      "selectedOptionId": 14
    },
    {
      "questionId": 5,
      "selectedOptionId": 17
    }
  ]
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| quizId | Long | ✅ | 퀴즈 ID |
| userId | Long | ✅ | 사용자 ID |
| answers | Array | ✅ | 답안 목록 |
| answers[].questionId | Long | ✅ | 문제 ID |
| answers[].selectedOptionId | Long | ✅ | 선택한 선택지 ID |

#### Response (200 OK)

```json
{
  "quizId": 1,
  "userId": 65,
  "correctAnswers": 4,
  "totalQuestions": 5,
  "score": 4,
  "isPassed": true,
  "timeSpent": 240,
  "submittedAt": "2024-01-07T15:45:00",
  "answers": [
    {
      "questionId": 1,
      "selectedOptionId": 1,
      "correctOptionId": 1,
      "isCorrect": true,
      "feedback": "정답입니다! 예금은 목돈을 한 번에 맡기는 방식입니다."
    },
    {
      "questionId": 2,
      "selectedOptionId": 6,
      "correctOptionId": 5,
      "isCorrect": false,
      "feedback": "아쉽습니다. 단리는 원금에만 이자가 붙는 방식입니다."
    },
    {
      "questionId": 3,
      "selectedOptionId": 9,
      "correctOptionId": 9,
      "isCorrect": true,
      "feedback": "정답입니다!"
    },
    {
      "questionId": 4,
      "selectedOptionId": 14,
      "correctOptionId": 14,
      "isCorrect": true,
      "feedback": "정답입니다!"
    },
    {
      "questionId": 5,
      "selectedOptionId": 17,
      "correctOptionId": 17,
      "isCorrect": true,
      "feedback": "정답입니다! 기사의 제목과 내용을 잘 파악했습니다."
    }
  ]
}
```

#### 채점 결과 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| correctAnswers | Integer | 정답 개수 |
| totalQuestions | Integer | 전체 문제 수 |
| score | Integer | 획득 점수 |
| isPassed | Boolean | 통과 여부 (60% 이상) |
| timeSpent | Integer | 소요 시간(초) |

---

### 4.3. 퀴즈 결과 조회

이전에 제출한 퀴즈 결과를 조회합니다.

```http
GET /api/quizzes/{id}/result?userId={userId}
```

#### Path Parameters

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| id | Long | 퀴즈 ID |

#### Query Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| userId | Long | ✅ | 사용자 ID |

#### Response

4.2 답안 제출 응답과 동일한 형식

---

### 4.4. 퀴즈 완료 처리

퀴즈를 완료 상태로 변경합니다.

```http
POST /api/quizzes/{id}/complete?userId={userId}
```

#### Response (200 OK)

```json
{
  "quizId": 1,
  "userId": 65,
  "completedAt": "2024-01-07T15:50:00",
  "nextQuizId": 2,
  "nextQuizUnlocked": true
}
```

---

## 5. 📈 레벨 관리 API

학습 레벨의 진행도를 관리합니다.

### 5.1. 레벨 진행 상황 조회

특정 레벨의 진행도를 조회합니다.

```http
GET /api/levels/{id}/progress?userId={userId}
```

#### Path Parameters

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| id | Long | 레벨 ID |

#### Query Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| userId | Long | ✅ | 사용자 ID |

#### Response (200 OK)

```json
{
  "levelId": 1,
  "levelTitle": "금융기초 1단계",
  "status": "IN_PROGRESS",
  "completedQuizzes": 2,
  "totalQuizzes": 4,
  "remainingToPass": 1,
  "progressPercentage": 50.0,
  "quizProgress": [
    {
      "quizId": 1,
      "quizTitle": "예금과 적금 개념 퀴즈",
      "completed": true,
      "score": 5
    },
    {
      "quizId": 2,
      "quizTitle": "금리 계산 퀴즈",
      "completed": true,
      "score": 4
    },
    {
      "quizId": 3,
      "quizTitle": "대출 상품 비교 퀴즈",
      "completed": false,
      "score": null
    },
    {
      "quizId": 4,
      "quizTitle": "신용카드 이해하기",
      "completed": false,
      "score": null
    }
  ]
}
```

---

### 5.2. 레벨 시작

새로운 레벨을 시작합니다.

```http
POST /api/levels/{id}/start?userId={userId}
```

#### Response (200 OK)

```json
{
  "levelId": 1,
  "sessionId": "level_1_65",
  "startedAt": "2024-01-07T16:00:00",
  "firstQuizId": 1
}
```

---

### 5.3. 레벨 완료 처리

레벨을 완료하고 다음 레벨을 잠금 해제합니다.

```http
POST /api/levels/{id}/complete?userId={userId}
```

#### Response (200 OK)

```json
{
  "levelId": 1,
  "isCompleted": true,
  "isPassed": true,
  "score": 18,
  "maxScore": 20,
  "scorePercentage": 90.0,
  "nextLevelId": 2,
  "nextLevelUnlocked": true,
  "pointsEarned": 500,
  "badgeEarned": "Silver Badge - Level 1",
  "achievementMessage": "🎉 축하합니다! 금융기초 1단계를 완료했습니다!",
  "completedAt": "2024-01-07T16:30:00"
}
```

---

## 6. 📝 오답 노트 API

틀린 문제를 관리하고 복습합니다.

### 6.1. 오답 노트 목록 조회

사용자의 오답 노트 목록을 조회합니다.

```http
GET /api/wrong-notes?userId={userId}&page={page}&size={size}&filter={filter}
```

#### Query Parameters

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|-------|------|
| userId | Long | ✅ | - | 사용자 ID |
| page | Integer | ❌ | 0 | 페이지 번호 (0부터 시작) |
| size | Integer | ❌ | 20 | 페이지 크기 |
| filter | String | ❌ | all | 필터 타입 |

#### filter 값

| 값 | 설명 |
|----|------|
| `all` | 전체 오답 |
| `unresolved` | 미해결 오답만 |
| `resolved` | 해결된 오답만 |
| `needreview` | 복습 필요 (2회 이상 오답) |

#### Response (200 OK)

```json
{
  "wrongNotes": [
    {
      "id": 15,
      "userId": 65,
      "questionId": 2,
      "questionText": "'단리'와 '복리'의 차이점은 무엇인가요?",
      "lastAnswerText": "복리는 원금에만 이자가 붙는 방식이다.",
      "correctAnswerText": "복리는 원금 + 이자에 이자가 붙는 방식이다.",
      "timesWrong": 2,
      "resolved": false,
      "personalNoteMd": "복리는 이자에도 이자가 붙는다는 것을 기억하자!",
      "quizTitle": "금리 계산 퀴즈",
      "subsectorName": "상업은행",
      "firstWrongAt": "2024-01-05T14:30:00",
      "lastWrongAt": "2024-01-07T15:45:00",
      "reviewedAt": null
    },
    {
      "id": 20,
      "userId": 65,
      "questionId": 8,
      "questionText": "신용카드 결제 시 주의할 점은?",
      "lastAnswerText": "한도까지 무조건 사용하는 것",
      "correctAnswerText": "결제 능력 내에서 사용하고 기한 내 납부",
      "timesWrong": 1,
      "resolved": false,
      "personalNoteMd": null,
      "quizTitle": "신용카드 이해하기",
      "subsectorName": "상업은행",
      "firstWrongAt": "2024-01-06T10:20:00",
      "lastWrongAt": "2024-01-06T10:20:00",
      "reviewedAt": null
    }
  ],
  "statistics": {
    "totalCount": 8,
    "unresolvedCount": 6,
    "resolvedCount": 2,
    "needReviewCount": 3
  }
}
```

---

### 6.2. 특정 오답 노트 상세 조회

하나의 오답 노트를 상세히 조회합니다.

```http
GET /api/wrong-notes/{noteId}?userId={userId}
```

#### Path Parameters

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| noteId | Long | 오답노트 ID |

#### Response (200 OK)

```json
{
  "id": 15,
  "userId": 65,
  "question": {
    "id": 2,
    "stemMd": "'단리'와 '복리'의 차이점은 무엇인가요?",
    "answerExplanationMd": "단리는 원금에만 이자가 붙고, 복리는 원금 + 이자에 이자가 붙는 방식입니다.",
    "teachingExplainerMd": "복리의 힘은 시간이 지날수록 커집니다...",
    "solvingKeypointsMd": "💡 복리 = 이자의 이자"
  },
  "lastAnswer": {
    "id": 6,
    "contentMd": "복리는 원금에만 이자가 붙는 방식이다."
  },
  "correctAnswer": {
    "id": 5,
    "contentMd": "복리는 원금 + 이자에 이자가 붙는 방식이다."
  },
  "timesWrong": 2,
  "resolved": false,
  "personalNoteMd": "복리는 이자에도 이자가 붙는다는 것을 기억하자!",
  "firstWrongAt": "2024-01-05T14:30:00",
  "lastWrongAt": "2024-01-07T15:45:00",
  "reviewedAt": null
}
```

---

### 6.3. 개인 메모 작성/수정

오답 노트에 개인 메모를 작성합니다.

```http
PUT /api/wrong-notes/{noteId}/personal-note?userId={userId}
```

#### Path Parameters

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| noteId | Long | 오답노트 ID |

#### Request Body (text/plain)

```
복리는 이자에도 이자가 붙는다는 것을 기억하자!
시간이 지날수록 원금과의 차이가 커진다.
```

#### Response (200 OK)

```
개인 메모가 저장되었습니다.
```

---

### 6.4. 해결 상태 토글

오답 노트의 해결 상태를 변경합니다.

```http
PUT /api/wrong-notes/{noteId}/toggle-resolved?userId={userId}
```

#### Response (200 OK)

```json
{
  "noteId": 15,
  "resolved": true,
  "resolvedAt": "2024-01-08T10:00:00"
}
```

---

### 6.5. 복습 완료 처리

오답 노트를 복습했음을 기록합니다.

```http
PUT /api/wrong-notes/{noteId}/mark-reviewed?userId={userId}
```

#### Response (200 OK)

```json
{
  "noteId": 15,
  "reviewedAt": "2024-01-08T11:30:00"
}
```

---

### 6.6. 오답 노트 삭제

오답 노트를 삭제합니다.

```http
DELETE /api/wrong-notes/{noteId}?userId={userId}
```

#### Response

`204 No Content`

---

### 6.7. 오답 노트 통계 조회

사용자의 오답 노트 통계를 조회합니다.

```http
GET /api/wrong-notes/statistics?userId={userId}
```

#### Response (200 OK)

```json
{
  "totalCount": 8,
  "unresolvedCount": 6,
  "resolvedCount": 2,
  "needReviewCount": 3,
  "averageTimesWrong": 1.5,
  "mostDifficultTopics": [
    {
      "topicName": "금리 계산",
      "wrongCount": 3
    },
    {
      "topicName": "신용카드",
      "wrongCount": 2
    }
  ]
}
```

---

## 7. 🔧 관리자 API

관리자용 통계 및 분석 API입니다.

### 7.1. 전체 오답 노트 통계

전체 사용자의 오답 노트 통계를 조회합니다.

```http
GET /api/admin/wrong-notes/statistics/overall
```

#### Response (200 OK)

```json
{
  "totalWrongNotesCount": 1250,
  "totalUniqueUsersCount": 150,
  "averageWrongNotesPerUser": 8.3,
  "sectorStatistics": [
    {
      "sectorId": 1,
      "sectorName": "은행업",
      "wrongNotesCount": 650,
      "affectedUsersCount": 120
    },
    {
      "sectorId": 2,
      "sectorName": "증권업",
      "wrongNotesCount": 400,
      "affectedUsersCount": 80
    }
  ],
  "mostDifficultQuestions": [
    {
      "questionId": 25,
      "questionText": "복리 계산 문제...",
      "wrongCount": 85,
      "wrongRate": 68.5
    }
  ]
}
```

---

### 7.2. 섹터별 오답 노트 통계

특정 섹터의 오답 통계를 조회합니다.

```http
GET /api/admin/wrong-notes/statistics/sector/{sectorId}
```

---

### 7.3. 서브섹터별 오답 노트 통계

특정 서브섹터의 오답 통계를 조회합니다.

```http
GET /api/admin/wrong-notes/statistics/subsector/{subsectorId}
```

---

### 7.4. 퀴즈별 오답 노트 통계

특정 퀴즈의 오답 통계를 조회합니다.

```http
GET /api/admin/wrong-notes/statistics/quiz/{quizId}
```

---

### 7.5. 관리자 대시보드

관리자 대시보드 데이터를 조회합니다.

```http
GET /api/admin/wrong-notes/dashboard
```

#### Response (200 OK)

```json
{
  "overview": {
    "totalUsers": 200,
    "activeUsersLast7Days": 125,
    "totalQuizzesCompleted": 3500,
    "totalWrongNotes": 1250
  },
  "recentActivity": [...],
  "topDifficultQuestions": [...],
  "userEngagementTrends": [...]
}
```

---

## 8. 💬 커뮤니티 API

학습자 간 지식 공유를 위한 커뮤니티 기능입니다.

### 8.1. 게시글 작성

새로운 커뮤니티 게시글을 작성합니다.

```http
POST /api/community/posts
```

#### Request Headers

```http
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN} (선택사항)
```

#### Request Body

```json
{
  "body": "오늘 예금과 적금의 차이를 확실히 이해했어요! 예금은 목돈을 한 번에, 적금은 매달 나눠서 저축하는 거였네요. 💡",
  "tags": ["예금", "적금", "저축"]
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| body | String | ✅ | 게시글 내용 (최대 5000자) |
| tags | Array<String> | ❌ | 태그 목록 (최대 5개) |

#### Response (201 Created)

```json
{
  "id": 42,
  "author": {
    "id": 65,
    "nickname": "금융초보",
    "badge": {
      "name": "Gold Level 3",
      "iconUrl": "https://example.com/badges/gold-3.png"
    }
  },
  "body": "오늘 예금과 적금의 차이를 확실히 이해했어요! 예금은 목돈을 한 번에, 적금은 매달 나눠서 저축하는 거였네요. 💡",
  "likeCount": 0,
  "commentCount": 0,
  "tags": ["예금", "적금", "저축"],
  "createdAt": "2024-01-08T12:00:00"
}
```

---

### 8.2. 게시글 목록 조회

커뮤니티 게시글 목록을 조회합니다.

```http
GET /api/community/posts?page={page}&size={size}&tag={tag}
```

#### Query Parameters

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|-------|------|
| page | Integer | ❌ | 0 | 페이지 번호 |
| size | Integer | ❌ | 20 | 페이지 크기 |
| tag | String | ❌ | - | 태그 필터 |

#### Response (200 OK)

```json
[
  {
    "id": 42,
    "author": {
      "id": 65,
      "nickname": "금융초보",
      "badge": {
        "name": "Gold Level 3",
        "iconUrl": "https://example.com/badges/gold-3.png"
      }
    },
    "body": "오늘 예금과 적금의 차이를 확실히 이해했어요!...",
    "likeCount": 5,
    "commentCount": 3,
    "tags": ["예금", "적금", "저축"],
    "createdAt": "2024-01-08T12:00:00"
  },
  {
    "id": 41,
    "author": {
      "id": 52,
      "nickname": "금융고수",
      "badge": {
        "name": "Diamond Level 5",
        "iconUrl": "https://example.com/badges/diamond-5.png"
      }
    },
    "body": "복리의 힘은 정말 대단해요. 시간이 지날수록...",
    "likeCount": 12,
    "commentCount": 7,
    "tags": ["금리", "복리"],
    "createdAt": "2024-01-08T10:30:00"
  }
]
```

---

## 9. 🏅 배지 API

사용자 배지 시스템을 관리합니다.

### 9.1. 배지 시스템 초기화

배지 시스템의 기본 배지들을 초기화합니다 (관리자용).

```http
POST /api/badges/init
```

#### Response (200 OK)

```
배지 시스템 초기화 완료
```

#### 초기화되는 배지

| 레벨 | 이름 | 필요 퀴즈 | 필요 정답 |
|------|------|----------|----------|
| 1 | Bronze | 5 | 10 |
| 2 | Silver | 10 | 30 |
| 3 | Gold | 20 | 60 |
| 4 | Platinum | 35 | 100 |
| 5 | Diamond | 50 | 150 |
| 6 | Master | 75 | 250 |

---

### 9.2. 사용자 배지 진행 상황 업데이트

사용자의 배지 획득 조건을 확인하고 업데이트합니다.

```http
POST /api/badges/update/{userId}
```

#### Path Parameters

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| userId | Long | 사용자 ID |

#### Response (200 OK)

```
사용자 65의 배지 진행 상황 업데이트 완료
```

#### 부수 효과

- 새로운 배지 획득 조건이 충족되면 자동으로 배지 부여
- user_badges 테이블에 진행도 업데이트

---

## 10. 🏥 헬스 체크 API

서버 상태를 확인하는 API입니다.

### 10.1. 서버 상태 확인

서버의 전반적인 상태를 확인합니다.

```http
GET /api/health
```

#### Response (200 OK)

```json
{
  "status": "UP",
  "timestamp": "2024-01-08T14:30:00",
  "service": "fin-main-server",
  "version": "1.2.1",
  "uptime": 3600000,
  "database": "Connected",
  "memory": {
    "total": 2048,
    "used": 512,
    "free": 1536,
    "unit": "MB"
  }
}
```

---

### 10.2. 핑 테스트

간단한 핑 테스트입니다.

```http
GET /api/health/ping
```

#### Response (200 OK)

```
pong
```

---

### 10.3. Spring Actuator 헬스 체크

Spring Actuator의 헬스 체크 엔드포인트입니다.

```http
GET /api/actuator/health
```

#### Response (200 OK)

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 107374182400,
        "free": 53687091200,
        "threshold": 10485760,
        "exists": true
      }
    }
  }
}
```

---

## ❌ 에러 응답

### 공통 에러 코드

| HTTP 코드 | 설명 | 일반적인 원인 |
|-----------|------|--------------|
| 400 | Bad Request | 잘못된 요청 파라미터, 검증 실패 |
| 401 | Unauthorized | 인증 실패, 토큰 없음/만료 |
| 403 | Forbidden | 권한 없음 |
| 404 | Not Found | 리소스를 찾을 수 없음 |
| 409 | Conflict | 중복된 데이터 (이메일, 닉네임 등) |
| 500 | Internal Server Error | 서버 내부 오류 |

### 에러 응답 형식

```json
{
  "error": "Resource not found",
  "message": "Quiz with id 999 does not exist",
  "timestamp": "2024-01-08T15:00:00",
  "path": "/api/quizzes/999",
  "status": 404
}
```

### 검증 실패 응답

```json
{
  "error": "Validation failed",
  "message": "Invalid input data",
  "timestamp": "2024-01-08T15:00:00",
  "path": "/api/auth/signup",
  "status": 400,
  "fieldErrors": [
    {
      "field": "email",
      "rejectedValue": "invalid-email",
      "message": "올바른 이메일 형식이 아닙니다"
    },
    {
      "field": "password",
      "rejectedValue": "***",
      "message": "비밀번호는 최소 8자 이상이어야 합니다"
    }
  ]
}
```

---

## 🔧 개발자 참고사항

### 1. 인증 방식

JWT 토큰을 사용한 무상태 인증:

```http
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**토큰 없이 사용하기 (개발/테스트용)**:
- 대부분의 API는 `userId` 파라미터로 접근 가능
- 프로덕션에서는 JWT 토큰 사용 권장

### 2. CORS 설정

- **허용 오리진**: `*` (모든 오리진)
- **허용 메서드**: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`
- **허용 헤더**: `*` (모든 헤더)
- **자격 증명**: `true`

### 3. 페이지네이션

페이지네이션을 지원하는 API:

| API | 기본 페이지 크기 |
|-----|-----------------|
| 오답 노트 목록 | 20 |
| 커뮤니티 게시글 | 20 |

```
GET /api/wrong-notes?userId=65&page=0&size=20
```

### 4. 날짜/시간 형식

- **ISO 8601 형식**: `2024-01-08T15:30:00`
- **타임존**: UTC 또는 서버 로컬 타임존
- **날짜만**: `2024-01-08`

### 5. 요청 제한

- **Rate Limiting**: 분당 100회 (향후 구현 예정)
- **파일 업로드**: 최대 10MB
- **요청 본문**: 최대 1MB

### 6. Swagger UI

대화형 API 문서:

- **로컬**: http://localhost:8080/api/swagger-ui/index.html
- **프로덕션**: http://54.180.103.186:8080/api/swagger-ui/index.html

### 7. 데이터베이스 트랜잭션

- 모든 쓰기 작업은 트랜잭션 내에서 실행
- 롤백 정책: RuntimeException 발생 시 자동 롤백

### 8. 캐싱 전략 (계획 중)

향후 Redis를 통한 캐싱 도입 예정:

- 퀴즈 콘텐츠 (TTL: 1시간)
- 섹터/서브섹터 정보 (TTL: 24시간)
- 사용자 세션 (TTL: 24시간)

---

## 📝 변경 이력

### v1.2.1 (2025-01-08)
- ✨ 퀴즈 정보 조회 API에 가상기사(ARTICLE) 정보 추가
- 📝 문제 타입 명시 (CONCEPT, STORY, ARTICLE)
- 🐛 오답 노트 통계 조회 버그 수정

### v1.2.0 (2025-01-05)
- ✨ 커뮤니티 API 추가
- ✨ 배지 시스템 API 추가
- 📝 API 문서 통합 및 재구성
- 🎨 가독성 개선 및 예시 추가

### v1.1.0 (2025-01-02)
- 🔐 JWT 토큰 인증 시스템 구현
- 🐛 사용자 ID 일관성 문제 해결
- ♻️ 오답 노트 API 리팩토링

### v1.0.0 (2024-12-20)
- 🎉 초기 API 문서 작성
- ✨ 기본 CRUD 엔드포인트 구현

---

## 📞 문의 및 지원

API 사용 중 문제가 발생하거나 문의사항이 있으시면:

- **이메일**: support@finsight.com
- **GitHub Issues**: [프로젝트 Issues 페이지]
- **Swagger UI**: 실시간 API 테스트 가능

---

**API 문서 버전**: 1.2.1  
**최종 업데이트**: 2025-01-08  
**작성자**: Finsight Development Team
