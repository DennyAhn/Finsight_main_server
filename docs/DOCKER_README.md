# 🐳 Fintech Server Docker 가이드

이 문서는 Fintech 서버의 Docker 기반 개발 및 배포 방법을 설명합니다.

## 📋 목차
1. [로컬 개발 환경](#로컬-개발-환경)
2. [EC2 프로덕션 배포](#ec2-프로덕션-배포)
3. [트러블슈팅](#트러블슈팅)

---

# 💻 로컬 개발 환경

## 사전 요구사항
- Docker Desktop (Windows/Mac)
- 최소 4GB RAM 권장

## 🚀 빠른 시작

### 1. 전체 스택 실행
```bash
# 데이터베이스 + 애플리케이션 + Nginx 모두 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f app
```

### 2. 서비스 확인
- **애플리케이션**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/api/swagger-ui/index.html
- **헬스 체크**: http://localhost:8080/api/actuator/health
- **MySQL**: localhost:3307

### 3. 서비스 중지
```bash
docker-compose down
```

## 🔧 개발 중 자주 사용하는 명령어

### 코드 수정 후 재시작
```bash
# 애플리케이션만 재빌드 및 재시작
docker-compose up -d --build app

# 로그 확인
docker-compose logs -f app
```

### 데이터베이스 접속
```bash
# MySQL 컨테이너 접속
docker-compose exec mysql mysql -u root -p
# 비밀번호: Aa5051140

# 데이터베이스 선택
USE findb;

# 테이블 확인
SHOW TABLES;
```

### 전체 정리 (데이터베이스 포함)
```bash
# 모든 컨테이너와 볼륨 삭제
docker-compose down -v
```

---

# ☁️ EC2 프로덕션 배포

현재 배포 방식: **Docker Hub → EC2 Pull 방식**

## 서버 정보
- **Public IP**: `54.180.103.186`
- **인스턴스**: AWS EC2 t2.micro (Amazon Linux 2)
- **데이터베이스**: AWS RDS MySQL 8.0
- **Docker Hub**: `dennyahn/fintech-server:latest`

## 접속 URL
- **API 베이스**: http://54.180.103.186:8080/api
- **Swagger UI**: http://54.180.103.186:8080/api/swagger-ui/index.html
- **헬스 체크**: http://54.180.103.186:8080/api/actuator/health

---

## 🔄 배포 프로세스

### 1단계: 로컬에서 Docker 이미지 빌드 & 푸시

```bash
# 1. 코드 수정 완료 후

# 2. Docker 이미지 빌드 (캐시 무시)
docker build --no-cache -t dennyahn/fintech-server:latest .

# 3. Docker Hub에 푸시
docker push dennyahn/fintech-server:latest
```

💡 **Tip**: Docker Hub 로그인이 필요하면 `docker login` 실행

---

### 2단계: EC2 서버에서 업데이트

#### Windows (PowerShell)
```powershell
# SSH 접속
ssh -i finquiz-key.pem ec2-user@54.180.103.186

# 작업 디렉토리 이동
cd ~/app

# 기존 컨테이너 중지 및 삭제
docker-compose -f docker-compose.prod.yml down

# 최신 이미지 다운로드
docker-compose -f docker-compose.prod.yml pull

# 컨테이너 시작
docker-compose -f docker-compose.prod.yml up -d

# 로그로 정상 작동 확인 (Ctrl+C로 종료)
docker-compose -f docker-compose.prod.yml logs -f app
```

#### Mac/Linux
```bash
# SSH 접속
ssh -i finquiz-key.pem ec2-user@54.180.103.186

# 나머지는 동일
cd ~/app
docker-compose -f docker-compose.prod.yml down
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d
docker-compose -f docker-compose.prod.yml logs -f app
```

---

## 📊 서버 상태 확인

### EC2 접속 후 확인 명령어

```bash
# 컨테이너 실행 상태
docker ps

# 애플리케이션 로그 (최근 100줄)
docker-compose -f docker-compose.prod.yml logs --tail=100 app

# 실시간 로그
docker-compose -f docker-compose.prod.yml logs -f app

# 서버 리소스 사용량
docker stats

# 디스크 사용량
df -h
```

### 로컬에서 확인 (SSH 없이)

```bash
# 헬스 체크
curl http://54.180.103.186:8080/api/actuator/health

# 브라우저에서 접속
http://54.180.103.186:8080/api/swagger-ui/index.html
```

---

## 🛠️ 서버 관리

### EC2 인스턴스 시작/중지

#### AWS Console
1. AWS Console → EC2 → 인스턴스
2. 인스턴스 선택 → **인스턴스 상태**
   - **시작**: 서버 켜기
   - **중지**: 서버 끄기 (비용 절약)
   - **재부팅**: 서버 재시작

⚠️ **주의**: 인스턴스 중지 시 Public IP가 변경될 수 있음

### SSH 연결 (Windows)

```powershell
# .pem 키 권한 설정 (최초 1회만)
icacls "finquiz-key.pem" /inheritance:r
icacls "finquiz-key.pem" /grant:r "%USERNAME%:(R)"

# SSH 접속
ssh -i finquiz-key.pem ec2-user@54.180.103.186
```

### SSH 연결 (Mac/Linux)

```bash
# .pem 키 권한 설정 (최초 1회만)
chmod 400 finquiz-key.pem

# SSH 접속
ssh -i finquiz-key.pem ec2-user@54.180.103.186
```

---

## ⚙️ 고급 관리

### 환경 변수 수정 (.env 파일)

```bash
# EC2 접속 후
cd ~/app

# .env 파일 편집
nano .env

# 변경사항 적용
docker-compose -f docker-compose.prod.yml up -d --force-recreate

# 로그 확인
docker-compose -f docker-compose.prod.yml logs -f app
```

### 컨테이너 관리

```bash
# 컨테이너 재시작 (이미지 변경 없이)
docker-compose -f docker-compose.prod.yml restart app

# 컨테이너 중지
docker-compose -f docker-compose.prod.yml stop

# 컨테이너 시작
docker-compose -f docker-compose.prod.yml start

# 컨테이너 상태 확인
docker-compose -f docker-compose.prod.yml ps
```

### 시스템 정리

```bash
# 사용하지 않는 이미지 제거
docker image prune -a

# 사용하지 않는 컨테이너 제거
docker container prune

# 전체 정리 (주의!)
docker system prune -a
```

---

## 🗄️ 데이터베이스 관리

### RDS 접속

```bash
# EC2 또는 로컬에서 접속
mysql -h finquiz-db.c7gqm40cytz9.ap-northeast-2.rds.amazonaws.com -u admin -p
# 비밀번호: Aa5051140!!

# 데이터베이스 선택
USE findb;

# 테이블 목록
SHOW TABLES;
```

---

## 🔍 트러블슈팅

### 문제 1: 컨테이너가 시작되지 않음

```bash
# 로그 확인
docker-compose -f docker-compose.prod.yml logs app

# 컨테이너 상세 정보
docker inspect fintech-app-prod

# 컨테이너 재생성
docker-compose -f docker-compose.prod.yml up -d --force-recreate
```

### 문제 2: DB 연결 실패

```bash
# RDS 엔드포인트 확인
cat .env | grep DB_HOST

# RDS 보안 그룹 확인 (AWS Console)
# - EC2 보안 그룹에서 3306 포트 허용 확인
```

### 문제 3: 외부 접속 안 됨

```bash
# EC2 보안 그룹 확인 (AWS Console)
# - 인바운드 규칙에 8080 포트 허용 확인

# 컨테이너 포트 바인딩 확인
docker ps
# PORTS 컬럼에 0.0.0.0:8080->8080/tcp 확인
```

### 문제 4: 메모리 부족

```bash
# 메모리 사용량 확인
free -h

# 컨테이너 리소스 사용량
docker stats

# 필요시 EC2 재부팅 (AWS Console)
```

### 문제 5: 로그 파일이 너무 큼

```bash
# 로그 크기 확인
docker inspect fintech-app-prod | grep -i log

# 시스템 로그 정리
sudo journalctl --vacuum-time=7d

# Docker 정리
docker system prune -a
```

---

## ✅ 배포 체크리스트

### 배포 전
- [ ] 로컬에서 충분히 테스트했는가?
- [ ] `.env` 파일 백업이 있는가?
- [ ] 데이터베이스 백업은 최신인가?
- [ ] 트래픽이 적은 시간대인가?

### 배포 중
- [ ] Docker 이미지 빌드 성공?
- [ ] Docker Hub 푸시 완료?
- [ ] EC2에서 이미지 Pull 완료?
- [ ] 컨테이너 정상 실행?

### 배포 후
- [ ] `docker ps` 로 컨테이너 확인
- [ ] 헬스 체크 API 정상 응답? (`{"status":"UP"}`)
- [ ] 로그에 에러 없음?
- [ ] Swagger UI 접속 가능?
- [ ] 실제 API 호출 테스트 완료?

---

## 💰 비용 절약 팁

```bash
# 개발하지 않을 때 EC2 중지
# AWS Console → EC2 → 인스턴스 중지
# 💡 중지 시 컴퓨팅 비용 청구 안 됨 (스토리지 비용만 청구)

# RDS 스냅샷 보관 기간 최소화
# AWS Console → RDS → 자동 백업 보관 기간 7일

# CloudWatch 로그 보관 기간 설정
# AWS Console → CloudWatch → 로그 그룹 → 보관 설정
```

---

## 📝 배포 아키텍처 요약

```
로컬 PC (Windows)
    ├─ 코드 수정
    ├─ docker build --no-cache -t dennyahn/fintech-server:latest .
    └─ docker push dennyahn/fintech-server:latest
         ↓
Docker Hub (dennyahn/fintech-server:latest)
         ↓
AWS EC2 (54.180.103.186)
    ├─ docker-compose -f docker-compose.prod.yml pull
    ├─ docker-compose -f docker-compose.prod.yml up -d
    └─ 컨테이너 실행 (포트 8080)
         ↓
AWS RDS (MySQL 8.0)
    └─ 데이터베이스 (findb)
```

---

## 📚 관련 문서

- [Deployment.md](./Deployment.md) - 전체 배포 현황
- [Deployment_solution.md](./Deployment_solution.md) - 배포 방식 비교 및 트러블슈팅
- [DOCKER_HUB_DEPLOY.md](./DOCKER_HUB_DEPLOY.md) - Docker Hub 배포 상세 가이드

---

## 👤 작성자

- **배포 담당**: dennyahn
- **배포 방식**: Docker Hub → EC2 Pull
- **최종 업데이트**: 2025년 10월 2일
