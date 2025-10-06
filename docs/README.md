# 📚 Finsight 서버 문서

이 폴더에는 Finsight 프로젝트의 모든 문서가 포함되어 있습니다.

## 📋 문서 목록

### 📘 API 문서
- **[API.md](API.md)** - 전체 API 엔드포인트 명세서
  - 인증 API
  - 퀴즈 API (일반 문제 + 가상기사 퀴즈)
  - 오답 노트 API
  - 커뮤니티 API
  - 배지 API
  - 관리자 API

### 🐳 Docker 관련
- **[DOCKER_README.md](DOCKER_README.md)** - Docker 개발 및 운영 가이드
  - 로컬 개발 환경 설정
  - EC2 프로덕션 서버 관리
  - 서버 시작/중지/업데이트 방법
  
- **[DOCKER_HUB_DEPLOY.md](DOCKER_HUB_DEPLOY.md)** - Docker Hub 배포 가이드
  - Docker Hub를 통한 배포 프로세스
  - 로컬 빌드 → Docker Hub 푸시 → EC2 Pull

### 🚀 배포 관련
- **[Deployment.md](Deployment.md)** - 현재 배포 현황
  - 배포 아키텍처
  - 배포 환경 (EC2, RDS, Docker Hub)
  - API 엔드포인트
  - 업데이트 방법

- **[Deployment_solution.md](Deployment_solution.md)** - 배포 문제 해결 과정 (포트폴리오용)
  - 배포 방식 비교 (Docker Hub vs EC2 직접 빌드)
  - 8가지 주요 문제와 해결 과정
  - 실전 성능 비교
  - 배운 점

- **[PRODUCTION_DEPLOYMENT.md](PRODUCTION_DEPLOYMENT.md)** - 프로덕션 배포 상세 가이드
  - EC2 환경 설정
  - 환경 변수 구성
  - 배포 명령어

### 🔄 기타
- **[Update.md](Update.md)** - 프로젝트 업데이트 로그
  - 기능 추가 내역
  - 버그 수정 내역

- **[USER_FLOW.md](USER_FLOW.md)** - 사용자 플로우 및 시스템 동작
  - 인증 플로우
  - 퀴즈 제출 플로우
  - 오답 노트 자동 생성 과정

---

## 🎯 빠른 시작

### 개발자용
1. **로컬 개발 시작**: [DOCKER_README.md](DOCKER_README.md) 참고
2. **API 사용법**: [API.md](API.md) 참고

### 배포 담당자용
1. **배포 프로세스**: [Deployment.md](Deployment.md) 참고
2. **문제 발생 시**: [Deployment_solution.md](Deployment_solution.md) 참고

### 프론트엔드 개발자용
1. **API 명세**: [API.md](API.md) 참고
2. **사용자 플로우**: [USER_FLOW.md](USER_FLOW.md) 참고

---

**💡 Tip**: GitHub에서는 이 문서들의 링크를 클릭하면 자동으로 해당 문서로 이동합니다!

