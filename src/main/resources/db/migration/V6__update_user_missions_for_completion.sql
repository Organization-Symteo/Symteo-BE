UPDATE user_missions
SET is_completed = 0
WHERE is_completed IS NULL;

UPDATE user_missions
SET is_drafted = 0
WHERE is_drafted IS NULL;

ALTER TABLE user_missions
    MODIFY COLUMN is_completed TINYINT(1) NOT NULL DEFAULT 0;

ALTER TABLE user_missions
    MODIFY COLUMN is_drafted TINYINT(1) NOT NULL DEFAULT 0;
