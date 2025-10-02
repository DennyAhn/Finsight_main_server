# =================================================================
# Docker Hub 배포 스크립트 (Windows PowerShell)
# =================================================================

# 설정
$DOCKER_USERNAME = "dennyahn"  # Docker Hub 사용자명
$IMAGE_NAME = "fintech-server"
$VERSION = "latest"

Write-Host "🚀 Docker 이미지 빌드 시작..." -ForegroundColor Green

# 1. Docker 이미지 빌드
docker build -t "${DOCKER_USERNAME}/${IMAGE_NAME}:${VERSION}" .

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Docker 이미지 빌드 실패!" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Docker 이미지 빌드 완료!" -ForegroundColor Green

# 2. Docker Hub에 로그인 (처음 1회만)
Write-Host "🔐 Docker Hub 로그인..." -ForegroundColor Yellow
docker login

# 3. Docker Hub에 푸시
Write-Host "📤 Docker Hub에 이미지 푸시..." -ForegroundColor Yellow
docker push "${DOCKER_USERNAME}/${IMAGE_NAME}:${VERSION}"

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Docker 이미지 푸시 실패!" -ForegroundColor Red
    exit 1
}

Write-Host "✅ 배포 완료!" -ForegroundColor Green
Write-Host "📦 이미지: ${DOCKER_USERNAME}/${IMAGE_NAME}:${VERSION}" -ForegroundColor Cyan

