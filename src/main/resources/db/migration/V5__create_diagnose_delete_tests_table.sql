CREATE TABLE `diagnoses` (
                              `id`           BIGINT NOT NULL AUTO_INCREMENT,
                              `user_id`      BIGINT NOT NULL,
                              `test_type`    VARCHAR(255) NULL,
                              `answers`      JSON NULL,
                              `created_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              `updated_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              `deleted_at`   DATETIME NULL,
                              PRIMARY KEY (`id`),
                              CONSTRAINT `fk_diagnoses_user_id`
                                  FOREIGN KEY (`user_id`) REFERENCES `Users` (`users_id`)
);

DROP TABLE ADHD_test;
DROP TABLE Anxiety_test;
DROP TABLE Attach_test;
DROP TABLE BurnOut_test;
DROP TABLE Depression_test;
DROP TABLE Stress_test;