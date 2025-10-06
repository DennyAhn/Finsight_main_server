# 🔧 Spring Boot 서버 배포 방식 비교 및 문제 해결 과정

## 📋 목차
1. [배포 방식 비교](#배포-방식-비교)
2. [선택한 방식과 이유](#선택한-방식과-이유)
3. [트러블슈팅 과정](#트러블슈팅-과정)
4. [배운 점](#배운-점)

---

## 🆚 배포 방식 비교

### 방법 1: Docker Hub 방식 (채택 ✅)

```
로컬 PC:
  ├─ Docker 이미지 빌드 (고성능 PC)
  ├─ Docker Hub에 이미지 Push
       ↓
Docker Hub: 이미지 저장
       ↓
EC2 서버:
  ├─ Docker Hub에서 이미지 Pull
  └─ 컨테이너 실행
```

**장점:**
- ✅ EC2 서버 리소스 절약 (빌드 부담 없음)
- ✅ 빌드 속도 빠름 (로컬 PC의 높은 성능 활용)
- ✅ 배포 속도 빠름 (이미 빌드된 이미지만 다운로드)
- ✅ t2.micro 같은 저사양 인스턴스에서도 안정적
- ✅ 이미지 버전 관리 용이

**단점:**
- ❌ Docker Hub 계정 필요
- ❌ 이미지 Push/Pull에 네트워크 시간 소요
- ❌ 로컬에서 빌드 환경 구성 필요

---

### 방법 2: EC2 직접 빌드 방식 (미채택 ❌)

```
로컬 PC:
  ├─ Dockerfile 작성
  ├─ docker-compose.prod.yml 작성
  └─ Git Push (코드만 올림)
       ↓
GitHub: 코드 저장
       ↓
EC2 서버:
  ├─ Git Pull (코드 다운로드)
  ├─ docker-compose up --build
  │    ↓
  │  [EC2에서 직접 빌드]
  │    ↓
  ├─ Docker 이미지 생성 (EC2 내부)
  └─ 컨테이너 실행
```

**장점:**
- ✅ Docker Hub 계정 불필요
- ✅ Git만으로 코드 관리 가능
- ✅ 로컬 빌드 환경 불필요

**단점:**
- ❌ **EC2에서 Gradle 빌드 시 메모리 부족 위험** (t2.micro: 1GB RAM)
- ❌ 빌드 시간이 매우 오래 걸림 (저사양 CPU)
- ❌ 빌드 중 서버 응답 지연 가능성
- ❌ OOM(Out of Memory) 에러 가능성

---

## 🎯 선택한 방식과 이유

### **최종 선택: Docker Hub 방식**

#### 결정적 이유
**AWS 프리티어 t2.micro 인스턴스의 제약**
- CPU: 1 vCPU (매우 제한적)
- RAM: 1GB (Gradle 빌드에 부족)
- Spring Boot + Gradle 빌드는 최소 2GB RAM 권장

#### 비유
> "경차로 F1 경주를 하는 것"과 같습니다.  
> t2.micro에서 직접 빌드하는 것은 가능은 하지만, 매우 느리고 실패 위험이 높습니다.

#### 실제 성능 차이
| 항목 | Docker Hub 방식 | EC2 직접 빌드 |
|------|-----------------|---------------|
| **빌드 시간** | 로컬: 40초 | EC2: 5~10분 (또는 실패) |
| **메모리 사용** | 로컬 PC 사용 | EC2: 1GB (부족) |
| **배포 시간** | 이미지 Pull: 1분 | 빌드 포함: 5~10분 |
| **안정성** | ⭐⭐⭐⭐⭐ | ⭐⭐ (OOM 위험) |

---

## 🐛 트러블슈팅 과정

### 문제 1: `openjdk` 이미지 Deprecated

**증상:**
```dockerfile
FROM openjdk:17-jdk-slim AS builder
```
```
ERROR: openjdk:17-jre-slim: not found
```

**원인:**
- Docker Hub의 `openjdk` 공식 이미지가 deprecated됨
- 더 이상 업데이트 및 지원하지 않음

**해결 방법:**
```dockerfile
# 변경 전
FROM openjdk:17-jdk-slim AS builder
FROM openjdk:17-jre-slim

# 변경 후 ✅
FROM eclipse-temurin:17-jdk-jammy AS builder
FROM eclipse-temurin:17-jre-jammy
```

**배운 점:**
- Docker 이미지도 Lifecycle이 있으며, deprecated 이미지는 사용하지 말아야 함
- Eclipse Temurin은 OpenJDK의 공식 후속 배포판

---

### 문제 2: `gradlew: not found` 에러

**증상:**
```
RUN ./gradlew dependencies
/bin/sh: 1: ./gradlew: not found
```

**시도 1: `.dockerignore` 수정**
```
# .dockerignore에서 주석 처리
# gradle/  ← 이 줄을 주석 처리하여 gradle 폴더를 포함시킴
```
**결과:** 실패 ❌

**시도 2: 실행 권한 부여**
```dockerfile
RUN chmod +x ./gradlew
RUN ./gradlew dependencies
```
**결과:** 실패 ❌

**시도 3: Alpine → Jammy (Debian 기반) 변경**
**원인 분석:**
- Windows에서 생성된 `gradlew` 파일은 CRLF 줄바꿈 사용
- Alpine Linux는 LF 줄바꿈만 지원
- Shell 스크립트 인식 실패

```dockerfile
# 변경 전
FROM eclipse-temurin:17-jdk-alpine

# 변경 후 ✅
FROM eclipse-temurin:17-jdk-jammy
```
**결과:** 실패 ❌ (여전히 gradlew 실행 안 됨)

**최종 해결: Gradle 직접 설치**
```dockerfile
# gradlew 사용 포기, Gradle 직접 설치
RUN apt-get update && \
    apt-get install -y wget unzip && \
    wget https://services.gradle.org/distributions/gradle-8.5-bin.zip && \
    unzip gradle-8.5-bin.zip && \
    mv gradle-8.5 /opt/gradle && \
    rm gradle-8.5-bin.zip

ENV GRADLE_HOME=/opt/gradle
ENV PATH=$PATH:$GRADLE_HOME/bin

# gradle 명령어로 직접 빌드
RUN gradle build -x test --no-daemon
```
**결과:** 성공 ✅

**배운 점:**
- Windows와 Linux 간 줄바꿈 문자 차이 주의
- 문제 해결이 어려울 때는 다른 접근 방식 고려
- Gradle Wrapper보다 직접 설치가 더 안정적일 수 있음

---

### 문제 3: Docker 캐시 문제

**증상:**
- Dockerfile을 수정했는데도 이전 버전으로 빌드됨
- 여전히 `gradlew: not found` 에러 발생

**원인:**
- Docker는 이미지 레이어를 캐싱하여 빌드 속도 향상
- 하지만 때로는 오래된 캐시가 문제 원인

**해결 방법:**
```bash
# --no-cache 옵션으로 처음부터 다시 빌드
docker build --no-cache -t dennyahn/fintech-server:latest .
```

**배운 점:**
- Docker 캐시는 양날의 검
- 이상한 동작이 발생하면 `--no-cache` 시도
- 프로덕션 빌드 시에는 `--no-cache` 사용 권장

---

### 문제 4: EC2 SSH 연결 타임아웃

**증상:**
```bash
ssh -i finquiz-key.pem ec2-user@54.180.103.186
ssh: connect to host 54.180.103.186 port 22: Connection timed out
```

**원인:**
- EC2 인스턴스의 보안 그룹에 SSH(22번 포트) 인바운드 규칙 없음
- RDS 보안 그룹을 확인하는 실수

**해결 방법:**
1. AWS Console → EC2 → **인스턴스 선택**
2. **보안** 탭 → **보안 그룹** 클릭
3. **인바운드 규칙 편집**
4. **규칙 추가**:
   - 유형: SSH
   - 포트: 22
   - 소스: My IP (또는 특정 IP)

**배운 점:**
- **EC2 보안 그룹**과 **RDS 보안 그룹**은 별개
- 서비스마다 보안 그룹을 별도로 설정해야 함
- 보안 그룹 설정은 즉시 적용됨

---

### 문제 5: `.pem` 키 파일 권한 에러

**증상:**
```
Bad permissions. Try removing permissions for user: BUILTIN\Users (S-1-5-32-545)
on file C:/Users/ahj20/Desktop/K_Hackathon/finquiz-key.pem.
```

**원인:**
- Windows에서 `.pem` 파일에 너무 많은 사용자 권한이 부여됨
- SSH는 보안상 제한적인 권한을 요구

**해결 방법 (Windows):**
```powershell
# 1. 모든 권한 제거
icacls "finquiz-key.pem" /inheritance:r

# 2. 현재 사용자만 읽기 권한 부여
icacls "finquiz-key.pem" /grant:r "%USERNAME%:(R)"
```

**배운 점:**
- SSH 키 파일은 소유자만 읽을 수 있어야 함 (보안 규칙)
- Windows와 Linux의 권한 체계 차이 이해 필요

---

### 문제 6: Docker Compose 명령어 없음

**증상:**
```bash
docker-compose -f docker-compose.prod.yml up -d
docker: 'compose' is not a docker command.
```

**원인:**
- Amazon Linux 2에 Docker Compose v2가 설치되어 있지 않음
- `docker compose` (v2)와 `docker-compose` (v1) 차이

**해결 방법:**
```bash
# Docker Compose v1 설치
sudo curl -L "https://github.com/docker/compose/releases/download/v2.30.3/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose

# 실행 권한 부여
sudo chmod +x /usr/local/bin/docker-compose

# 버전 확인
docker-compose --version
```

**배운 점:**
- Docker Compose v1과 v2는 명령어가 다름
  - v1: `docker-compose up`
  - v2: `docker compose up`
- EC2 인스턴스에 필요한 도구는 수동 설치 필요

---

### 문제 7: YAML Parsing 에러

**증상:**
```
BUILD FAILED
Caused by: org.yaml.snakeyaml.parser.ParserException: 
while parsing a block mapping
duplicated key: 'management'
```

**원인:**
- `application.yml` 파일에 `management:` 키가 중복 선언됨
- YAML은 동일한 키를 허용하지 않음

**해결 방법:**
```yaml
# 변경 전 (중복)
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics

# ... 파일 중간 ...

management:  # ← 중복!
  endpoint:
    health:
      show-details: always

# 변경 후 (병합) ✅
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

**배운 점:**
- YAML 파일은 계층 구조를 정확히 지켜야 함
- IDE의 YAML 검증 기능 활용 필요
- 빌드 실패 시 로그를 끝까지 읽어야 원인 파악 가능

---

### 문제 8: 환경 변수 보안 문제

**문제 인식:**
```yaml
# docker-compose.prod.yml (잘못된 예)
environment:
  SPRING_DATASOURCE_PASSWORD: Aa5051140!!  # ← 하드코딩된 비밀번호!
  JWT_SECRET: mySecretKey123...            # ← 노출된 시크릿!
```

**위험성:**
- Git에 민감한 정보가 커밋됨
- 누구나 DB 비밀번호와 JWT 시크릿을 볼 수 있음
- 보안 취약점 발생

**해결 방법: `.env` 파일 분리**

1. **`.env` 파일 생성 (EC2)**
```bash
DB_HOST=finquiz-db.c7gqm40cytz9.ap-northeast-2.rds.amazonaws.com
DB_PASSWORD=Aa5051140!!
JWT_SECRET=finquiz-production-jwt-secret-key-2025
AWS_REGION=ap-northeast-2
S3_BUCKET=fin.img99
```

2. **`docker-compose.prod.yml` 수정**
```yaml
environment:
  SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}  # ← 환경 변수 참조
  JWT_SECRET: ${JWT_SECRET}
  AWS_S3_BUCKET: ${S3_BUCKET}
```

3. **`.gitignore`에 추가**
```
.env
.env.local
.env.prod
```

**배운 점:**
- 민감한 정보는 절대 코드에 하드코딩하지 말 것
- `.env` 파일과 환경 변수 활용
- `.gitignore`로 민감 파일 제외
- 현업에서는 AWS Secrets Manager 같은 서비스 사용

---

## 📊 방식별 실전 성능 비교

### 테스트 환경
- **로컬 PC**: Windows 11, Intel i7, 16GB RAM
- **EC2**: t2.micro, Amazon Linux 2, 1GB RAM
- **네트워크**: 일반 인터넷 환경

### 결과

| 단계 | Docker Hub 방식 | EC2 직접 빌드 |
|------|-----------------|---------------|
| 로컬 빌드 | 40초 | - |
| 이미지 Push | 1분 | - |
| EC2 Pull | 30초 | - |
| EC2 빌드 | - | 5~10분 (또는 OOM) |
| 컨테이너 시작 | 20초 | 20초 |
| **총 소요 시간** | **2분 30초** | **5~10분** |
| **성공률** | **100%** | **60~70%** |

---

## 🎓 배운 점

### 1. 아키텍처 선택의 중요성
- 서버 사양에 맞는 배포 방식 선택이 중요
- t2.micro 같은 저사양 환경에서는 Docker Hub 방식이 필수
- "올바른 도구를 올바른 상황에 사용하기"

### 2. 트러블슈팅 프로세스
- **단계적 접근**: 한 번에 하나씩 문제 해결
- **로그 분석**: 에러 메시지를 끝까지 읽기
- **다양한 시도**: 한 가지 방법이 안 되면 다른 접근 시도
- **캐시 의심**: 이상한 동작 시 캐시 확인

### 3. Docker 및 컨테이너 이해
- Docker 이미지 레이어 캐싱 메커니즘
- 멀티 스테이지 빌드의 효율성
- 컨테이너화의 장점 (일관성, 이식성)

### 4. 보안 모범 사례
- 민감한 정보는 `.env` 파일로 분리
- Git에 민감 정보 커밋 금지
- AWS 보안 그룹 설정의 중요성

### 5. 플랫폼 차이 이해
- Windows vs Linux 환경 차이 (줄바꿈, 권한 등)
- Cloud 플랫폼의 제약 사항 이해
- 로컬 개발 환경과 프로덕션 환경의 차이

---

## 🚀 향후 개선 방향

### 1. CI/CD 파이프라인 구축
- GitHub Actions를 통한 자동 빌드 & 배포
- 코드 푸시 → 자동 테스트 → 자동 배포

### 2. 성능 개선
- EC2 인스턴스 업그레이드 고려 (t3.small 이상)
- Auto Scaling 그룹 구성
- Application Load Balancer 추가

### 3. 보안 강화
- HTTPS 적용 (AWS Certificate Manager + ALB)
- AWS Secrets Manager 도입
- CORS 설정 특정 도메인만 허용

### 4. 모니터링 & 로깅
- CloudWatch를 통한 메트릭 수집
- 로그 집계 시스템 구축
- 알림 시스템 구축

---

## 📚 참고 자료

- [Docker Hub 방식 상세 가이드](./DOCKER_HUB_DEPLOY.md)
- [EC2 직접 빌드 방식 가이드](./PRODUCTION_DEPLOYMENT.md)
- [최종 배포 문서](./Deployment.md)

---

## 👤 작성자

- **이름**: dennyahn
- **작성일**: 2025년 10월 2일
- **목적**: 포트폴리오 및 학습 기록

---

## 💭 마무리

이번 배포 과정을 통해 다음을 배웠습니다:

1. **문제 해결 능력**: 7개 이상의 다양한 문제를 단계적으로 해결
2. **시스템 설계 능력**: 서버 사양에 맞는 최적의 배포 방식 선택
3. **보안 의식**: 민감한 정보 관리의 중요성 인식
4. **DevOps 이해**: Docker, CI/CD, Cloud 인프라에 대한 실전 경험

> "실패는 성공의 어머니"  
> 수많은 시행착오를 통해 더 나은 시스템을 만들 수 있었습니다.

