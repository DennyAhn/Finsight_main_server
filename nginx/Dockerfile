# ===================================================================
# Nginx Dockerfile for FinTech Server
# ===================================================================

FROM nginx:alpine

# Nginx 설정 파일 복사
COPY nginx.conf /etc/nginx/nginx.conf

# 로그 디렉토리 생성
RUN mkdir -p /var/log/nginx

# Nginx 사용자 권한 설정
RUN chown -R nginx:nginx /var/log/nginx

# 포트 노출
EXPOSE 80

# Nginx 실행
CMD ["nginx", "-g", "daemon off;"]
