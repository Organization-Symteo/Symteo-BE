-- 1단계: 부모 테이블 (독립형)
CREATE TABLE `users` (
                         `users_id` BIGINT NOT NULL AUTO_INCREMENT,
                         `social_type` VARCHAR(20) NOT NULL,
                         `social_id` VARCHAR(255) NOT NULL,
                         `email` VARCHAR(255) NULL,
                         `nickname` VARCHAR(10) NULL,
                         `role` VARCHAR(20) NOT NULL DEFAULT 'USER',
                         `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         `deleted_at` DATETIME NULL,
                         PRIMARY KEY (`users_id`)
);

CREATE TABLE `missions` (
                            `mission_id` BIGINT NOT NULL AUTO_INCREMENT,
                            `m_contents` TEXT NULL,
                            `created_at` DATETIME NOT NULL,
                            `dead_line` DATETIME NOT NULL,
                            `is_restarted` BOOLEAN NOT NULL DEFAULT 0,
                            `is_drafted` BOOLEAN NOT NULL DEFAULT 0,
                            PRIMARY KEY (`mission_id`)
);

CREATE TABLE `strength` (
    `strength_id` BIGINT NOT NULL AUTO_INCREMENT,
    `strength_contents` TEXT NOT NULL,
    PRIMARY KEY (`strength_id`)
);

CREATE TABLE `stress_points` (
    `st_point_id` BIGINT NOT NULL AUTO_INCREMENT,
    `st_contents` TEXT NOT NULL,
    PRIMARY KEY (`st_point_id`)
);

CREATE TABLE `experiences` (
    `experience_id` BIGINT NOT NULL AUTO_INCREMENT,
    `ex_content` TEXT NOT NULL,
    PRIMARY KEY (`experience_id`)
);

CREATE TABLE `experienced_difficulties` (
    `ex_difficult_id` BIGINT NOT NULL AUTO_INCREMENT,
    `ex_difficult_content` TEXT NOT NULL,
    PRIMARY KEY (`ex_difficult_id`)
);

CREATE TABLE `today_lines` (
    `today_line_id` BIGINT NOT NULL AUTO_INCREMENT,
    `to_line_contents` TEXT NOT NULL,
    PRIMARY KEY (`today_line_id`)
);

-- 2단계: 유저 종속 테이블 (1:1, 1:N)
CREATE TABLE `settings` (
                            `settings_id` BIGINT NOT NULL AUTO_INCREMENT,
                            `users_id` BIGINT NOT NULL UNIQUE,
                            `is_cheer_msg_on` BOOLEAN NOT NULL DEFAULT TRUE,
                            `is_analysis_ready_on` BOOLEAN NOT NULL DEFAULT TRUE,
                            `is_monthly_report_on` BOOLEAN NOT NULL DEFAULT TRUE,
                            `is_lock_enabled` BOOLEAN NOT NULL DEFAULT FALSE,
                            PRIMARY KEY (`settings_id`),
                            CONSTRAINT `FK_Users_TO_Settings` FOREIGN KEY (`users_id`) REFERENCES `users` (`users_id`)
);

CREATE TABLE `user_tokens` (
                              `token_id` BIGINT NOT NULL AUTO_INCREMENT,
                              `user_id` BIGINT NOT NULL,
                              `refresh_token` VARCHAR(512) NOT NULL,
                              `expires_at` DATETIME NOT NULL,
                              PRIMARY KEY (`token_id`),
                              CONSTRAINT `FK_Users_TO_UserTokens` FOREIGN KEY (`user_id`) REFERENCES `users` (`users_id`)
);

CREATE TABLE `ai_counselor_settings` (
                                         `user_id` BIGINT NOT NULL,
                                         `expert_nature` VARCHAR(20) NOT NULL,
                                         `support_style` VARCHAR(20) NOT NULL,
                                         `action_style` VARCHAR(20) NOT NULL,
                                         `tone_distance` VARCHAR(20) NOT NULL,
                                         PRIMARY KEY (`user_id`),
                                         CONSTRAINT `FK_Users_TO_AICounselor` FOREIGN KEY (`user_id`) REFERENCES `users` (`users_id`)
);

CREATE TABLE `today_emotion` (
                                 `to_emotion_id` BIGINT NOT NULL AUTO_INCREMENT,
                                 `users_id` BIGINT NOT NULL,
                                 `to_emotion` INT NOT NULL,
                                 `te_created_at` DATETIME NOT NULL,
                                 PRIMARY KEY (`to_emotion_id`),
                                 CONSTRAINT `FK_Users_TO_Emotion` FOREIGN KEY (`users_id`) REFERENCES `users` (`users_id`)
);

-- 3단계: 미션/상담 관련 (N:M 해소)
CREATE TABLE `user_missions` (
                                 `u_mission_id` BIGINT NOT NULL AUTO_INCREMENT,
                                 `users_id` BIGINT NOT NULL,
                                 `mission_id` BIGINT NOT NULL,
                                 `completed_at` DATETIME NULL,
                                 `mu_contents` TEXT NOT NULL,
                                 `is_completed` BOOLEAN NOT NULL,
                                 `is_drafted` BOOLEAN NOT NULL,
                                 PRIMARY KEY (`u_mission_id`),
                                 CONSTRAINT `FK_Users_TO_UserMissions` FOREIGN KEY (`users_id`) REFERENCES `users` (`users_id`),
                                 CONSTRAINT `FK_Missions_TO_UserMissions` FOREIGN KEY (`mission_id`) REFERENCES `missions` (`mission_id`)
);

CREATE TABLE `drafts` (
    `draft_id` BIGINT NOT NULL AUTO_INCREMENT,
    `u_mission_id` BIGINT NOT NULL,
    `dm_contents` TEXT NOT NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    PRIMARY KEY (`draft_id`),
    CONSTRAINT `FK_UM_TO_Drafts` FOREIGN KEY (`u_mission_id`) REFERENCES `user_missions` (`u_mission_id`)
);

CREATE TABLE `mission_images` (
    `m_image_id` BIGINT NOT NULL AUTO_INCREMENT,
    `u_mission_id` BIGINT NOT NULL,
    `image_url` TEXT NOT NULL,
    PRIMARY KEY (`m_image_id`),
    CONSTRAINT `FK_UM_TO_Images` FOREIGN KEY (`u_mission_id`) REFERENCES `user_missions` (`u_mission_id`)
);

CREATE TABLE `counseling` (
    `counsel_id` BIGINT NOT NULL AUTO_INCREMENT,
    `users_id` BIGINT NOT NULL,
    `created_at` DATE NOT NULL,
    `updated_at` DATE NOT NULL,
    PRIMARY KEY (`counsel_id`),
    CONSTRAINT `FK_Users_TO_Counsel` FOREIGN KEY (`users_id`) REFERENCES `users` (`users_id`)
);

-- 4단계: 테스트(질문지) 7개
CREATE TABLE `anxiety_test` (
    `an_test_id` BIGINT NOT NULL AUTO_INCREMENT,
    `users_id` BIGINT NOT NULL,
    `an_test_1` INT,
    `an_test_7` INT,
    `created_at` DATE,
    PRIMARY KEY (`an_test_id`),
    CONSTRAINT `FK_U_AnxietyT` FOREIGN KEY (`users_id`) REFERENCES `users` (`users_id`)
);

CREATE TABLE `stress_test` (
    `st_test_id` BIGINT NOT NULL AUTO_INCREMENT,
    `users_id` BIGINT NOT NULL,
    `st_test_1` INT,
    `st_test_10` INT,
    `created_at` DATE,
    PRIMARY KEY (`st_test_id`),
    CONSTRAINT `FK_U_StressT` FOREIGN KEY (`users_id`) REFERENCES `users` (`users_id`)
);

CREATE TABLE `adhd_test` (
    `ad_test_id` BIGINT NOT NULL AUTO_INCREMENT,
    `users_id` BIGINT NOT NULL,
    `ad_test_1` INT,
    `ad_test_18` INT,
    `created_at` DATE,
    PRIMARY KEY (`ad_test_id`),
    CONSTRAINT `FK_U_ADHDT` FOREIGN KEY (`users_id`) REFERENCES `users` (`users_id`)
);

CREATE TABLE `burnout_test` (
    `bo_test_id` BIGINT NOT NULL AUTO_INCREMENT,
    `users_id` BIGINT NOT NULL,
    `bo_test_1` INT,
    `bo_test_22` INT,
    `created_at` DATE,
    PRIMARY KEY (`bo_test_id`),
    CONSTRAINT `FK_U_BurnOutT` FOREIGN KEY (`users_id`) REFERENCES `users` (`users_id`)
);

CREATE TABLE `depression_test` (
    `de_test_id` BIGINT NOT NULL AUTO_INCREMENT,
    `users_id` BIGINT NOT NULL,
    `de_test_1` INT,
    `de_test_9` INT,
    `created_at` DATE,
    PRIMARY KEY (`de_test_id`),
    CONSTRAINT `FK_U_DepressionT` FOREIGN KEY (`users_id`) REFERENCES `users` (`users_id`)
);

CREATE TABLE `attach_test` (
    `at_test_id` BIGINT NOT NULL AUTO_INCREMENT,
    `users_id` BIGINT NOT NULL,
    `at_test_1` INT,
    `at_test_36` INT,
    `created_at` DATE,
    PRIMARY KEY (`at_test_id`),
    CONSTRAINT `FK_U_AttachT` FOREIGN KEY (`users_id`) REFERENCES `users` (`users_id`)
);

-- 5단계: 결과 리포트 8개
CREATE TABLE `reports` (
    `report_id` BIGINT NOT NULL AUTO_INCREMENT,
    `users_id` BIGINT NOT NULL,
    `r_type` VARCHAR(50),
    `created_at` DATETIME,
    `completed_at` DATETIME,
    PRIMARY KEY (`report_id`),
    CONSTRAINT `FK_U_Rep` FOREIGN KEY (`users_id`) REFERENCES `users` (`users_id`)
);

CREATE TABLE `adhd_reports` (
    `ADHD_report_id` BIGINT NOT NULL AUTO_INCREMENT,
    `users_id` BIGINT NOT NULL,
    `def_score` INT,
    `symptom_count` INT,
    PRIMARY KEY (`ADHD_report_id`),
    CONSTRAINT `FK_U_ADHDRep` FOREIGN KEY (`users_id`) REFERENCES `users` (`users_id`)
);

CREATE TABLE `adhd_detail_reports` (
    `ADHD_detail_id` BIGINT NOT NULL AUTO_INCREMENT,
    `ADHD_report_id` BIGINT NOT NULL,
    `ex_difficult_id` BIGINT NOT NULL,
    `experience_id` BIGINT NOT NULL,
    PRIMARY KEY (`ADHD_detail_id`),
    CONSTRAINT `FK_AR_Detail` FOREIGN KEY (`ADHD_report_id`) REFERENCES `adhd_reports` (`ADHD_report_id`),
    CONSTRAINT `FK_ED_Detail` FOREIGN KEY (`ex_difficult_id`) REFERENCES `experienced_difficulties` (`ex_difficult_id`),
    CONSTRAINT `FK_EX_Detail` FOREIGN KEY (`experience_id`) REFERENCES `experiences` (`experience_id`)
);

CREATE TABLE `depression_reports` (
    `de_report_id` BIGINT NOT NULL AUTO_INCREMENT,
    `users_id` BIGINT NOT NULL,
    `depression_scroe` INT,
    PRIMARY KEY (`de_report_id`),
    CONSTRAINT `FK_U_DepRep` FOREIGN KEY (`users_id`) REFERENCES `users` (`users_id`)
);

CREATE TABLE `stress_reports` (
    `st_report_id` BIGINT NOT NULL AUTO_INCREMENT,
    `users_id` BIGINT NOT NULL,
    `stress_temperature` INT,
    PRIMARY KEY (`st_report_id`),
    CONSTRAINT `FK_U_StrRep` FOREIGN KEY (`users_id`) REFERENCES `users` (`users_id`)
);

CREATE TABLE `anxiety_reports` (
    `an_report_id` BIGINT NOT NULL AUTO_INCREMENT,
    `users_id` BIGINT NOT NULL,
    `anxiety_score` INT,
    PRIMARY KEY (`an_report_id`),
    CONSTRAINT `FK_U_AnxRep` FOREIGN KEY (`users_id`) REFERENCES `users` (`users_id`)
);

CREATE TABLE `attachment_reports` (
    `at_report_id` BIGINT NOT NULL AUTO_INCREMENT,
    `users_id` BIGINT NOT NULL,
    `strength_id` BIGINT NOT NULL,
    `st_point_id` BIGINT NOT NULL,
    PRIMARY KEY (`at_report_id`),
    CONSTRAINT `FK_U_AttRep` FOREIGN KEY (`users_id`) REFERENCES `users` (`users_id`),
    CONSTRAINT `FK_ST_AttRep` FOREIGN KEY (`strength_id`) REFERENCES `strength` (`strength_id`),
    CONSTRAINT `FK_SP_AttRep` FOREIGN KEY (`st_point_id`) REFERENCES `stress_points` (`st_point_id`)
);

CREATE TABLE `burnout_reports` (
    `burnout_id` BIGINT NOT NULL AUTO_INCREMENT,
    `users_id` BIGINT NOT NULL,
    `exh_score` INT,
    PRIMARY KEY (`burnout_id`),
    CONSTRAINT `FK_U_BurRep` FOREIGN KEY (`users_id`) REFERENCES `users` (`users_id`)
);

CREATE TABLE `ai_reports` (
    `ai_report_id` BIGINT NOT NULL AUTO_INCREMENT,
    `users_id` BIGINT NOT NULL,
    `ai_title` VARCHAR(50),
    `ai_contents` TEXT,
    PRIMARY KEY (`ai_report_id`),
    CONSTRAINT `FK_U_AIRep` FOREIGN KEY (`users_id`) REFERENCES `users` (`users_id`)
);
