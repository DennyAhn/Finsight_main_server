# 🚀 Fintech Server 배포 가이드

## 📋 목차
1. [배포 아키텍처](#배포-아키텍처)
2. [배포 환경](#배포-환경)
3. [배포 결과](#배포-결과)
4. [API 엔드포인트](#api-엔드포인트)
5. [업데이트 방법](#업데이트-방법)

---

## 🏗️ 배포 아키텍처

```
로컬 PC (Windows)
    ├─ Dockerfile (멀티 스테이지 빌드)
    ├─ docker-compose.prod.yml
    └─ .env (환경 변수)
         ↓
    [Docker Build]
         ↓
    Docker Hub (dennyahn/fintech-server:latest)
         ↓
    AWS EC2 (Amazon Linux 2)
         ├─ Docker Pull
         ├─ Docker Compose Up
         └─ 컨테이너 실행 (포트 8080)
              ↓
         AWS RDS (MySQL 8.0)
              └─ 데이터베이스 (findb)
```

---

## 🖥️ 배포 환경

### 1. AWS EC2
- **인스턴스 타입**: t2.micro (프리티어)
- **OS**: Amazon Linux 2
- **Public IP**: 54.180.103.186
- **포트**: 8080 (HTTP)
- **도커 버전**: Docker 27.5.1, Docker Compose v2.39.4

### 2. AWS RDS
- **엔진**: MySQL 8.0.35
- **인스턴스**: db.t3.micro
- **Endpoint**: finquiz-db.c7gqm40cytz9.ap-northeast-2.rds.amazonaws.com
- **데이터베이스**: findb
- **포트**: 3306

### 3. Docker Hub
- **리포지토리**: dennyahn/fintech-server
- **태그**: latest
- **이미지 크기**: 약 200MB

---

## ✅ 배포 결과

### 서버 상태
```bash
# 컨테이너 확인
$ docker ps
CONTAINER ID   IMAGE                            COMMAND               STATUS         PORTS
7f357407212f   dennyahn/fintech-server:latest   "java -jar app.jar"   Up 10 minutes  0.0.0.0:8080->8080/tcp

# 헬스 체크
$ curl http://localhost:8080/api/actuator/health
{"status":"UP"}
```

### 애플리케이션 로그
```
Started FinMainServerApplication in 21.899 seconds
Spring Boot :: (v3.2.0)
Profile active: prod
```

---

## 🌐 API 엔드포인트

### 기본 URL
```
http://54.180.103.186:8080/api
```

### 헬스 체크
```bash
# 내부 접속
curl http://localhost:8080/api/actuator/health

# 외부 접속
curl http://54.180.103.186:8080/api/actuator/health
```

### Swagger UI (API 문서)
```
http://54.180.103.186:8080/api/swagger-ui/index.html
```

---

## 🔄 업데이트 방법

### 1. 로컬에서 새 이미지 빌드 & 푸시
```bash
# 1. 코드 수정 후 Docker 이미지 빌드
docker build --no-cache -t dennyahn/fintech-server:latest .

# 2. Docker Hub에 푸시
docker push dennyahn/fintech-server:latest
```

### 2. EC2에서 업데이트 적용
```bash
# SSH 접속
ssh -i finquiz-key.pem ec2-user@54.180.103.186

# 프로젝트 디렉토리로 이동
cd ~/app

# 컨테이너 중지 및 삭제
docker-compose -f docker-compose.prod.yml down

# 최신 이미지 다운로드
docker-compose -f docker-compose.prod.yml pull

# 컨테이너 재시작
docker-compose -f docker-compose.prod.yml up -d

# 로그 확인
docker-compose -f docker-compose.prod.yml logs -f app
```

---

## 🔒 보안 설정

### 1. 환경 변수 관리
- `.env` 파일 사용 (Git에 커밋하지 않음)
- DB 비밀번호, JWT 시크릿 등 민감 정보 분리

### 2. AWS 보안 그룹
- **EC2 인바운드 규칙**:
  - SSH (22): 특정 IP만 허용 권장
  - HTTP (8080): 0.0.0.0/0 (전체 허용)
  
- **RDS 보안 그룹**:
  - MySQL (3306): EC2 보안 그룹만 허용

### 3. CORS 설정
- 모든 오리진 허용 (`allowedOriginPatterns: *`)
- 개발 완료 후 특정 도메인만 허용하도록 변경 권장

---

## 📊 모니터링

### 컨테이너 상태 확인
```bash
# 컨테이너 목록
docker ps

# 리소스 사용량
docker stats

# 로그 확인
docker-compose -f docker-compose.prod.yml logs -f app
```

### 애플리케이션 메트릭
```bash
# Actuator 엔드포인트
curl http://54.180.103.186:8080/api/actuator/health
curl http://54.180.103.186:8080/api/actuator/info
curl http://54.180.103.186:8080/api/actuator/metrics
```

---

## 🛠️ 트러블슈팅

### 문제: 컨테이너가 시작되지 않음
```bash
# 로그 확인
docker-compose -f docker-compose.prod.yml logs app

# 컨테이너 재시작
docker-compose -f docker-compose.prod.yml restart app
```

### 문제: DB 연결 실패
```bash
# RDS 엔드포인트 확인
echo $DB_HOST

# MySQL 연결 테스트
mysql -h finquiz-db.c7gqm40cytz9.ap-northeast-2.rds.amazonaws.com -u admin -p
```

### 문제: 외부 접속 불가
```bash
# EC2 보안 그룹 8080 포트 확인
# AWS Console → EC2 → Security Groups → Inbound Rules
```

---

## 📝 참고 문서

- [Deployment_solution.md](./Deployment_solution.md) - 배포 방식 비교 및 트러블슈팅 과정
- [DOCKER_HUB_DEPLOY.md](./DOCKER_HUB_DEPLOY.md) - Docker Hub 배포 상세 가이드
- [PRODUCTION_DEPLOYMENT.md](./PRODUCTION_DEPLOYMENT.md) - EC2 직접 빌드 방식 가이드

---

## 📅 배포 이력

| 날짜 | 버전 | 변경 사항 |
|------|------|-----------|
| 2025-10-02 | 1.0.0 | 초기 배포 (Docker Hub 방식) |

---

## 👥 작성자

- **배포 담당**: dennyahn
- **배포 일자**: 2025년 10월 2일
- **배포 방식**: Docker Hub → EC2 Pull

