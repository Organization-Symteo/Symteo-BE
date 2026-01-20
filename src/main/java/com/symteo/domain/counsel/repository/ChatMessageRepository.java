package com.symteo.domain.counsel.repository;

import com.symteo.domain.counsel.entity.ChatMessage;
import com.symteo.domain.counsel.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findAllByChatRoom_ChatroomIdAndRole(Long chatRoomId, Role role);
}
