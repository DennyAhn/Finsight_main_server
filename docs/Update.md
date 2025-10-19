# 커뮤니티 기능 업데이트 사항

## 업데이트 개요
핀테크 서버에 커뮤니티 기능이 새롭게 추가되었습니다.

---

## 🆕 새로운 기능

### 1. 커뮤니티 게시글 시스템
- **게시글 작성 기능**: 사용자가 텍스트 기반 게시글을 작성할 수 있습니다
- **게시글 목록 조회 기능**: 모든 게시글을 시간순으로 조회할 수 있습니다
- **태그 시스템**: 게시글에 태그를 추가하여 분류할 수 있습니다
- **작성자 배지 표시**: 게시글 작성 시점의 사용자 배지가 함께 저장됩니다

### 2. 태그 관리 시스템
- **자동 태그 생성**: 존재하지 않는 태그는 자동으로 생성됩니다
- **태그 중복 방지**: 동일한 이름의 태그는 하나만 존재합니다
- **다대다 관계**: 하나의 게시글에 여러 태그를 연결할 수 있습니다

### 3. 사용자 인증 통합
- **JWT 토큰 지원**: JWT 토큰을 통한 사용자 인증을 지원합니다
- **게스트 사용자 지원**: 토큰이 없는 경우 기본 게스트 사용자로 처리됩니다
- **Spring Security 연동**: 기존 인증 시스템과 통합되어 동작합니다

---

## 🗄️ 데이터베이스 스키마 추가

### 새로 추가된 테이블

#### 1. `community_posts` 테이블
```sql
CREATE TABLE community_posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_id BIGINT NOT NULL,
    author_badge_id BIGINT,
    body TEXT NOT NULL,
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (author_id) REFERENCES users(id),
    FOREIGN KEY (author_badge_id) REFERENCES badges(id)
);
```

#### 2. `tags` 테이블
```sql
CREATE TABLE tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);
```

#### 3. `post_tag_links` 테이블
```sql
CREATE TABLE post_tag_links (
    post_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (post_id, tag_id),
    FOREIGN KEY (post_id) REFERENCES community_posts(id),
    FOREIGN KEY (tag_id) REFERENCES tags(id)
);
```

---

## 📂 새로 추가된 클래스 및 패키지

### Controller
- `com.fintech.server.community.controller.CommunityController`
  - `/api/community/posts` 엔드포인트 관리
  - 게시글 작성 및 조회 API 제공

### Service
- `com.fintech.server.community.service.CommunityService`
  - 게시글 생성 비즈니스 로직
  - 태그 처리 로직
  - 사용자 배지 스냅샷 저장

### Entity
- `com.fintech.server.community.entity.CommunityPost`
  - 게시글 엔티티
  - JPA Auditing 적용 (생성일시, 수정일시 자동 관리)
- `com.fintech.server.community.entity.Tag`
  - 태그 엔티티
  - 태그명 중복 방지 (unique constraint)
- `com.fintech.server.community.entity.PostTagLink`
  - 게시글-태그 연결 엔티티
  - 복합키 사용
- `com.fintech.server.community.entity.PostTagLinkId`
  - PostTagLink의 복합키 클래스

### Repository
- `com.fintech.server.community.repository.CommunityPostRepository`
  - 게시글 데이터 접근
  - 작성자 및 배지 정보를 포함한 조회 쿼리
- `com.fintech.server.community.repository.TagRepository`
  - 태그 데이터 접근
  - 태그명으로 검색 기능
- `com.fintech.server.community.repository.PostTagLinkRepository`
  - 게시글-태그 연결 데이터 접근

### DTO
- `com.fintech.server.community.dto.PostRequestDto`
  - 게시글 작성 요청 DTO
- `com.fintech.server.community.dto.PostResponseDto`
  - 게시글 응답 DTO
  - 중첩 클래스로 AuthorDto, BadgeDto 포함

---

## 🔧 기술적 특징

### 1. JPA Auditing 적용
- `@CreatedDate`, `@LastModifiedDate` 어노테이션 사용
- 게시글의 생성일시와 수정일시 자동 관리

### 2. 복합키 설계
- `PostTagLink` 엔티티에서 `@EmbeddedId` 사용
- 게시글 ID와 태그 ID를 조합한 복합키로 중복 방지

### 3. 지연 로딩 적용
- 모든 연관관계에 `FetchType.LAZY` 적용
- 필요한 경우에만 `JOIN FETCH` 쿼리 사용

### 4. 트랜잭션 관리
- 서비스 클래스에 `@Transactional` 적용
- 조회 전용 메서드는 `@Transactional(readOnly = true)` 적용

---

## 🚀 향후 확장 계획

### 1. 예정된 기능
- 게시글 좋아요 기능
- 댓글 시스템
- 게시글 수정/삭제 기능
- 태그별 게시글 필터링
- 페이징 처리

### 2. 성능 최적화 계획
- 게시글 목록 조회 시 페이징 적용
- 태그 통계 캐싱
- 인덱스 최적화

---

## 📝 개발자 노트

### 주의사항
1. **배지 스냅샷**: 게시글 작성 시점의 사용자 배지가 저장되므로, 사용자의 배지가 변경되어도 기존 게시글의 배지는 변경되지 않습니다.
2. **게스트 사용자**: JWT 토큰이 없는 경우 ID 64번 사용자를 기본값으로 사용합니다.
3. **태그 대소문자**: 현재 태그는 대소문자를 구분합니다.

### 디버깅 로그
- CommunityService에 상세한 디버깅 로그가 포함되어 있습니다.
- 운영 환경 배포 시 로그 레벨 조정이 필요합니다.

---

**업데이트 일시**: 2024년 1월 (커뮤니티 기능 최초 구현)
**담당자**: 개발팀
**관련 이슈**: 커뮤니티 기능 구현 요청

---

# EC2 SSH 접속 문제 해결

## 문제 상황
HTTPS 설정을 위해 EC2 서버에 SSH로 접속을 시도했으나, 지속적으로 권한 거부 오류가 발생했습니다.

```bash
ssh -i ~/finquiz-key.pem ubuntu@54.180.103.186
# Permission denied (publickey,gssapi-keyex,gssapi-with-mic)
```

---

## 🔍 문제 원인

### 잘못된 사용자 이름 사용
- **시도한 사용자**: `ubuntu`
- **실제 AMI**: Amazon Linux 2023 (`al2023-ami-2023.8.20250915.0`)
- **올바른 사용자**: **`ec2-user`**

### 각 AMI별 기본 사용자 이름

| AMI 종류 | 기본 사용자 이름 |
|----------|------------------|
| **Amazon Linux 2023** | `ec2-user` ✅ |
| Ubuntu | `ubuntu` |
| Debian | `admin` 또는 `debian` |
| RHEL | `ec2-user` 또는 `root` |
| SUSE | `ec2-user` 또는 `root` |

---

## ✅ 해결 방법

### 올바른 SSH 명령어

**WSL (Linux 환경):**
```bash
ssh -i ~/finquiz-key.pem ec2-user@54.180.103.186
```

**PowerShell (Windows 환경):**
```powershell
ssh -i "C:\Users\ahj20\Desktop\K_Hackathon\finquiz-key.pem" ec2-user@54.180.103.186
```

---

## 📝 디버깅 과정

### 1. 네트워크 연결 확인
```bash
ssh -vvv -i ~/finquiz-key.pem ubuntu@54.180.103.186
```
- **결과**: ✅ EC2 서버까지 연결 성공
- **판단**: 네트워크 및 보안 그룹 설정은 정상

### 2. 키 파일 권한 확인
```bash
ls -la ~/finquiz-key.pem
# -r-------- 1 root root 1678 Oct 5 15:23 /root/finquiz-key.pem
```
- **결과**: ✅ 권한 400, 정상
- **판단**: 키 파일 권한 문제 아님

### 3. SSH 상세 로그 분석
```
debug1: Trying private key: /root/finquiz-key.pem
debug3: sign_and_send_pubkey: using publickey with RSA SHA256:DAnFGGmwn3vTEpttxv/lRsfIhWXhlKqUjlIETNxeIeM
debug1: Authentications that can continue: publickey,gssapi-keyex,gssapi-with-mic
debug1: No more authentication methods to try.
```
- **결과**: 키는 전송되었지만 서버가 거부
- **판단**: 키 자체는 맞지만, **사용자 이름이 잘못됨**

### 4. EC2 인스턴스 정보 확인
- **AMI ID**: `ami-077ad873396d76f6a`
- **AMI 이름**: `al2023-ami-2023.8.20250915.0-kernel-6.1-x86_64`
- **판단**: **Amazon Linux 2023** → 기본 사용자는 `ec2-user`

### 5. 최종 해결
```bash
ssh -i ~/finquiz-key.pem ec2-user@54.180.103.186
```
- **결과**: ✅ 접속 성공!

---

## 💡 교훈

### 1. AMI 유형 확인의 중요성
EC2 인스턴스에 SSH 접속 시, **AMI 유형**을 먼저 확인해야 합니다.
- AWS 콘솔 → EC2 → 인스턴스 → **세부 정보** 탭 → **AMI 이름** 확인

### 2. 일반적인 실수
- Ubuntu를 사용한다고 가정하고 `ubuntu` 사용자로 접속 시도
- 실제로는 Amazon Linux 2023을 사용하고 있었음
- 키 파일이나 보안 그룹 문제로 착각하기 쉬움

### 3. 빠른 확인 방법
**SSH 접속 시 `-v` 옵션 사용:**
```bash
ssh -v -i keyfile.pem username@ip-address
```
이렇게 하면 어느 단계에서 실패하는지 정확히 알 수 있습니다.

---

## 🔧 향후 대응

### EC2 Instance Connect 활용
키 파일 문제나 사용자 이름을 모를 때는 **EC2 Instance Connect**를 사용하면 편리합니다:

1. AWS 콘솔 → EC2 → 인스턴스 선택
2. **연결** 버튼 클릭
3. **EC2 Instance Connect** 탭 선택
4. **연결** 클릭

AWS가 자동으로 올바른 사용자 이름과 임시 키를 생성하여 접속시켜줍니다.

---

**해결 일시**: 2025년 10월 5일
**문제**: SSH 접속 권한 거부 (잘못된 사용자 이름)
**해결**: `ubuntu` → `ec2-user`로 변경

---

# 퀴즈 시스템 핵심 기능 업데이트

## 📋 시스템 개요
**금융 교육 플랫폼의 완전한 퀴즈 학습 시스템**으로, 사용자가 섹터 → 서브섹터 → 레벨 → 퀴즈 순서로 학습하며, 실시간 정답 체크와 배지 시스템이 연동된 종합적인 학습 플랫폼입니다.

---

## 🏗️ 시스템 아키텍처

### 📊 데이터 구조
- **7개 섹터** × **3개 서브섹터** = **21개 서브섹터**
- **각 서브섹터마다 3개 레벨** (초급자/중급자/고급자) = **63개 레벨**
- **각 레벨마다 1개 퀴즈** = **63개 퀴즈**
- **각 퀴즈마다 4개 문제** = **252개 문제**

### 🎚️ 난이도 시스템
- **초급자**: 4문제 중 1개 이상 정답 (25% 이상)
- **중급자**: 4문제 중 2개 이상 정답 (50% 이상)  
- **고급자**: 4문제 중 3개 이상 정답 (75% 이상)

---

## 🚀 핵심 기능

### 1️⃣ 실시간 정답 체크 시스템
- **문제별 즉시 피드백**: 각 문제를 풀 때마다 정답 여부와 해설 제공
- **서버 기반 정답 검증**: 보안성을 위한 서버에서 정답 체크
- **마크다운 해설 지원**: 풍부한 해설과 학습 자료 제공

### 2️⃣ 완전한 점수 시스템
- **정답 기반 점수**: 맞춘 문제만 점수 획득 (1정답 = 1점)
- **퀴즈 완료 처리**: 4문제 완료 후 최종 점수 집계
- **통과 여부 판정**: 레벨별 기준에 따른 자동 통과/실패 처리

### 3️⃣ 자동 배지 시스템
- **6단계 배지**: 브론즈 → 실버 → 골드 → 플레티넘 → 다이아 → 마스터
- **실시간 업데이트**: 퀴즈 완료 시 자동으로 배지 진행률 업데이트
- **조건 기반 획득**: 퀴즈 완료 수 + 정답 수 기준으로 배지 획득

### 4️⃣ 오답 노트 시스템
- **자동 생성**: 틀린 문제 자동으로 오답 노트에 추가
- **학습 지원**: 틀린 문제 재학습을 위한 시스템

---

## 🔄 완전한 API 플로우

### 1️⃣ 섹터 및 서브섹터 조회
```http
GET /api/sectors
GET /api/subsectors/{sectorId}
```

### 2️⃣ 레벨 및 퀴즈 조회
```http
GET /api/levels/{levelId}/quizzes
GET /api/quizzes/{quizId}
```

### 3️⃣ 퀴즈 실행 및 채점
```http
POST /api/quizzes/submit-answer
POST /api/quizzes/{quizId}/complete
```

### 4️⃣ 배지 및 진행률 조회
```http
GET /api/badges/user/{userId}/current
GET /api/progress/user/{userId}/level/{levelId}
```

---

## 🎯 징검다리 기능 개선

### 문제점
- 징검다리 `stepDescription`이 모든 서브섹터/레벨에서 "기초 금융 상식"으로 하드코딩됨
- 사용자가 어떤 서브섹터의 어떤 레벨을 진행하든 동일한 설명만 표시됨

### 해결 방법
**파일**: `src/main/java/com/fintech/server/quiz/service/LevelService.java`
```java
// 수정 전
String stepDescription = "기초 금융 상식";

// 수정 후
String stepDescription = level.getTitle() != null ? level.getTitle() : "기초 금융 상식";
if (level.getLearningGoal() != null && !level.getLearningGoal().trim().isEmpty()) {
    stepDescription = level.getLearningGoal();
}
```

### 결과
- ✅ **금융권 서브섹터**: "1금융권과 2금융권의 차이를 배워요."
- ✅ **예금/적금 서브섹터**: "이자 계산 방식, 고정 vs 변동금리를 배워요."
- ✅ **레벨별 차별화**: 초급자, 중급자, 고급자별로 다른 설명

---

## 🔧 LevelService 개선사항

### 1. 하드코딩된 값 제거
- `QUESTIONS_PER_LEVEL = 4` → `@Value("${quiz.questions-per-level:4}")`
- `PASS_SCORE = 3` → `@Value("${quiz.pass-score:3}")`
- `timeLimitPerQuestion = 15` → `@Value("${quiz.time-limit-per-question:15}")`
- `estimatedTimePerQuiz = 5` → `@Value("${quiz.estimated-time-per-quiz:5}")`

### 2. 예외 처리 개선
- `RuntimeException` → `IllegalArgumentException`, `IllegalStateException`
- 더 구체적인 예외 메시지 제공

### 3. 시간 계산 개선
- `calculateActualTimeSpent()` 메서드 추가
- 실제 소요시간과 예상 시간을 구분하여 계산

### 4. 설정 파일 추가
**application.yml**:
```yaml
quiz:
  questions-per-level: 4
  pass-score: 3
  time-limit-per-question: 15
  estimated-time-per-quiz: 5
```

---

## 📊 성능 최적화

### 1. 쿼리 최적화
- `findByIdWithQuizzes()`: LEFT JOIN FETCH로 N+1 문제 해결
- `findByIdWithSubsector()`: 서브섹터 정보를 한 번에 조회

### 2. 메모리 사용량 최적화
- 불필요한 메서드 제거 (`getQuizProgress()`)
- 사용하지 않는 import 정리

### 3. 코드 가독성 향상
- 메서드명을 더 명확하게 변경
- 주석 및 문서화 개선

---

## 🎉 최종 결과

### 기능적 개선
- ✅ **동적 설명 생성**: 서브섹터/레벨별 맞춤형 징검다리 설명
- ✅ **설정 가능한 값**: 하드코딩 제거로 유연성 증대
- ✅ **정확한 시간 계산**: 실제 소요시간 기반 진행률 계산
- ✅ **향상된 예외 처리**: 더 구체적인 오류 메시지

### 기술적 개선
- ✅ **성능 최적화**: N+1 쿼리 문제 해결
- ✅ **코드 품질**: 가독성 및 유지보수성 향상
- ✅ **설정 관리**: 외부 설정 파일을 통한 값 관리
- ✅ **메모리 효율성**: 불필요한 코드 제거

---

**업데이트 일시**: 2025년 10월 19일
**담당자**: AI Assistant
**관련 이슈**: 징검다리 기능 개선 및 성능 최적화