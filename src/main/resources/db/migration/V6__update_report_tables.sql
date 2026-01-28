-- V6: reports 테이블 및 자식 테이블 재구성
-- 부모 테이블(reports)을 먼저 생성한 후 자식 테이블들 생성

-- 1. 부모 테이블(reports) 먼저 확인 및 생성
-- V1에서 이미 생성되었을 수 있지만, 구조가 다를 수 있으므로 확인
CREATE TABLE IF NOT EXISTS `reports` (
    `report_id` BIGINT NOT NULL AUTO_INCREMENT,
    `users_id` BIGINT NOT NULL,
    `r_type` VARCHAR(50),
    `created_at` DATETIME,
    `completed_at` DATETIME,
    PRIMARY KEY (`report_id`),
    CONSTRAINT `FK_Reports_User` FOREIGN KEY (`users_id`) REFERENCES `users` (`users_id`) ON DELETE CASCADE
);

-- 2. 기존 자식 테이블 삭제 (FK 때문에 순서 중요)
DROP TABLE IF EXISTS `diagnose_ai_reports`;
DROP TABLE IF EXISTS `anxiety_reports`;
DROP TABLE IF EXISTS `depression_reports`;

-- 3. depression_reports 테이블 재생성 (report_id 포함)
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

-- 4. anxiety_reports 테이블 재생성 (report_id 포함)
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

-- 5. 진단 결과 분석 전용 AI 리포트 테이블 생성
CREATE TABLE `diagnose_ai_reports` (
    `diag_ai_id` BIGINT NOT NULL AUTO_INCREMENT,
    `users_id` BIGINT NOT NULL,
    `report_id` BIGINT NOT NULL,
    `ai_contents` TEXT,
    PRIMARY KEY (`diag_ai_id`),
    CONSTRAINT `FK_DiagAI_Report` FOREIGN KEY (`report_id`) REFERENCES `reports` (`report_id`) ON DELETE CASCADE,
    CONSTRAINT `FK_DiagAI_User` FOREIGN KEY (`users_id`) REFERENCES `users` (`users_id`) ON DELETE CASCADE
);

