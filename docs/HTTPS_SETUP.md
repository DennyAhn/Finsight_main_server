# 🔒 HTTPS 설정 가이드 (EC2 + Nginx + Let's Encrypt)

## 📋 개요

Vercel (HTTPS)에서 배포된 프론트엔드가 EC2 (HTTP) 백엔드와 통신할 때 발생하는 **Mixed Content 문제**를 해결합니다.

---

## 🎯 목표

```
프론트엔드 (Vercel)
  └─ HTTPS ✅ (https://your-app.vercel.app)
       ↓ API 호출
백엔드 (EC2)
  └─ HTTPS ✅ (https://api.yourdomain.com)
```

---

## 📝 사전 준비

### 필수 사항
1. **도메인** - 백엔드 API용 (예: `api.yourdomain.com`)
2. **EC2 실행 중** - 현재 상태 유지
3. **EC2 보안 그룹** - 80, 443 포트 열기

### 옵션 1: 도메인이 있는 경우
- 도메인 DNS 설정에서 A 레코드 추가
- `api.yourdomain.com` → EC2 Public IP (`54.180.103.186`)

### 옵션 2: 도메인이 없는 경우
무료 도메인 서비스 사용:
- **Duck DNS**: https://www.duckdns.org/ (추천)
- **FreeDNS**: https://freedns.afraid.org/
- **No-IP**: https://www.noip.com/

---

## 🚀 설치 방법

### 1단계: EC2 보안 그룹 설정

AWS Console에서:
1. EC2 → 보안 그룹
2. 인바운드 규칙 추가:
   - HTTP (80) - 모든 곳에서 (0.0.0.0/0)
   - HTTPS (443) - 모든 곳에서 (0.0.0.0/0)

### 2단계: EC2 접속

```bash
ssh -i C:/Users/ahj20/Desktop/K_Hackathon/finquiz-key.pem ubuntu@54.180.103.186
```

### 3단계: Certbot 설치

```bash
# 시스템 업데이트
sudo apt update

# Certbot 설치
sudo apt install certbot python3-certbot-nginx -y
```

### 4단계: Nginx 설정 파일 생성

```bash
# Nginx 설정 디렉토리로 이동
cd ~/app

# nginx.conf 생성
cat > nginx.conf << 'EOF'
events {
    worker_connections 1024;
}

http {
    upstream backend {
        server fintech-app-prod:8080;
    }

    server {
        listen 80;
        server_name api.yourdomain.com;  # ⚠️ 실제 도메인으로 변경!

        # HTTPS로 리다이렉트
        return 301 https://$server_name$request_uri;
    }

    server {
        listen 443 ssl http2;
        server_name api.yourdomain.com;  # ⚠️ 실제 도메인으로 변경!

        # SSL 인증서 경로 (Let's Encrypt가 자동으로 설정)
        ssl_certificate /etc/letsencrypt/live/api.yourdomain.com/fullchain.pem;
        ssl_certificate_key /etc/letsencrypt/live/api.yourdomain.com/privkey.pem;

        # SSL 설정
        ssl_protocols TLSv1.2 TLSv1.3;
        ssl_prefer_server_ciphers on;
        ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512:ECDHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES256-GCM-SHA384;

        # API 요청
        location /api/ {
            proxy_pass http://backend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        # Health Check
        location /actuator/health {
            proxy_pass http://backend/api/actuator/health;
            proxy_set_header Host $host;
        }
    }
}
EOF
```

### 5단계: docker-compose.prod.yml 업데이트

```bash
# EC2에서 실행
cd ~/app

# docker-compose.prod.yml 수정 (nginx 서비스 추가)
cat > docker-compose.prod.yml << 'EOF'
version: '3.8'

services:
  app:
    image: dennyahn/fintech-server:latest
    container_name: fintech-app-prod
    restart: unless-stopped
    expose:
      - "8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:mysql://${DB_HOST}:${DB_PORT:-3306}/${DB_NAME}?useSSL=true&serverTimezone=UTC&characterEncoding=UTF-8
      SPRING_DATASOURCE_USERNAME: ${DB_USERNAME}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      SPRING_DATASOURCE_DRIVER_CLASS_NAME: com.mysql.cj.jdbc.Driver
      SPRING_JPA_HIBERNATE_DDL_AUTO: validate
      SPRING_JPA_SHOW_SQL: false
      SPRING_JPA_DATABASE_PLATFORM: org.hibernate.dialect.MySQL8Dialect
      SERVER_PORT: 8080
      SERVER_SERVLET_CONTEXT_PATH: /api
      JWT_SECRET: ${JWT_SECRET}
      AWS_REGION: ${AWS_REGION:-ap-northeast-2}
      AWS_S3_BUCKET: ${S3_BUCKET}
      LOGGING_LEVEL_ROOT: WARN
      LOGGING_LEVEL_COM_FINTECH_SERVER: INFO
    networks:
      - fintech-network

  nginx:
    image: nginx:alpine
    container_name: fintech-nginx-prod
    restart: unless-stopped
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
      - /etc/letsencrypt:/etc/letsencrypt:ro
    depends_on:
      - app
    networks:
      - fintech-network

networks:
  fintech-network:
    driver: bridge
EOF
```

### 6단계: SSL 인증서 발급

```bash
# 컨테이너 실행 (nginx 포함)
docker-compose -f docker-compose.prod.yml up -d

# SSL 인증서 발급
sudo certbot certonly --nginx -d api.yourdomain.com

# 이메일 입력 (인증서 갱신 알림용)
# 약관 동의 (Y)
# 뉴스레터 수신 여부 (N)
```

### 7단계: Nginx 재시작

```bash
# nginx 컨테이너 재시작
docker-compose -f docker-compose.prod.yml restart nginx

# 로그 확인
docker-compose -f docker-compose.prod.yml logs nginx
```

---

## ✅ 테스트

### 1. HTTPS 접속 확인
```bash
curl https://api.yourdomain.com/api/actuator/health
```

**예상 결과:**
```json
{"status":"UP"}
```

### 2. 브라우저에서 확인
```
https://api.yourdomain.com/api/actuator/health
```

### 3. 프론트엔드에서 API 호출
```javascript
// 프론트엔드 코드
const API_URL = 'https://api.yourdomain.com/api';

fetch(`${API_URL}/health`)
  .then(res => res.json())
  .then(data => console.log(data));
```

---

## 🔄 SSL 인증서 자동 갱신

Let's Encrypt 인증서는 **90일마다** 갱신해야 합니다.

### 자동 갱신 설정

```bash
# Cron 작업 추가
sudo crontab -e

# 매일 자정에 갱신 확인
0 0 * * * certbot renew --quiet && docker-compose -f /home/ubuntu/app/docker-compose.prod.yml restart nginx
```

---

## 🚨 트러블슈팅

### 문제 1: 인증서 발급 실패
```
Error: Unable to find a virtual host
```

**해결:**
- 도메인 DNS 설정 확인
- A 레코드가 EC2 IP를 가리키는지 확인
- DNS 전파 대기 (최대 48시간)

### 문제 2: nginx 컨테이너 시작 실패
```
Error: cannot load certificate
```

**해결:**
- 인증서 경로 확인: `/etc/letsencrypt/live/api.yourdomain.com/`
- docker-compose.prod.yml의 volumes 설정 확인

### 문제 3: Mixed Content 에러 지속
```
Mixed Content: The page at 'https://...'
```

**해결:**
- 프론트엔드 API URL이 HTTPS로 설정되었는지 확인
- 브라우저 캐시 삭제
- 시크릿 모드에서 테스트

---

## 💡 추가 최적화

### CORS 설정 확인

Spring Boot `SecurityConfig.java`에서:
```java
configuration.setAllowedOriginPatterns(Arrays.asList(
    "https://your-app.vercel.app",
    "https://api.yourdomain.com"
));
```

### Nginx 성능 최적화

```nginx
# gzip 압축
gzip on;
gzip_types text/plain application/json;

# 캐싱
proxy_cache_path /var/cache/nginx levels=1:2 keys_zone=my_cache:10m;
proxy_cache my_cache;
```

---

## 📚 참고 자료

- [Let's Encrypt 공식 문서](https://letsencrypt.org/getting-started/)
- [Certbot 사용 가이드](https://certbot.eff.org/)
- [Nginx SSL 설정](https://nginx.org/en/docs/http/configuring_https_servers.html)

---

**🎉 설정 완료 후 프론트엔드가 안전하게 백엔드 API를 호출할 수 있습니다!**

