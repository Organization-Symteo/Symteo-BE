CREATE TABLE `Chat_Rooms` (
                              `chatroom_id`   BIGINT NOT NULL AUTO_INCREMENT,
                              `user_id`       BIGINT NOT NULL,
                              `chatsummary`   VARCHAR(255) NULL,
                              `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              `deleted_at`    DATETIME NULL,
                              PRIMARY KEY (`chatroom_id`),
                              CONSTRAINT `fk_chat_rooms_user_id` FOREIGN KEY (`user_id`) REFERENCES `Users` (`users_id`)
);

CREATE TABLE `Chat_Messages` (
                                 `id`            BIGINT NOT NULL AUTO_INCREMENT,
                                 `message`       TEXT NULL,
                                 `role`          VARCHAR(20) NOT NULL DEFAULT 'USER',
                                 `chatroom_id`   BIGINT NOT NULL,
                                 `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 `deleted_at`    DATETIME NULL,
                                 PRIMARY KEY (`id`),
                                 CONSTRAINT `fk_chat_messages_chatroom_id`
                                     FOREIGN KEY (`chatroom_id`) REFERENCES `Chat_Rooms` (`chatroom_id`)
                                         ON DELETE CASCADE
);