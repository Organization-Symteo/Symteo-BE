-- 기존 리포트 테이블 삭제 (도메인 재구성)
DROP TABLE IF EXISTS `anxiety_reports`;
DROP TABLE IF EXISTS `depression_reports`;

-- 우울증 상세 리포트 (report_id 컬럼 추가 및 FK 설정)
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

-- 불안 상세 리포트 (report_id 컬럼 추가 및 FK 설정)
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

-- 진단 결과 분석 전용 AI 리포트
CREATE TABLE `diagnose_ai_reports` (
                                       `diag_ai_id` BIGINT NOT NULL AUTO_INCREMENT,
                                       `users_id` BIGINT NOT NULL,
                                       `report_id` BIGINT NOT NULL,      -- 부모 리포트(reports 테이블)와 1:1 연결
                                       `ai_contents` TEXT,               -- AI 분석 줄글 저장
                                       PRIMARY KEY (`diag_ai_id`),
                                       CONSTRAINT `FK_DiagAI_Report` FOREIGN KEY (`report_id`) REFERENCES `reports` (`report_id`) ON DELETE CASCADE,
                                       CONSTRAINT `FK_DiagAI_User` FOREIGN KEY (`users_id`) REFERENCES `users` (`users_id`) ON DELETE CASCADE
);