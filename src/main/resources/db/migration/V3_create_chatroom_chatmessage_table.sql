-- 1. ChatRoom 테이블 생성 (부모 테이블)
CREATE TABLE chat_room (
      chatroom_id BIGINT AUTO_INCREMENT PRIMARY KEY,
      user_id BIGINT NOT NULL,
      chatsummary VARCHAR(255)
);

-- 2. ChatMessage 테이블 생성 (자식 테이블)
CREATE TABLE chat_message (
      id BIGINT AUTO_INCREMENT PRIMARY KEY,
      message VARCHAR(255),
      role VARCHAR(20) DEFAULT 'USER',
      chatroom_id BIGINT,

    -- 외래 키 설정
      CONSTRAINT fk_chat_message_chatroom
      FOREIGN KEY (chatroom_id)
      REFERENCES chat_room (chatroom_id)
      ON DELETE CASCADE
);

-- 3. Counsel 테이블 drop