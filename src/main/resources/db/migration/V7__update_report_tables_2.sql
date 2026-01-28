-- 외래키 제약 조건 잠시 해제 (안전한 테이블 재생성을 위함)
SET FOREIGN_KEY_CHECKS = 0;

-- 기존 테이블 삭제 (구조 업데이트를 위해 삭제 후 재생성)
DROP TABLE IF EXISTS `stress_reports`;
DROP TABLE IF EXISTS `burnout_reports`;

-- reports 테이블에 중복 방지용 diagnose_id 컬럼 추가
ALTER TABLE `reports` ADD COLUMN `diagnose_id` BIGINT AFTER `users_id`;
ALTER TABLE `reports` ADD CONSTRAINT `FK_Reports_Diagnose` FOREIGN KEY (`diagnose_id`) REFERENCES `diagnoses` (`id`) ON DELETE CASCADE;

-- stress_reports 테이블 재생성 (엔티티 필드 users_id 및 PK 컬럼명 일치)
CREATE TABLE `stress_reports` (
                                  `st_report_id` BIGINT NOT NULL AUTO_INCREMENT,
                                  `users_id` BIGINT NOT NULL,       -- Java 엔티티 @JoinColumn(name="users_id")와 일치
                                  `report_id` BIGINT NOT NULL,
                                  `stress_score` INT,               -- PSS 총점
                                  `stress_level` VARCHAR(20),       -- 정상, 위험 등
                                  `control_level` VARCHAR(20),      -- 낮음, 보통, 높음 등
                                  `overload_level` VARCHAR(20),
                                  `control_percent` DOUBLE,         -- 지난달 비교용
                                  `overload_percent` DOUBLE,
                                  PRIMARY KEY (`st_report_id`),
                                  CONSTRAINT `FK_U_Stress` FOREIGN KEY (`users_id`) REFERENCES `users` (`users_id`),
                                  CONSTRAINT `FK_R_Stress` FOREIGN KEY (`report_id`) REFERENCES `reports` (`report_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- burnout_reports 테이블 재생성 (배터리 점수 및 상세 수치 포함)
CREATE TABLE `burnout_reports` (
                                   `bu_report_id` BIGINT NOT NULL AUTO_INCREMENT,
                                   `users_id` BIGINT NOT NULL,       -- Java 엔티티 @JoinColumn(name="users_id")와 일치
                                   `report_id` BIGINT NOT NULL,
                                   `total_burnout_score` INT,        -- 마음배터리 계산용 총점
                                   `exhaustion_level` VARCHAR(20),
                                   `cynicism_level` VARCHAR(20),
                                   `inefficacy_level` VARCHAR(20),
                                   `exhaustion_percent` DOUBLE,
                                   `cynicism_percent` DOUBLE,
                                   `inefficacy_percent` DOUBLE,
                                   `total_burnout_level` VARCHAR(20),
                                   PRIMARY KEY (`bu_report_id`),
                                   CONSTRAINT `FK_U_Burnout` FOREIGN KEY (`users_id`) REFERENCES `users` (`users_id`),
                                   CONSTRAINT `FK_R_Burnout` FOREIGN KEY (`report_id`) REFERENCES `reports` (`report_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 외래키 제약 조건 다시 활성화
SET FOREIGN_KEY_CHECKS = 1;