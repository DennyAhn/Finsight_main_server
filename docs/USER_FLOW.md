# 🔄 사용자 플로우 가이드

## 📋 개요

FinTech 교육 플랫폼의 전체 사용자 플로우를 단계별로 설명합니다. JWT 토큰 기반 인증을 통해 일관된 사용자 경험을 제공합니다.

## 🎯 전체 플로우 다이어그램

```
📱 프론트엔드                    🖥️ 백엔드 서버                   🗄️ 데이터베이스
     │                              │                            │
1️⃣  │ ──── 게스트 로그인 요청 ────► │ AuthService                  │
     │                              │ ├─ 새 User 생성              │ ──► users 테이블
     │                              │ ├─ Account 생성              │ ──► accounts 테이블
     │                              │ └─ JWT 토큰 발급             │
     │ ◄──── 토큰 + userId 응답 ──── │                              │
     │                              │                              │
2️⃣  │ ──── 퀴즈 정보 조회 ────────► │ QuizService                  │
     │ ◄──── 퀴즈 데이터 응답 ────── │                              │ ◄── quizzes, questions 테이블
     │                              │                              │
3️⃣  │ ──── 답변 제출 (JWT 토큰) ──► │ JwtAuthenticationFilter      │
     │                              │ ├─ 토큰 검증                 │
     │                              │ ├─ 사용자 ID 추출            │
     │                              │ └─ SecurityContext 설정      │
     │                              │                              │
     │                              │ QuizService                  │
     │                              │ ├─ 답변 채점                 │
     │                              │ ├─ UserAnswer 저장           │ ──► user_answers 테이블
     │                              │ └─ 오답 시 WrongNote 생성    │ ──► user_wrong_notes 테이블
     │ ◄──── 채점 결과 응답 ────────│                              │
     │                              │                              │
4️⃣  │ ──── 오답 노트 조회 ────────► │ WrongNoteService             │
     │ ◄──── 오답 노트 목록 ────────│                              │ ◄── user_wrong_notes 테이블
```

## 🔐 1단계: 게스트 로그인

### API 호출
```http
POST /api/auth/guest
Content-Type: application/json
```

### 응답 예시
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI2MyIsImlhdCI6MTc1ODgwNjM3NSwiZXhwIjoxNzYxMzk4Mzc1fQ.5L7gVzWzNaXwHHFzyv-__kJOOVo7ZH1ZrB-PFveEf8M",
  "userId": 63
}
```

### 백엔드 처리 과정
1. `AuthService.createGuestUserAndLogin()` 호출
2. 새로운 `User` 엔티티 생성 (nickname: "익명의 사용자{timestamp}")
3. 연관된 `Account` 엔티티 생성 (12시간 만료)
4. JWT 토큰 생성 (사용자 ID를 subject로 설정)
5. 토큰과 사용자 ID 반환

### 프론트엔드 처리
- 받은 `accessToken`을 로컬 스토리지에 저장
- 이후 모든 API 호출 시 Authorization 헤더에 포함

## 📚 2단계: 퀴즈 정보 조회

### API 호출
```http
GET /api/quizzes/{quizId}
Authorization: Bearer {accessToken}
```

### 응답 예시
```json
{
  "id": 1,
  "title": "금융권 개념 퀴즈",
  "questions": [
    {
      "id": 1,
      "stemMd": "은행의 기본적인 역할 중 '여·수신'과 '지급결제'를 주로 수행하는 금융기관은 무엇인가?",
      "options": [
        {"id": 1, "label": "A", "contentMd": "보험사"},
        {"id": 2, "label": "B", "contentMd": "1금융권 은행"},
        {"id": 3, "label": "C", "contentMd": "저축은행"},
        {"id": 4, "label": "D", "contentMd": "증권사"}
      ]
    }
  ]
}
```

### 프론트엔드 처리
- 퀴즈 UI 렌더링
- 사용자 답변 선택 인터페이스 제공

## ✅ 3단계: 답변 제출 (핵심 단계)

### API 호출
```http
POST /api/quizzes/submit-answer
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "quizId": 1,
  "questionId": 1,
  "selectedOptionId": 1
}
```

### 백엔드 처리 과정 (상세)

#### 3.1 JWT 인증 필터 (`JwtAuthenticationFilter`)
1. Authorization 헤더에서 JWT 토큰 추출
2. `AuthService.getUserIdFromToken()`으로 사용자 ID 추출
3. 토큰 유효성 검증
4. Spring Security Context에 인증 정보 설정

#### 3.2 퀴즈 서비스 (`QuizService.submitAnswer`)
1. `getCurrentUserId()`로 인증된 사용자 ID 가져오기
   - JWT 토큰이 있으면: Security Context에서 추출
   - 토큰이 없으면: 요청 본문의 userId 사용 (Fallback)
2. 사용자 및 선택 옵션 조회
3. 정답 여부 판단
4. `UserAnswer` 엔티티 생성 및 저장
5. **오답인 경우**: `WrongNoteService.createOrUpdateWrongNote()` 호출
6. 채점 결과 반환

#### 3.3 오답 노트 서비스 (오답인 경우만)
1. 동일한 문제의 기존 오답 노트 확인
2. 기존 노트가 있으면: `timesWrong` 증가, `lastWrongAt` 업데이트
3. 기존 노트가 없으면: 새 `UserWrongNote` 생성
4. 문제의 학습 콘텐츠를 스냅샷으로 저장

### 응답 예시
```json
{
  "correct": false,
  "correctOptionId": 2,
  "feedback": "여·수신과 지급결제를 주로 수행하는 금융기관은 B. 1금융권 은행이 정답입니다."
}
```

### 중요한 특징
- **사용자 ID 일관성**: JWT 토큰을 통해 동일한 사용자 ID(63) 사용
- **자동 오답 노트 생성**: 틀린 답변 시 자동으로 상세한 학습 자료 포함
- **학습 콘텐츠 스냅샷**: 문제의 힌트, 해설, 핵심 포인트를 오답 노트에 저장

## 📝 4단계: 오답 노트 조회

### API 호출
```http
GET /api/wrong-notes?userId=63&page=0&size=20&filter=all
```

### 응답 예시
```json
{
  "wrongNotes": [
    {
      "id": 4,
      "userId": 63,
      "questionId": 1,
      "questionText": "은행의 기본적인 역할 중 '여·수신'과 '지급결제'를 주로 수행하는 금융기관은 무엇인가?",
      "lastAnswerOptionId": 1,
      "lastAnswerText": "보험사",
      "correctOptionId": 2,
      "correctAnswerText": "1금융권 은행",
      "timesWrong": 1,
      "firstWrongAt": "2025-09-25T22:20:31",
      "lastWrongAt": "2025-09-25T22:20:31",
      "resolved": false,
      "snapshotTeachingSummaryMd": "자, 여러분! 오늘은 은행의 기본적인 역할을 정리해봅시다.",
      "snapshotTeachingExplainerMd": "은행은 크게 두 가지 중요한 일을 하고 있어요...",
      "snapshotKeypointsMd": "- 1금융권 = 주요 은행, 2금융권 = 저축은행, 보험사 등...",
      "allOptions": [
        {"id": 1, "text": "보험사", "isCorrect": false},
        {"id": 2, "text": "1금융권 은행", "isCorrect": true},
        {"id": 3, "text": "저축은행", "isCorrect": false},
        {"id": 4, "text": "증권사", "isCorrect": false}
      ]
    }
  ],
  "statistics": {
    "totalCount": 1,
    "unresolvedCount": 1,
    "resolvedCount": 0,
    "needReviewCount": 1
  }
}
```

### 프론트엔드 활용
- 오답 노트 목록 표시
- 개별 오답 노트 상세 보기
- 개인 메모 작성/수정 기능
- 해결 상태 토글 기능

## 🔄 5단계: 추가 학습 플로우

### 5.1 개인 메모 작성
```http
PUT /api/wrong-notes/{noteId}/personal-note?userId=63
Content-Type: text/plain

예금과 적금의 차이점을 정확히 기억하자. 예금은 목돈을 한번에, 적금은 매달 일정 금액을 넣는 방식이다.
```

### 5.2 해결 상태 변경
```http
PUT /api/wrong-notes/{noteId}/toggle-resolved?userId=63
```

### 5.3 복습 완료 처리
```http
PUT /api/wrong-notes/{noteId}/mark-reviewed?userId=63
```

## 🔍 데이터베이스 변화 추적

### 게스트 로그인 후
```sql
-- users 테이블
INSERT INTO users (id, nickname, email, is_guest, created_at) 
VALUES (63, '익명의 사용자1758806375', 'guest_uuid@example.com', true, NOW());

-- accounts 테이블  
INSERT INTO accounts (user_id, email, status, expires_at) 
VALUES (63, 'guest_uuid@example.com', 'active', DATE_ADD(NOW(), INTERVAL 12 HOUR));
```

### 오답 제출 후
```sql
-- user_answers 테이블
INSERT INTO user_answers (user_id, question_id, selected_option_id, is_correct) 
VALUES (63, 1, 1, false);

-- user_wrong_notes 테이블 (오답인 경우)
INSERT INTO user_wrong_notes (
  user_id, question_id, last_answer_option_id, correct_option_id,
  times_wrong, first_wrong_at, last_wrong_at, resolved,
  snapshot_teaching_summary_md, snapshot_teaching_explainer_md, snapshot_keypoints_md
) VALUES (
  63, 1, 1, 2, 1, NOW(), NOW(), false,
  '자, 여러분! 오늘은 은행의 기본적인 역할을 정리해봅시다.',
  '은행은 크게 두 가지 중요한 일을 하고 있어요...',
  '- 1금융권 = 주요 은행, 2금융권 = 저축은행, 보험사 등...'
);
```

## ⚡ 성능 및 최적화

### JWT 토큰 관리
- **만료 시간**: 30일 (설정 가능)
- **토큰 갱신**: 현재 미구현 (향후 추가 예정)
- **토큰 저장**: 프론트엔드 로컬 스토리지

### 데이터베이스 최적화
- 사용자별 오답 노트 조회 시 인덱스 활용
- 페이징 처리로 대용량 데이터 효율적 처리
- N+1 문제 방지를 위한 JOIN 쿼리 최적화

### 캐싱 전략 (향후 개선)
- 퀴즈 데이터 Redis 캐싱
- JWT 토큰 블랙리스트 관리
- 자주 조회되는 오답 노트 통계 캐싱

## 🛠️ 개발자 가이드

### 로그 추적
주요 로그 포인트:
```
JWT 토큰 인증 성공: userId=63
JWT 토큰에서 사용자 ID 추출: 63  
오답 노트 자동 생성 완료: userId=63, questionId=1
오답 노트 저장 완료: userId=63, questionId=1, timesWrong=1
```

### 에러 처리
- JWT 토큰 만료: Fallback으로 userId 필드 사용
- 사용자 미존재: RuntimeException 발생
- 오답 노트 생성 실패: 로그 기록 후 답변 제출은 성공 처리

### 테스트 방법
1. **단위 테스트**: 각 서비스 메서드별 테스트
2. **통합 테스트**: API 호출부터 DB 저장까지 전체 플로우
3. **부하 테스트**: 동시 사용자 시나리오 테스트

---

**문서 버전**: 1.0.0  
**작성일**: 2025-09-25  
**작성자**: FinTech 개발팀
