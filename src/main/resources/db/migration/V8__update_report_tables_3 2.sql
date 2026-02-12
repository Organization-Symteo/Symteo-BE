-- strength 테이블이 없으면 생성
CREATE TABLE IF NOT EXISTS `strength` (
    `strength_id` BIGINT NOT NULL AUTO_INCREMENT,
    `strength_contents` TEXT NOT NULL,
    PRIMARY KEY (`strength_id`)
);

-- stress_points 테이블이 없으면 생성
CREATE TABLE IF NOT EXISTS `stress_points` (
    `st_point_id` BIGINT NOT NULL AUTO_INCREMENT,
    `st_contents` TEXT NOT NULL,
    PRIMARY KEY (`st_point_id`)
);

-- strength 테이블에 attachment_type 컬럼 추가 (존재하지 않을 경우에만)
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'strength'
AND COLUMN_NAME = 'attachment_type';

SET @query = IF(@col_exists = 0,
    'ALTER TABLE `strength` ADD COLUMN `attachment_type` VARCHAR(255)',
    'SELECT "Column attachment_type already exists" AS msg');
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- strength 테이블에 title 컬럼 추가
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'strength'
AND COLUMN_NAME = 'title';

SET @query = IF(@col_exists = 0,
    'ALTER TABLE `strength` ADD COLUMN `title` VARCHAR(255)',
    'SELECT "Column title already exists" AS msg');
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- stress_points 테이블에 attachment_type 컬럼 추가
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'stress_points'
AND COLUMN_NAME = 'attachment_type';

SET @query = IF(@col_exists = 0,
    'ALTER TABLE `stress_points` ADD COLUMN `attachment_type` VARCHAR(255)',
    'SELECT "Column attachment_type already exists" AS msg');
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- stress_points 테이블에 title 컬럼 추가
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'stress_points'
AND COLUMN_NAME = 'title';

SET @query = IF(@col_exists = 0,
    'ALTER TABLE `stress_points` ADD COLUMN `title` VARCHAR(255)',
    'SELECT "Column title already exists" AS msg');
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

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
    INDEX `idx_attachment_report` (`report_id`),
    INDEX `idx_attachment_user` (`users_id`),
    CONSTRAINT `FK_AttRep_Report` FOREIGN KEY (`report_id`) REFERENCES `reports` (`report_id`) ON DELETE CASCADE,
    CONSTRAINT `FK_AttRep_SP1` FOREIGN KEY (`st_point_id_1`) REFERENCES `stress_points` (`st_point_id`),
    CONSTRAINT `FK_AttRep_SP2` FOREIGN KEY (`st_point_id_2`) REFERENCES `stress_points` (`st_point_id`),
    CONSTRAINT `FK_AttRep_ST1` FOREIGN KEY (`strength_id_1`) REFERENCES `strength` (`strength_id`),
    CONSTRAINT `FK_AttRep_ST2` FOREIGN KEY (`strength_id_2`) REFERENCES `strength` (`strength_id`)
);