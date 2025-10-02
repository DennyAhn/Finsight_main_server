# 🚀 Docker Hub를 이용한 EC2 배포 가이드

## ⚠️ 왜 이 방식을 사용하나?

AWS t2.micro 인스턴스는 **메모리가 1GB밖에 없어서** EC2에서 직접 빌드하면 실패합니다!
따라서 **로컬 PC에서 빌드**하고, EC2에서는 **이미지만 다운로드**하는 방식을 사용합니다.

---

## 📋 사전 준비

### 1. Docker Hub 계정 생성
1. https://hub.docker.com 접속
2. 무료 계정 생성
3. 사용자명 기억하기 (예: `dennyahn`)

### 2. 로컬에 Docker 설치
- Windows: Docker Desktop 설치
- 설치 확인: `docker --version`

---

## 🔧 1단계: 설정 파일 수정

### `deploy.ps1` 파일 수정
```powershell
$DOCKER_USERNAME = "dennyahn"  # ← 본인의 Docker Hub 사용자명
```

### `docker-compose.prod.yml` 파일 수정
```yaml
image: dennyahn/fintech-server:latest  # ← 본인의 Docker Hub 사용자명
```

---

## 🚀 2단계: 로컬에서 빌드 및 배포

### Windows PowerShell에서 실행:
```powershell
# 1. Docker Hub에 로그인 (처음 1회만)
docker login

# 2. 이미지 빌드 및 푸시
.\deploy.ps1
```

**소요 시간:** 약 5-10분 (로컬 PC 성능에 따라)

---

## 🖥️ 3단계: EC2에서 실행

### EC2 접속:
```bash
ssh -i "키페어.pem" ubuntu@54.180.103.186
```

### EC2에서 실행:
```bash
# 프로젝트 디렉토리로 이동 (없으면 git clone 먼저)
cd Fin_main_server

# 최신 코드 가져오기
git pull origin main

# .env 파일 생성 (처음 1회만)
cat > .env << 'EOF'
DB_HOST=finquiz-db.c7gqm40cytz9.ap-northeast-2.rds.amazonaws.com
DB_PORT=3306
DB_NAME=findb
DB_USERNAME=admin
DB_PASSWORD=Aa5051140!!
JWT_SECRET=finquiz-production-jwt-secret-key-2025
AWS_REGION=ap-northeast-2
S3_BUCKET=fin.img99
EOF

# 기존 컨테이너 중지
docker-compose -f docker-compose.prod.yml down

# 최신 이미지 다운로드 및 실행
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d

# 로그 확인
docker-compose -f docker-compose.prod.yml logs -f
```

**소요 시간:** 약 1-2분 (이미지 다운로드만 하므로 빠름!)

---

## ✅ 4단계: 확인

### 브라우저에서 접속:
- Health Check: http://54.180.103.186/actuator/health
- Swagger UI: http://54.180.103.186/api/swagger-ui.html

---

## 🔄 업데이트 프로세스

코드를 수정했을 때:

### 로컬:
```powershell
# 1. 코드 수정
# 2. Git 커밋 & 푸시
git add .
git commit -m "업데이트 내용"
git push origin main

# 3. Docker 이미지 새로 빌드 & 푸시
.\deploy.ps1
```

### EC2:
```bash
# 1. 코드 업데이트
git pull origin main

# 2. 새 이미지 다운로드 & 재시작
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d
```

---

## 📊 리소스 비교

| 방식 | EC2 빌드 시간 | EC2 메모리 사용 | 성공률 |
|------|---------------|-----------------|--------|
| EC2에서 빌드 | 10-30분 | 2-3GB | ❌ 10% |
| Docker Hub 사용 | 1-2분 | 500MB | ✅ 99% |

---

## 🆘 문제 해결

### 이미지 푸시 실패
```powershell
# Docker Hub 재로그인
docker login
```

### EC2에서 이미지 다운로드 실패
```bash
# Docker Hub이 public인지 확인
# 또는 EC2에서 docker login 실행
```

### 메모리 부족
```bash
# 스왑 메모리 추가 (EC2에서)
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
```

---

## 💡 장점

✅ EC2 리소스 절약 (t2.micro에서 안정적)
✅ 빌드 시간 단축 (로컬에서 한 번만 빌드)
✅ 배포 속도 향상 (이미지만 다운로드)
✅ 여러 서버에 동일 이미지 배포 가능

