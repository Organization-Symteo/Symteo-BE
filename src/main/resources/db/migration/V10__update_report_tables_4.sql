-- strength 테이블 컬럼 추가
ALTER TABLE `strength` ADD COLUMN `attachment_type` VARCHAR(255) NOT NULL;
ALTER TABLE `strength` ADD COLUMN `title` VARCHAR(255) NOT NULL;

-- ㄴtress_points 테이블 컬럼 추가
ALTER TABLE `stress_points` ADD COLUMN `attachment_type` VARCHAR(255) NOT NULL;
ALTER TABLE `stress_points` ADD COLUMN `title` VARCHAR(255) NOT NULL;

-- attachment_reports 테이블 구조 변경 (2개씩 매핑하기 위해 컬럼 확장)
DROP TABLE IF EXISTS `attachment_reports`;

CREATE TABLE `attachment_reports` (
                                      `at_report_id`           BIGINT NOT NULL AUTO_INCREMENT,
                                      `report_id`              BIGINT NOT NULL,
                                      `users_id`               BIGINT NOT NULL,
                                      `st_point_id_1`          BIGINT NOT NULL,
                                      `st_point_id_2`          BIGINT NOT NULL,
                                      `strength_id_1`          BIGINT NOT NULL,
                                      `strength_id_2`          BIGINT NOT NULL,
                                      `anxiety_score`          DOUBLE,
                                      `avoidance_score`        DOUBLE,
                                      `attachment_type`        VARCHAR(255),
                                      `ai_full_content`        TEXT,
                                      `action_guide_sentence`  TEXT,
                                      PRIMARY KEY (`at_report_id`),
                                      CONSTRAINT `FK_Rep_AttRep` FOREIGN KEY (`report_id`) REFERENCES `reports` (`report_id`),
                                      CONSTRAINT `FK_U_AttRep` FOREIGN KEY (`users_id`) REFERENCES `users` (`users_id`),
                                      CONSTRAINT `FK_SP1_AttRep` FOREIGN KEY (`st_point_id_1`) REFERENCES `stress_points` (`st_point_id`),
                                      CONSTRAINT `FK_SP2_AttRep` FOREIGN KEY (`st_point_id_2`) REFERENCES `stress_points` (`st_point_id`),
                                      CONSTRAINT `FK_ST1_AttRep` FOREIGN KEY (`strength_id_1`) REFERENCES `strength` (`strength_id`),
                                      CONSTRAINT `FK_ST2_AttRep` FOREIGN KEY (`strength_id_2`) REFERENCES `strength` (`strength_id`)
);