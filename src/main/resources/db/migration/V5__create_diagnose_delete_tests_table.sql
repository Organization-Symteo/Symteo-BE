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
                                  FOREIGN KEY (`user_id`) REFERENCES `users` (`users_id`)
);

DROP TABLE adhd_test;
DROP TABLE anxiety_test;
DROP TABLE attach_test;
DROP TABLE burnout_test;
DROP TABLE depression_test;
DROP TABLE stress_test;