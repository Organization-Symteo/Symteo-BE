-- V1에서 생성된 테이블이 없을 경우를 대비한 재생성
-- depression_reports와 anxiety_reports 테이블 구조 업데이트

-- 기존 테이블 삭제 후 재생성 (안전한 마이그레이션을 위해)
DROP TABLE IF EXISTS `diagnose_ai_reports`;
DROP TABLE IF EXISTS `anxiety_reports`;
DROP TABLE IF EXISTS `depression_reports`;

-- 1. depression_reports 테이블 재생성 (report_id 포함)
CREATE TABLE `depression_reports` (
    `de_report_id` BIGINT NOT NULL AUTO_INCREMENT,
    `users_id` BIGINT NOT NULL,
    `report_id` BIGINT NOT NULL,
    `depression_score` INT,
    `severity` VARCHAR(20),
    `core_score` INT,
    `physical_score` INT,
    `psychic_score` INT,
    `is_safety_flow` BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (`de_report_id`),
    CONSTRAINT `FK_DepRep_Report` FOREIGN KEY (`report_id`) REFERENCES `reports` (`report_id`) ON DELETE CASCADE,
    CONSTRAINT `FK_DepRep_User` FOREIGN KEY (`users_id`) REFERENCES `users` (`users_id`) ON DELETE CASCADE
);

-- 2. anxiety_reports 테이블 재생성 (report_id 포함)
CREATE TABLE `anxiety_reports` (
    `an_report_id` BIGINT NOT NULL AUTO_INCREMENT,
    `users_id` BIGINT NOT NULL,
    `report_id` BIGINT NOT NULL,
    `anxiety_score` INT,
    `severity` VARCHAR(20),
    `emotional_score` INT,
    `tension_score` INT,
    PRIMARY KEY (`an_report_id`),
    CONSTRAINT `FK_AnxRep_Report` FOREIGN KEY (`report_id`) REFERENCES `reports` (`report_id`) ON DELETE CASCADE,
    CONSTRAINT `FK_AnxRep_User` FOREIGN KEY (`users_id`) REFERENCES `users` (`users_id`) ON DELETE CASCADE
);

-- 3. 진단 결과 분석 전용 AI 리포트 테이블 생성
CREATE TABLE `diagnose_ai_reports` (
    `diag_ai_id` BIGINT NOT NULL AUTO_INCREMENT,
    `users_id` BIGINT NOT NULL,
    `report_id` BIGINT NOT NULL,
    `ai_contents` TEXT,
    PRIMARY KEY (`diag_ai_id`),
    CONSTRAINT `FK_DiagAI_Report` FOREIGN KEY (`report_id`) REFERENCES `reports` (`report_id`) ON DELETE CASCADE,
    CONSTRAINT `FK_DiagAI_User` FOREIGN KEY (`users_id`) REFERENCES `users` (`users_id`) ON DELETE CASCADE
);

