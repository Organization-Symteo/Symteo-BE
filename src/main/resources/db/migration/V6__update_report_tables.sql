-- V1에서 생성된 테이블 구조 업데이트
-- depression_reports와 anxiety_reports에 report_id 컬럼 추가 및 FK 설정

-- 1. depression_reports 테이블에 report_id 컬럼 추가
ALTER TABLE `depression_reports`
    ADD COLUMN `report_id` BIGINT NOT NULL AFTER `users_id`,
    ADD COLUMN `severity` VARCHAR(20) AFTER `depression_score`,
    ADD COLUMN `core_score` INT AFTER `severity`,
    ADD COLUMN `physical_score` INT AFTER `core_score`,
    ADD COLUMN `psychic_score` INT AFTER `physical_score`,
    ADD COLUMN `is_safety_flow` BOOLEAN DEFAULT FALSE AFTER `psychic_score`,
    MODIFY COLUMN `depression_scroe` INT COMMENT 'Deprecated: 오타 있음, depression_score 사용 권장';

-- 2. depression_reports에 reports 테이블 참조 FK 추가
ALTER TABLE `depression_reports`
    ADD CONSTRAINT `FK_DepRep_Report` FOREIGN KEY (`report_id`) REFERENCES `reports` (`report_id`) ON DELETE CASCADE;

-- 3. anxiety_reports 테이블에 report_id 컬럼 추가
ALTER TABLE `anxiety_reports`
    ADD COLUMN `report_id` BIGINT NOT NULL AFTER `users_id`,
    ADD COLUMN `severity` VARCHAR(20) AFTER `anxiety_score`,
    ADD COLUMN `emotional_score` INT AFTER `severity`,
    ADD COLUMN `tension_score` INT AFTER `emotional_score`;

-- 4. anxiety_reports에 reports 테이블 참조 FK 추가
ALTER TABLE `anxiety_reports`
    ADD CONSTRAINT `FK_AnxRep_Report` FOREIGN KEY (`report_id`) REFERENCES `reports` (`report_id`) ON DELETE CASCADE;

-- 5. 진단 결과 분석 전용 AI 리포트 테이블 생성
CREATE TABLE IF NOT EXISTS `diagnose_ai_reports` (
    `diag_ai_id` BIGINT NOT NULL AUTO_INCREMENT,
    `users_id` BIGINT NOT NULL,
    `report_id` BIGINT NOT NULL,
    `ai_contents` TEXT,
    PRIMARY KEY (`diag_ai_id`),
    CONSTRAINT `FK_DiagAI_Report` FOREIGN KEY (`report_id`) REFERENCES `reports` (`report_id`) ON DELETE CASCADE,
    CONSTRAINT `FK_DiagAI_User` FOREIGN KEY (`users_id`) REFERENCES `users` (`users_id`) ON DELETE CASCADE
);

