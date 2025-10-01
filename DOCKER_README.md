# 🐳 Docker 배포 가이드

이 문서는 FinTech 서버를 Docker를 사용하여 로컬에서 실행하는 방법을 설명합니다.

## 📋 사전 요구사항

- Docker Desktop (Windows/Mac) 또는 Docker Engine (Linux)
- Docker Compose
- 최소 4GB RAM 권장

## 🚀 빠른 시작

### 1. 전체 스택 실행 (데이터베이스 + 애플리케이션)

```bash
# 모든 서비스 빌드 및 실행
docker-compose up --build

# 백그라운드에서 실행
docker-compose up -d --build
```

### 2. 애플리케이션만 실행 (데이터베이스가 이미 실행 중인 경우)

```bash
# 애플리케이션만 빌드 및 실행
docker-compose up --build app
```

## 🔧 개별 명령어

### Docker 이미지 빌드

```bash
# 애플리케이션 이미지 빌드
docker build -t fintech-app .

# 특정 태그로 빌드
docker build -t fintech-app:latest .
```

### 컨테이너 실행

```bash
# MySQL만 실행
docker-compose up mysql

# 애플리케이션만 실행 (MySQL이 실행 중이어야 함)
docker-compose up app
```

## 📊 서비스 상태 확인

### 실행 중인 컨테이너 확인

```bash
# 모든 컨테이너 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs

# 특정 서비스 로그 확인
docker-compose logs app
docker-compose logs mysql
```

### 헬스 체크

```bash
# 애플리케이션 헬스 체크
curl http://localhost:8080/api/actuator/health

# Swagger UI 접속
http://localhost:8080/api/swagger-ui.html
```

## 🗄️ 데이터베이스 관리

### 데이터베이스 접속

```bash
# MySQL 컨테이너에 접속
docker-compose exec mysql mysql -u root -p

# 데이터베이스 선택
USE findb;
```

### 데이터 백업

```bash
# 데이터베이스 덤프 생성
docker-compose exec mysql mysqldump -u root -p findb > backup.sql
```

### 데이터 복원

```bash
# 백업 파일 복원
docker-compose exec -T mysql mysql -u root -p findb < backup.sql
```

## 🛠️ 개발 및 디버깅

### 컨테이너 내부 접속

```bash
# 애플리케이션 컨테이너 접속
docker-compose exec app bash

# MySQL 컨테이너 접속
docker-compose exec mysql bash
```

### 환경 변수 설정

`docker-compose.yml` 파일의 `environment` 섹션에서 환경 변수를 수정할 수 있습니다:

```yaml
environment:
  SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/findb
  SPRING_PROFILES_ACTIVE: docker
```

## 🧹 정리 명령어

### 서비스 중지 및 제거

```bash
# 모든 서비스 중지
docker-compose down

# 볼륨까지 제거 (데이터 삭제됨)
docker-compose down -v

# 이미지까지 제거
docker-compose down --rmi all
```

### 시스템 정리

```bash
# 사용하지 않는 컨테이너 제거
docker container prune

# 사용하지 않는 이미지 제거
docker image prune

# 사용하지 않는 볼륨 제거
docker volume prune

# 전체 시스템 정리
docker system prune -a
```

## 🔍 문제 해결

### 포트 충돌

만약 포트 8080이나 3307이 이미 사용 중이라면, `docker-compose.yml`에서 포트를 변경하세요:

```yaml
ports:
  - "8081:8080"  # 로컬 포트 8081로 변경
  - "3308:3306"  # 로컬 포트 3308로 변경
```

### 메모리 부족

Docker Desktop에서 메모리 할당량을 늘리세요:
- Docker Desktop → Settings → Resources → Memory

### 로그 확인

```bash
# 실시간 로그 확인
docker-compose logs -f app

# 특정 시간대 로그 확인
docker-compose logs --since="2024-01-01T00:00:00" app
```

## 📝 주요 포트

- **애플리케이션**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **MySQL**: localhost:3307
- **Actuator Health**: http://localhost:8080/api/actuator/health

## 🎯 다음 단계

1. 프로덕션 환경 배포를 위한 설정 최적화
2. CI/CD 파이프라인 구축
3. 모니터링 및 로깅 시스템 구축
4. 보안 설정 강화
