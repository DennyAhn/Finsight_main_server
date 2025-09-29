-- User 테이블에 displayed_badge_id 컬럼 추가
ALTER TABLE users 
ADD COLUMN displayed_badge_id BIGINT NULL,
ADD CONSTRAINT FK_users_displayed_badge 
    FOREIGN KEY (displayed_badge_id) REFERENCES badges(id) ON DELETE SET NULL;

-- 컬럼 추가 확인
DESCRIBE users;
