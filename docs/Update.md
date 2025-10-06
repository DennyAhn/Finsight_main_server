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