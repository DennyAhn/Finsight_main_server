# 🚀 Finsight API Documentation

> **금융 교육 플랫폼 백엔드 서버** - 포트폴리오용 API 명세서

---

## 📋 프로젝트 개요

### 🎯 서비스 소개
**Finsight**는 금융 교육을 위한 인터랙티브 퀴즈 플랫폼입니다. 사용자가 단계별로 금융 지식을 학습하고, 실시간 채점과 오답 노트를 통해 효과적인 학습을 지원합니다.

### 🛠️ 기술 스택
- **Backend**: Spring Boot 3.2, Java 17
- **Database**: MySQL 8.0
- **Authentication**: JWT
- **Documentation**: Swagger UI
- **Deployment**: Docker, AWS EC2

### 🌐 서버 정보
| 환경 | URL | 상태 |
|------|-----|------|
| **로컬 개발** | `http://localhost:8080/api` | ✅ 활성 |
| **프로덕션** | `http://54.180.103.186:8080/api` | ✅ 활성 |
| **Swagger UI** | `http://54.180.103.186:8080/api/swagger-ui/index.html` | ✅ 활성 |

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

#### 로그인
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

**응답 예시:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 123
}
```

**특징:**
- ✅ **다중 사용자 지원**: 각 브라우저별 독립적인 계정 관리
- ✅ **닉네임 일관성**: 다른 서버 갔다가 돌아와도 같은 닉네임 유지
- ✅ **자동 계정 재사용**: 기존 계정이 있으면 자동으로 재사용
- ✅ **만료 시간 연장**: 사용할 때마다 12시간씩 연장
- ✅ **에러 처리**: 재사용 실패 시 자동으로 새 계정 생성

---

### 📚 **학습 콘텐츠 조회**

#### 전체 섹터 조회
```http
GET /api/sectors
```

#### 서브섹터 상세 정보
```http
GET /api/subsectors/{subsectorId}
```

#### 레벨별 퀴즈 목록 (사용자 진행상황 포함)
```http
GET /api/levels/{levelId}/quizzes?userId={userId}
```

---

### 🧩 **퀴즈 실행**

#### 퀴즈 문제 조회
```http
GET /api/quizzes/{quizId}
```

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

#### 퀴즈 완료 처리
```http
POST /api/quizzes/{quizId}/complete?userId={userId}
```

---

### 📊 **진행률 관리**

#### 레벨 진행상황 조회 (징검다리 포함)
```http
GET /api/levels/{levelId}/progress?userId={userId}
```

#### 서브섹터별 진행률 조회
```http
GET /api/progress/user/me/subsector/{subsectorId}
```

#### 사용자 전체 진행률 조회
```http
GET /api/progress/user/me
```

---

### 📝 **오답 노트 관리**

#### 오답 노트 목록 조회
```http
GET /api/wrong-notes?userId={userId}&page=0&size=20
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

---

### 🏅 **배지 시스템**

#### 사용자 현재 배지 조회
```http
GET /api/badges/user/{userId}/current
```

#### 사용자 모든 배지 목록 (진행률 포함)
```http
GET /api/badges/user/{userId}/all
```

#### 배지 시스템 초기화 (관리자용)
```http
POST /api/badges/init
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
GET /api/community/posts?page=0&size=20&tag=예금
```

---

### 📈 **대시보드 & 통계**

#### 사용자 대시보드
```http
GET /api/dashboard?userId={userId}
```

#### 오답 노트 통계
```http
GET /api/wrong-notes/statistics?userId={userId}
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
- **개발 편의**: 대부분 API에서 `userId` 파라미터로 접근 가능


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
- **헬스 체크**: `/api/health`
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
- ✅ **11개 주요 API 그룹** 구현
- ✅ **50+ 엔드포인트** 개발
- ✅ **실시간 채점 시스템** 구축
- ✅ **징검다리 진행률 시스템** 구현
- ✅ **6단계 배지 시스템** 완성
- ✅ **오답 노트 관리 시스템** 구축
- ✅ **커뮤니티 기능** 추가
- ✅ **다중 사용자 지원 게스트 로그인** 구현
- ✅ **닉네임 일관성 보장** 시스템

### 기술적 성과
- 🏗️ **계층적 아키텍처**: Controller → Service → Repository 패턴
- 🔒 **보안**: JWT 인증, CORS 설정, 입력값 검증
- 📊 **성능**: 쿼리 최적화, 트랜잭션 관리
- 🧪 **테스트**: 단위 테스트, 통합 테스트 구현
- 📚 **문서화**: Swagger UI, 상세 API 문서

---

## 📞 **연락처**

- **프로젝트**: [GitHub Repository]
- **API 테스트**: [Swagger UI](http://54.180.103.186:8080/api/swagger-ui/index.html)
- **이메일**: support@finsight.com

---

**API 버전**: 1.4.0  
**최종 업데이트**: 2025-01-15  
**개발 기간**: 2024.12 ~ 2025.01  
**개발자**: Finsight Development Team