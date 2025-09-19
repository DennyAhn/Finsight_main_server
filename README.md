# Finsight Main Server

금융 교육 플랫폼의 메인 서버입니다.

## 🚀 기술 스택

- **Backend**: Spring Boot 3.2.0
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA
- **Security**: Spring Security
- **Build Tool**: Gradle
- **Java**: 17

## 📋 주요 기능

### 퀴즈 시스템
- 퀴즈 조회 API
- 답안 제출 및 채점 API
- 사용자 답변 기록 저장

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
- `local`: MySQL 데이터베이스 사용
- `h2`: H2 인메모리 데이터베이스 사용 (테스트용)

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
    url: jdbc:mysql://localhost:3307/findb
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: create
    show-sql: true
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
      "answerExplanationMd": "예금은 보통 목돈을 한번에 맡기는 방식...",
      "options": [
        {
          "id": 1,
          "label": "A",
          "contentMd": "예금은 은행에 돈을 한 번에 맡기는 것이고..."
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

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.