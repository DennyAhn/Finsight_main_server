-- ===================================================================
-- 초기 데이터베이스 설정 스크립트
-- ===================================================================

-- 데이터베이스 생성 (이미 docker-compose에서 생성되지만 안전을 위해)
CREATE DATABASE IF NOT EXISTS findb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 데이터베이스 사용
USE findb;

-- 필요한 테이블들이 애플리케이션 시작 시 자동으로 생성됩니다.
-- (JPA의 ddl-auto: validate 설정으로 인해)

-- 초기 데이터 삽입 (필요한 경우)
-- 예시: 기본 관리자 계정이나 초기 설정 데이터

-- 완료 메시지
SELECT 'Database initialization completed successfully!' as message;
