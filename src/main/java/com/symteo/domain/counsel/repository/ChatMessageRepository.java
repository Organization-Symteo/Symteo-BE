package com.symteo.domain.counsel.repository;

import com.symteo.domain.counsel.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
}
