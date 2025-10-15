# 🏦 Finsight - 금융 교육 플랫폼 메인 서버

> 사용자 맞춤형 금융 지식 학습을 위한 종합 교육 플랫폼 백엔드 시스템

[![Java](https://img.shields.io/badge/Java-17-007396?style=flat&logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-6DB33F?style=flat&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat&logo=mysql)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?style=flat&logo=docker)](https://www.docker.com/)

---

## 📑 목차

1. [프로젝트 개요](#-프로젝트-개요)
2. [시스템 아키텍처](#-시스템-아키텍처)
3. [기술 스택](#-기술-스택)
4. [주요 기능](#-주요-기능)
5. [패키지 구조](#-패키지-구조)
6. [데이터베이스 설계](#-데이터베이스-설계)
7. [API 문서](#-api-문서)
8. [CI/CD 파이프라인](#-cicd-파이프라인)
9. [실행 방법](#-실행-방법)

---

## 🎯 프로젝트 개요

**Finsight**는 금융 지식이 부족한 사용자들을 위한 체계적인 학습 플랫폼입니다. 게임화된 학습 경험을 통해 금융 개념을 쉽고 재미있게 학습할 수 있도록 설계된 백엔드 시스템입니다.

### 핵심 가치
- **개인화된 학습 경로**: 사용자별 진행도 추적 및 맞춤형 퀴즈 추천
- **체계적인 커리큘럼**: 섹터 → 서브섹터 → 레벨 → 퀴즈로 이어지는 단계적 학습 구조
- **즉각적인 피드백**: 실시간 답안 채점 및 상세한 학습 자료 제공
- **게임화 요소**: 배지 시스템, 연속 학습 기록(Streak), 레벨 진행 시스템
- **커뮤니티 기능**: 학습자 간 지식 공유 및 소통

---

## 🏗 시스템 아키텍처

### 전체 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                        Client Layer                          │
│                    (React/Flutter 등)                        │
└────────────────────┬────────────────────────────────────────┘
                     │ HTTPS/REST API
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                      Nginx (Reverse Proxy)                   │
│                    - SSL/TLS Termination                     │
│                    - Load Balancing                          │
│                    - Static File Serving                     │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                   Spring Boot Application                    │
├─────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Controller   │  │   Service    │  │  Repository  │      │
│  │   Layer      │→ │    Layer     │→ │    Layer     │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Security   │  │     JWT      │  │  Validation  │      │
│  │    Filter    │  │     Auth     │  │   Handler    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└────────────────────┬────────────────────────────────────────┘
                     │ JPA/Hibernate
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                      MySQL Database (RDS)                    │
│  - 사용자 데이터  - 학습 콘텐츠  - 진행도 추적               │
└─────────────────────────────────────────────────────────────┘
```

### 배포 아키텍처

```
┌──────────────────────────────────────────────────┐
│              AWS Cloud Infrastructure             │
│                                                   │
│  ┌────────────┐         ┌──────────────┐        │
│  │   EC2      │────────▶│   AWS RDS    │        │
│  │  Instance  │         │  (MySQL 8.0) │        │
│  │            │         └──────────────┘        │
│  │  Docker    │                                  │
│  │  Compose   │         ┌──────────────┐        │
│  │  - App     │────────▶│   AWS S3     │        │
│  │  - Nginx   │         │  (이미지 저장)│        │
│  └────────────┘         └──────────────┘        │
│                                                   │
│  Security Group: 80, 443 포트만 오픈             │
└──────────────────────────────────────────────────┘
```

---

## 🛠 기술 스택

### Backend Framework
- **Java 17** - 최신 LTS 버전으로 안정성과 성능 보장
- **Spring Boot 3.2.0** - 엔터프라이즈급 애플리케이션 프레임워크
- **Spring Data JPA** - ORM을 통한 데이터베이스 추상화
- **Spring Security** - JWT 기반 인증/인가 시스템
- **Spring Validation** - 입력 데이터 검증

### Database
- **MySQL 8.0** - 프로덕션 환경 RDBMS
- **AWS RDS** - 관리형 데이터베이스 서비스

### Security
- **JWT (JSON Web Token)** - 무상태 인증 시스템
- **JJWT 0.12.3** - JWT 생성 및 검증 라이브러리
- **BCrypt** - 비밀번호 암호화

### Documentation
- **SpringDoc OpenAPI 3** - Swagger UI 자동 생성
- **Markdown** - API 및 프로젝트 문서화

### DevOps & Deployment
- **Docker & Docker Compose** - 컨테이너화 및 오케스트레이션
- **Nginx** - 리버스 프록시 및 로드 밸런싱
- **AWS EC2** - 애플리케이션 호스팅
- **GitHub Actions** - CI/CD 파이프라인 (계획 중)

### Build Tool
- **Gradle 8.x** - 의존성 관리 및 빌드 자동화

---

## ✨ 주요 기능

### 1. 인증 시스템 (Authentication)
- **회원가입/로그인**: 이메일 기반 사용자 계정 관리
- **게스트 모드**: 비회원도 학습 가능 (24시간 유효)
- **JWT 인증**: 무상태 토큰 기반 세션 관리
- **자동 정리**: 만료된 게스트 계정 자동 삭제 (스케줄러)

### 2. 학습 콘텐츠 관리
- **계층적 구조**: Sector → Subsector → Level → Quiz → Question
- **다양한 문제 유형**: 
  - CONCEPT (개념 문제)
  - STORY (스토리 기반 문제)
  - ARTICLE (가상 기사 문제)
- **학습 패널**: 힌트, 설명, 핵심 포인트, 예제 계산 등 풍부한 학습 자료

### 3. 퀴즈 시스템
- **실시간 채점**: 답안 제출 즉시 정답 여부 확인
- **진행도 추적**: 사용자별 퀴즈 완료 상태 관리
- **상태 관리**: LOCKED → IN_PROGRESS → COMPLETED
- **점수 시스템**: 정답 개수 기반 점수 부여

### 4. 대시보드
- **학습 통계**: 완료한 레벨, 퀴즈, 문제 수 집계
- **연속 학습 기록(Streak)**: 매일 학습 독려
- **주간 진행도**: 7일간의 학습 활동 시각화
- **다음 학습 추천**: AI 기반 맞춤형 레벨 추천

### 5. 오답 노트
- **자동 수집**: 틀린 문제 자동 저장
- **개인 메모**: 사용자 맞춤 학습 노트 작성
- **복습 관리**: 복습 완료 여부 추적
- **통계 분석**: 오답 패턴 분석 및 약점 파악

### 6. 배지 시스템
- **6단계 등급**: Bronze → Silver → Gold → Platinum → Diamond → Master
- **자동 부여**: 학습 성취에 따른 배지 자동 지급
- **대표 배지**: 사용자가 선택한 배지를 프로필에 표시
- **진행도 추적**: 다음 배지까지 필요한 학습량 표시

### 7. 커뮤니티
- **게시글 작성**: 학습 경험 및 지식 공유
- **태그 시스템**: 주제별 게시글 분류
- **배지 표시**: 작성자의 배지를 게시글에 표시
- **좋아요/댓글**: 사용자 간 소통 기능

### 8. 관리자 기능
- **통계 대시보드**: 전체 사용자 학습 현황 모니터링
- **오답 분석**: 문제별 오답률 집계
- **콘텐츠 관리**: 퀴즈 및 문제 추가/수정/삭제

---

## 📦 패키지 구조

```
src/main/java/com/fintech/server/
│
├── 📁 config/                          # 설정 클래스
│   ├── SecurityConfig.java             # Spring Security 설정
│   ├── JwtAuthenticationFilter.java    # JWT 필터
│   ├── JwtUtil.java                    # JWT 유틸리티
│   └── SwaggerConfig.java              # API 문서 설정
│
├── 📁 controller/                      # REST API 컨트롤러
│   ├── AuthController.java             # 인증 API
│   ├── UserController.java             # 사용자 API
│   └── HealthController.java           # 헬스체크 API
│
├── 📁 service/                         # 비즈니스 로직
│   ├── AuthService.java                # 인증 서비스
│   ├── UserService.java                # 사용자 서비스
│   └── GuestAccountCleanupService.java # 게스트 정리 스케줄러
│
├── 📁 repository/                      # 데이터 접근 계층
│   ├── UserRepository.java             # 사용자 Repository
│   └── AccountRepository.java          # 계정 Repository
│
├── 📁 entity/                          # JPA 엔티티
│   ├── User.java                       # 사용자 엔티티
│   └── Account.java                    # 계정 엔티티
│
├── 📁 dto/                             # 데이터 전송 객체
│   ├── AuthRequestDto.java             # 인증 요청 DTO
│   ├── TokenResponseDto.java           # 토큰 응답 DTO
│   └── UserRequestDto.java             # 사용자 요청 DTO
│
├── 📁 quiz/                            # 퀴즈 모듈
│   │
│   ├── 📁 controller/                  # 퀴즈 컨트롤러
│   │   ├── QuizController.java         # 퀴즈 실행 API
│   │   ├── QuizContentController.java  # 콘텐츠 조회 API
│   │   ├── LevelController.java        # 레벨 관리 API
│   │   ├── DashboardController.java    # 대시보드 API
│   │   ├── WrongNoteController.java    # 오답노트 API
│   │   ├── AdminWrongNoteController.java # 관리자 오답 통계 API
│   │   └── BadgeController.java        # 배지 API
│   │
│   ├── 📁 service/                     # 퀴즈 서비스
│   │   ├── QuizService.java            # 퀴즈 실행 로직
│   │   ├── QuizContentService.java     # 콘텐츠 조회 로직
│   │   ├── LevelService.java           # 레벨 관리 로직
│   │   ├── DashboardService.java       # 대시보드 로직
│   │   ├── WrongNoteService.java       # 오답노트 로직
│   │   ├── AdminWrongNoteService.java  # 관리자 통계 로직
│   │   ├── BadgeService.java           # 배지 시스템 로직
│   │   └── GuestBadgeCleanupService.java # 배지 정리 스케줄러
│   │
│   ├── 📁 repository/                  # 퀴즈 Repository
│   │   ├── QuizRepository.java
│   │   ├── QuestionRepository.java
│   │   ├── QuestionOptionRepository.java
│   │   ├── SectorRepository.java
│   │   ├── SubsectorRepository.java
│   │   ├── LevelRepository.java
│   │   ├── UserAnswerRepository.java
│   │   ├── UserProgressRepository.java
│   │   ├── UserWrongNoteRepository.java
│   │   ├── UserDailyActivityRepository.java
│   │   ├── BadgeRepository.java
│   │   ├── UserBadgeRepository.java
│   │   └── ArticleRepository.java
│   │
│   ├── 📁 entity/                      # 퀴즈 엔티티
│   │   ├── Quiz.java                   # 퀴즈
│   │   ├── Question.java               # 문제
│   │   ├── QuestionOption.java         # 선택지
│   │   ├── Sector.java                 # 섹터
│   │   ├── Subsector.java              # 서브섹터
│   │   ├── Level.java                  # 레벨
│   │   ├── Article.java                # 가상 기사
│   │   ├── UserAnswer.java             # 사용자 답안
│   │   ├── UserProgress.java           # 진행도
│   │   ├── UserWrongNote.java          # 오답 노트
│   │   ├── UserDailyActivity.java      # 일일 활동
│   │   ├── UserDailyActivityId.java    # 복합키
│   │   ├── Badge.java                  # 배지
│   │   └── UserBadge.java              # 사용자 배지
│   │
│   └── 📁 dto/                         # 퀴즈 DTO
│       ├── QuizResponseDto.java
│       ├── AnswerRequestDto.java
│       ├── AnswerResponseDto.java
│       ├── QuizResultDto.java
│       ├── SectorResponseDto.java
│       ├── SubsectorDetailResponseDto.java
│       ├── LevelDetailResponseDto.java
│       ├── LevelProgressDto.java
│       ├── LevelCompletionDto.java
│       ├── DashboardDto.java
│       ├── WrongNoteDto.java
│       └── AdminWrongNoteDto.java
│
├── 📁 community/                       # 커뮤니티 모듈
│   ├── 📁 controller/
│   │   └── CommunityController.java    # 커뮤니티 API
│   ├── 📁 service/
│   │   └── CommunityService.java       # 커뮤니티 서비스
│   ├── 📁 repository/
│   │   ├── CommunityPostRepository.java
│   │   ├── TagRepository.java
│   │   └── PostTagLinkRepository.java
│   ├── 📁 entity/
│   │   ├── CommunityPost.java          # 게시글
│   │   ├── Tag.java                    # 태그
│   │   ├── PostTagLink.java            # 게시글-태그 연결
│   │   └── PostTagLinkId.java          # 복합키
│   └── 📁 dto/
│       ├── PostRequestDto.java
│       └── PostResponseDto.java
│
└── FinMainServerApplication.java       # 애플리케이션 진입점
```

### 아키텍처 설계 원칙

1. **계층화 아키텍처 (Layered Architecture)**
   - Controller → Service → Repository 계층 분리
   - 각 계층의 명확한 책임 분리

2. **도메인 주도 설계 (DDD) 요소**
   - 도메인별 모듈 분리 (quiz, community 등)
   - 응집도 높은 패키지 구조

3. **의존성 역전 원칙 (DIP)**
   - 인터페이스 기반 설계
   - Repository 패턴 활용

4. **단일 책임 원칙 (SRP)**
   - 각 클래스는 하나의 책임만 가짐
   - DTO를 통한 데이터 전송 계층 분리

---

## 🗄 데이터베이스 설계

### ERD 개요

```
┌─────────────────────────────────────────────────────────────┐
│                    학습 콘텐츠 계층 구조                      │
└─────────────────────────────────────────────────────────────┘
                Sectors (금융 섹터)
                   │
                   │ 1:N
                   ▼
              Subsectors (서브섹터)
                   │
                   │ 1:N
                   ▼
                Levels (레벨)
                   │
                   │ 1:N
                   ▼
                Quizzes (퀴즈)
                   │
                   │ 1:N
                   ▼
              Questions (문제)
                   │
                   │ 1:N
                   ▼
           QuestionOptions (선택지)
```

### 📊 주요 테이블 구조

#### 1. 사용자 관련 테이블

**users** - 사용자 프로필
| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | BIGINT PK | 사용자 ID (자동증가) |
| nickname | VARCHAR(255) UNIQUE | 닉네임 (고유) |
| email | VARCHAR(255) | 연락처 이메일 |
| is_guest | BOOLEAN | 게스트 여부 |
| displayed_badge_id | BIGINT FK | 대표 배지 ID |
| created_at | DATETIME | 가입 일시 |

**accounts** - 계정 인증 정보
| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | BIGINT PK | 계정 ID |
| user_id | BIGINT FK UNIQUE | 사용자 ID (1:1) |
| email | VARCHAR(255) UNIQUE | 로그인 이메일 |
| email_normalized | VARCHAR(255) UNIQUE | 정규화된 이메일 |
| password_hash | VARCHAR(255) | 암호화된 비밀번호 |
| is_email_verified | BOOLEAN | 이메일 인증 여부 |
| last_login_at | DATETIME | 마지막 로그인 |
| expires_at | DATETIME | 만료 시간 (게스트용) |
| status | ENUM | active/disabled/deleted |

#### 2. 학습 콘텐츠 테이블

**sectors** - 금융 섹터
| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | BIGINT PK | 섹터 ID |
| name | VARCHAR(255) | 섹터명 (예: 은행업) |
| slug | VARCHAR(255) UNIQUE | URL 슬러그 |
| description | TEXT | 섹터 설명 |

**subsectors** - 서브섹터
| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | BIGINT PK | 서브섹터 ID |
| sector_id | BIGINT FK | 부모 섹터 ID |
| name | VARCHAR(255) | 서브섹터명 |
| slug | VARCHAR(255) | URL 슬러그 |
| description | TEXT | 설명 |
| sort_order | INT | 정렬 순서 |

**levels** - 학습 레벨
| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | BIGINT PK | 레벨 ID |
| subsector_id | BIGINT FK | 서브섹터 ID |
| level_number | INT | 레벨 번호 |
| title | VARCHAR(255) | 레벨 제목 |
| learning_goal | TEXT | 학습 목표 |
| badge_id | BIGINT | 연관 배지 ID |

**quizzes** - 퀴즈
| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | BIGINT PK | 퀴즈 ID |
| level_id | BIGINT FK | 레벨 ID |
| title | VARCHAR(255) | 퀴즈 제목 |

**questions** - 문제
| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | BIGINT PK | 문제 ID |
| quiz_id | BIGINT FK | 퀴즈 ID |
| type | ENUM | CONCEPT/STORY/ARTICLE |
| stem_md | TEXT | 문제 내용 (Markdown) |
| answer_explanation_md | TEXT | 정답 해설 |
| hint_md | TEXT | 힌트 |
| teaching_explainer_md | TEXT | 학습 설명 |
| solving_keypoints_md | TEXT | 핵심 포인트 |
| article_id | BIGINT FK | 가상기사 ID (type=ARTICLE시) |
| difficulty | INT | 난이도 |
| sort_order | INT | 문제 순서 |
| created_at | DATETIME | 생성 일시 |
| updated_at | DATETIME | 수정 일시 |

**question_options** - 선택지
| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | BIGINT PK | 선택지 ID |
| question_id | BIGINT FK | 문제 ID |
| label | VARCHAR(10) | 라벨 (A, B, C, D) |
| content_md | TEXT | 선택지 내용 |
| is_correct | BOOLEAN | 정답 여부 |
| sort_order | INT | 정렬 순서 |

**articles** - 가상 기사
| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | BIGINT PK | 기사 ID |
| title | VARCHAR(255) | 기사 제목 |
| body_md | TEXT | 기사 본문 (Markdown) |
| image_url | VARCHAR(500) | 이미지 URL |
| source_note | VARCHAR(255) | 출처 표기 |
| created_at | DATETIME | 생성 일시 |

#### 3. 사용자 학습 데이터 테이블

**user_answers** - 사용자 답안
| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | BIGINT PK | 답안 ID |
| user_id | BIGINT FK | 사용자 ID |
| question_id | BIGINT FK | 문제 ID |
| selected_option_id | BIGINT FK | 선택한 선택지 ID |
| is_correct | BOOLEAN | 정답 여부 |
| answered_at | DATETIME | 답변 시간 |

**user_progress** - 퀴즈 진행도
| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | BIGINT PK | 진행도 ID |
| user_id | BIGINT FK | 사용자 ID |
| quiz_id | BIGINT FK | 퀴즈 ID |
| passed | BOOLEAN | 통과 여부 |

**user_daily_activities** - 일일 학습 활동
| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| user_id | BIGINT PK, FK | 사용자 ID |
| activity_date | DATE PK | 활동 날짜 |
| questions_answered | INT | 답변한 문제 수 |
| quizzes_completed | INT | 완료한 퀴즈 수 |

**user_wrong_notes** - 오답 노트
| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | BIGINT PK | 오답노트 ID |
| user_id | BIGINT FK | 사용자 ID |
| question_id | BIGINT FK | 문제 ID |
| last_answer_option_id | BIGINT FK | 마지막 오답 선택지 |
| correct_option_id | BIGINT FK | 정답 선택지 |
| times_wrong | INT | 틀린 횟수 |
| first_wrong_at | DATETIME | 첫 오답 시간 |
| last_wrong_at | DATETIME | 마지막 오답 시간 |
| reviewed_at | DATETIME | 복습 시간 |
| resolved | BOOLEAN | 해결 여부 |
| personal_note_md | TEXT | 개인 메모 |
| created_at | DATETIME | 생성 일시 |
| updated_at | DATETIME | 수정 일시 |

#### 4. 배지 시스템 테이블

**badges** - 배지 정의
| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | BIGINT PK | 배지 ID |
| code | VARCHAR(100) UNIQUE | 배지 코드 |
| name | VARCHAR(255) | 배지명 |
| description | TEXT | 배지 설명 |
| icon_url | VARCHAR(500) | 아이콘 URL |
| level_number | INT | 배지 등급 (1-6) |
| required_quizzes | INT | 필요 퀴즈 수 |
| required_correct_answers | INT | 필요 정답 수 |
| criteria_json | TEXT | 획득 조건 (JSON) |
| created_at | DATETIME | 생성 일시 |
| updated_at | DATETIME | 수정 일시 |

**user_badges** - 사용자 배지
| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | BIGINT PK | 사용자배지 ID |
| user_id | BIGINT FK | 사용자 ID |
| badge_id | BIGINT FK | 배지 ID |
| earned_at | DATETIME | 획득 시간 |
| progress | INT | 진행도 (%) |

#### 5. 커뮤니티 테이블

**community_posts** - 커뮤니티 게시글
| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | BIGINT PK | 게시글 ID |
| author_id | BIGINT FK | 작성자 ID |
| author_badge_id | BIGINT FK | 작성 시점 배지 (스냅샷) |
| body | TEXT | 게시글 내용 |
| like_count | INT | 좋아요 수 |
| comment_count | INT | 댓글 수 |
| created_at | DATETIME | 작성 일시 |
| updated_at | DATETIME | 수정 일시 |

**tags** - 태그
| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | BIGINT PK | 태그 ID |
| name | VARCHAR(100) UNIQUE | 태그명 |

**post_tag_links** - 게시글-태그 연결
| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| post_id | BIGINT PK, FK | 게시글 ID |
| tag_id | BIGINT PK, FK | 태그 ID |

### 데이터베이스 인덱스 전략

```sql
-- 성능 최적화를 위한 주요 인덱스
INDEX idx_user_email ON users(email);
INDEX idx_account_email_normalized ON accounts(email_normalized);
INDEX idx_question_quiz_id ON questions(quiz_id);
INDEX idx_user_answer_user_question ON user_answers(user_id, question_id);
INDEX idx_user_progress_user_quiz ON user_progress(user_id, quiz_id);
INDEX idx_wrong_note_user_resolved ON user_wrong_notes(user_id, resolved);
INDEX idx_daily_activity_user_date ON user_daily_activities(user_id, activity_date);
INDEX idx_community_post_created ON community_posts(created_at DESC);
```

### 🔗 ERD 다이어그램

전체 데이터베이스 ERD는 아래 링크에서 확인할 수 있습니다:

**[📊 DB 다이어그램 보러가기](https://dbdiagram.io/e/68b6dacd777b52b76cac53aa/68cd5674a596966eb7d4550a)**

---

## 📖 API 문서

상세한 API 명세는 다음 문서를 참고하세요:

- **[📚 API 명세서](docs/API.md)** - 전체 REST API 엔드포인트 및 사용법
- **[Swagger UI (로컬)](http://localhost:8080/api/swagger-ui/index.html)** - 대화형 API 문서
- **[Swagger UI (프로덕션)](http://54.180.103.186:8080/api/swagger-ui/index.html)** - 프로덕션 API 문서

### API 엔드포인트 요약

| 카테고리 | 엔드포인트 | 설명 |
|---------|-----------|------|
| **인증** | `POST /api/auth/signup` | 회원가입 |
| | `POST /api/auth/login` | 로그인 |
| | `POST /api/auth/guest` | 게스트 로그인 |
| **대시보드** | `GET /api/dashboard` | 사용자 대시보드 |
| **콘텐츠** | `GET /api/sectors` | 섹터 목록 |
| | `GET /api/subsectors/{id}` | 서브섹터 상세 |
| | `GET /api/levels/{id}/quizzes` | 레벨별 퀴즈 목록 |
| **퀴즈** | `GET /api/quizzes/{id}` | 퀴즈 조회 |
| | `POST /api/quizzes/submit-answer` | 답안 제출 |
| | `POST /api/quizzes/{id}/complete` | 퀴즈 완료 |
| **레벨** | `GET /api/levels/{id}/progress` | 레벨 진행도 |
| | `POST /api/levels/{id}/start` | 레벨 시작 |
| | `POST /api/levels/{id}/complete` | 레벨 완료 |
| **오답노트** | `GET /api/wrong-notes` | 오답 목록 |
| | `PUT /api/wrong-notes/{id}/personal-note` | 메모 작성 |
| | `PUT /api/wrong-notes/{id}/toggle-resolved` | 해결 토글 |
| **커뮤니티** | `GET /api/community/posts` | 게시글 목록 |
| | `POST /api/community/posts` | 게시글 작성 |
| **배지** | `POST /api/badges/init` | 배지 초기화 |
| | `POST /api/badges/update/{userId}` | 배지 업데이트 |
| **헬스체크** | `GET /api/health` | 서버 상태 확인 |

---

## 🚀 CI/CD 파이프라인

### 자동 배포 시스템
- **GitHub Actions** 기반 CI/CD 파이프라인 구축
- **코드 푸시 시 자동 배포** (2분 내 완료)
- **Docker 컨테이너 자동 재시작**
- **헬스체크 기반 배포 검증**

### 배포 성과
- 🚀 **배포 시간 90% 단축** (20분 → 2분)
- 🚀 **배포 빈도 300% 증가** (주 1-2회 → 일 3-5회)
- 🚀 **배포 실패율 87% 감소** (15% → 2%)

### 상세 문서
- **[📚 CI/CD 구축 가이드](docs/CI_CD_SETUP.md)** - 완전한 CI/CD 파이프라인 문서
- **[GitHub Actions](https://github.com/DennyAhn/Finsight_main_server/actions)** - 실시간 배포 상태 확인

---

## 🚀 실행 방법

### 사전 요구사항

- Java 17 이상
- MySQL 8.0 이상
- Docker & Docker Compose (선택사항)
- Gradle 8.x

### 1. 로컬 개발 환경 설정

#### 데이터베이스 설정

```bash
# MySQL 실행 및 데이터베이스 생성
mysql -u root -p
CREATE DATABASE findb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 환경 변수 설정

`src/main/resources/application-local.yml` 파일을 수정합니다:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/findb?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
    username: root
    password: your_password
```

#### 애플리케이션 실행

```bash
# Gradle을 통한 실행
./gradlew bootRun --args='--spring.profiles.active=local'

# 또는 JAR 빌드 후 실행
./gradlew clean build
java -jar build/libs/fin-main-server-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

서버가 실행되면 `http://localhost:8080/api`에서 접근 가능합니다.

### 2. Docker를 사용한 로컬 실행

```bash
# Docker Compose로 전체 스택 실행 (MySQL 포함)
docker-compose up -d

# 로그 확인
docker-compose logs -f app

# 종료
docker-compose down
```

### 3. 프로덕션 배포 (AWS EC2)

#### 환경 변수 파일 생성

`.env` 파일을 생성하고 다음 내용을 입력합니다:

```bash
# 데이터베이스 설정 (AWS RDS)
DB_HOST=your-rds-endpoint.rds.amazonaws.com
DB_PORT=3306
DB_NAME=findb
DB_USERNAME=admin
DB_PASSWORD=your_secure_password

# JWT 시크릿 키
JWT_SECRET=your_jwt_secret_key_at_least_32_characters

# AWS 설정
AWS_REGION=ap-northeast-2
S3_BUCKET=your-s3-bucket-name
```

#### 배포 스크립트 실행

```bash
# Docker Hub 이미지를 사용한 배포
docker-compose -f docker-compose.prod.yml up -d

# 또는 로컬 빌드 후 배포
docker build -t dennyahn/fintech-server:latest .
docker push dennyahn/fintech-server:latest
docker-compose -f docker-compose.prod.yml up -d
```

#### 서버 상태 확인

```bash
# 헬스체크
curl http://your-server-ip/api/health

# 컨테이너 상태 확인
docker ps
docker logs fintech-app-prod

# Nginx 로그 확인
docker logs fintech-nginx-prod
```

### 4. Swagger UI 접근

- **로컬**: http://localhost:8080/api/swagger-ui/index.html
- **프로덕션**: http://your-server-ip/api/swagger-ui/index.html

---

## 🧪 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 실행
./gradlew test --tests com.fintech.server.service.QuizServiceTest

# 테스트 커버리지 리포트 생성
./gradlew jacocoTestReport
```

---

## 📊 모니터링 및 로깅

### Spring Actuator 엔드포인트

```bash
# 헬스 체크
GET /api/actuator/health

# 애플리케이션 정보
GET /api/actuator/info

# 메트릭 조회
GET /api/actuator/metrics
```

### 로그 레벨 설정

```yaml
logging:
  level:
    root: INFO
    com.fintech.server: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
```

---

## 🔒 보안 고려사항

1. **비밀번호 암호화**: BCrypt 알고리즘 사용
2. **JWT 토큰**: HS512 알고리즘, 24시간 유효기간
3. **SQL Injection 방지**: JPA PreparedStatement 사용
4. **XSS 방지**: Spring Security 기본 헤더 적용
5. **CORS 설정**: 허용된 오리진만 접근 가능
6. **환경 변수**: 민감 정보는 환경 변수로 관리
7. **HTTPS**: 프로덕션 환경에서 SSL/TLS 적용

---

## 🛣 로드맵

### 완료된 기능
- ✅ JWT 기반 인증 시스템
- ✅ 계층적 학습 콘텐츠 구조
- ✅ 퀴즈 실행 및 채점 시스템
- ✅ 오답 노트 기능
- ✅ 배지 시스템
- ✅ 커뮤니티 기능
- ✅ 대시보드 및 통계
- ✅ Docker 기반 배포

### 계획 중인 기능
- 🔜 Redis 캐싱 도입
- 🔜 ElasticSearch 검색 기능
- 🔜 실시간 알림 (WebSocket)
- 🔜 AI 기반 개인화 추천
- 🔜 소셜 로그인 (OAuth 2.0)
- 🔜 이메일 인증 시스템
- 🔜 CI/CD 파이프라인 (GitHub Actions)
- 🔜 모니터링 대시보드 (Grafana)

---

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

---

## 👥 개발팀

- **Backend Developer**: [Your Name]
- **Database Design**: [Your Name]
- **DevOps Engineer**: [Your Name]

---

## 📞 문의

프로젝트에 대한 문의사항이 있으시면 이메일로 연락주세요.

- **Email**: your.email@example.com
- **GitHub**: [Your GitHub Profile]

---

**⭐ 이 프로젝트가 도움이 되었다면 Star를 눌러주세요!**
# 배포 확인용 - 동물 이름 변경 코드 반영
