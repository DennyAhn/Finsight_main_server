#!/bin/bash

# =================================================================
# Docker Hub 배포 스크립트
# =================================================================

# 설정
DOCKER_USERNAME="dennyahn"  # Docker Hub 사용자명
IMAGE_NAME="fintech-server"
VERSION="latest"

echo "🚀 Docker 이미지 빌드 시작..."

# 1. Docker 이미지 빌드
docker build -t ${DOCKER_USERNAME}/${IMAGE_NAME}:${VERSION} .

if [ $? -ne 0 ]; then
    echo "❌ Docker 이미지 빌드 실패!"
    exit 1
fi

echo "✅ Docker 이미지 빌드 완료!"

# 2. Docker Hub에 로그인 (처음 1회만)
echo "🔐 Docker Hub 로그인..."
docker login

# 3. Docker Hub에 푸시
echo "📤 Docker Hub에 이미지 푸시..."
docker push ${DOCKER_USERNAME}/${IMAGE_NAME}:${VERSION}

if [ $? -ne 0 ]; then
    echo "❌ Docker 이미지 푸시 실패!"
    exit 1
fi

echo "✅ 배포 완료!"
echo "📦 이미지: ${DOCKER_USERNAME}/${IMAGE_NAME}:${VERSION}"

