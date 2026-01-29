-- 1. Missions 테이블에서 잘못된 컬럼 삭제
ALTER TABLE missions DROP COLUMN is_restarted;
ALTER TABLE missions DROP COLUMN is_drafted;

-- 2. UserMissions 테이블에 새로고침 여부 컬럼 추가
ALTER TABLE user_missions ADD COLUMN is_restarted TINYINT(1) DEFAULT 0 NOT NULL;