# Finsight Main Server

금융 교육 플랫폼의 메인 서버입니다.

## 🚀 기술 스택

- **Backend**: Spring Boot 3.2.0
- **Database**: MySQL 8.0 (H2 제거됨)
- **ORM**: Spring Data JPA
- **Security**: Spring Security (인증 비활성화)
- **Build Tool**: Gradle
- **Java**: 17

## 📋 주요 기능

### 퀴즈 시스템
- ✅ 퀴즈 조회 API (학습 패널 포함)
- ✅ 답안 제출 및 채점 API
- ✅ 사용자 답변 기록 저장
- ✅ 자동 사용자 생성 (테스트용)
- ✅ 데이터 영구 보존 (서버 재시작 시에도 유지)

### API 엔드포인트

#### 퀴즈 조회
```
GET /api/quizzes/{id}
```

#### 답안 제출
```
POST /api/quizzes/submit-answer
Content-Type: application/json

{
  "questionId": 1,
  "selectedOptionId": 1
}
```

## 🛠️ 실행 방법

### 1. 데이터베이스 설정
MySQL 데이터베이스를 실행하고 `findb` 데이터베이스를 생성합니다.

### 2. 애플리케이션 실행
```bash
./gradlew bootRun
```

### 3. 프로필 설정
- `local`: MySQL 데이터베이스 사용 (기본값)
- `prod`: 프로덕션 환경용 MySQL 설정

## 📁 프로젝트 구조

```
src/
├── main/
│   ├── java/
│   │   └── com/fintech/server/
│   │       ├── config/          # 설정 클래스
│   │       ├── controller/      # REST 컨트롤러
│   │       ├── dto/            # 데이터 전송 객체
│   │       ├── entity/         # JPA 엔티티
│   │       ├── repository/     # 데이터 접근 계층
│   │       ├── service/        # 비즈니스 로직
│   │       └── quiz/           # 퀴즈 관련 모듈
│   └── resources/
│       ├── application.yml     # 애플리케이션 설정
│       └── application-local.yml
└── test/
    └── java/
        └── com/fintech/server/
```

## 🔧 설정

### 데이터베이스 설정 (application-local.yml)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3307/findb?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: validate  # 기존 데이터 보존
    show-sql: true
    properties:
      hibernate:
        format_sql: true
    database-platform: org.hibernate.dialect.MySQL8Dialect
```

### user_answers 테이블 생성
```sql
CREATE TABLE user_answers (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  selected_option_id BIGINT,
  is_correct BOOLEAN NOT NULL,
  answered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (question_id) REFERENCES questions(id),
  FOREIGN KEY (selected_option_id) REFERENCES question_options(id)
);
```

## 📝 API 응답 예시

### 퀴즈 조회 응답
```json
{
    "id": 1,
    "title": "예금과 적금 개념 퀴즈",
    "questions": [
        {
            "id": 1,
            "stemMd": "'예금'과 '적금'의 사전적 뜻으로 가장 옳은 것은?",
            "answerExplanationMd": " \"예금은 보통 목돈을 한번에 맡기는 방식 적금 정해진 기간 동안 정기적으로 일정 금액을 납입하는 방식\"",
            "hintMd": null,
            "teachingExplainerMd": "**예금**이라는 단어에서 '예(預)'는 '미리 맡긴다'는 뜻이에요. 이미 목돈이 있는 상태에서 한 번에 맡기고, 일정 기간 후에 이자와 함께 돌려받는 방식이에요.\\n\\n**적금**의 '적(積)'은 '쌓는다'는 뜻이에요. 매달 꾸준히 돈을 부어 목돈을 쌓는 방식이고, 마찬가지로 만기 시 이자와 함께 돌려받아요.\\n\\n이 차이를 알면 둘 중 어느 구조가 자신에게 맞는지 상상해보는 것이 포인트예요.\\n\\n'돈을 어떻게 넣느냐'가 바로 핵심이에요.",
            "solvingKeypointsMd": "💡 **핵심 포인트**\\n\\n* **예금**\\n    = 목돈을 한 번에 넣고, 전액에 이자가 붙는 구조\\n* **적금**\\n    = 매달 일정 금액을 넣으며, 쌓인 금액에 점점 이자가 붙는 구조\\n\\n✓ 둘 다 저축 상품이고, 돈을 어떻게 넣느냐가 가장 중요한 차이점!",
            "options": [
                {
                    "id": 4,
                    "label": "D",
                    "contentMd": "예금은 돈을 빌리는 것이고, 적금은 돈을 저축하는 것이다."
                },
                {
                    "id": 3,
                    "label": "C",
                    "contentMd": "예금과 적금은 같은 뜻으로, 아무 차이가 없다."
                },
                {
                    "id": 2,
                    "label": "B",
                    "contentMd": "예금은 매달 돈을 내는 것이고, 적금은 한 번에 맡기는 것이다."
                },
                {
                    "id": 1,
                    "label": "A",
                    "contentMd": "예금은 은행에 돈을 한 번에 맡기는 것이고, 적금은 일정 기간 동안 나눠서 돈을 넣는 것이다."
                }
            ]
        }
    ]
}
```

### 답안 제출 응답
```json
{
  "isCorrect": true,
  "correctOptionId": 1,
  "feedback": "정답입니다! 예금과 적금의 차이를 잘 이해하셨네요."
}
```

## 🆕 최근 업데이트 (v1.1.0)

### ✨ 새로운 기능
- **학습 패널 지원**: 퀴즈 조회 시 힌트, 학습 패널, 핵심 포인트 정보 제공
- **데이터 영구 보존**: 서버 재시작 시에도 사용자 답변 기록 유지
- **자동 사용자 생성**: 테스트용 사용자 자동 생성 기능
- **H2 제거**: MySQL 전용으로 변경하여 데이터 일관성 향상

### 🔧 개선사항
- `ddl-auto`를 `validate`로 변경하여 기존 데이터 보존
- Question 엔티티의 options를 List로 변경하여 순서 보장
- Spring Security 인증 비활성화로 개발 편의성 향상
- API 응답에 새로운 필드들 추가

### 🐛 버그 수정
- `user_answers` 테이블 생성 문제 해결
- 답안 제출 시 발생하던 에러 수정
- 데이터베이스 연결 안정성 향상

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.
