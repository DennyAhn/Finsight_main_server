# 🌐 Nginx 설정 가이드

이 폴더는 FinTech 서버의 Nginx 리버스 프록시 설정을 포함합니다.

## 📁 파일 구조

```
nginx/
├── Dockerfile          # Nginx 컨테이너 이미지 빌드 설정
├── nginx.conf          # Nginx 메인 설정 파일
└── README.md           # 이 파일
```

## 🔧 주요 설정 내용

### 1. CORS 설정
- **프론트엔드 도메인**: `https://finsight-c-7rus.vercel.app`
- **허용 메서드**: GET, POST, PUT, DELETE, OPTIONS, PATCH
- **허용 헤더**: Authorization, Content-Type, Range 등
- **인증 정보**: 쿠키 및 인증 헤더 허용

### 2. 리버스 프록시
- **업스트림**: `app:8080` (Spring Boot 애플리케이션)
- **로드 밸런싱**: 준비됨 (추가 서버 확장 가능)
- **타임아웃**: 연결 30초, 송신/수신 30초

### 3. 보안 헤더
- `X-Frame-Options`: SAMEORIGIN
- `X-Content-Type-Options`: nosniff
- `X-XSS-Protection`: 1; mode=block
- `Referrer-Policy`: strict-origin-when-cross-origin

### 4. 성능 최적화
- **Gzip 압축**: 활성화
- **정적 파일 캐싱**: 1년
- **Keep-Alive**: 65초

## 🚀 사용 방법

### 전체 스택 실행
```bash
# 프로젝트 루트에서 실행
docker-compose up --build
```

### Nginx만 실행
```bash
# Nginx 서비스만 실행
docker-compose up nginx
```

### 설정 테스트
```bash
# Nginx 설정 문법 검사
docker-compose exec nginx nginx -t

# Nginx 재시작
docker-compose restart nginx
```

## 🌍 접근 URL

- **API 엔드포인트**: http://localhost/api/
- **Swagger UI**: http://localhost/swagger-ui/
- **헬스 체크**: http://localhost/actuator/health

## 🔍 문제 해결

### CORS 오류
프론트엔드에서 CORS 오류가 발생하면 `nginx.conf`의 다음 부분을 확인하세요:

```nginx
add_header Access-Control-Allow-Origin "https://finsight-c-7rus.vercel.app" always;
```

### 프록시 오류
Spring Boot 애플리케이션이 정상 실행 중인지 확인:

```bash
# 애플리케이션 로그 확인
docker-compose logs app

# 헬스 체크
curl http://localhost/actuator/health
```

### 포트 충돌
80번 포트가 사용 중이라면 `docker-compose.yml`에서 포트를 변경:

```yaml
ports:
  - "8080:80"  # 로컬 포트 8080으로 변경
```

## 📝 설정 커스터마이징

### 새로운 프론트엔드 도메인 추가
`nginx.conf`에서 CORS 설정을 수정:

```nginx
# 여러 도메인 허용
add_header Access-Control-Allow-Origin "https://finsight-c-7rus.vercel.app, https://your-new-domain.com" always;
```

### SSL/HTTPS 설정
프로덕션 환경에서는 SSL 인증서를 설정하여 HTTPS를 활성화하세요:

1. SSL 인증서 파일을 `nginx/ssl/` 폴더에 배치
2. `nginx.conf`의 주석 처리된 HTTPS 서버 블록 활성화
3. 인증서 경로 수정

## 🔄 업데이트 이력

- **v1.0**: 초기 설정 (프론트엔드 CORS, 리버스 프록시)
- **v1.1**: 보안 헤더 추가
- **v1.2**: 성능 최적화 설정 추가
