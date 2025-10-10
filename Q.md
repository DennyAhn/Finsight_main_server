# 📋 API 플로우 가이드

## 🎯 서브섹터 선택 → 레벨 선택 → 4문제 퀴즈 구조

### 📖 개요
사용자가 섹터를 선택하고, 서브섹터를 선택한 후, 레벨(초급자/중급자/고급자)을 선택하면 해당 레벨의 4문제 퀴즈가 제공되는 완전한 학습 플로우입니다.

---

## 🔄 API 플로우 순서

### 1️⃣ 섹터 목록 조회
**사용자가 처음 접속했을 때 모든 섹터와 서브섹터를 조회합니다.**

```http
GET /api/sectors
```

**Response 예시:**
```json
[
  {
    "id": 1,
    "name": "금융권",
    "slug": "banking",
    "description": "은행, 저축은행 등 금융기관의 기본 업무를 다룹니다.",
    "subsectors": [
      {
        "id": 1,
        "name": "금융권",
        "slug": "commercial-banking",
        "description": "1금융권과 2금융권의 차이를 배워요.",
        "sortOrder": 1
      },
      {
        "id": 2,
        "name": "예금/적금",
        "slug": "deposits",
        "description": "예금과 적금, 금리 개념을 배워요.",
        "sortOrder": 2
      }
    ]
  }
]
```

---

### 2️⃣ 서브섹터 선택 후 레벨 목록 조회
**사용자가 특정 서브섹터를 선택하면 해당 서브섹터의 레벨들을 조회합니다.**

```http
GET /api/subsectors/{id}
```

**Path Parameters:**
- `id`: 서브섹터 ID (Long)

**Response 예시:**
```json
{
  "id": 1,
  "name": "금융권",
  "slug": "commercial-banking",
  "description": "1금융권과 2금융권의 차이를 배워요.",
  "sectorName": "금융권",
  "levels": [
    {
      "id": 1,
      "levelNumber": 1,
      "title": "초급자",
      "learningGoal": "1금융권과 2금융권의 차이를 배워요."
    },
    {
      "id": 2,
      "levelNumber": 2,
      "title": "중급자",
      "learningGoal": "1금융권과 2금융권 특징, 금리구조, 예금자 보호 한도 차이를 배워요."
    },
    {
      "id": 3,
      "levelNumber": 3,
      "title": "고급자",
      "learningGoal": "핀테크 기업 개념을 배워요."
    }
  ]
}
```

---

### 3️⃣ 레벨 선택 후 퀴즈 목록 조회
**사용자가 특정 레벨을 선택하면 해당 레벨의 퀴즈들을 조회합니다.**

```http
GET /api/levels/{levelId}/quizzes?userId={userId}
```

**Path Parameters:**
- `levelId`: 레벨 ID (Long)

**Query Parameters:**
- `userId`: 사용자 ID (Long) - 필수

**Response 예시:**
```json
{
  "levelId": 1,
  "levelTitle": "초급자",
  "subsectorName": "금융권",
  "quizzes": [
    {
      "id": 1,
      "title": "금융권 초급자 퀴즈",
      "sortOrder": 1,
      "status": "LOCKED",
      "totalQuestions": 4,
      "userScore": null,
      "maxScore": 4,
      "completedAt": null
    }
  ],
  "totalQuizzes": 1,
  "completedQuizzes": 0,
  "overallStatus": "NOT_STARTED"
}
```

**퀴즈 상태 (status):**
- `LOCKED`: 잠김 (이전 퀴즈 미완료)
- `IN_PROGRESS`: 진행 중
- `COMPLETED`: 완료

---

### 4️⃣ 퀴즈 문제 조회
**사용자가 특정 퀴즈를 선택하면 해당 퀴즈의 4개 문제와 선택지들을 조회합니다.**

```http
GET /api/quizzes/{id}
```

**Path Parameters:**
- `id`: 퀴즈 ID (Long)

**Response 예시:**
```json
{
  "id": 1,
  "title": "금융권 초급자 퀴즈",
  "timeLimitSec": 300,
  "passingScore": 1,
  "questions": [
    {
      "id": 1,
      "type": "CONCEPT",
      "stemMd": "은행의 기본적인 역할 중 '여·수신'과 '지급결제'...",
      "difficulty": 1,
      "sortOrder": 1,
      "optionsRequired": 4,
      "maxCorrect": 1,
      "optionShuffle": true,
      "answerExplanationMd": "은행의 기본 역할은...",
      "options": [
        {
          "label": "A",
          "contentMd": "첫 번째 선택지"
        },
        {
          "label": "B",
          "contentMd": "두 번째 선택지"
        },
        {
          "label": "C",
          "contentMd": "세 번째 선택지"
        },
        {
          "label": "D",
          "contentMd": "네 번째 선택지"
        }
      ]
    },
    {
      "id": 2,
      "type": "STORY",
      "stemMd": "대출이 필요해 은행을 찾은 민수...",
      "options": [...]
    },
    {
      "id": 3,
      "type": "ARTICLE",
      "stemMd": "아래 기사 내용에 따라 C와 D 금융기관의 금융...",
      "options": [...]
    },
    {
      "id": 4,
      "type": "CONCEPT",
      "stemMd": "다음 중 '핀테크(FinTech)'를 가장 잘 설명한 것...",
      "options": [...]
    }
  ]
}
```

---

## 🎯 핵심 특징

### 📊 데이터 구조
- **총 21개의 레벨** (7개 서브섹터 × 3개 레벨)
- **각 레벨마다 1개의 퀴즈**
- **각 퀴즈마다 4개의 문제**

### 🎚️ 난이도 설정
- **초급자**: `passing_score = 1` (4문제 중 1개 맞으면 통과)
- **중급자**: `passing_score = 2` (4문제 중 2개 맞으면 통과)
- **고급자**: `passing_score = 3` (4문제 중 3개 맞으면 통과)

### ⏰ 시간 제한
- **모든 레벨**: `time_limit_sec = 300` (5분)

### 📝 문제 유형
- **CONCEPT**: 개념 문제
- **STORY**: 스토리텔링 문제
- **ARTICLE**: 기사형 문제

---

## 🔧 기술적 구현

### Repository 레이어
```java
// 레벨별 퀴즈 조회
List<Quiz> findByLevelIdOrderById(Long levelId);

// 퀴즈 상세 조회 (N+1 문제 해결)
Optional<Quiz> findByIdWithDetails(@Param("quizId") Long quizId);
```

### Service 레이어
```java
private static final int QUESTIONS_PER_LEVEL = 4; // 레벨당 4문제
private static final int PASS_SCORE = 3; // 기본 통과 점수
```

### Controller 레이어
```java
@GetMapping("/sectors")                    // 섹터 목록
@GetMapping("/subsectors/{id}")           // 서브섹터 상세
@GetMapping("/levels/{levelId}/quizzes")  // 레벨별 퀴즈
@GetMapping("/quizzes/{id}")              // 퀴즈 상세
```

---

## 🚀 사용 예시

### 프론트엔드 구현 예시
```javascript
// 1. 섹터 목록 조회
const sectors = await fetch('/api/sectors').then(r => r.json());

// 2. 서브섹터 선택 후 레벨 목록 조회
const subsector = await fetch(`/api/subsectors/${subsectorId}`).then(r => r.json());

// 3. 레벨 선택 후 퀴즈 목록 조회
const levelQuizzes = await fetch(`/api/levels/${levelId}/quizzes?userId=${userId}`).then(r => r.json());

// 4. 퀴즈 선택 후 문제 조회
const quiz = await fetch(`/api/quizzes/${quizId}`).then(r => r.json());
```

---

## ✅ 완성된 기능

- [x] 서브섹터 선택
- [x] 레벨 선택 (초급자/중급자/고급자)
- [x] 4문제 퀴즈 구조
- [x] 레벨별 난이도 설정
- [x] 5분 제한 시간
- [x] 다양한 문제 유형 (CONCEPT/STORY/ARTICLE)
- [x] N+1 문제 해결을 위한 최적화된 쿼리
- [x] 완전한 API 플로우 구현

---

## 📞 문의사항

API 사용 중 문제가 발생하거나 추가 기능이 필요한 경우 개발팀에 문의해주세요.

**Happy Coding! 🚀**
