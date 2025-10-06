# 🚀 프로덕션 배포 가이드 (AWS RDS)

이 문서는 FinTech 서버를 AWS EC2에서 RDS와 함께 배포하는 방법을 설명합니다.

## 📋 사전 요구사항

- AWS EC2 인스턴스 (Ubuntu 20.04+ 권장)
- Docker & Docker Compose 설치
- AWS RDS MySQL 인스턴스 실행 중
- 도메인 및 SSL 인증서 (선택사항)

## 🔧 AWS RDS 연결 정보

```
엔드포인트: finquiz-db.c7gqm40cytz9.ap-northeast-2.rds.amazonaws.com
포트: 3306
사용자명: admin
비밀번호: Aa5051140!!
데이터베이스: findb
```

## 🚀 배포 단계

### 1. EC2 인스턴스 준비

```bash
# Docker 설치
sudo apt update
sudo apt install -y docker.io docker-compose

# Docker 서비스 시작
sudo systemctl start docker
sudo systemctl enable docker

# 사용자를 docker 그룹에 추가
sudo usermod -aG docker $USER
```

### 2. 프로젝트 클론 및 설정

```bash
# 프로젝트 클론
git clone <your-repository-url>
cd Fin_main_server

# 환경변수 설정
export JWT_SECRET="your-super-secret-jwt-key-for-production"
export S3_BUCKET="your-s3-bucket-name"
```

### 3. 프로덕션 배포 실행

```bash
# 프로덕션 환경으로 실행
docker-compose -f docker-compose.prod.yml up -d --build

# 로그 확인
docker-compose -f docker-compose.prod.yml logs -f
```

### 4. 서비스 상태 확인

```bash
# 컨테이너 상태 확인
docker-compose -f docker-compose.prod.yml ps

# 헬스 체크
curl http://localhost/actuator/health

# API 테스트
curl http://localhost/api/health
```

## 🔒 보안 설정

### 1. 방화벽 설정

```bash
# UFW 방화벽 설정
sudo ufw allow 22    # SSH
sudo ufw allow 80    # HTTP
sudo ufw allow 443   # HTTPS
sudo ufw enable
```

### 2. SSL 인증서 설정 (Let's Encrypt)

```bash
# Certbot 설치
sudo apt install -y certbot python3-certbot-nginx

# SSL 인증서 발급
sudo certbot --nginx -d your-domain.com

# 자동 갱신 설정
sudo crontab -e
# 다음 라인 추가:
# 0 12 * * * /usr/bin/certbot renew --quiet
```

### 3. 환경변수 보안

```bash
# .env 파일 생성
cat > .env << EOF
JWT_SECRET=your-super-secret-jwt-key-for-production
S3_BUCKET=your-s3-bucket-name
DB_PASSWORD=Aa5051140!!
EOF

# 파일 권한 설정
chmod 600 .env
```

## 📊 모니터링 설정

### 1. 로그 관리

```bash
# 로그 로테이션 설정
sudo tee /etc/logrotate.d/docker-containers << EOF
/var/lib/docker/containers/*/*.log {
    rotate 7
    daily
    compress
    size=1M
    missingok
    delaycompress
    copytruncate
}
EOF
```

### 2. 시스템 모니터링

```bash
# htop 설치
sudo apt install -y htop

# Docker 통계 확인
docker stats

# 디스크 사용량 확인
df -h
```

## 🔄 업데이트 및 유지보수

### 1. 애플리케이션 업데이트

```bash
# 최신 코드 가져오기
git pull origin main

# 서비스 재시작
docker-compose -f docker-compose.prod.yml down
docker-compose -f docker-compose.prod.yml up -d --build
```

### 2. 데이터베이스 백업

```bash
# RDS 스냅샷 생성 (AWS 콘솔에서)
# 또는 mysqldump 사용
mysqldump -h finquiz-db.c7gqm40cytz9.ap-northeast-2.rds.amazonaws.com \
          -u admin -p findb > backup_$(date +%Y%m%d_%H%M%S).sql
```

### 3. 서비스 재시작

```bash
# 특정 서비스만 재시작
docker-compose -f docker-compose.prod.yml restart app

# 모든 서비스 재시작
docker-compose -f docker-compose.prod.yml restart
```

## 🚨 문제 해결

### 1. 연결 문제

```bash
# RDS 연결 테스트
telnet finquiz-db.c7gqm40cytz9.ap-northeast-2.rds.amazonaws.com 3306

# 보안 그룹 확인 (AWS 콘솔)
# - 인바운드 규칙에 3306 포트 허용
# - 소스: EC2 보안 그룹 또는 IP 주소
```

### 2. 메모리 부족

```bash
# 메모리 사용량 확인
free -h
docker stats

# 스왑 메모리 설정
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
```

### 3. 로그 확인

```bash
# 애플리케이션 로그
docker-compose -f docker-compose.prod.yml logs app

# Nginx 로그
docker-compose -f docker-compose.prod.yml logs nginx

# 시스템 로그
sudo journalctl -u docker
```

## 📈 성능 최적화

### 1. JVM 튜닝

```yaml
# docker-compose.prod.yml에 추가
environment:
  JAVA_OPTS: "-Xms512m -Xmx1024m -XX:+UseG1GC"
```

### 2. Nginx 최적화

```nginx
# nginx.conf에 추가
worker_processes auto;
worker_connections 1024;
```

## 🔍 헬스 체크 엔드포인트

- **애플리케이션**: `http://your-domain.com/actuator/health`
- **API**: `http://your-domain.com/api/health`
- **Swagger**: `http://your-domain.com/api/swagger-ui.html`

## 📞 지원

문제가 발생하면 다음을 확인하세요:

1. AWS RDS 인스턴스 상태
2. EC2 보안 그룹 설정
3. Docker 컨테이너 로그
4. 시스템 리소스 사용량

---

**주의사항**: 프로덕션 환경에서는 반드시 강력한 JWT 시크릿 키와 데이터베이스 비밀번호를 사용하세요!
