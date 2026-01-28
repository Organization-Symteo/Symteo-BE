-- 부모 테이블: 하나의 진단 세션에 대한 마스터 레코드
CREATE TABLE IF NOT EXISTS `reports` (
                                         `report_id` BIGINT NOT NULL AUTO_INCREMENT,
                                         `users_id` BIGINT NOT NULL,
                                         `r_type` VARCHAR(50), -- "DEPRESSION_ANXIETY_COMPLEX" 등으로 저장
    `created_at` DATETIME,
    PRIMARY KEY (`report_id`)
    );

-- 자식 테이블들: 동일한 report_id를 공유함
CREATE TABLE IF NOT EXISTS `depression_reports` (
                                                    `de_report_id` BIGINT NOT NULL AUTO_INCREMENT,
                                                    `report_id` BIGINT NOT NULL, -- 부모 ID
                                                    `depression_score` INT,
    -- ... 생략
                                                    PRIMARY KEY (`de_report_id`),
    CONSTRAINT `FK_Dep_Parent` FOREIGN KEY (`report_id`) REFERENCES `reports` (`report_id`)
    );

CREATE TABLE IF NOT EXISTS `anxiety_reports` (
                                                 `an_report_id` BIGINT NOT NULL AUTO_INCREMENT,
                                                 `report_id` BIGINT NOT NULL, -- 부모 ID
                                                 `anxiety_score` INT,
    -- ... 생략
                                                 PRIMARY KEY (`an_report_id`),
    CONSTRAINT `FK_Anx_Parent` FOREIGN KEY (`report_id`) REFERENCES `reports` (`report_id`)
    );